# 0009. Ownership boundaries: `:core`, `:local_db`, `:network`, `:service`, `:ui`

**Status:** Accepted
**Date:** 2026-09-10

**Reverses:** `0008-single-core-module.md` (the merge; its ADR-0003 supersession is undone)
**Restores:** `0003-pure-jvm-domain-modules.md` — Wall 2 is a compile error again
**Restores:** `0004-convention-plugins-enforce-layering.md` — layering is a compile error again

## Context

ADR 0008 collapsed thirteen `core:*` modules into one `:core` to cut build-file maintenance. It
was explicit that this traded away two compile-time walls and left `check-context.sh` and
`internal` as the guards. An audit against a set of stated ownership boundaries then found:

- **`internal` was not doing the job it was assigned.** 33 types were `public` — every Room
  entity, DAO, projection row, local data source and remote data source. `core/CLAUDE.md`,
  `docs/architecture/module-graph.md` and ADR 0008 itself all asserted these were `internal` and
  therefore invisible to `feature:*`. They were not: `feature:auth` could import `AccountDao` and
  it would compile. The wall ADR 0008 banked on had a hole in it the whole time.
- **`:core` put Compose, Room, Supabase and WorkManager on the domain layer's classpath.**
  Nothing had leaked yet — `model/` and `domain/` still had zero framework imports — but only
  discipline was stopping it.

Everything else audited clean: no read touched the network, no DTO or entity escaped its package,
no feature reached past `domain`, and the sync engine's invariants all held.

## Decision

Split by ownership, not by layer count:

| Module | Type | Owns |
|---|---|---|
| `:core` | **JVM** | `model`, `common`, `domain` — models, repository interfaces, use cases |
| `:local_db` | Android | Room database, entities, DAOs, local data sources, entity↔domain mapping |
| `:network` | Android | Supabase client, DTOs, remote data sources, DTO↔domain mapping, Keystore store |
| `:service` | Android | repository implementations, sync engine, WorkManager, preferences |
| `:ui` | Android | design system, MVI base classes, route keys |

`:service` is the only module that sees both `:local_db` and `:network`. Nothing has a cycle.

**Packages were not renamed.** Files keep `com.finflow.core.database`, `com.finflow.core.network`,
`com.finflow.core.data`; each Android module's `namespace` is its existing package root. Not one
import statement changed as a result of the split — the only edit was `BuildConfig` moving back to
`com.finflow.core.network`, which is where it was before ADR 0008.

### Where the ambiguous pieces went, and why

- **`datastore` → `:service`.** Its only consumers are repositories, `CurrentUserProvider` and
  `DataStoreSynchronizer`. It holds the per-table sync watermarks, which are a service concern.
- **`security` → `:network`.** `EncryptedKeyValueStore` exists solely to back
  `EncryptedSessionManager`, Supabase's `SessionManager`. Sole consumer.
- **`SyncTable` → `:service`** (from `core.model`). Verified used by nothing in `database/` or
  `network/`; it is an enum of table names, not a domain concept.
- **`PendingRecord` and `RemoteRecord` stayed in `:core`.** They cross `local_db → service` and
  `network → service`, so both sides need them and neither module can own them. This is the one
  data-shaped concession in the domain module and is documented as such in `core/CLAUDE.md`.
- **`DataErrorMapper` stayed in `:network`.** It must name `RestException` to map SQLSTATE, so
  moving it to `:service` would put Supabase on that classpath. `:service` calls `map(throwable)`
  without ever seeing a Supabase type.

### Features do not depend on `:service`

The requested dependency direction included `feature → service`. That was declined: features need
repository *interfaces* (in `:core`) and get *implementations* through Hilt at the `:app`
composition root. Wiring `:service` into features would put every data source and repository
implementation on their compile classpath, contradicting the same spec's rule that features must
never depend on local or remote data sources. `:app → :service`, `feature:* → :core + :ui`.

## Consequences

**Four walls are compile errors again**, each verified by adding the import, building, and
reverting:

| Probe | Result |
|---|---|
| `feature/dashboard` imports `database.dao.AccountDao` | unresolved |
| `feature/dashboard` imports `data.repository.AccountRepositoryImpl` | unresolved |
| `core/domain` imports `androidx.room.Entity` | unresolved |
| `service` imports `database.entity.AccountEntity` | internal, not visible |

The fourth is the 33-type hole closing. The third is ADR 0003's wall returning.

**Data sources stay `public`; their constructors became `internal`.** A local data source is the
module's boundary and returns only domain models, so it must be visible. Making the constructor
`internal` keeps the DAO out of its public signature — the pattern `SyncInitializer` already used.

**`FinFlowDatabase` became `internal`.** A public `@Database` cannot expose `internal` DAO return
types. Room's KSP accepts this; verified by a clean build and a byte-identical exported schema.

**`./gradlew test` is useful again.** `:core` and `:core:testing` are JVM, so domain and model
tests no longer run under the slower Android unit-test task. Both commands are needed now.

**Build-file count went 2 → 6**, reversing part of ADR 0008's stated benefit. That is the price of
the walls, and it was paid knowingly this time rather than discovered later.

**Incremental builds should improve** over the single `:core`: touching a Room entity no longer
recompiles the design system, and the six modules compile in parallel again.

**`check-context.sh` gained check 11**, asserting entities, DAOs, projection rows and DTOs are
`internal`. The classpath is the real wall now, but the check fails earlier with a better message
— and it is the specific regression this ADR exists to prevent recurring.

## What did not change

The offline-first behaviour, deliberately. Reads are still Room `Flow`s that never touch the
network; writes still land in Room with a pending `SyncStatus` and return at the SQLite commit;
`TableSyncRunner` still does push-then-pull with per-table watermarks, tombstones and
last-write-wins. Because local and remote data sources already returned domain models, the sync
engine needed no change at all — `TableSyncRunner<T>` stayed generic over one type. The split
moved directories; it did not touch the algorithm.

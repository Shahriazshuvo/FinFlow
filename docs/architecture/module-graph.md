# Module graph

16 modules. The interesting part is not the list — it is which edges are **absent**, and that
their absence is enforced by the compiler again.

## Allowed edges

```
app ──▶ service, ui, core, feature:*
 │
feature:* ──▶ core, ui                    (+ core:testing on testImplementation)
 │
service ──▶ local_db, network, core
 │
local_db ──▶ core      network ──▶ core      ui ──▶ core
 │
core        pure JVM — depends on nothing in the project
```

| Module | Type | Owns |
|---|---|---|
| `core` | **JVM** | `model`, `common`, `domain` — models, repository interfaces, use cases |
| `core:testing` | **JVM** | fixtures, `TEST_CLOCK`, model builders |
| `local_db` | Android | Room database, entities, DAOs, local data sources, entity↔domain mapping |
| `network` | Android | Supabase client, DTOs, remote data sources, DTO↔domain mapping, Keystore store |
| `service` | Android | repository implementations, sync engine, WorkManager, preferences |
| `ui` | Android | design system, MVI base classes, `@Serializable` route keys |
| `feature:*` | Android | presentation and navigation only, ×9 |

Packages are unchanged from before the split — everything is still `com.finflow.core.*`. The
module a file belongs to is its directory, not its package.

## The four walls

**Wall 1 — features cannot reach the data layer.** `AndroidFeatureConventionPlugin` wires exactly
`:core` and `:ui`. `:service`, `:local_db` and `:network` are absent, so a DAO, DTO or repository
implementation is an unresolved reference. Features talk to use cases in `com.finflow.core.domain`;
that is the whole surface.

Enforced by: the classpath, plus check 3 of `scripts/check-context.sh` for a better message.

**Wall 2 — the domain layer has no framework.** `:core` applies `JvmLibraryConventionPlugin`, so
the Android SDK is not on its classpath at all. An `android.*`, `androidx.room.*`, Supabase or
Compose import there does not compile. Domain logic and its tests run on the JVM in milliseconds.

Enforced by: `build-logic/convention/src/main/kotlin/JvmLibraryConventionPlugin.kt`.

**Wall 3 — Room and wire types never escape their module.** Entities, DAOs, `FinFlowDatabase` and
the projection rows are `internal` to `:local_db`; DTOs and their mappers are `internal` to
`:network`. Local and remote data sources are the public boundary and return **domain models**.
Their constructors are `internal`, so a public class cannot leak a DAO through its signature.

Enforced by: `internal` across a real module boundary, plus check 11 of `scripts/check-context.sh`.

**Wall 4 — `:service` has no UI.** It is the only module that sees both `:local_db` and
`:network`, and it does not depend on `:ui`, so Compose and Material3 are not on the sync
engine's classpath.

## The edges that do not exist at all

**No feature is on another feature's classpath.** Nothing wires one. The two sanctioned channels
are a shared use case in `com.finflow.core.domain`, and a route key in
`com.finflow.core.navigation`. There is no cross-feature reader layer, no feature `api` module and
no event bus — see `docs/adr/0006-centralized-data-layer.md`.

**No feature depends on `:service`.** Features need repository *interfaces*, which are in `:core`;
the *implementations* are bound by Hilt at the `:app` composition root. Wiring `:service` into
features would hand them every data source and repository impl, defeating Wall 1.

**`:local_db` and `:network` never see each other.** Only `:service` knows both exist. That is
what lets `TableSyncRunner` stay a single generic algorithm rather than six hand-rolled loops.

## Why the walls point this way

The arrows point inward. `core` knows nothing; `local_db` and `network` know domain models and
implement persistence and transport for them; `service` knows both and orchestrates; features know
use cases. Nothing outer is visible to anything inner, so the domain can be reasoned about and
tested without the app existing.

## Offline-first, and where it lives

The read path is `feature → use case → repository interface (core) → repository impl (service) →
local data source (local_db) → Room`. **`:network` is not on it.** Writes land in Room with a
pending `SyncStatus` and return at the SQLite commit, then fire `SyncTrigger` — a one-method
interface in `:service`, so the write path never names WorkManager. Sync is push-then-pull per
table with incremental watermarks. Details: `service/CLAUDE.md`.

## Module table

The canonical list with responsibilities is the table in `CLAUDE.md`, checked against
`settings.gradle.kts` by `scripts/check-context.sh`, so the two cannot drift apart.

## Adding a module

1. `include(":x")` in `settings.gradle.kts`.
2. A `build.gradle.kts` applying the right convention plugin alias and a namespace.
3. Add the row to the `CLAUDE.md` module table — `scripts/check-context.sh` fails until you do.
4. Add the edge to this file if it changes the shape above.
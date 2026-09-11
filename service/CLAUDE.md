# :service — repository implementations and the sync engine

The only module that sees both `:local_db` and `:network`. It owns the offline-first behaviour:
Room-first writes, push-then-pull sync, per-table watermarks. It holds no UI and must never
depend on Compose.

Packages: `data/` (repositories, sync algorithm, session), `sync/` (WorkManager), `datastore/`
(preferences and sync watermarks).

**Never expose a Room entity or a DTO.** You cannot — they are `internal` to their own modules.
Local and remote data sources hand you domain models already.

Every repository implementation in the app lives here, along with the machinery they share.
Repositories are **not** owned by the feature that displays them: accounts are read by the
accounts screen, the dashboard and the transaction form. See
`docs/adr/0006-centralized-data-layer.md` and `APP_SPEC.md` §1 and §3.

The densest correctness rules in the repo live here. Spec detail: `APP_SPEC.md` §12
(`sed -n '385,418p' APP_SPEC.md`).

| Package | Role |
|---|---|
| `data/repository/` | Every `XRepositoryImpl`, `internal`, implementing an interface from `domain/` |
| `data/sync/TableSyncRunner.kt` | The one sync algorithm, parameterised per table |
| `data/sync/Synchronizer.kt` | Watermark storage, the `Syncable` interface, `SyncTrigger` |
| `data/sync/PendingChangesMonitor.kt` | Observes whether anything is waiting to push |
| `data/session/CurrentUserProvider.kt` | Resolves the signed-in user id |
| `data/di/DataModule.kt` | Every repository binding, plus the `Syncable` multibinding set |

## Write path

Every mutating repository method does the same three things, in order:

1. Write to Room immediately with a `SyncStatus` — `PENDING_CREATE`, `PENDING_UPDATE` or
   `PENDING_DELETE`. Never delete a row outright; a delete is a pending tombstone.
2. Stamp `updatedAt` from the **injected `Clock`**, never `Instant.now()`.
3. Call `syncTrigger.requestSync()`.

The method returns as soon as Room has the write. It never waits on the network, and it never
returns a network error to the caller — that is the entire point of the layer.

Uniqueness is checked locally *before* the write as well as in Postgres, because a write that has
already returned cannot report a server rejection. See `BudgetLocalDataSource.hasMonthConflict`
and `CategoryLocalDataSource.hasNameConflict`.

## Sync

All sync goes through `TableSyncRunner`. **Do not hand-roll a sync loop in a repository.** Its
invariants, in the order they bite:

- **Push before pull.** A local edit reaches the server with a fresh `updated_at` *before* the
  pull runs, so last-write-wins resolves in the user's favour. Reversing this silently discards
  offline edits.
- **Still-pending rows are skipped by the pull** (`stillPending` in `TableSyncRunner.pull`). A
  failed push keeps local precedence, so a network blip cannot overwrite the user's work.
- **Tombstones are data, not noise.** The pull deliberately fetches rows with `deletedAt != null`
  and deletes them locally. Filtering them out server-side would resurrect deleted records.
- **The watermark is `max(updated_at)` of the response**, stored per `SyncTable` in `datastore/`
  via `Synchronizer`. Not "now" — using the clock would skip rows written during the request.
- **`SyncStatus` is local-only.** It is never serialized to Supabase.
- **Table order is explicit, because Dagger's is not.** `Syncable` is contributed `@IntoSet`, and
  a multibinding set has no defined iteration order. Every `Syncable` declares
  `val table: SyncTable`, and `SyncWorker` sorts by its ordinal so parents pull before children
  and foreign keys hold. A new `Syncable` that omits a correct `table` breaks referential
  integrity intermittently and only under sync.

## Boundaries

- Repository implementations are `internal`. Only the `domain/` interface is meant to escape.
- Every exception crosses through `DataErrorMapper` into an `AppError` (`APP_SPEC.md` §15). No
  Ktor, Supabase or SQLite exception type may reach the domain layer. SQLSTATE `23505` becomes
  `AppError.Duplicate(field)`; `42501` becomes `AppError.Forbidden`; a lost connection becomes
  `AppError.Offline` and a reached-but-failing server becomes `AppError.Network`. The mapper
  covers Room as well as Supabase because `syncWith` wraps both in one `runCatchingApp`.
- WorkManager is not visible to a repository. Repositories see only the `SyncTrigger`
  fun-interface; `sync/` is the one package that knows WorkManager exists.
- Never depend on a feature. Features depend on this.

## Tests

`TransactionRepositoryImplTest` is the reference. `mockk` the data sources and the `SyncTrigger`,
and pin time with `TEST_CLOCK` from `core:testing` — never `Instant.now()`. Cover the five
scenarios: pending create, pending update, pending delete, a push that fails, and a full round
trip.

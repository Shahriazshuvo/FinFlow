# core:data — offline-first repositories and the sync engine

Every repository implementation in the app lives here, along with the machinery they share.
Repositories are **not** owned by the feature that displays them: accounts are read by the
accounts screen, the dashboard and the transaction form, so a `feature:accounts` that owned the
repository would become a dependency of all three. See
`docs/adr/0006-centralized-data-layer.md` and `APP_SPEC.md` §27.

The densest correctness rules in the repo live here. Spec detail: `APP_SPEC.md` §12
(`sed -n '338,373p' APP_SPEC.md`).

## What is in this module

| Package | Role |
|---|---|
| `repository/` | Every `XRepositoryImpl`, `internal`, implementing an interface from `core:domain` |
| `sync/TableSyncRunner.kt` | The one sync algorithm, parameterised per table |
| `sync/Synchronizer.kt` | Watermark storage, the `Syncable` interface, `SyncTrigger` |
| `sync/PendingChangesMonitor.kt` | Observes whether anything is waiting to push |
| `session/CurrentUserProvider.kt` | Resolves the signed-in user id |
| `di/DataModule.kt` | Every repository binding, plus the `Syncable` multibinding set |

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
- **The watermark is `max(updated_at)` of the response**, stored per `SyncTable` in
  `core:datastore` via `Synchronizer`. Not "now" — using the clock would skip rows written
  during the request.
- **`SyncStatus` is local-only.** It is never serialized to Supabase.
- **Table order is explicit, because Dagger's is not.** `Syncable` is contributed `@IntoSet`, and
  a multibinding set has no defined iteration order. Every `Syncable` declares
  `val table: SyncTable`, and `SyncWorker` sorts by its ordinal so parents pull before children
  and foreign keys hold. A new `Syncable` that omits a correct `table` breaks referential
  integrity intermittently and only under sync.

## Boundaries

- Repository implementations are `internal`. Only the `core:domain` interface escapes the module.
- Every exception crosses through `NetworkErrorMapper` into an `AppError`. No Ktor, Supabase or
  SQLite exception type may reach the domain layer. SQLSTATE `23505` becomes
  `AppError.Conflict(field)`; `42501` becomes `AppError.Forbidden`.
- `core:data` exposes `core:domain` via `api(...)` — the one intentional transitive edge.
- WorkManager is not visible here. Repositories see only the `SyncTrigger` fun-interface;
  `core:sync` is the single module that knows WorkManager exists.
- This module must never depend on a feature. Features depend on it.

## Tests

`TransactionRepositoryImplTest` is the reference. `mockk` the data sources and the `SyncTrigger`,
and pin time with `TEST_CLOCK` from `core:testing` — never `Instant.now()`. Cover the five
scenarios: pending create, pending update, pending delete, a push that fails, and a full round
trip.
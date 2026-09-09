# core:data — offline-first repositories and the sync engine

The densest correctness rules in the repo live here. Spec detail: `APP_SPEC.md` §12
(`sed -n '333,368p' APP_SPEC.md`). Playbook: skill `finflow-offline-sync`.

## Write path

Every mutating repository method does the same three things, in order:

1. Write to Room immediately with a `SyncStatus` — `PENDING_CREATE`, `PENDING_UPDATE` or
   `PENDING_DELETE`. Never delete a row outright; a delete is a pending tombstone.
2. Stamp `updatedAt` from the **injected `Clock`**, never `Instant.now()`.
3. Call `syncTrigger.requestSync()`.

The method returns as soon as Room has the write. It never waits on the network, and it never
returns a network error to the caller — that is the entire point of the layer.

## Sync

All sync goes through `TableSyncRunner`, parameterised per table. **Do not hand-roll a sync loop
in a repository.** Its five invariants, in the order they bite:

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

## Boundaries

- Repository implementations are `internal`. Only the `core:domain` interface escapes the module.
- Every exception crosses through `NetworkErrorMapper` into an `AppError`. No Ktor, Supabase or
  SQLite exception type may reach the domain layer.
- `core:data` exposes `core:domain` via `api(...)` — the one intentional transitive edge.
- WorkManager is not visible here. Repositories see only the `SyncTrigger` fun-interface;
  `core:sync` is the single module that knows WorkManager exists.

## Tests

`TransactionRepositoryImplTest` is the reference. Fake the data sources, `mockk` the
`SyncTrigger`, and pin time with `Clock.fixed`. Cover the five scenarios: pending create, pending
update, pending delete, a push that fails, and a full round trip.
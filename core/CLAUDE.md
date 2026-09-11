# :core — domain models, contracts and shared utilities

The innermost module, and the only **pure JVM** one. No Android SDK, no Room, no Supabase, no
Compose is on this classpath, so importing one is a compile error rather than a review comment.
That wall is the point of the module — see `docs/adr/0009-module-ownership-boundaries.md`.

| Package | Role |
|---|---|
| `model/` | Domain models, `Money`, drafts, `SyncStatus`, `SessionState` |
| `common/` | `AppResult`, `AppError`, dispatchers, formatters, validators, `Clock` |
| `domain/repository/` | Every repository **interface**. Implementations live in `:service`. |
| `domain/usecase/` | One file per domain, `operator fun invoke`, constructor-injected |

## Rules

- **Money is `Money`** — an integer-minor-unit value class. Never `Double` or `Float`. It is
  `Long` in Room and `numeric(12,2)` in Postgres, converted at each boundary.
- **Inject `java.time.Clock`.** Never `Instant.now()` or `LocalDate.now()` — sync and
  pending-write tests pin time.
- **A repository interface returns domain types only.** No Room entity, no DTO, no
  `SupabaseClient`, no `PagingSource`. If a signature needs one, the boundary is in the wrong
  place.
- **Errors cross as `AppError`**, never as a transport exception. `:network`'s `DataErrorMapper`
  does the translation.

## `PendingRecord` and `RemoteRecord`

These two are the one deliberate compromise in this module. They are replication bookkeeping —
`PendingRecord` crosses `:local_db → :service`, `RemoteRecord` crosses `:network → :service` —
so both sides need them and they cannot live in either. They have no domain consumers. Do not
take them as licence to add more data-shaped types here.

## Known deviations

Recorded rather than fixed, so nobody re-derives them:

- `syncStatus` is a field on all six persisted models and reaches the UI. Replication state is
  part of the domain contract today.
- ~35 of 41 use cases are pass-throughs to a repository with no logic.
- `SyncSnapshot` in `domain/usecase/sync/` is shaped for the sync status chip.
- `common/` holds Hilt modules and hardcoded English strings (`"Today"`, validation messages)
  below the UI layer, so they cannot be localized.
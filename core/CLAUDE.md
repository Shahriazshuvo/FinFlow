# core — one module, twelve packages

Everything below the presentation layer lives here: models, use cases, Room, Supabase,
DataStore, the sync engine, the design system and the MVI base classes. It is a single Gradle
module (`docs/adr/0008-single-core-module.md`); the twelve packages under
`com.finflow.core.*` are what used to be twelve modules.

**The layer boundaries are now conventions, not compile errors.** Nothing stops a repository
from importing a DTO, or a feature from importing a DAO — the compiler used to. Two guards
remain, and both matter:

- `internal` still hides DTOs, entities, mappers and repository implementations from
  `feature:*` and `app`, because `internal` is module-scoped and those are separate modules.
  Keep new data-layer types `internal`.
- Check 3 of `scripts/check-context.sh` fails on any `feature/**.kt` importing
  `com.finflow.core.{data,database,network,datastore,security,sync}`. Run it before you finish.

Inside `:core`, respect the package walls by hand. The two that used to be enforced and now
are not: **DTOs never leave `network/`** and **entities never leave `database/`**. If a
`*Dto` or `*Entity` appears anywhere outside its own package, the wall has been breached.

| Package | Role |
|---|---|
| `model/` | Domain models, `Money`, drafts, sync enums |
| `common/` | `AppResult`, `AppError`, dispatchers, formatters, validators, `Clock` |
| `domain/` | Every repository interface and use case, one file per domain |
| `database/` | Room entities, DAOs, local data sources |
| `network/` | Supabase client, DTOs, remote data sources |
| `datastore/` | Preferences and sync watermarks |
| `data/` | Every repository implementation, the sync engine, session |
| `sync/` | WorkManager worker and scheduling |
| `security/` | Keystore-backed `EncryptedKeyValueStore` for the auth session |
| `designsystem/` | Theme, design tokens, reusable Compose components |
| `ui/` | `MviViewModel` base, contract interfaces, effect collection |
| `navigation/` | `@Serializable` route keys, so features never import each other |

---

# data/ — offline-first repositories and the sync engine

Every repository implementation in the app lives here, along with the machinery they share.
Repositories are **not** owned by the feature that displays them: accounts are read by the
accounts screen, the dashboard and the transaction form. See
`docs/adr/0006-centralized-data-layer.md` and `APP_SPEC.md` §1 and §3.

The densest correctness rules in the repo live here. Spec detail: `APP_SPEC.md` §12
(`sed -n '362,395p' APP_SPEC.md`).

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

---

# database/ — Room entities, DAOs, local data sources

Room is the source of truth for every piece of financial data. Playbook for schema changes: skill
`finflow-room-migration`. The Postgres side this mirrors: `docs/supabase/schema.sql`.

## Entity conventions

Entities mirror `public.*` in Postgres **exactly**, because the sync engine assumes the two agree.

- `@ColumnInfo(name = "snake_case")` on every field, matching the Postgres column name.
- `Money` persists as `Long` (integer minor units), `numeric(12,2)` on the server.
- `Instant` and `LocalDate` go through `FinFlowTypeConverters`. Enums persist as `String`.
- `SyncMetadata` is `@Embedded` in every syncable entity — local-only bookkeeping that is never
  serialized to Supabase.
- Index every column combination a `user_id`-scoped query filters or sorts on, plus every foreign
  key and `sync_status`. See `TransactionEntity` for the pattern.
- Foreign keys are `ForeignKey.NO_ACTION`. Nothing is hard-deleted — deletes are soft, via
  `deleted_at` tombstones, so a cascade would be wrong.

## The mapper wall

Entities never leave this package. Local data sources return domain models from `model/`,
converted in `database/mapper/EntityMappers.kt`. If a `*Entity` type appears in a `domain/`
signature, the wall has been breached. Since the merge nothing enforces this but review — the
entities and their mappers are `internal` to all of `:core`, not to `database/`.

## Changing the schema

Bumping `version` in `FinFlowDatabase.kt` is a four-step ritual, and skipping any step ships a
crash on upgrade:

1. Bump `version` and write the `Migration`.
2. Build — `exportSchema = true` writes `core/schemas/<db>/N.json`. **Commit that file.**
3. Add a `MigrationTestHelper` test migrating N-1 → N with real data.
4. Mirror the change in `docs/supabase/schema.sql`, and update the DTO and both mappers in
   `network/`.

Current state: `version = 1`, with `1.json` committed. `scripts/check-context.sh` fails if the
version and the exported schema files disagree.

---

# designsystem/ — theme, tokens and shared components

This is the **one package where literal `.dp` and alpha values are legal**, and only inside
`designsystem/theme/`. Everywhere else in the app they are a bug that `scripts/check-context.sh`
fails on. Spec: `APP_SPEC.md` §17.

## Tokens

`Dimens.kt` holds every non-spacing dimension — icon sizes, strokes, corner radii, component
heights, elevations, alphas. `Spacing.kt` holds the padding/gap scale. Both are `@Immutable` data
classes exposed through `staticCompositionLocalOf` and read as `FinFlowTheme.dimens.iconMd`,
`FinFlowTheme.spacing.lg`, `FinFlowTheme.alphas.disabled`.

Naming is a `xs / sm / md / lg / xl / xxl` scale within a semantic group (`iconMd`, `cornerLg`).

**Before adding a token, look for the one that already fits.** A new token is justified only by a
new semantic role, not by a value that happens not to be in the list — adding `corner13` to avoid
reusing `cornerMd` defeats the point. When you do add one, put it in the right group in the data
class with the others, and give the group a comment if it is new.

Do not enumerate the token inventory anywhere else in the docs. `Dimens.kt` and `Spacing.kt` are
the inventory; a copy of it is the most drift-prone artifact this repo could contain.

## Components

`designsystem/component/` holds `AppBars`, `Buttons`, `Cards`, `ConfirmDialog`, `SelectionSheet`,
`States`, `SyncStatusChip`, `TextFields`. House style, visible in `Buttons.kt`:

- `modifier: Modifier = Modifier` is the first optional parameter, after the required ones.
- State is hoisted. A component takes values and lambdas, never a ViewModel.
- Components own their own loading/disabled behaviour so screens cannot forget it — see
  `FinFlowButton`, which disables itself while `isLoading`.
- Every public component has a `@Preview`.
- A KDoc that says *why* the component exists, not what it renders.

## What belongs here

A widget graduates from a feature into this package when it appears in **two or more** features,
represents app-wide design language, or handles a common loading / empty / error / dialog / sheet
/ form pattern. A component that depends on one feature's `State` or UI model stays in that
feature's `components/`.
# FinFlow — agent context

Offline-first Android personal-finance app. Kotlin · Compose · Material 3 · Clean Architecture +
MVI · Hilt · Room · Supabase · WorkManager. 23 Gradle modules.

**Current state:** the data layer is complete and centralised — every repository, use case and
sync path exists and is wired. Presentation is built for `feature:auth` and `feature:accounts`;
the other seven screens are `EmptyState` stubs. Building them is the work in front of you, and it
is **presentation-only work**: the use cases they need already exist in `core:domain`.

## Modules

| Module | Type | Responsibility |
|---|---|---|
| `app` | app | Nav host, Hilt root, `MainActivity`, theme wiring |
| `core:model` | **JVM** | Domain models, `Money`, drafts, sync enums |
| `core:common` | **JVM** | `AppResult`, `AppError`, dispatchers, formatters, validators, `Clock` |
| `core:domain` | **JVM** | Every repository interface and use case, one file per domain |
| `core:designsystem` | Android | Theme, design tokens, reusable Compose components |
| `core:ui` | Android | `MviViewModel` base, contract interfaces, effect collection |
| `core:database` | Android | Room entities, DAOs, local data sources |
| `core:network` | Android | Supabase client, DTOs, remote data sources |
| `core:datastore` | Android | Preferences and sync watermarks |
| `core:data` | Android | Every repository implementation, the sync engine, session |
| `core:sync` | Android | WorkManager worker and scheduling |
| `core:navigation` | Android | `@Serializable` route keys, so features never import each other |
| `core:security` | Android | Keystore-backed `EncryptedKeyValueStore` for the auth session |
| `core:testing` | **JVM** | `MainDispatcherRule`, fixed clock, domain-model builders |
| `feature:*` | Android | auth, dashboard, transactions, accounts, categories, budgets, goals, analytics, settings — **presentation only** |

## Hard rules

Violating any of these breaks the build or the architecture.

- **`core:model`, `core:common`, `core:domain` are pure JVM.** No Android import may enter them.
- **`feature:*` may depend only on `core:designsystem`, `core:ui`, `core:navigation`,
  `core:common`, `core:model`, `core:domain`.** Never `core:database`, `core:network` or
  `core:data` — enforced by `AndroidFeatureConventionPlugin`, so a violation is a compile error,
  not a review comment. Features never see each other either; they share through the data layer
  and through route keys in `core:navigation`.
- **Data is not UI-scoped.** Repositories live in `core:data`, not in the feature that displays
  them — three screens read accounts. See `docs/adr/0006-centralized-data-layer.md`.
- **No literal dp, alpha or duration outside the design system's `theme/` package.** They live in
  `Dimens.kt` and `Spacing.kt` and are read as `FinFlowTheme.dimens` / `.spacing` / `.alphas`.
- **Money is `Money`** (`core:model`, integer minor units) — `Long` in Room, `numeric(12,2)` in
  Postgres. Never `Double` or `Float`.
- **Room is the source of truth.** Every read is a `Flow` from Room; every write lands in Room
  first with a `SyncStatus`, then WorkManager pushes. The UI never sees a Room entity or a DTO.
- **MVI:** `XState` / `XIntent` / `XEffect`, ViewModel extends `MviViewModel` from `core:ui`.
- **Only the Supabase anon/publishable key ships.** RLS is the security boundary; `user_id`
  filters in queries are defence in depth, not the boundary.
- **Inject `java.time.Clock`.** Never call `Instant.now()` or `LocalDate.now()` directly — sync
  and pending-write tests pin time.

## Toolchain landmines

- **AGP 9 has built-in Kotlin.** Never apply `org.jetbrains.kotlin.android` in a convention
  plugin — it fails with "Cannot add extension with name 'kotlin'". There is deliberately no such
  alias in the version catalog; the Kotlin version is pinned via the root `buildscript` classpath.
- **AGP 9's `CommonExtension` is not generic** and the `xxx(action)` DSL helpers moved to
  `ApplicationExtension` / `LibraryExtension`. Configure shared properties through `apply { }`.
- **`compileSdk` is 36**, `targetSdk` 36, `minSdk` 24. Six catalog entries are held back on
  purpose because their next version needs compileSdk 37 — read the comments in
  `gradle/libs.versions.toml` before bumping anything. `supabase` 3.7.0+ needs Kotlin 2.4.x.
- **KSP must track the Kotlin version's semver line.**
- JDK 17. Core-library desugaring is mandatory — `java.time` is native only from API 26.
- Room is at `version = 1` with schemas exported to `core/database/schemas` and committed.

## Commands

```bash
./gradlew :app:assembleDevDebug  # whole module graph — variants are dev/qa/prod × debug/release
./gradlew test                   # JVM unit tests
./gradlew testDevDebugUnitTest   # Android unit tests (plain `testDebugUnitTest` is ambiguous)
bash scripts/check-context.sh    # context-layer invariants — run before you finish
```

## Where to look

Read these **on demand**, not preemptively.

| Doing this | Read |
|---|---|
| Building a feature screen, ViewModel, Contract | `feature/CLAUDE.md`, then `feature:accounts` as the worked example |
| Writing a test — fakes, fixed clock, model builders | `core/testing/src/main/kotlin/com/finflow/core/testing/` |
| Any Compose UI — spacing, colors, components | `core/designsystem/CLAUDE.md` |
| Sync, watermarks, tombstones, "why is this stale" | `core/data/CLAUDE.md` |
| Room entity, column, DAO, database version | `core/database/CLAUDE.md` |
| How two features share data, or navigate to each other | `APP_SPEC.md` §3 and §6, then `core/navigation/src/main/kotlin/com/finflow/core/navigation/FinFlowRoutes.kt` |
| Gradle, AGP, convention plugins, version catalog | `build-logic/CLAUDE.md` |
| Any SQL, RLS or Postgres | skill `supabase-postgres-best-practices`, then `docs/supabase/README.md` |
| Why a decision was made | `docs/adr/` — index in `docs/README.md` |
| A detail from the spec | `docs/architecture/spec-map.md` — then read **only** that line range |
| Module dependency edges and where each wall is enforced | `docs/architecture/module-graph.md` |

`feature/CLAUDE.md`, `core/data/CLAUDE.md`, `core/database/CLAUDE.md`,
`core/designsystem/CLAUDE.md` and `build-logic/CLAUDE.md` load automatically when you edit files
in those trees.

## APP_SPEC.md

`APP_SPEC.md` is ~775 lines of architecture intent — module structure, dependency rules, the MVI
contract, per-feature requirements, security and money handling. **Do not read it whole.** Its 22
section numbers are cited from KDoc in 28 source files, so they are frozen: never renumber, append
new sections at the end. Use `docs/architecture/spec-map.md` to find the line range you need, then:

```bash
sed -n '362,395p' APP_SPEC.md    # §12, Sync Strategy
```

**The spec was renumbered once,** in commit `960bf38`, from a 31-section product spec to the
current 22-section architecture proposal. Every citation in the source was realigned to the new
numbering afterwards — see `docs/adr/0007-analytics-aggregated-from-room.md` for the one place the
new spec and the code deliberately disagree. If the spec is ever rewritten again, `§N` references
in KDoc must be remapped in the same pass; `scripts/check-context.sh` catches only the citations
that dangle, not the ones that silently land on the wrong section.

## Before you finish

- `bash scripts/check-context.sh && ./gradlew test`
- Added or renamed a module → update the table above and `docs/architecture/module-graph.md`
- Bumped the Room version → migration + committed `schemas/N.json` + a migration test
- Made an architectural decision → add `docs/adr/NNNN-*.md`
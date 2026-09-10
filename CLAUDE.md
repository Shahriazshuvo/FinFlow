# FinFlow — agent context

Offline-first Android personal-finance app. Kotlin · Compose · Material 3 · Clean Architecture +
MVI · Hilt · Room · Supabase · WorkManager. 12 Gradle modules.

**Current state:** the data layer is complete and centralised — every repository, use case and
sync path exists and is wired. Presentation is built for `feature:auth` and `feature:accounts`;
the other seven screens are `EmptyState` stubs. Building them is the work in front of you, and it
is **presentation-only work**: the use cases they need already exist in `com.finflow.core.domain`.

## Modules

| Module | Type | Responsibility |
|---|---|---|
| `app` | app | Nav host, Hilt root, `MainActivity`, theme wiring |
| `core` | Android | Everything below presentation, as twelve packages — see the table in `core/CLAUDE.md` |
| `core:testing` | Android | `MainDispatcherRule`, fixed clock, domain-model builders |
| `feature:*` | Android | auth, dashboard, transactions, accounts, categories, budgets, goals, analytics, settings — **presentation only** |

`:core` is one module holding `model`, `common`, `domain`, `database`, `network`, `datastore`,
`data`, `sync`, `security`, `designsystem`, `ui` and `navigation` as packages under
`com.finflow.core.*`. It was twelve modules until
`docs/adr/0008-single-core-module.md`; that ADR is the place to start if you are wondering why
a layering rule below is a script check rather than a compile error.

## Hard rules

Violating any of these breaks the build or the architecture.

- **`feature:*` holds presentation only.** It may use `com.finflow.core.` `designsystem`, `ui`,
  `navigation`, `common`, `model` and `domain`, and never `data`, `database`, `network`,
  `datastore`, `security` or `sync`. Since `:core` is one module the compiler will not stop you —
  **check 3 of `scripts/check-context.sh` is the only guard, so run it before you finish.**
  Features never see each other either; they share through use cases and through route keys in
  `com.finflow.core.navigation`.
- **DTOs never leave `core/network/`, entities never leave `core/database/`.** Both are
  `internal` to `:core` as a whole, so nothing inside `:core` enforces this any more.
- **Data is not UI-scoped.** Repositories live in `com.finflow.core.data`, not in the feature
  that displays them — three screens read accounts. See `docs/adr/0006-centralized-data-layer.md`.
- **No literal dp, alpha or duration outside the design system's `theme/` package.** They live in
  `Dimens.kt` and `Spacing.kt` and are read as `FinFlowTheme.dimens` / `.spacing` / `.alphas`.
- **Money is `Money`** (`com.finflow.core.model`, integer minor units) — `Long` in Room,
  `numeric(12,2)` in Postgres. Never `Double` or `Float`.
- **Room is the source of truth.** Every read is a `Flow` from Room; every write lands in Room
  first with a `SyncStatus`, then WorkManager pushes. The UI never sees a Room entity or a DTO.
- **MVI:** `XState` / `XIntent` / `XEffect`, ViewModel extends `MviViewModel` from
  `com.finflow.core.ui`.
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
- Room is at `version = 1` with schemas exported to `core/schemas` and committed.

## Commands

```bash
./gradlew :app:assembleDevDebug  # whole module graph — variants are dev/qa/prod × debug/release
./gradlew testDevDebugUnitTest   # every unit test (plain `testDebugUnitTest` is ambiguous)
bash scripts/check-context.sh    # context-layer invariants — run before you finish
```

`:core` is an Android module, so **all** unit tests run under `testDevDebugUnitTest`. Plain
`./gradlew test` no longer runs anything useful — nothing is a JVM module any more.

## Where to look

Read these **on demand**, not preemptively.

| Doing this | Read |
|---|---|
| Building a feature screen, ViewModel, Contract | `feature/CLAUDE.md`, then `feature:accounts` as the worked example |
| Writing a test — fakes, fixed clock, model builders | `core/testing/src/main/kotlin/com/finflow/core/testing/` |
| Anything inside `:core` — Compose UI, sync, Room, the package walls | `core/CLAUDE.md` |
| How two features share data, or navigate to each other | `APP_SPEC.md` §3 and §6, then `core/src/main/kotlin/com/finflow/core/navigation/FinFlowRoutes.kt` |
| Gradle, AGP, convention plugins, version catalog | `build-logic/CLAUDE.md` |
| Any SQL, RLS or Postgres | skill `supabase-postgres-best-practices`, then `docs/supabase/README.md` |
| Why a decision was made | `docs/adr/` — index in `docs/README.md` |
| A detail from the spec | `docs/architecture/spec-map.md` — then read **only** that line range |
| Module dependency edges and where each wall is enforced | `docs/architecture/module-graph.md` |

`feature/CLAUDE.md`, `core/CLAUDE.md` and `build-logic/CLAUDE.md` load automatically when you
edit files in those trees.

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

- `bash scripts/check-context.sh && ./gradlew testDevDebugUnitTest`
- Touched anything under `feature/` → check-context is the **only** thing standing between you
  and an import of the data layer. It is not optional any more.
- Added or renamed a module → update the table above and `docs/architecture/module-graph.md`
- Bumped the Room version → migration + the new `N.json` committed under `core/schemas` + a
  migration test
- Made an architectural decision → add `docs/adr/NNNN-*.md`
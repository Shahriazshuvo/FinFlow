# FinFlow — agent context

Offline-first Android personal-finance app. Kotlin · Compose · Material 3 · Clean Architecture +
MVI · Hilt · Room · Supabase · WorkManager. 16 Gradle modules.

**Current state:** the data layer is complete and centralised — every repository, use case and
sync path exists and is wired. Presentation is built for `feature:auth` and `feature:accounts`;
the other seven screens are `EmptyState` stubs. Building them is the work in front of you, and it
is **presentation-only work**: the use cases they need already exist in `com.finflow.core.domain`.

## Modules

| Module | Type | Responsibility |
|---|---|---|
| `app` | app | Nav host, Hilt root, `MainActivity`, theme wiring |
| `core` | **JVM** | Domain models, `Money`, repository interfaces, use cases, `AppResult`, validators, `Clock` |
| `core:testing` | **JVM** | `MainDispatcherRule`, fixed clock, domain-model builders |
| `local_db` | Android | Room database, entities, DAOs, local data sources, entity↔domain mapping |
| `network` | Android | Supabase client, DTOs, remote data sources, DTO↔domain mapping, Keystore session store |
| `service` | Android | Repository implementations, the sync engine, WorkManager, preferences |
| `ui` | Android | Design system, `MviViewModel` base, `@Serializable` route keys |
| `feature:*` | Android | auth, dashboard, transactions, accounts, categories, budgets, goals, analytics, settings — **presentation only** |

```
app ──▶ service, ui, feature:*, core
feature:* ──▶ core, ui
service ──▶ local_db, network, core
local_db ──▶ core        network ──▶ core        ui ──▶ core
```

Packages still read `com.finflow.core.*` — the module split moved directories, not packages.
`docs/adr/0009-module-ownership-boundaries.md` explains the ownership lines.

## Hard rules

Violating any of these breaks the build or the architecture.

- **`feature:*` holds presentation only.** Its classpath is `:core` and `:ui` and nothing else,
  so importing a DAO, a DTO or a repository implementation does not compile. Repository
  *implementations* are bound by Hilt at the `:app` composition root, which is why a feature
  never needs `:service`. Features never see each other either; they share through use cases and
  through route keys in `com.finflow.core.navigation`.
- **DTOs never leave `:network`, Room entities and DAOs never leave `:local_db`.** Both are
  `internal`, and now that these are real modules that is a compile-time fact. Check 11 of
  `scripts/check-context.sh` fails if one is made public.
- **`:core` is pure JVM.** No Android, Room, Supabase or Compose on its classpath — an
  `androidx.*` import in the domain layer is a compile error.
- **`:service` never depends on Compose.** It is the only module that sees both `:local_db` and
  `:network`; keep UI out of it.
- **Data is not UI-scoped.** Repository implementations live in `:service`, not in the feature
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
- Room is at `version = 1` with schemas exported to `local_db/schemas` and committed.

## Commands

```bash
./gradlew :app:assembleDevDebug  # whole module graph — variants are dev/qa/prod × debug/release
./gradlew test                   # JVM unit tests (:core, :core:testing)
./gradlew testDevDebugUnitTest   # Android unit tests (plain `testDebugUnitTest` is ambiguous)
bash scripts/check-context.sh    # context-layer invariants — run before you finish
```

`:core` is pure JVM again, so its tests run under `test` and the Android modules' under
`testDevDebugUnitTest`. Run both.

## Where to look

Read these **on demand**, not preemptively.

| Doing this | Read |
|---|---|
| Building a feature screen, ViewModel, Contract | `feature/CLAUDE.md`, then `feature:accounts` as the worked example |
| Writing a test — fakes, fixed clock, model builders | `core/testing/src/main/kotlin/com/finflow/core/testing/` |
| Repositories, sync, watermarks, "why is this stale" | `service/CLAUDE.md` |
| Room entity, column, DAO, database version | `local_db/CLAUDE.md` |
| Supabase, DTOs, remote data sources, error mapping | `network/CLAUDE.md` |
| Any Compose UI — spacing, colors, components, MVI base | `ui/CLAUDE.md` |
| Domain models, use cases, `Money`, `AppResult` | `core/CLAUDE.md` |
| How two features share data, or navigate to each other | `APP_SPEC.md` §3 and §6, then `ui/src/main/kotlin/com/finflow/core/navigation/FinFlowRoutes.kt` |
| Gradle, AGP, convention plugins, version catalog | `build-logic/CLAUDE.md` |
| Any SQL, RLS or Postgres | skill `supabase-postgres-best-practices`, then `docs/supabase/README.md` |
| Why a decision was made | `docs/adr/` — index in `docs/README.md` |
| A detail from the spec | `docs/architecture/spec-map.md` — then read **only** that line range |
| Module dependency edges and where each wall is enforced | `docs/architecture/module-graph.md` |

`feature/CLAUDE.md`, `core/CLAUDE.md`, `local_db/CLAUDE.md`, `network/CLAUDE.md`,
`service/CLAUDE.md`, `ui/CLAUDE.md` and `build-logic/CLAUDE.md` load automatically when you edit
files in those trees.

## APP_SPEC.md

`APP_SPEC.md` is ~861 lines of architecture intent — module structure, dependency rules, the MVI
contract, per-feature requirements, security and money handling. **Do not read it whole.** Its 22
section numbers are cited from KDoc in 34 source files, so they are frozen: never renumber, append
new sections at the end. Use `docs/architecture/spec-map.md` to find the line range you need, then:

```bash
sed -n '385,418p' APP_SPEC.md    # §12, Sync Strategy
```

**The spec was renumbered once,** in commit `960bf38`, from a 31-section product spec to the
current 22-section architecture proposal. Every citation in the source was realigned to the new
numbering afterwards — see `docs/adr/0007-analytics-aggregated-from-room.md` for the one place the
new spec and the code deliberately disagree. If the spec is ever rewritten again, `§N` references
in KDoc must be remapped in the same pass; `scripts/check-context.sh` catches only the citations
that dangle, not the ones that silently land on the wrong section.

## Before you finish

- `bash scripts/check-context.sh && ./gradlew test testDevDebugUnitTest`
- Made a Room or DTO type `public` → don't. Check 11 fails, and the module boundary is the
  only reason `feature:*` cannot see them.
- Added or renamed a module → update the table above and `docs/architecture/module-graph.md`
- Bumped the Room version → migration + the new `N.json` committed under `local_db/schemas` + a
  migration test
- Made an architectural decision → add `docs/adr/NNNN-*.md`
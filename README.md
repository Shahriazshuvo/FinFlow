# FinFlow

Offline-first personal finance for Android — income and expense tracking, budgets, saving
goals and analytics, built to demonstrate production-grade Android engineering.

Kotlin · Jetpack Compose · Material 3 · Clean Architecture + MVI · Hilt · Room · Supabase ·
WorkManager · Coroutines & Flow.

## Architecture

Room is the source of truth. Every read comes from the local database and every write lands
there first, marked pending; WorkManager pushes to Supabase in the background. The UI never
blocks on the network and never sees a Room entity or a Supabase DTO.

```
Compose UI ── Intent ──▶ ViewModel ──▶ UseCase ──▶ Repository interface
                                                        │
                                              Repository implementation
                                                   │            │
                                          Room (truth)    Supabase (remote)
                                                   │
                            Flow ◀──────────────────
```

### Modules

| Module | Type | Responsibility |
|---|---|---|
| `app` | app | Nav host, Hilt root, `MainActivity`, theme wiring |
| `core:model` | **JVM** | Domain models, `Money`, drafts, sync enums |
| `core:common` | **JVM** | `AppResult`, `AppError`, dispatchers, formatters, validators |
| `core:domain` | **JVM** | Repository interfaces and use cases |
| `core:designsystem` | Android | Theme, design tokens, reusable Compose components |
| `core:ui` | Android | `MviViewModel` base, effect handling |
| `core:database` | Android | Room entities, DAOs, local data sources |
| `core:network` | Android | Supabase client, DTOs, remote data sources |
| `core:datastore` | Android | Preferences and sync watermarks |
| `core:data` | Android | Offline-first repository implementations, sync engine |
| `core:sync` | Android | WorkManager worker and scheduling |
| `feature:*` | Android | Auth, dashboard, transactions, budgets, goals, analytics, settings |

`core:model`, `core:common` and `core:domain` are pure JVM modules — no Android on the
classpath at all, so the domain layer compiles and tests in milliseconds. Feature modules
have no path to `core:database`, `core:network` or `core:data`, which makes the layering
rules a compile error rather than a code-review convention.

### Design tokens

Every dp, alpha and duration lives in `core/designsystem/theme/Dimens.kt` and `Spacing.kt`
and is read through `FinFlowTheme.dimens` / `.spacing` / `.alphas`. No literal sizes appear
in components or feature code.

### Money

Amounts are integer minor units (`Money` in `core:model`) everywhere locally, and
`numeric(12,2)` in Postgres — never a floating point number.

## Setup

1. Run `docs/supabase/schema.sql` in your Supabase project's SQL editor.
2. Add credentials to `local.properties` (git-ignored):

   ```properties
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_ANON_KEY=<publishable anon key>
   ```

   Only the publishable/anon key belongs in the app. Row Level Security is the security
   boundary. A clone with no credentials still builds.
   Suffix a key with `_DEV`, `_QA` or `_PROD` to point one environment at its own project.
3. `./gradlew :app:assembleDevDebug`

Requires JDK 17.

## Build and test

Builds are flavored `dev` / `qa` / `prod` across `debug` / `release`. `dev` and `qa` install
alongside `prod`.

```bash
./gradlew :app:assembleDevDebug    # whole module graph
./gradlew :app:assembleProdRelease # release build
./gradlew test                     # JVM unit tests
./gradlew testDevDebugUnitTest     # Android unit tests
bash scripts/check-context.sh      # documentation invariants
```

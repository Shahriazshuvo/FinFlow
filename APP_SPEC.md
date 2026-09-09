# FinFlow Specification

## 1. Project Summary

FinFlow is a production-grade Android personal finance app built to demonstrate senior-level Android engineering. The app helps users track income, expenses, budgets, saving goals, and analytics with offline-first data handling.

The project uses Kotlin, Jetpack Compose, Material 3, Clean Architecture, MVI, Room, Supabase, WorkManager, Coroutines, Flow, Hilt, and automated testing.

## 2. Product Goals

- Track income and expenses by account and category.
- Support offline-first transaction management.
- Sync local financial data with Supabase.
- Show dashboard summaries and analytics.
- Support monthly budgets and saving goals.
- Demonstrate production-grade Android architecture.
- Keep user data isolated through Supabase Row Level Security.
- Maintain a professional GitHub repository with documentation, tests, screenshots, and a clear demo.

## 3. Non-Goals

- No bank account integration in the MVP.
- No payment processing.
- No investment trading features.
- No financial advice or prediction as a core feature.
- No service-role key or secret key inside the Android app.
- No direct UI dependency on Supabase or Room.

## 4. Target Tech Stack

- Language: Kotlin
- UI: Jetpack Compose
- Design: Material 3
- Architecture: Clean Architecture + MVI
- Dependency Injection: Hilt
- Local Database: Room
- Remote Backend: Supabase
- Remote Database: PostgreSQL
- Authentication: Supabase Auth
- Async: Coroutines + Flow
- Background Sync: WorkManager
- Serialization: Kotlin Serialization
- Paging: Paging 3, if transaction list becomes large
- Testing: JUnit, Turbine, MockK, Room tests, Compose UI tests
- Static Analysis: Detekt + Ktlint

## 5. Architecture

FinFlow follows Clean Architecture with MVI, multi-module boundaries, and offline-first synchronization.

Layer flow:

```text
Compose UI
  -> sends Intent
ViewModel
  -> executes UseCase
UseCase
  -> calls Repository Interface
Repository Implementation
  -> coordinates Room LocalDataSource and Supabase RemoteDataSource
Room
  -> emits Flow back to UI as source of truth
```

Core rules:

- Compose screens render immutable `UiState`.
- User actions are represented as `Intent`.
- ViewModels process intents and update state.
- One-time events use `Effect`.
- UI never talks directly to Room.
- UI never talks directly to Supabase.
- ViewModel never depends on DTOs, Room entities, or Supabase implementation details.
- Domain models stay independent from framework-specific data classes.
- Room is the source of truth for financial data.
- Supabase is used for auth and remote persistence.
- WorkManager handles retryable background sync.

## 6. MVI Contract Pattern

Each feature uses this contract shape:

```kotlin
data class FeatureState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface FeatureIntent {
    data object Load : FeatureIntent
}

sealed interface FeatureEffect {
    data class ShowSnackbar(val message: String) : FeatureEffect
}
```

State should contain:

- Loading state
- Screen data
- Empty state
- Form fields
- Validation errors
- Selected filters
- Dialog or bottom sheet visibility

Effect should contain:

- Navigation
- Snackbar messages
- Toast-like one-time messages
- Export/share actions
- Permission request events

## 7. Module Structure

```text
FinFlow/
├── app/
├── core/
│   ├── common/
│   ├── designsystem/
│   ├── ui/
│   ├── model/
│   ├── database/
│   ├── network/
│   ├── domain/
│   ├── data/
│   ├── datastore/
│   └── sync/
├── feature/
│   ├── auth/
│   ├── dashboard/
│   ├── transactions/
│   ├── budgets/
│   ├── goals/
│   ├── analytics/
│   └── settings/
└── build-logic/
```

Module responsibilities:

- `app`: App entry point, navigation host, root DI setup, app theme.
- `core:common`: Shared non-UI functionality such as result wrappers, dispatchers, date utilities, validation helpers, currency formatting, error mapping, and small pure Kotlin extensions.
- `core:designsystem`: Shared UI system such as theme, typography, colors, spacing, icons, reusable Compose widgets, screen scaffolds, empty states, loading states, buttons, text fields, cards, dialogs, and bottom sheets.
- `core:ui`: The `MviViewModel` base class, the `UiState`/`UiIntent`/`UiEffect` contract interfaces, and effect-collection helpers shared by every feature.
- `core:model`: Domain models shared across features.
- `core:database`: Room database, entities, DAOs, local data sources.
- `core:network`: Supabase client, DTOs, remote data sources.
- `core:domain`: Repository interfaces and use cases.
- `core:data`: Offline-first repository implementations and the sync engine that reconciles Room with Supabase.
- `core:datastore`: User preferences, local app settings, and sync watermarks.
- `core:sync`: WorkManager workers and sync orchestration.
- `feature:*`: Feature UI, route, MVI contract, ViewModel, UI mappers, and feature-specific components only.
- `build-logic`: Gradle convention plugins for consistent module setup.

## 8. Feature Module Structure

Each feature should follow this structure:

```text
feature/transactions/
└── src/main/kotlin/.../transactions/
    ├── presentation/
    │   ├── TransactionsContract.kt
    │   ├── TransactionsRoute.kt
    │   ├── TransactionsScreen.kt
    │   ├── TransactionsViewModel.kt
    │   ├── TransactionsUiMapper.kt
    │   └── components/
    └── navigation/
        └── TransactionsNavigation.kt
```

Naming convention:

- `AuthState`, `AuthIntent`, `AuthEffect`
- `DashboardState`, `DashboardIntent`, `DashboardEffect`
- `TransactionsState`, `TransactionsIntent`, `TransactionsEffect`
- `BudgetsState`, `BudgetsIntent`, `BudgetsEffect`
- `GoalsState`, `GoalsIntent`, `GoalsEffect`
- `AnalyticsState`, `AnalyticsIntent`, `AnalyticsEffect`
- `SettingsState`, `SettingsIntent`, `SettingsEffect`

## 9. Reusable UI and Common Functionality

FinFlow must avoid duplicate widget design. Shared widgets and repeated UI patterns belong in `core:designsystem`, not inside individual feature modules.

Reusable UI belongs in `core:designsystem` when:

- The same visual pattern appears in two or more features.
- The component represents app-wide design language.
- The component handles common loading, empty, error, dialog, sheet, form, or list behavior.
- The component should look consistent across dashboard, transactions, budgets, goals, analytics, and settings.

Feature-local UI belongs in `feature:*` only when:

- The component is specific to one feature.
- The component depends on one feature's `State`, `Intent`, or UI model.
- Reusing it elsewhere would create coupling or unclear naming.

Recommended `core:designsystem` structure:

```text
core/designsystem/
└── src/main/kotlin/.../designsystem/
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   ├── Type.kt
    │   └── Spacing.kt
    ├── component/
    │   ├── FinFlowButton.kt
    │   ├── FinFlowOutlinedButton.kt
    │   ├── FinFlowTextField.kt
    │   ├── FinFlowPasswordField.kt
    │   ├── FinFlowTopAppBar.kt
    │   ├── FinFlowBottomBar.kt
    │   ├── FinFlowCard.kt
    │   ├── AmountCard.kt
    │   ├── ProgressCard.kt
    │   ├── EmptyState.kt
    │   ├── ErrorState.kt
    │   ├── LoadingState.kt
    │   ├── ConfirmDialog.kt
    │   ├── SelectionSheet.kt
    │   └── SyncStatusChip.kt
    └── icon/
        └── FinFlowIcons.kt
```

Recommended `core:common` structure:

```text
core/common/
└── src/main/kotlin/.../common/
    ├── result/
    │   └── AppResult.kt
    ├── error/
    │   └── AppError.kt
    ├── dispatcher/
    │   └── DispatcherProvider.kt
    ├── formatter/
    │   ├── CurrencyFormatter.kt
    │   └── DateFormatter.kt
    ├── validation/
    │   ├── AmountValidator.kt
    │   └── AuthValidator.kt
    └── extension/
        └── FlowExtensions.kt
```

Reusable UI rules:

- Do not recreate the same button, text field, card, top bar, empty state, loading state, error state, dialog, or bottom sheet style in feature modules.
- Feature screens should compose reusable design system components.
- Design system components must be stateless where possible.
- Design system components should accept values and callbacks, not ViewModels.
- Design system components must not depend on feature modules.
- Design system components must not depend on Room, Supabase, repositories, or use cases.
- If a feature component becomes useful in another feature, move it to `core:designsystem` and rename it generically.

Example:

```kotlin
@Composable
fun AmountCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    trendLabel: String? = null,
    isPositive: Boolean? = null
)
```

Usage:

```kotlin
AmountCard(
    title = "This Month Expense",
    amount = state.monthlyExpense
)
```

## 10. Backend Tables

Supabase tables:

- `profiles`
- `accounts`
- `categories`
- `transactions`
- `budgets`
- `goals`

Analytics is derived from transaction, budget, category, and goal data. Separate analytics tables are not required for the MVP.

## 11. Domain Models

Main domain models:

- `Profile`
- `Account`
- `Category`
- `Transaction`
- `Budget`
- `Goal`
- `MonthlySummary`
- `CategorySpending`
- `BudgetUsage`

Each local entity should include sync metadata:

```kotlin
enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}
```

Local-only sync fields:

- `syncStatus`
- `lastSyncedAt`
- `remoteUpdatedAt`
- `localUpdatedAt`

## 12. Offline-First Strategy

Read flow:

```text
Screen opens
-> ViewModel sends Load intent
-> UseCase observes Repository
-> Repository returns Flow from Room
-> UI renders Room data
-> Sync pulls latest Supabase data
-> Room updates
-> UI updates automatically
```

Write flow:

```text
User creates/updates/deletes data
-> ViewModel sends Intent
-> UseCase validates request
-> Repository writes to Room first
-> Row is marked pending
-> UI updates immediately
-> WorkManager syncs pending changes to Supabase
-> On success, local row is marked synced
-> On failure, pending row remains and retry is scheduled
```

Delete strategy:

- Use soft delete with `deleted_at`.
- Local delete should mark `PENDING_DELETE`.
- Remote sync should update `deleted_at` in Supabase.
- Hard delete can be deferred or avoided for portfolio MVP.

## 13. Authentication

MVP auth:

- Email/password signup
- Email/password login
- Logout
- Session restore
- Authenticated-only app area

Later auth:

- Password reset
- Biometric app unlock
- OAuth provider login

Security rules:

- Use Supabase publishable key in Android.
- Never use service-role key in Android.
- Store Supabase URL/key through `local.properties` or build config.
- Do not commit real keys to GitHub.

## 14. Dashboard

Dashboard should show:

- Current month income
- Current month expense
- Net amount
- Account balance summary
- Recent transactions
- Budget usage warning
- Goal progress summary
- Sync status indicator

## 15. Transactions

User can:

- View transaction list
- Add income or expense
- Edit transaction
- Soft delete transaction
- Filter by type, category, account, and date range
- Search by note
- See pending sync status when offline

Transaction list should read from Room. Supabase updates should arrive through sync and update Room.

## 16. Budgets

User can:

- Create monthly category budget
- Edit budget amount
- Delete budget
- See spent amount
- See remaining amount
- See usage percentage

Budget usage is calculated from expense transactions for the selected month.

## 17. Goals

User can:

- Create saving goal
- Set target amount
- Set current amount
- Set optional target date
- Update progress
- Delete goal

Goal progress should be displayed as percentage and remaining amount.

## 18. Analytics

Analytics screen should include:

- Monthly income vs expense
- Category-wise spending breakdown
- Budget usage
- Net savings trend
- Top spending categories

MVP analytics can be calculated locally from Room queries. Supabase analytics views can be used later as an optimization or comparison point.

## 19. Settings

Settings should include:

- Profile name
- Currency code
- Theme preference
- Sync now action
- Logout
- App version

## 20. Error Handling

Use a domain-level error model:

```kotlin
sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Unauthorized : AppError
    data class Validation(val message: String) : AppError
    data class Unknown(val message: String?) : AppError
}
```

Error handling rules:

- Convert Supabase/network exceptions inside data layer.
- Expose domain errors to use cases/ViewModels.
- Show user-friendly messages in UI.
- Keep retryable sync failures in local sync state.

## 21. Testing Strategy

Minimum tests:

- Use case unit tests
- ViewModel MVI intent/state/effect tests
- Repository tests with fake local and remote data sources
- Room DAO tests
- Sync worker tests
- Compose UI tests for critical screens

Important MVI tests:

- Given initial state, when intent is sent, then expected state is emitted.
- Given use case failure, when intent is sent, then error effect is emitted.
- Given offline write, when transaction is created, then pending state is stored.

## 22. Development Phases

Phase 1: Foundation

- Create Android project
- Add `APP_SPEC.md`
- Configure Gradle convention plugins
- Create modules
- Add app theme and design system foundation

Phase 2: Core Data

- Add domain models
- Add Room entities and DAOs
- Add Supabase client setup
- Add repository contracts
- Add local-first repository implementations

Phase 3: Auth

- Implement signup
- Implement login
- Implement session restore
- Implement logout
- Protect authenticated routes

Phase 4: Transactions

- Transaction list
- Add/edit transaction
- Soft delete
- Search and filters
- Local pending sync status

Phase 5: Sync

- Push pending local changes
- Pull remote changes
- Resolve basic conflicts using `updated_at`
- Add manual sync and background sync

Phase 6: Dashboard, Budgets, Goals

- Dashboard summary
- Monthly budgets
- Goal progress
- Empty/loading/error states

Phase 7: Analytics

- Income vs expense
- Category spending
- Budget usage
- Net savings trend

Phase 8: Polish

- Tests
- README
- Screenshots
- Demo video
- GitHub release

## 23. AI-Native Development Workflow

Use AI as a coding partner, but keep `APP_SPEC.md` as the source of truth.

**This section is now enforced by structure rather than by remembering to say it.** The rules that
used to live here are loaded automatically:

- `CLAUDE.md` at the repo root — the module graph, the hard invariants, and the toolchain
  constraints, loaded into every session.
- `feature/CLAUDE.md`, `core/data/CLAUDE.md`, `core/database/CLAUDE.md`,
  `core/designsystem/CLAUDE.md` — loaded when the agent touches that subtree.
- `.agents/skills/finflow-*` — task-triggered playbooks for building a feature module, working with
  design tokens, changing the sync engine, and migrating Room schemas.
- `docs/architecture/spec-map.md` — lets an agent load one section of this file instead of all of it.

Two rules remain human-side and cannot be automated:

- Review generated code before committing.
- Keep commits small and meaningful.

Recommended commit order:

```text
docs: add FinFlow product and architecture specification
chore: initialize Android project
chore: configure Gradle convention plugins
chore: add core and feature modules
feat: add design system foundation
feat: add domain models
feat: add Room database foundation
feat: add Supabase client setup
feat: implement auth with MVI
feat: implement transactions with offline-first storage
feat: add background sync worker
feat: add dashboard summary
feat: add budgets
feat: add goals
feat: add analytics
test: add ViewModel and repository tests
docs: update README with screenshots and architecture
```

## 24. AI Prompts

The six canned prompts that used to live here ("You are a senior Android engineer…") have been
replaced by skills, which trigger on the task instead of waiting to be pasted:

| Was | Now |
|---|---|
| Prompt 1: Project Setup | `CLAUDE.md` (loaded every session) |
| Prompt 2: MVI Feature Implementation | `.agents/skills/finflow-feature-module/` |
| Prompt 3: Repository Layer | `.agents/skills/finflow-offline-sync/` |
| Prompt 4: Offline Sync | `.agents/skills/finflow-offline-sync/` |
| Prompt 5: Supabase Integration | `.agents/skills/supabase/` + `docs/supabase/README.md` |
| Prompt 6: Code Review | §25 below, checked by `scripts/check-context.sh` |

See `docs/README.md` for the full map of what is loaded when.

## 25. Definition of Done

The project is portfolio-ready when:

- App builds successfully.
- Core features work offline.
- Shared UI components are reused from `core:designsystem`.
- Common non-UI helpers are reused from `core:common`.
- Feature modules do not duplicate app-wide widget designs.
- Sync retries safely after network failure.
- Supabase RLS prevents cross-user data access.
- MVI state/effect behavior is tested.
- README includes architecture, setup, screenshots, and demo.
- Codebase has meaningful commit history.
- No secrets are committed.

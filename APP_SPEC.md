# FinFlow App Architecture Proposal

## 1. Architecture Goal

FinFlow should use a practical production Android architecture inspired by Now in Android, adapted for a fintech app.

The architecture should be:

- Modular
- Offline-capable
- Secure by design
- Testable
- Portfolio-ready
- Easy to extend with AI later

The core idea:

```text
app = application shell
feature:* = screens and presentation logic
core:domain = use cases and repository contracts
core:data = repository implementations and sync coordination
core:database = Room local source of truth
core:network = Supabase remote data source
core:sync = WorkManager background sync
core:security = encrypted session and secure storage
```

FinFlow should not use feature-local data/domain layers by default. Shared business logic belongs in `core:domain`; shared data access belongs in `core:data`.

---

## 2. Recommended Module Structure

```text
FinFlow/
├── app/
│   ├── MainActivity
│   ├── FinFlowApplication
│   ├── ui/
│   └── navigation/
│
├── feature/
│   ├── auth/
│   ├── dashboard/
│   ├── accounts/
│   ├── transactions/
│   ├── categories/
│   ├── budgets/
│   ├── goals/
│   ├── analytics/
│   └── settings/
│
├── core/
│   ├── model/
│   ├── common/
│   ├── domain/
│   ├── data/
│   ├── database/
│   ├── network/
│   ├── datastore/
│   ├── security/
│   ├── sync/
│   ├── navigation/
│   ├── designsystem/
│   ├── ui/
│   └── testing/
│
└── build-logic/
```

This is close to Now in Android, but FinFlow needs stronger `security`, `sync`, and money-handling boundaries.

---

## 3. Dependency Rules

Allowed dependencies:

```text
app
 ↓
feature:*
 ↓
core:domain
core:model
core:common
core:ui
core:designsystem
core:navigation
```

Data-side dependencies:

```text
core:data
 ↓
core:domain
core:database
core:network
core:datastore
core:common
core:model

core:sync
 ↓
core:data
core:datastore
core:common
```

Feature modules must not depend on:

```text
core:data
core:database
core:network
core:sync
```

This makes architecture violations compile-time visible. ViewModels call use cases only.

---

## 4. Feature Module Shape

Each feature should be presentation-focused:

```text
feature/transactions/
├── navigation/
│   └── TransactionsNavigation.kt
└── presentation/
    ├── TransactionsRoute.kt
    ├── TransactionsScreen.kt
    ├── TransactionsViewModel.kt
    ├── TransactionsState.kt
    ├── TransactionsIntent.kt
    ├── TransactionsEffect.kt
    ├── TransactionsUiMapper.kt
    └── components/
```

Use the same shape for accounts, budgets, goals, analytics, dashboard, categories, auth, and settings.

Do not create `data/` and `domain/` folders inside every feature unless a feature has genuinely private business logic. For this app, centralized `core:domain` and `core:data` are cleaner.

---

## 5. App Shell

The `app` module owns:

- `MainActivity`
- `FinFlowApplication`
- Root theme setup
- Auth-aware routing
- Main bottom navigation
- Top-level navigation graph
- App-wide snackbar host
- Sync startup wiring

The app module may depend on every feature module so it can register navigation destinations.

---

## 6. Navigation

Use Navigation Compose.

Recommended root graph:

```text
Root
├── AuthGraph
│   ├── Login
│   └── Signup
└── MainGraph
    ├── Dashboard
    ├── Transactions
    ├── Accounts
    ├── Budgets
    ├── Goals
    ├── Analytics
    └── Settings
```

Navigation rules:

- App owns root graph composition.
- Features expose route registration functions.
- Pass IDs, not domain objects.
- Use typed route objects where practical.
- Auth state decides whether root starts in Auth or Main.

Example:

```text
transactions/{transactionId}
accounts/{accountId}
budgets/{budgetId}
goals/{goalId}
```

---

## 7. UDF / MVI Pattern

Each screen follows predictable unidirectional data flow:

```text
User action
 → Intent
 → ViewModel
 → UseCase
 → Repository
 → StateFlow update
 → Compose UI
```

Recommended contracts:

```kotlin
data class TransactionsState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionUiModel> = emptyList(),
    val error: UiMessage? = null,
)

sealed interface TransactionsIntent {
    data object Refresh : TransactionsIntent
    data class Delete(val id: String) : TransactionsIntent
    data class SearchChanged(val query: String) : TransactionsIntent
}

sealed interface TransactionsEffect {
    data class ShowSnackbar(val message: String) : TransactionsEffect
    data class NavigateToDetail(val transactionId: String) : TransactionsEffect
}
```

State is durable UI state. Effects are one-time events such as snackbar and navigation.

---

## 8. Data Architecture

Use Room as the local source of truth.

```text
Compose UI
 → ViewModel
 → UseCase
 → Repository interface
 → Repository implementation
 → Room
 → Flow
 → UI
```

Network sync is background work:

```text
Repository write
 → Room row marked pending
 → Sync requested
 → WorkManager pushes to Supabase
 → Row marked synced
```

Rules:

- Reads come from Room.
- Writes land in Room first.
- Supabase is remote source of truth.
- WorkManager handles push/pull sync.
- Repositories hide local/remote implementation details.
- UI never sees Room entities or Supabase DTOs.

---

## 9. Supabase Mapping

Use the existing Supabase schema as backend truth.

| Table/View | Feature | Android Model Strategy |
|---|---|---|
| `profiles` | auth/settings | Profile domain model, created by signup trigger |
| `accounts` | accounts/dashboard | Account plus calculated balance |
| `categories` | categories/transactions/budgets | Category domain model |
| `transactions` | transactions/dashboard/analytics | Transaction ledger model |
| `budgets` | budgets/dashboard | Budget domain model |
| `goals` | goals/dashboard | Goal domain model |
| `monthly_income_expense` | analytics/dashboard | Analytics read model |
| `monthly_category_spending` | analytics | Category spending read model |
| `budget_usage` | budgets/analytics/dashboard | Budget usage read model |

Do not invent backend fields. Do not create profile/default data after signup; the database trigger handles it.

---

## 10. Money Handling

Domain code must never use floating point money.

Recommended local/domain representation:

```text
Money(minorUnits: Long)
```

Postgres currently uses:

```text
numeric(12,2)
```

That is acceptable, but conversion must be explicit at the network boundary.

Rules:

- UI text input parses to `Money`.
- Domain uses `Money`.
- Room stores integer minor units.
- Supabase DTO maps `numeric(12,2)` to/from `Money`.
- Formatting belongs in `core:common`.

---

## 11. Account Balance

The backend does not define `current_balance`. Do not add it to Android models as stored truth.

Balance should be calculated:

```text
current balance =
opening_balance
+ income transactions
- expense transactions
```

Only include transactions where:

```text
deleted_at is null
```

Expose this as a read model:

```kotlin
data class AccountWithBalance(
    val account: Account,
    val balance: Money,
)
```

The calculation can live in a Room DAO query/local data source and be exposed through `AccountRepository`.

---

## 12. Sync Strategy

Use simple offline-first sync, not a complex distributed sync engine.

Recommended algorithm:

```text
1. Push pending local changes.
2. Pull remote rows where updated_at > lastSyncedAt.
3. Apply remote rows unless the local row is still pending.
4. Respect deleted_at tombstones.
5. Update sync watermark.
```

Pending statuses:

```text
SYNCED
PENDING_CREATE
PENDING_UPDATE
PENDING_DELETE
```

Conflict policy:

```text
Local pending changes win until successfully pushed.
Remote changes apply when local row has no pending write.
```

This is simple, understandable, and good enough for a portfolio-grade personal finance app.

---

## 13. Feature Architecture

### Auth

Flow:

```text
Login/Signup Screen
 → AuthViewModel
 → AuthUseCase
 → AuthRepository
 → Supabase Auth
 → encrypted session storage
```

Signup must not create profile, account, or default categories manually. Supabase trigger does that.

After signup:

```text
Signup success
 → restore session
 → trigger initial sync
 → navigate to Main
```

### Dashboard

Dashboard is orchestration only.

It combines:

- Total balance
- Monthly income
- Monthly expense
- Recent transactions
- Budget usage
- Goal progress

Do not create a giant `DashboardRepository` with duplicated logic. Prefer a dashboard use case that combines existing use cases.

### Accounts

Responsibilities:

- List accounts
- Add account
- Edit account
- Soft-delete account
- Account detail
- Calculated balance

Types:

```text
cash
bank
card
wallet
```

### Transactions

Primary ledger feature.

Responsibilities:

- List transactions
- Add transaction
- Edit transaction
- Soft-delete transaction
- Filter by account/category/type/date/query

Transactions must validate:

- Amount > 0
- Account exists
- Category exists
- Category type matches transaction type when possible

### Categories

Responsibilities:

- Income/expense category list
- Create category
- Edit category
- Soft-delete category

Duplicate names should map to user-friendly UI errors.

### Budgets

Responsibilities:

- Budget list
- Create/edit budget
- Budget progress
- Remaining amount
- Usage percent

The uniqueness rule is:

```text
user_id + category_id + month
```

Duplicate budget errors should be represented clearly in the UI.

### Goals

Current schema approach:

```text
Update goals.current_amount directly.
```

Do not invent contribution history in Android.

Optional backend change later:

```text
goal_contributions table
```

Add it only if the product needs contribution history/auditability.

### Analytics

Use backend views:

```text
monthly_income_expense
monthly_category_spending
budget_usage
```

Android should not duplicate these aggregations unless offline analytics is explicitly needed.

---

## 14. Security Architecture

Security boundary:

```text
Supabase Auth + PostgreSQL RLS
```

Android-side `userId` filters are for correctness and cache separation, not security.

Rules:

- Never ship Supabase service-role key.
- Only use anon/publishable key.
- Store sessions securely.
- Use Android Keystore-backed encrypted storage where appropriate.
- Keep RLS enabled on every user-owned table.
- Treat RLS failures as authorization/session errors.
- Clear local cached user data on logout.

---

## 15. Error Handling

Use shared error types in `core:common`.

Recommended categories:

```text
Unauthorized
Network
Offline
Validation
Duplicate
NotFound
Database
Unknown
```

Repositories should map raw Supabase/Room exceptions into domain-safe errors. UI should receive user-friendly messages through UI mappers.

Examples:

```text
category duplicate index → "Category already exists."
budget duplicate index → "Budget already exists for this month."
RLS denied → "You do not have permission or your session expired."
network unavailable → "You're offline. Changes will sync later."
```

---

## 16. DataStore

Use DataStore for lightweight app preferences only:

- Theme
- Currency display preference
- Onboarding completion
- Last selected filters
- Sync watermarks if not stored in Room

Do not store relational finance data in DataStore.

---

## 17. Design System

Use `core:designsystem` for:

- Theme
- Colors
- Typography
- Shapes
- Spacing
- Buttons
- Text fields
- App bars
- Cards
- Dialogs
- Loading states
- Empty states
- Error states
- Financial amount display
- Progress components

Use `core:ui` for:

- MVI base helpers
- Effect collection
- UI error message mapping
- Shared Compose utilities

Keep feature-specific components inside feature modules until reused by multiple features.

---

## 18. Testing Strategy

### Unit Tests

Test:

- Use cases
- Validators
- Money conversion
- Mappers
- Repository logic
- Sync algorithm

### Flow Tests

Use Turbine for:

- ViewModel state
- Repository flows
- Sync status flows

### Compose UI Tests

Test:

- Login
- Signup
- Transaction list
- Add transaction
- Account list
- Budget progress
- Goal progress
- Analytics empty/loading/success states

### Integration Tests

Use fake local/remote data sources for most tests.

Run limited Supabase integration tests for:

- Auth
- RLS
- Duplicate constraints
- Analytics views
- Signup trigger

---

## 19. Build Variants

Support:

```text
dev
qa
prod
```

Each environment should have:

- Different application ID suffix for dev/qa
- Separate Supabase URL/key if needed
- Debug logging enabled only for debug/dev
- Release minification eventually enabled

Never include service-role secrets in any variant.

---

## 20. Future AI Architecture

Add later:

```text
feature:ai
```

AI should not query Room or Supabase directly.

Flow:

```text
AI screen
 → AiViewModel
 → AskFinancialQuestionUseCase
 → FinancialContextBuilder
 → existing finance use cases
 → prompt/context
 → LLM provider
 → structured insight
 → UI
```

Example questions:

- How much did I spend on food this month?
- Why did expenses increase?
- Am I over budget?
- What were my biggest expenses?
- How much did I save compared with last month?

Keep AI behind domain abstractions so providers can change later.

---

## 21. Architectural Decisions

| Decision | Choice | Reason |
|---|---|---|
| UI | Jetpack Compose | Modern Android UI |
| Architecture | Now in Android-style modular architecture | Scales without overcomplicating |
| Feature modules | Presentation-focused | Prevents duplicated domain/data layers |
| Domain | Central `core:domain` | Shared use cases and contracts |
| Data | Central `core:data` | Repository implementations and sync coordination |
| Local DB | Room | Local source of truth |
| Backend | Supabase | Auth, Postgres, RLS, analytics views |
| State | UDF/MVI | Predictable screen behavior |
| DI | Hilt | Standard Android dependency injection |
| Sync | WorkManager | Reliable background sync |
| Money | Integer minor units locally | Avoids floating point errors |
| Preferences | DataStore | Lightweight settings |
| Security | Supabase Auth + RLS + encrypted session | Appropriate fintech baseline |
| Testing | JUnit, MockK, Turbine, Compose tests | Good coverage for layers |
| AI readiness | Future `feature:ai` through use cases | Adds AI without leaking data layers |

---

## 22. Final Recommendation

FinFlow should use:

```text
Now in Android modular style
+ Clean Architecture boundaries
+ Room-first offline source of truth
+ Supabase-backed sync
+ feature-owned presentation
+ core-owned domain/data
+ strong security and money handling
```

This is the best fit for a real-world fintech portfolio app: serious, maintainable, testable, and scalable without unnecessary enterprise complexity.

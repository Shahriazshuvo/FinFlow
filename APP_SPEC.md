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
app       = application shell
feature:* = screens and presentation logic
core      = domain models, repository contracts, use cases (pure JVM)
local_db  = Room local source of truth
network   = Supabase remote data source and encrypted session storage
service   = repository implementations, sync coordination, preferences
ui        = design system, MVI base classes, route keys
```

Ownership, not layer count, decides the split. `service` is the only module that sees both
`local_db` and `network`; neither of those knows the other exists.

FinFlow should not use feature-local data/domain layers by default. Shared business logic belongs
in `com.finflow.core.domain`; shared data access belongs in `service`.

See `docs/adr/0009-module-ownership-boundaries.md` for why these lines fall where they do.

---

## 2. Recommended Module Structure

```text
FinFlow/
├── app/                     MainActivity, FinFlowApplication, ui/, navigation/
│
├── feature/                 auth, dashboard, accounts, transactions, categories,
│                            budgets, goals, analytics, settings
│
├── core/                    pure JVM — depends on nothing in the project
│   ├── model/               domain models, Money, drafts, SyncStatus
│   ├── common/              AppResult, AppError, dispatchers, formatters, validators, Clock
│   ├── domain/              repository interfaces + use cases
│   └── testing/             fixtures, TEST_CLOCK, model builders (:core:testing)
│
├── local_db/                Room database, entities, DAOs, local data sources, mappers
│   └── schemas/             exported Room schemas, committed
│
├── network/                 Supabase client, DTOs, remote data sources, mappers
│   └── security/            Keystore-backed encrypted session storage
│
├── service/                 repository implementations, sync engine, datastore
│
├── ui/                      designsystem/, ui/ (MVI base), navigation/ (route keys)
│
└── build-logic/
```

Packages are `com.finflow.core.*` throughout; the module a file belongs to is its directory, not
its package.

This is close to Now in Android, but FinFlow needs stronger `security`, `sync`, and money-handling boundaries.

---

## 3. Dependency Rules

Allowed dependencies:

```text
app ──▶ service, ui, core, feature:*

feature:* ──▶ core, ui            (+ core:testing on testImplementation)

service ──▶ local_db, network, core

local_db ──▶ core     network ──▶ core     ui ──▶ core

core ──▶ nothing
```

Feature modules must not depend on:

```text
service
local_db
network
```

This makes architecture violations compile-time visible. ViewModels call use cases only.

Four walls hold it up:

1. **Features cannot reach the data layer.** `AndroidFeatureConventionPlugin` wires exactly
   `core` and `ui`; a DAO, DTO or repository implementation is an unresolved reference.
2. **The domain layer has no framework.** `core` is a pure JVM module, so the Android SDK, Room,
   Supabase and Compose are not on its classpath at all.
3. **Room and wire types never escape.** Entities, DAOs and projection rows are `internal` to
   `local_db`; DTOs and their mappers are `internal` to `network`. The data sources are the public
   boundary and return domain models.
4. **`service` has no UI.** It does not depend on `ui`, so Compose is off the sync engine's
   classpath.

Features never depend on `service`: they need repository *interfaces*, which live in `core`, and
the *implementations* are bound by Hilt at the `app` composition root.

---

## 4. Feature Module Shape

Each feature should be presentation-focused:

Each role gets its own package:

```text
feature/transactions/
├── navigation/
│   └── TransactionsNavigation.kt
└── presentation/
    ├── TransactionsRoute.kt          stateful entry point — stays at the root
    ├── screen/
    │   └── TransactionsScreen.kt     stateless, internal, @Preview
    ├── viewmodel/
    │   └── TransactionsViewModel.kt  @HiltViewModel, internal
    ├── contract/
    │   ├── TransactionsState.kt
    │   ├── TransactionsIntent.kt
    │   └── TransactionsEffect.kt
    ├── mapper/
    │   └── TransactionsUiMapper.kt
    └── components/
```

`XRoute.kt` stays at the presentation root on purpose: it is the one file that touches all four
packages, so it belongs above them rather than inside any one. `contract/` must not import from
`viewmodel/` — a KDoc link to the ViewModel is written fully qualified so the arrow does not point
backwards. Tests mirror the package of what they test.

Check 10 of `scripts/check-context.sh` fails if a presentation file is left at the root.

Use the same shape for accounts, budgets, goals, analytics, dashboard, categories, auth, and settings.

Do not create `data/` and `domain/` folders inside every feature unless a feature has genuinely private business logic. For this app, centralized `core` and `service` are cleaner.

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
It also depends on `service`, purely so Hilt can see the repository bindings; no app code calls
into it directly.

> **Not yet built — auth-aware routing.** `app/src/main/kotlin/com/finflow/ui/FinFlowApp.kt`
> hardcodes `isAuthenticated = true`, so the app always cold-starts in the main graph regardless
> of session state. The session flow that would drive it already exists
> (`AuthViewModel` observes it); only the wiring at the shell is missing.

---

## 6. Navigation

Use Navigation Compose.

Recommended root graph:

```text
Root
├── AuthGraph
│   └── Login/Signup          one screen, not two — see below
└── MainGraph
    ├── Dashboard
    ├── Transactions
    ├── Accounts
    ├── Categories
    ├── Budgets
    ├── Goals
    ├── Analytics
    └── Settings
```

Two deliberate departures from the sketch above as it was first drawn: the auth graph holds a
**single** Login/Signup destination, because §13 specifies one screen serving both modes and
`AuthState.mode` switches between them; and **Categories** is a live destination, though it is
not in the bottom bar.

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

These are modelled as `@Serializable` route keys (`TransactionDetailRouteKey` and friends) rather
than string paths, with `navigateTo*Detail` helpers alongside them.

> **Not yet built — detail destinations.** All four keys and their navigation helpers exist, but
> no feature registers a `composable<XDetailRouteKey>` for them, so none is reachable yet. A key
> becomes a live destination the moment its owning feature registers it.

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
- Formatting belongs in `com.finflow.core.common.formatter`.

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

> **Not yet built — the initial sync trigger.** Session restore and navigation both work; the sync
> step does not happen. `RequestSyncUseCase` exists but has no callers. Today a fresh signup has
> no data until the first local write fires `SyncTrigger`, or until the 6-hour periodic
> `SyncWorker` run scheduled at app start.

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

> **Not yet built — none of these four are enforced.** `feature:transactions` is still an
> `EmptyState` stub with no ViewModel, and neither `SaveTransactionUseCase` nor the repository
> checks any of them. `AmountValidator` exists in `com.finflow.core.common.validation` but has no
> call site on this path. Validation belongs behind the use case when the screen is built, so
> every entry point enforces the same rules.

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

Android should not duplicate the backend aggregations unless offline analytics is explicitly
needed.

**It is needed, so Android does duplicate them.** Analytics are aggregated from Room, not read
from the Supabase views. Reads must work offline like every other screen (§8), and a chart that
disagreed with the transaction list on the same device would be worse than no chart. This is the
escape hatch the paragraph above names, taken deliberately:
`docs/adr/0007-analytics-aggregated-from-room.md`.

The three backend views still exist and remain the reference definition:

```text
monthly_income_expense
monthly_category_spending
budget_usage
```

The Room aggregate queries in `TransactionDao` mirror them, and their projection rows in
`local_db` are named after them. **When a view definition changes in
`docs/supabase/schema.sql`, the matching Room query must change in the same commit** — nothing
mechanical enforces that agreement.

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

Use shared error types in `com.finflow.core.common.error`.

Categories:

```text
Unauthorized
Forbidden
Network
Offline
Validation
Duplicate
NotFound
Database
Unknown
```

`Forbidden` is separate from `Unauthorized` on purpose: an RLS policy refusing a row is not a
session problem, so signing in again would not help, and in a fintech app it is worth noticing
rather than retrying. §14 requires that distinction.

Repositories should map raw Supabase/Room exceptions into domain-safe errors. UI should receive user-friendly messages through UI mappers.

Examples:

```text
category duplicate index → Duplicate("name")   → "Category already exists."
budget duplicate index   → Duplicate("category") → "Budget already exists for this month."
RLS denied (42501)       → Forbidden           → "You do not have permission to do that."
network unavailable      → Offline             → "You're offline. Changes will sync later."
expired session          → Unauthorized        → "Your session expired. Please sign in again."
```

`Duplicate` carries the form field the message attaches to, so the screen can mark the offending
input rather than raising a snackbar the user cannot act on.

---

## 16. DataStore

Use DataStore for lightweight app preferences only:

- Theme
- Currency display preference
- Onboarding completion
- Last selected transaction filters
- Sync watermarks
- The signed-in user id

Sync watermarks live here rather than in Room: they are per-table replication bookkeeping, not
finance data, and keeping them out of the database means a sync failure cannot corrupt a
transaction.

"Last selected filters" persists only the type, category ids and account ids. The search query
and the date range are deliberately dropped — restoring a stale search box or a date window the
user has forgotten setting is worse than starting clean.

Do not store relational finance data in DataStore.

---

## 17. Design System

Both of these live in the `ui` module, as the `designsystem/` and `ui/` packages.

Use `com.finflow.core.designsystem` for:

- Theme
- Colors
- Typography
- Shapes  *(not yet built — `Theme.kt` passes only `colorScheme`, `typography` and `content`, so
  M3 defaults apply)*
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

Use `com.finflow.core.ui` for:

- MVI base helpers
- Effect collection
- UI error message mapping
- Shared Compose utilities

Route keys live in the same module, under `com.finflow.core.navigation`, so one feature can
navigate to another without depending on it.

Literal `dp`, alpha and duration values are legal **only** inside `designsystem/theme/`;
everywhere else they are a bug that check 2 of `scripts/check-context.sh` fails on. Read them as
`FinFlowTheme.dimens` / `.spacing` / `.alphas`.

> **Built but unwired:** `SyncStatusChip` and `ObserveSyncStatusUseCase` both exist and have no
> call sites. The sync status they render is available; no screen shows it yet.

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
- Debug logging disabled for prod
- Release minification eventually enabled

Never include service-role secrets in any variant.

`BuildConfig.DEBUG_LOGGING` is **flavor**-scoped, not build-type-scoped: it is true for `dev` and
`qa` and false for `prod`. A release `qa` build therefore has it on, which is the point — QA
testers run release builds and still need logs. It is deliberately not tied to `debuggable`.

> **Built but unwired:** nothing reads `DEBUG_LOGGING` yet. `SyncWorker` currently logs
> unconditionally through `android.util.Log`.

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
| Domain | Central `core` (pure JVM) | Shared use cases and contracts, framework-free |
| Data | Central `service` | Repository implementations and sync coordination |
| Module split | By ownership: `core`/`local_db`/`network`/`service`/`ui` | Each wall is a compile error |
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
+ Clean Architecture boundaries enforced by the compiler
+ Room-first offline source of truth
+ Supabase-backed background sync
+ feature-owned presentation
+ core-owned domain, service-owned data
+ strong security and money handling
```

This is the best fit for a real-world fintech portfolio app: serious, maintainable, testable, and scalable without unnecessary enterprise complexity.

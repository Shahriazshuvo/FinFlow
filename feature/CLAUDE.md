# feature:* — MVI presentation modules

Covers all nine feature modules; they are identical in shape. Spec: `APP_SPEC.md` §4 (what a
feature contains), §3 and §6 (how features share without seeing each other), §7 (MVI), plus §13,
the per-screen requirements.

**A feature holds presentation and navigation only.** Repositories live in `core:data`, use cases
in `core:domain`. Data is not UI-scoped — three screens read accounts — so no feature owns a
table. Reasoning: `docs/adr/0006-centralized-data-layer.md`.

**Two reference implementations.** `feature:auth` and `feature:accounts` are both fully built;
copy their shape. `feature:accounts` is the better model for a list-plus-editor screen, `auth` for
a form. The other seven are `EmptyState` stubs with finished use cases waiting behind them.

## File layout

One top-level class per file.

```
feature/accounts/src/main/kotlin/com/finflow/feature/accounts/
├── presentation/
│   ├── AccountsState.kt         data class, implements UiState
│   ├── AccountsIntent.kt        sealed interface, implements UiIntent
│   ├── AccountsEffect.kt        sealed interface, implements UiEffect
│   ├── AccountsViewModel.kt     @HiltViewModel, extends MviViewModel, internal
│   ├── AccountsUiMapper.kt      domain model → UI model
│   ├── AccountsRoute.kt         stateful: hiltViewModel, state collection, effects
│   ├── AccountsScreen.kt        stateless, internal, @Preview
│   └── components/              feature-local composables only
└── navigation/
    └── AccountsNavigation.kt    the NavGraphBuilder extension only
```

The `@Serializable` route key is **not** here — it lives in `core:navigation` so another feature
can navigate to this one without depending on it.

## The contract

`XState` is an immutable data class implementing `UiState` — loading, data, empty, form fields,
validation errors, selected filters, dialog visibility. `XIntent` and `XEffect` are sealed
interfaces. Navigation, snackbars and share actions are **effects, not state**.

Derived values belong on the state as computed properties (`AuthState.canSubmit`,
`AccountsState.isEmpty`), not as stored fields the reducer has to remember to keep in step.

`XViewModel : MviViewModel<XState, XIntent, XEffect>` gets state, intent and effect plumbing for
free — `onIntent`, `setState`, `sendEffect`, `currentState`. `handleIntent` is an exhaustive
`when`. Effects go through a `Channel`, so one emitted while the screen is backgrounded is
delivered once when it returns rather than dropped or replayed.

Lists come from Room as a `Flow`, so they are collected in `init` rather than fetched by a `Load`
intent. Sync updates Room and the screen follows on its own — see `AccountsViewModel.init`.

## Route vs Screen

`XRoute` is the only stateful composable: `hiltViewModel()`, `collectAsStateWithLifecycle()`, and
`ObserveAsEvents(viewModel.effect)` from `core:ui` for one-shot events. `XScreen` is `internal`,
takes state plus lambdas, holds no ViewModel, and has a `@Preview`. That split is what makes the
screen previewable and testable.

Keep the ViewModel `internal`. `XRoute` is public, so resolve the ViewModel in the body rather
than as a default parameter — an internal type in a public signature does not compile.

## Rules

- **Call use cases, never repositories.** A feature cannot even see `core:data` — the convention
  plugin does not put it on the classpath. Same for `core:database`, `core:network` and
  `core:datastore`: if you need a preference, there is a use case for it
  (`ObserveCurrencyCodeUseCase`), not a `DataStore`.
- **Never import another feature.** Share data through a use case, and navigate through a key in
  `core:navigation`. `scripts/check-context.sh` and the classpath both enforce this.
- **No literal dp, alpha or duration.** Use `FinFlowTheme.dimens` / `.spacing` / `.alphas`.
  `scripts/check-context.sh` fails the build on a literal in this tree.
- **Formatted strings cross the boundary, not `Money`.** The `UiMapper` turns domain models into
  UI models the screen can render directly.
- **Errors reach the UI through `AppError.toUserMessage()`** in `core:ui/error/`. A raw Supabase
  or SQLite exception must never reach a composable.
- `internal` by default. Only the navigation entry point and `XRoute` are public.
- A `build.gradle.kts` here is six lines — the plugin alias and a namespace. If you are adding
  dependencies, you are almost certainly reaching through a wall you should not be reaching
  through.
- A widget that appears in two or more features belongs in `core:designsystem`, not in
  `components/`.

## Tests

One ViewModel test per feature: `MainDispatcherRule` from `core:testing`, Turbine on `state` and
`effect`, MockK for the use cases. Assert intent → state and intent → effect. `AuthViewModelTest`
is the reference.

`core:testing` is already on every feature's test classpath. Use `testAccount()`,
`testTransaction()` and friends rather than hand-building models, and `TEST_CLOCK` rather than a
local `Clock.fixed`.

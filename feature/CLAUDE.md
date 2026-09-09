# feature:* — MVI presentation modules

Covers all seven feature modules; they are identical in shape. Playbook with worked templates:
skill `finflow-mvi-feature`. Spec: `APP_SPEC.md` §6 and §8.

**All seven are stubs today** — `Route`, `Screen`, `Navigation` only. There is no `Contract` or
`ViewModel` anywhere in the repo yet, so the first one you write sets the house style for the
other six. Follow this file exactly rather than inventing a pattern.

## File layout

```
feature/goals/src/main/kotlin/com/finflow/feature/goals/
├── presentation/
│   ├── GoalsContract.kt      GoalsState / GoalsIntent / GoalsEffect
│   ├── GoalsViewModel.kt     @HiltViewModel, extends MviViewModel
│   ├── GoalsUiMapper.kt      domain model → UI model
│   ├── GoalsRoute.kt         stateful: hiltViewModel, state collection, effects
│   ├── GoalsScreen.kt        stateless, internal, @Preview
│   └── components/           feature-local composables only
└── navigation/
    └── GoalsNavigation.kt    @Serializable route key + navigateToX + xScreen
```

## The contract

`XState` is an immutable data class implementing `UiState` — loading, data, empty, form fields,
validation errors, selected filters, dialog visibility. `XIntent` and `XEffect` are sealed
interfaces. Navigation, snackbars and share actions are **effects, not state**.

`XViewModel : MviViewModel<XState, XIntent, XEffect>` gets state, intent and effect plumbing for
free — `onIntent`, `setState`, `sendEffect`, `currentState`. `handleIntent` is an exhaustive
`when`. Effects go through a `Channel`, so one emitted while the screen is backgrounded is
delivered once when it returns rather than dropped or replayed.

## Route vs Screen

`XRoute` is the only stateful composable: `hiltViewModel()`, `collectAsStateWithLifecycle()`, and
`ObserveAsEvents(viewModel.effect)` from `core:ui` for one-shot events. `XScreen` is `internal`,
takes state plus lambdas, holds no ViewModel, and has a `@Preview`. That split is what makes the
screen previewable and testable.

## Rules

- **Call use cases, never repositories.** A feature cannot even see `core:data` — the convention
  plugin does not put it on the classpath.
- **No literal dp, alpha or duration.** Use `FinFlowTheme.dimens` / `.spacing` / `.alphas`.
  `scripts/check-context.sh` fails the build on a literal in this tree.
- **Formatted strings cross the boundary, not `Money`.** The `UiMapper` turns domain models into
  UI models the screen can render directly.
- `internal` by default. Only the navigation entry points and `XRoute` are public.
- A `build.gradle.kts` here is six lines — the plugin alias and a namespace. If you are adding
  dependencies, you are almost certainly reaching through a wall you should not be reaching
  through.
- A widget that appears in two or more features belongs in `core:designsystem`, not in
  `components/`.

## Tests

One ViewModel test per feature: Turbine on `state` and `effect`, MockK or a hand-written fake for
the use cases, `StandardTestDispatcher`. Assert intent → state and intent → effect.
# core:designsystem — theme, tokens and shared components

This is the **one module where literal `.dp` and alpha values are legal**, and only inside
`theme/`. Everywhere else in the app they are a bug that `scripts/check-context.sh` fails on.
Spec: `APP_SPEC.md` §17.

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

`component/` holds `AppBars`, `Buttons`, `Cards`, `ConfirmDialog`, `SelectionSheet`, `States`,
`SyncStatusChip`, `TextFields`. House style, visible in `Buttons.kt`:

- `modifier: Modifier = Modifier` is the first optional parameter, after the required ones.
- State is hoisted. A component takes values and lambdas, never a ViewModel.
- Components own their own loading/disabled behaviour so screens cannot forget it — see
  `FinFlowButton`, which disables itself while `isLoading`.
- Every public component has a `@Preview`.
- A KDoc that says *why* the component exists, not what it renders.

## What belongs here

A widget graduates from a feature into this module when it appears in **two or more** features,
represents app-wide design language, or handles a common loading / empty / error / dialog / sheet
/ form pattern. A component that depends on one feature's `State` or UI model stays in that
feature's `components/`.
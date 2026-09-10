# 0002. Money is an integer minor-unit value class, never a float

**Status:** Accepted
**Date:** 2025-01-15

## Context

Binary floating point cannot represent most decimal fractions. `0.1 + 0.2 != 0.3` is the famous
case; the one that matters here is that a few thousand additions of `Double` cents drift, and a
budget that reads `$499.99999999` or an account total that disagrees with the sum of its rows is
not a rounding nuisance in a finance app — it destroys trust in every number on the screen.

Postgres stores amounts as `numeric(12,2)`, which is exact. Nothing on the Android side should be
less exact than the database.

## Decision

`Money` in `core:model` is a value class over a `Long` count of minor units (cents). It is stored
as `Long` in Room and serialized to `numeric(12,2)` in Postgres. `Double` and `Float` never
appear in a monetary path.

Conversion to and from major units goes through `BigDecimal`, never `Double`.

## Consequences

- Arithmetic is exact and associative; totals do not depend on summation order.
- Currencies with other minor-unit exponents (JPY has none, KWD has three) need the exponent
  handled at format time rather than assumed to be two.
- Division needs an explicit rounding decision at each call site. There is no silently-correct
  default, which is the point.
- `Money` must not cross into a composable. Formatting happens in a feature's `UiMapper`, so a
  screen cannot invent its own currency rendering.

## Alternatives considered

- **`BigDecimal` end to end.** Rejected: correct but allocation-heavy on a hot list path, and it
  admits scale mismatches (`1.5` vs `1.50`) that compare unequal.
- **`Double` with rounding at the edges.** Rejected: pushes the error to whoever forgets to round,
  and the failure is silent and cumulative.
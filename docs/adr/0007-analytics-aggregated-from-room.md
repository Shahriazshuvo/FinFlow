# 0007. Analytics are aggregated from Room, not read from the Supabase views

**Status:** Accepted
**Date:** 2026-09-10

## Context

`APP_SPEC.md` §13 (Analytics) says:

> Use backend views: `monthly_income_expense`, `monthly_category_spending`, `budget_usage`.
> Android should not duplicate these aggregations unless offline analytics is explicitly needed.

The code does duplicate them. `TransactionDao` carries `GROUP BY` queries, `AggregateRows.kt`
holds their projections, `AnalyticsLocalDataSource` combines them and `AnalyticsRepositoryImpl`
exposes them as `Flow`s. This ADR records why, so the next reader does not "fix" it back.

The question was reopened when the spec was rewritten in commit `960bf38`. The previous spec
specified Room aggregation outright; the current one prefers the views. The conclusion did not
change, but the reasoning now has to be written down rather than pointed at.

## Decision

Analytics are computed from Room. The three Supabase views remain in the schema and remain the
server-side cross-check, but no screen reads them.

This is the escape hatch §13 names — *"unless offline analytics is explicitly needed"* — and for
FinFlow it is needed, on three counts:

- **Offline is the normal case, not a degraded one** (§1, §8, `docs/adr/0001`). A chart backed by a
  view is blank on a train. Every other screen in the app keeps working there; the analytics screen
  would be the only one that does not.
- **A view disagrees with the list rendered beside it.** A transaction added offline is `PENDING_CREATE`
  in Room and absent from the server, so a server-computed total contradicts the transaction list on
  the same screen. Two numbers that disagree are worse than one number that is slightly stale.
- **The aggregation is four `GROUP BY` queries.** The duplication §13 warns about is real but small,
  and it is duplication of arithmetic that is fixed by the schema, not of business rules that drift.

## Consequences

- The analytics screen has no network loading state and no empty-because-offline state.
- The Room queries and the view definitions must agree. `docs/supabase/schema.sql` is the reference
  for both; changing one without the other makes the cross-check meaningless.
- Sums are computed over integer minor units in SQLite and `numeric(12,2)` in Postgres, so the two
  agree exactly rather than to within a rounding error — see `docs/adr/0002`.
- Rows with a non-null `deleted_at` are excluded on both sides. A tombstone that is filtered in one
  place and not the other is the most likely way these two drift apart.
- If offline analytics is ever dropped as a requirement, this decision reverses cleanly:
  `AnalyticsRepositoryImpl` is the only place that would change, because nothing above it knows
  where the numbers come from.

## Alternatives considered

- **Read the views, as §13 prefers.** Rejected for the three reasons above. It would be the right
  call for an online-first app, and it is why the spec states that preference.
- **Views when online, Room when offline.** Rejected: two code paths that must produce identical
  numbers, with the disagreement only visible to users on bad connections — the worst place to put a
  correctness bug.
- **Cache the view results in Room.** Rejected: this is the view approach with a staleness window
  bolted on, and it still cannot account for local pending writes.

# 0001. Room is the source of truth; the network is a background concern

**Status:** Accepted
**Date:** 2025-01-15

## Context

FinFlow is offline-first (`APP_SPEC.md` §8, §12). A personal-finance app is used on trains, in
basements and on aeroplanes, and a user who records a coffee purchase expects it on screen
whether or not Supabase is reachable.

Two shapes were available. Either the UI reads from the network and caches, or the UI reads from
the database and the network fills it in behind. The first makes every screen carry a loading and
an error state for data the device already has, and makes correctness depend on connectivity.

## Decision

Every read is a `Flow` from Room. Every write lands in Room first with a `SyncStatus`
(`PENDING_CREATE`, `PENDING_UPDATE`, `PENDING_DELETE`), stamps `updatedAt` from an injected
`Clock`, then asks WorkManager to push. A repository write returns as soon as Room has it; it
never waits on the network and never returns a network error to its caller.

Deletes are tombstones, never row removals, so a delete can be pushed after the fact.

## Consequences

- Screens have no network loading state for data already stored. The only network-shaped UI is
  the sync indicator.
- Writes cannot report server-side rejections at the call site. Uniqueness is therefore checked
  locally before the write as well as being enforced by Postgres — see the `findConflictingId`
  queries on `BudgetDao` and `CategoryDao`.
- Conflict resolution is last-write-wins on `updated_at`, which is only correct if push happens
  before pull. `TableSyncRunner` enforces that ordering, and reversing it silently discards
  offline edits.
- The Room schema must mirror the Postgres schema closely enough that a row survives the round
  trip, which couples migrations on both sides.

## Alternatives considered

- **Network-first with a cache.** Rejected: makes offline a degraded mode rather than the normal
  one, and puts a loading state on every screen.
- **Supabase Realtime as the read path.** Rejected: a live subscription is not a substitute for
  local persistence, and it fails exactly when offline support matters.
# core:database — Room entities, DAOs, local data sources

Room is the source of truth for every piece of financial data. Playbook for schema changes: skill
`finflow-room-migration`. The Postgres side this mirrors: `docs/supabase/schema.sql`.

## Entity conventions

Entities mirror `public.*` in Postgres **exactly**, because the sync engine assumes the two agree.

- `@ColumnInfo(name = "snake_case")` on every field, matching the Postgres column name.
- `Money` persists as `Long` (integer minor units), `numeric(12,2)` on the server.
- `Instant` and `LocalDate` go through `FinFlowTypeConverters`. Enums persist as `String`.
- `SyncMetadata` is `@Embedded` in every syncable entity — local-only bookkeeping that is never
  serialized to Supabase.
- Index every column combination a `user_id`-scoped query filters or sorts on, plus every foreign
  key and `sync_status`. See `TransactionEntity` for the pattern.
- Foreign keys are `ForeignKey.NO_ACTION`. Nothing is hard-deleted — deletes are soft, via
  `deleted_at` tombstones, so a cascade would be wrong.

## The mapper wall

Entities never leave this module. Local data sources return domain models from `core:model`,
converted in `mapper/EntityMappers.kt`. If a `*Entity` type appears in a `core:domain` signature,
the wall has been breached.

## Changing the schema

Bumping `version` in `FinFlowDatabase.kt` is a four-step ritual, and skipping any step ships a
crash on upgrade:

1. Bump `version` and write the `Migration`.
2. Build — `exportSchema = true` writes `core/database/schemas/<db>/N.json`. **Commit that file.**
3. Add a `MigrationTestHelper` test migrating N-1 → N with real data.
4. Mirror the change in `docs/supabase/schema.sql`, and update the DTO and both mappers in
   `core:network`.

Current state: `version = 1`, with `1.json` committed. `scripts/check-context.sh` fails if the
version and the exported schema files disagree.
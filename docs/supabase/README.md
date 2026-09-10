# Supabase backend

`schema.sql` is the source of truth for the remote side of FinFlow. Run it in the Supabase
SQL editor; it is idempotent and safe to re-run.

## What the client relies on

- **RLS is the security boundary.** Every table is `user_id`-scoped and the app ships only
  the publishable (anon) key. The `user_id` filters in `core:network` are defence in depth,
  not the protection itself.
- **`handle_new_user` seeds new accounts.** On signup it creates the `profiles` row, a
  default `Cash` account and eight starter categories. The Android client therefore never
  inserts a profile and has no "create default categories" path.
- **`set_updated_at` triggers** keep `updated_at` accurate on every table. Incremental pull
  in `core:data` filters on `updated_at > watermark`, so this trigger is what makes sync
  correct.
- **Soft delete via `deleted_at`.** Pulls deliberately fetch soft-deleted rows: a tombstone
  is how one device learns about a record another device removed. `profiles` has no
  `deleted_at` — profiles are never deleted.
- **Analytics views** (`monthly_income_expense`, `monthly_category_spending`,
  `budget_usage`) exist and are `security_invoker`. The MVP computes the same figures
  locally from Room so analytics work offline; the views are the server-side cross-check.

## Money

Money columns are `numeric(12,2)`. The client holds amounts as integer minor units
(`Money` in `core:model`) so local sums and budget comparisons are exact, and converts to
`BigDecimal` at the network boundary — never through a `Double`.

## Credentials

Add to `local.properties` (git-ignored), or set the same names as environment variables:

```properties
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<publishable anon key>
```

These serve every build flavor. To point one environment at its own Supabase project, add a
suffixed pair — the suffix wins, and anything unsuffixed remains the fallback:

```properties
SUPABASE_URL_QA=https://<qa-project-ref>.supabase.co
SUPABASE_ANON_KEY_QA=<qa publishable anon key>
```

Recognised suffixes are `_DEV`, `_QA` and `_PROD`, matching the three flavors in `APP_SPEC.md`
§19. Missing credentials are not a build failure — they resolve to `""`, so a fresh clone builds —
but `SupabaseModule` fails fast at injection time with a message naming the keys.

**Never put the service-role key in the app.** Only the anon/publishable key ships; RLS is the
security boundary, and `user_id` filters in queries are defence in depth, not the boundary.

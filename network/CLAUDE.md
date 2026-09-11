# :network — Supabase client, DTOs, remote data sources

Everything that talks to Supabase, and nothing else. Spec: `APP_SPEC.md` §8 and §14.

**Network is never on the read path.** A screen reading accounts never reaches this module —
reads are Room `Flow`s. This module is only exercised by the sync engine in `:service`, in the
background. If you find yourself adding a fetch to satisfy a screen, the answer is a Room query
plus sync, not a call from here.

## What is in this module

| Package | Role |
|---|---|
| `network/di/` | `SupabaseModule` — the `SupabaseClient`, credentials from `BuildConfig` |
| `network/dto/` | The seven wire DTOs and `BigDecimalSerializer`. **All `internal`.** |
| `network/mapper/` | `DtoMappers` — DTO↔domain, `internal` |
| `network/datasource/` | `AuthRemoteDataSource` + six table sources — the public boundary |
| `network/error/` | `DataErrorMapper` — every exception becomes an `AppError` |
| `network/util/` | Postgres timestamp parsing |
| `security/` | `EncryptedKeyValueStore`, Keystore-backed, behind Supabase's `SessionManager` |

## The DTO wall

DTOs are `internal` and never leave this module. Remote data sources return **domain models** —
`fetchSince` decodes an `AccountDto` and maps it to `Account` in the same expression, so
`:service` never sees a wire type. Check 11 of `scripts/check-context.sh` fails if a DTO is made
public.

DTOs mirror `docs/supabase/schema.sql` column for column. Money is `numeric(12,2)` decoded as
`BigDecimal` and converted to `Money` minor units by the mapper — never a floating point number.
Timestamps are `String` on the wire.

`toDomain()` hardcodes `syncStatus = SYNCED`: anything arriving from the server is by definition
synced, and the local replication bookkeeping is applied by `:local_db`, not here.

## Errors

`DataErrorMapper` is the one translation point (`APP_SPEC.md` §15). No Ktor, Supabase or SQLite
exception may escape as itself. It covers Room as well as Supabase, because a repository's
`syncWith` wraps both in one `runCatchingApp` — that is why it lives here rather than in
`:service`, where it would drag Supabase types onto that classpath.

SQLSTATE `23505` becomes `AppError.Duplicate(field)` with the index name mapped back to a form
field; `42501` becomes `AppError.Forbidden`; a lost connection becomes `AppError.Offline`.

## Credentials

Only the anon/publishable key ships. RLS is the security boundary; the `user_id` filters in
queries are defence in depth, not the boundary. Credentials come from `local.properties` or the
environment via `network/build.gradle.kts`, per flavor, and a fresh clone builds without them.
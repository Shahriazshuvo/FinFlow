# 0008. `core` is one Gradle module; layering is a script check, not a compile error

**Status:** Accepted
**Date:** 2026-09-10

**Supersedes:** `0003-pure-jvm-domain-modules.md`
**Amends:** `0004-convention-plugins-enforce-layering.md`,
`0006-centralized-data-layer.md` (the enforcement mechanism only — the layering itself stands)

## Context

`core` was thirteen Gradle subprojects: `model`, `common`, `domain`, `database`, `network`,
`datastore`, `data`, `sync`, `security`, `designsystem`, `ui`, `navigation`, `testing`. Thirteen
`build.gradle.kts` files and thirteen `settings.gradle.kts` entries for 130 Kotlin files.

The cost being paid for that split was maintenance friction: every dependency change meant
finding the right one of thirteen build files, and the seven `api(project(...))` edges between
them had to be kept correct by hand. The benefit being bought was two compile-time walls:

- **Wall 1** — features could not see `core:data`, `core:database`, `core:network` or
  `core:datastore`, because `AndroidFeatureConventionPlugin` did not put them on the classpath.
- **Wall 2** — `model`, `common` and `domain` applied `JvmLibraryConventionPlugin`, so the
  Android SDK was not on the domain layer's classpath at all (ADR 0003).

At 9 domains and 1 developer, the friction is paid daily and the walls catch a mistake that has
not yet been made once.

## Decision

Merge twelve of the thirteen into a single Android library at `core/`, keeping the existing
package names (`com.finflow.core.<former module>`) exactly as they were. Enforce the feature →
data-layer boundary in `scripts/check-context.sh` instead of in the classpath.

`core:testing` stays a separate module. It `api`-exports JUnit, MockK, Turbine and
`coroutines-test`; folding it into `:core` would put four test libraries on the production
compile classpath of `:app` and all nine features. It converts from a JVM library to an Android
one, because a JVM module cannot resolve an AAR — and `:core` is an AAR now.

**Result: 13 build files → 2, 23 modules → 12.** No source file changed its package or its
imports; the merge was a directory move plus one `BuildConfig` import.

## Consequences

**Wall 1 is now check 3 of `scripts/check-context.sh`** — a grep for
`import com.finflow.core.{data,database,network,datastore,security,sync}.` under `feature/`. Note
it covers **six** packages where the classpath covered four: `datastore` and `security` were
reachable in principle before and nothing would have reported it, because the old check 3 named
only three packages and relied on the classpath for the rest. The check is stricter than what it
replaces, and weaker in the way that matters — it runs when someone runs it.

ADR 0004 chose convention plugins over Lint/Konsist precisely because a lint rule "runs late,
produces a warning rather than a failure, and is one flag away from being off." That objection
now applies to this decision. It is accepted knowingly: check-context.sh exits non-zero, is named
in `CLAUDE.md`'s "Before you finish", and is cheap enough to run every time. There is no CI in
this repo to run it automatically — if CI is ever added, this check belongs in it.

**Wall 2 is gone.** An `android.*` import in `com.finflow.core.domain` compiles now. Nothing
catches it; keeping the domain layer framework-free is a review matter. `JvmLibraryConventionPlugin`
and `JvmHiltConventionPlugin` are left in `build-logic` unused, for a future pure-JVM module.

**`internal` became load-bearing.** It is module-scoped, so DTOs, entities, mappers and repository
implementations stay invisible to `feature:*` and `app` — that part of the boundary survives the
merge intact and is now the only part the compiler still enforces. New data-layer types must be
`internal`. Inside `:core`, though, `internal` no longer separates anything: a repository can
import `AccountDto` directly, which was a compile error the day before this ADR.

**Unit tests moved to the Android task.** `model`, `common` and `domain` tests ran on a plain JVM
test task; they now run under `testDevDebugUnitTest`, which is slower. `./gradlew test` no longer
runs anything useful — there are no JVM modules left. `CLAUDE.md` and the docs say
`testDevDebugUnitTest` everywhere now.

**Incremental builds got coarser.** Thirteen modules compiled and cached independently under
`org.gradle.parallel` and `org.gradle.caching`. Touching one file in `:core` now recompiles all
126, reruns KSP for Room and Hilt over the whole module, and rebuilds all nine features. One R
class instead of thirteen claws a little back. This was accepted as the price of the build-file
ergonomics, not sold as a speedup.

**Two things moved on disk.** `core/database/schemas/` → `core/schemas/`, because
`AndroidRoomConventionPlugin` sets `schemaDirectory("$projectDir/schemas")`. The FQCN-named
subfolder keeps its name and `1.json` is byte-identical, since `FinFlowDatabase` kept its package.
And `BuildConfig` moved from `com.finflow.core.network` to `com.finflow.core` — one import in
`SupabaseModule.kt`.

**Three unused test dependencies were dropped** rather than carried into the merged build file:
`robolectric` and `androidx-junit` (declared by `core:database`, which had no test source set at
all) and `androidx-work-testing` (declared by `core:sync`, same). No Robolectric runner or
`work-testing` usage exists anywhere in the repo.

**The context layer collapsed with the code.** `core/data/CLAUDE.md`, `core/database/CLAUDE.md`
and `core/designsystem/CLAUDE.md` became one `core/CLAUDE.md`.

## The way out

If `:core` grows past comfort, the next step is the one ADR 0006 already named: per-domain data
modules (`:data:accounts`) sitting beside the features — **not** data inside features. That would
also restore Wall 1 as a compile error for the modules it splits out.
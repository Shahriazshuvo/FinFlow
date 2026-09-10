# 0004. Layering is a compile error, not a review convention

**Status:** Accepted
**Date:** 2025-01-15

## Context

Architecture documents describing which module may depend on which are followed until the first
deadline. The dependency that breaks the layering is always locally reasonable — someone needs one
field from a DTO and adding the module takes ten seconds. By the time it is visible in review, the
import is load-bearing.

Eighteen modules, later twenty, each with its own `build.gradle.kts`, also means eighteen places
for the Android and Kotlin configuration to drift apart.

## Decision

Module configuration lives in convention plugins in `build-logic`, not in the modules. A feature's
`build.gradle.kts` is a plugin alias and a namespace, and declares no dependencies of its own —
`AndroidFeatureConventionPlugin` decides what a feature can see.

The walls are therefore properties of the classpath. Code that reaches through one does not
compile.

## Consequences

- Adding a dependency to a feature means editing the convention plugin, which is a visible,
  reviewable, single-place change rather than one line in one of nine files.
- The rules are only as good as the plugin. When the architecture changes, the plugin changes
  first and the docs follow — see [0006](0006-feature-owned-vertical-slices.md).
- `scripts/check-context.sh` covers what the classpath cannot: it catches feature-to-feature
  imports, hardcoded design literals and stale documentation, none of which is a compile error.
- Convention plugins are pinned to AGP internals. AGP 9 moved the `xxx(action)` DSL helpers to
  `ApplicationExtension`/`LibraryExtension` and made `CommonExtension` non-generic, and AGP 9
  applies Kotlin itself — applying `org.jetbrains.kotlin.android` in a convention plugin fails
  with "Cannot add extension with name 'kotlin'".

## Alternatives considered

- **Documented rules plus code review.** Rejected: this is the failure mode being designed out.
- **A custom Lint or Konsist check.** Rejected as the primary mechanism: it runs late, produces a
  warning rather than a failure, and is one flag away from being off. It is a reasonable
  supplement, which is the role `check-context.sh` plays.
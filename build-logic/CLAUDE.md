# build-logic — Gradle convention plugins

Every module's build config lives here, not in its `build.gradle.kts`. A feature module's build
file is six lines: a plugin alias and a namespace.

## AGP 9 landmines

These are the ways an agent breaks this build while trying to help.

- **Never apply `org.jetbrains.kotlin.android`.** AGP 9 has built-in Kotlin support and registers
  the `kotlin` extension itself; applying KGP's Android plugin on top fails with *"Cannot add
  extension with name 'kotlin'"*. There is deliberately no such alias in the version catalog —
  the absence is intentional, not an oversight.
- **The Kotlin version is pinned through the root `buildscript` classpath**, not through a plugin
  alias. AGP 9 carries a runtime dependency on KGP 2.2.10 and silently pins it unless a newer KGP
  is on that classpath. Without the block in the root `build.gradle.kts`, the catalog's `kotlin`
  version would only reach the Compose compiler plugin, leaving the Kotlin and Compose compilers
  mismatched.
- **`CommonExtension` is not generic in AGP 9**, and the `xxx(action)` DSL helpers moved to
  `ApplicationExtension` / `LibraryExtension`. Configure shared properties through `apply { }` on
  the exposed values — see `configureKotlinAndroid`.
- **KSP must track the Kotlin version's semver line.** They are bumped together or not at all.

## Version catalog

`compileSdk` is **36**. Six entries in `gradle/libs.versions.toml` carry a comment saying their
next version requires compileSdk 37 — `coreKtx`, `lifecycle`, `composeBom`, `navigation`,
`androidxHilt` — and `supabase` is held at 3.6.0 because 3.7.0+ needs Kotlin 2.4.x. **Read those
comments before bumping anything.** They are not stale pins; they are a coordinated hold.

Moving to compileSdk 37 means bumping `compileSdk`, then moving those five AndroidX entries
together in one commit.

Catalog accessors — `libs`, `lib()`, `bundle()`, `version()`, `intVersion()`, `pluginId()` — live
in `com/finflow/buildlogic/CatalogExtensions.kt`.

## The plugins

`AndroidApplication`, `AndroidLibrary`, `AndroidLibraryCompose`, `AndroidFeature`, `AndroidHilt`,
`AndroidRoom`, `JvmLibrary`, `JvmHilt`, `KotlinSerialization`.

`AndroidFeatureConventionPlugin` is load-bearing architecture, not convenience: it wires exactly
`:core` and `:ui` into every feature module and deliberately omits `:service`, `:local_db` and
`:network`. That omission is what makes the layering rule a compile error — a feature cannot
inject a repository implementation, touch a DAO or import a DTO. Do not add those to make
something compile; the thing you are reaching for belongs behind a use case.

Features do not need `:service` because repository implementations are bound by Hilt at the
`:app` composition root. Background: `docs/adr/0009-module-ownership-boundaries.md` and
`docs/adr/0006-centralized-data-layer.md`.

No feature is ever wired to another feature, which is why feature-to-feature coupling cannot
happen by accident. Route keys live in `:ui` for exactly this reason.

`core:testing` is added to every feature's **test** classpath here, so a feature test gets
`MainDispatcherRule`, `TEST_CLOCK` and the model builders without declaring anything.

Java toolchain 17, core-library desugaring on (mandatory — `minSdk` is 24 and `java.time` is
native only from API 26). Unit-test deps come free via `configureUnitTestDependencies`.

## Build flavors

`Flavors.kt` declares one dimension, `environment`, with `dev`, `qa` and `prod` (`APP_SPEC.md`
§19). `dev` and `qa` take an `applicationIdSuffix` so all three install side by side, and carry
`BuildConfig.DEBUG_LOGGING = true` — a non-production affordance, deliberately not tied to the
debug build type.

Both `AndroidApplicationConventionPlugin` and `AndroidLibraryConventionPlugin` call
`configureFlavors`. Every Android module must declare the dimension or variant resolution fails
with "unable to find a matching variant"; `:core` and `:core:testing` are exempt because
`JvmLibraryConventionPlugin` makes them pure JVM, so they have no
Android variants. `configureFlavors` is **two typed overloads**, not one generic function, because
AGP 9's `CommonExtension` is not generic and only the application flavor type has
`applicationIdSuffix`.

Adding flavors renamed the variant-aware tasks. Use `:app:assembleDevDebug` and
`testDevDebugUnitTest`; plain `assembleDebug` and `testDebugUnitTest` are now ambiguous.

Supabase credentials are attached per flavor in `network/build.gradle.kts`, reading
`SUPABASE_URL_<FLAVOR>` with a fallback to the unsuffixed key.

Java toolchain 17, core-library desugaring on (mandatory — `minSdk` is 24 and `java.time` is
native only from API 26). Unit-test deps come free via `configureUnitTestDependencies`.
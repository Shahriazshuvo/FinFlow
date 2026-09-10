# 0003. `core:model`, `core:common` and `core:domain` keep Android off the classpath

**Status:** Accepted
**Date:** 2025-01-15

## Context

Domain logic — money arithmetic, validators, balance derivation, use cases — is the part of the
app most worth testing and least in need of a device. When domain code sits in an Android module,
its tests need Robolectric or an emulator, and a test suite that takes minutes stops being run.

There is also a subtler pull: if `android.text.TextUtils` or `androidx.annotation` is on the
classpath, someone eventually uses it, and the domain layer quietly acquires a framework
dependency that makes it untestable and unportable.

## Decision

`core:model`, `core:common` and `core:domain` apply `JvmLibraryConventionPlugin`, not the Android
library plugin. The Android SDK is not on their compile classpath, so an `android.*` or
`androidx.*` import there is a compile error rather than a review comment.

Hilt in these modules uses `hilt-core`, not `hilt-android`, for the same reason —
`JvmHiltConventionPlugin`.

## Consequences

- Domain tests are plain JUnit and run in milliseconds.
- `java.time` is used directly. This is why core-library desugaring is mandatory: `java.time` is
  native only from API 26 and `minSdk` is 24.
- Anything genuinely Android-shaped that the domain needs must be expressed as an interface here
  and implemented in an Android module — `SyncTrigger` is the example.
- `@Parcelize` is unavailable on domain models. Navigation passes IDs, not objects, which
  `APP_SPEC.md` §6 wants anyway.

## Alternatives considered

- **Android library modules with a lint rule.** Rejected: a lint rule is a warning someone can
  suppress; an absent classpath is not.
- **One `core` module.** Rejected: it would have to be an Android module for Room and Supabase,
  which loses the fast test suite entirely.
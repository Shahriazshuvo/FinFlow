# Module graph

18 modules. The interesting part is not the list — it is which edges are **absent**, and that
their absence is enforced by the build rather than by review.

## Allowed edges

```
app ──────────────▶ feature:*  ──▶ core:designsystem, core:ui, core:common, core:model, core:domain
 │                                       │
 └──▶ core:data ──▶ core:database ──┐    └──▶ (nothing else — see below)
      │            core:network ────┤
      │            core:datastore ──┘
      └──▶ core:domain (via `api`, the one intentional transitive edge)

core:sync ──▶ core:data          (WorkManager lives here and nowhere else)

core:model ◀── everything        pure JVM, depends on nothing in the project
core:common ──▶ core:model       pure JVM
core:domain ──▶ core:model, core:common   pure JVM
```

## The two walls

**Wall 1 — features cannot reach the data layer.** `AndroidFeatureConventionPlugin` wires exactly
five project dependencies into every feature module and omits `core:database`, `core:network` and
`core:data`. A feature that tries to inject a repository implementation, touch a DAO or import a
DTO does not compile. Features talk to use cases in `core:domain`; that is the whole surface.

Enforced in: `build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt`.

**Wall 2 — the domain layer has no Android.** `core:model`, `core:common` and `core:domain` apply
`JvmLibraryConventionPlugin`, so the Android SDK is not on their classpath at all. An
`android.*` or `androidx.*` import there is a compile error. The payoff is that domain logic and
its tests run in milliseconds on the JVM with no emulator and no Robolectric.

Enforced in: `build-logic/convention/src/main/kotlin/JvmLibraryConventionPlugin.kt`.

## Why the walls point this way

Both walls exist to keep the dependency arrows pointing inward. `core:model` knows about nothing;
`core:domain` knows about models; `core:data` knows about domain interfaces and implements them;
features know about domain use cases. Nothing outer is visible to anything inner, so the pure-JVM
core can be reasoned about and tested without the app existing.

The one deliberate exception is `core:data`'s `api(project(":core:domain"))` — the module that
implements the repository interfaces re-exports them, so Hilt bindings in `app` can see both
sides.

## Module table

The canonical list with responsibilities is the table in `CLAUDE.md`. It is checked against
`settings.gradle.kts` by `scripts/check-context.sh`, so the two cannot drift apart. `README.md`
carries an abridged copy for people arriving from GitHub.

## Adding a module

1. `include(":core:x")` in `settings.gradle.kts`.
2. A `build.gradle.kts` applying the right convention plugin alias and a namespace.
3. Add the row to the `CLAUDE.md` module table — `scripts/check-context.sh` fails until you do.
4. Add the edge to this file if it changes the shape above.

# Module graph

12 modules. The interesting part is not the list — it is which edges are **absent**, and how
their absence is kept absent.

## Allowed edges

```
app ──▶ feature:*  ──▶ core
 │                       │
 └──────────────────────▶┘

core:testing ──▶ core     test classpaths only (features + core's own tests)
```

That is the whole graph. Everything that used to be an edge between core modules is now a
call between packages inside `:core`:

```
com.finflow.core.
  model         depends on nothing
  common    ──▶ model
  domain    ──▶ model, common               repository interfaces + use cases
  database  ──▶ model, common               Room; entities never leave this package
  network   ──▶ model, common, security     Supabase; DTOs never leave this package
  datastore ──▶ model, common
  security                                  the encrypted session manager
  data      ──▶ domain, database, network, datastore
  sync      ──▶ data, datastore, common     WorkManager lives here and nowhere else
  designsystem
  ui        ──▶ designsystem, common, model
  navigation                                @Serializable route keys
```

## The wall

**Features cannot reach the data layer.** A feature holds presentation and navigation only. It
uses `com.finflow.core.` `designsystem`, `ui`, `navigation`, `common`, `model` and `domain`, and
must not touch `data`, `database`, `network`, `datastore`, `security` or `sync`. Features talk to
use cases in `com.finflow.core.domain`; that is the whole surface.

Enforced in: **check 3 of `scripts/check-context.sh`**, which fails on any `feature/**.kt` with
an `import com.finflow.core.{data,database,network,datastore,security,sync}.`.

**This is a script, not the compiler.** Until `docs/adr/0008-single-core-module.md` there were
two walls and both were compile errors:

- *Wall 1* was `AndroidFeatureConventionPlugin` wiring six core modules and omitting four. With
  one `:core` there is nothing to omit — a feature's classpath now carries the DAOs, DTOs and
  repository implementations along with the use cases.
- *Wall 2* was `core:model`/`common`/`domain` applying `JvmLibraryConventionPlugin`, which kept
  the Android SDK off the domain layer's classpath entirely. `:core` is an Android module, so an
  `android.*` import in `com.finflow.core.domain` now compiles. Nothing catches it.

What still holds mechanically is **`internal`**. `internal` is module-scoped, and `feature:*` and
`app` are still separate modules, so DTOs, entities, mappers and repository implementations
remain invisible to them. Keep new data-layer types `internal` — it is load-bearing now, not
tidiness.

Inside `:core` the package walls are convention. The two that lost their enforcement and matter
most: **DTOs never leave `network/`, entities never leave `database/`.**

## The edge that does not exist at all

**No feature is ever on another feature's classpath.** Nothing wires one, so it cannot happen by
accident — this wall is untouched by the merge. The two sanctioned channels between features are:

- **Shared data** — both call the same use case in `com.finflow.core.domain`. There is one source
  of truth underneath, so nothing needs to be passed between them.
- **Route keys** — every `@Serializable` destination key lives in `com.finflow.core.navigation`,
  so a feature can navigate to a screen it cannot otherwise see. The
  `NavGraphBuilder.xScreen()` builder stays in the owning feature, because it references the
  screen composable.

This is why there is no cross-feature reader layer, no feature `api` module and no event bus. See
`docs/adr/0006-centralized-data-layer.md`.

## Why the walls point this way

The dependency arrows still point inward: `model` knows about nothing; `domain` knows about
models; `data` knows about domain interfaces and implements them; features know about domain use
cases. The merge did not change the design — it changed who checks it. Read the package list
above as the intended graph and keep new code inside it.

## Module table

The canonical list with responsibilities is the table in `CLAUDE.md`, and the package table is in
`core/CLAUDE.md`. `CLAUDE.md` is checked against `settings.gradle.kts` by
`scripts/check-context.sh`, so the two cannot drift apart. `README.md` carries an abridged copy
for people arriving from GitHub.

## Adding a module

1. `include(":x")` in `settings.gradle.kts`.
2. A `build.gradle.kts` applying the right convention plugin alias and a namespace.
3. Add the row to the `CLAUDE.md` module table — `scripts/check-context.sh` fails until you do.
4. Add the edge to this file if it changes the shape above.

If `:core` grows past comfort, the path out is the one ADR 0006 already named: per-domain data
modules (`:data:accounts`) beside the features, not data inside features.
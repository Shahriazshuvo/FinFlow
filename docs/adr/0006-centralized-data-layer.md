# 0006. The data layer is centralized; features hold presentation only

**Status:** Accepted
**Date:** 2026-09-10

## Context

An attempt was made to give each feature its own vertical slice — repository interface,
implementation, use cases and DI inside `feature/<name>/` — following §5 of
`FinFlow_Claude_Architecture_Prompt.md`. The stated motive was real: `core:data`'s `DataModule`
had grown to 104 lines binding eight unrelated repositories, and adding a feature meant editing
files every other feature also touched.

The slice structure was built and it worked. Then it was checked against practice, and the
evidence went the other way.

**Three serious codebases, all centralizing the data layer:**

- [Ivy Wallet](https://github.com/Ivy-Apps/ivy-wallet) — an open-source personal finance manager,
  45 modules, with near-identical features to this app (`accounts`, `budgets`, `categories`,
  `transactions`, `home`, `settings`). Data lives in `:shared:data:core` and `:shared:domain`;
  `feature/accounts/build.gradle.kts` declares no Room or network dependency at all.
- [Mifos mobile-wallet](https://github.com/openMF/mobile-wallet) — a real fintech wallet on Apache
  Fineract, 55 modules, `:core:data` / `:core:database` / `:core:network`, 30+ presentation-only
  features.
- [Now in Android](https://github.com/android/nowinandroid) — a single `:core:data`; feature
  modules contain no repositories.

Google's guidance says the same: *"A data module usually contains a repository, data sources and
model classes… Feature modules depend on data modules"*
([Common modularization patterns](https://developer.android.com/topic/modularization/patterns)).

**The decisive evidence was internal, though.** Making the slice structure work required inventing
`CrossFeatureReaders.kt` — 75 lines, seven interfaces, fourteen extra `@Binds` — purely so the
dashboard and the transaction form could read account data that now lived inside
`feature:accounts`. That indirection was a workaround for a structural error, not a design.

A feature is a UI concept. Data is not UI-scoped. Accounts are read by three screens, so
`feature:accounts` should not be a load-bearing dependency of any of them.

The contention argument also does not survive scrutiny at this size. A shared `DataModule` is a
merge-conflict tax at fifty features and twenty engineers. At nine domains and one developer it is
not; NIA and Ivy both run about ten repositories out of one module without difficulty.

## Decision

Repository interfaces and use cases live in `core:domain`, one file per domain. Implementations
live in `core:data`. Data sources stay in `core:database` and `core:network`, visible only to
`core:data`. A feature module contains presentation and navigation, and nothing else.

`AndroidFeatureConventionPlugin` wires only `core:designsystem`, `core:ui`, `core:navigation`,
`core:common`, `core:model` and `core:domain` into a feature, so reaching the data layer is a
compile error.

Features share through two channels and no others: the same use case over the same data, and
`@Serializable` route keys in `core:navigation`.

## Consequences

- `CrossFeatureReaders.kt` and its fourteen bindings are deleted. The dashboard injects use cases
  directly.
- The silent failure introduced by the slice structure is gone. Under it, a feature module absent
  from `app/build.gradle.kts` contributed no `@IntoSet Syncable` and its table stopped syncing with
  no build error — a bug that actually occurred, for `feature:accounts` and `feature:categories`.
  With bindings in `core:data`, which `:app` always depends on, the failure cannot happen, and
  `check-context.sh`'s guard against it was removed as dead weight.
- `TableSyncRunner` and `CurrentUserProvider` return to `internal`. They were made public only
  because feature repositories constructed them.
- Repository implementations are `internal` again; only the `core:domain` interface escapes.
- A feature cannot reach `core:datastore` either, so preferences now go through
  `UserPreferencesRepository` and `ObserveCurrencyCodeUseCase` rather than a feature injecting
  `FinFlowPreferencesDataSource` directly. That gap was invisible while the wall was down.
- `core:data` is a Gradle module without Compose, so the repository layer compiles and tests
  faster than it did inside a feature module.
- If FinFlow ever reaches the scale where one `DataModule` is genuinely contended, the next step is
  per-domain data modules (`:data:accounts`) sitting *beside* features — which is what Google's
  guidance actually describes — not data inside features.

## Deviation from `FinFlow_Claude_Architecture_Prompt.md`

Section numbers here refer to that document, not to `APP_SPEC.md`; the two number differently.

**§5 places `data/` and `domain/` inside each feature.** This decision does not, for the reasons
above. It is the one place that document is out of step with how comparable Compose apps are
actually built, and §27.9 of the same document ("avoid feature-to-feature coupling") is better
served by centralizing than by the reader indirection that feature ownership forced.

Two other deviations recorded while here:

**§14 asks for the `monthly_income_expense` / `budget_usage` views** rather than duplicating
aggregation on Android. `AnalyticsRepositoryImpl` aggregates from Room instead, because §16
requires reads to work offline and a server-computed chart goes stale or blank without a network,
and can disagree with the transaction list rendered beside it. `APP_SPEC.md` §18 already specifies
Room; the views remain the server-side cross-check.

**§3 lists `core/security`, `core/navigation` and `core/di` as modules.** The first two now exist.
There is no `core:di`: Hilt modules live beside the code they bind, which is what makes a binding
easy to find.

## Alternatives considered

- **Keep the vertical slices.** Rejected on the evidence above, and because the reader indirection
  they required is a cost with no offsetting benefit at this size.
- **Per-domain data modules (`:data:accounts`) beside features.** This is what Google's wording
  suggests and it is a reasonable end state, but none of the three reference codebases does it, and
  it would add six Gradle modules to solve contention this project does not yet have. Recorded as
  the next step if it ever does.
- **Feature `api`/`impl` splits, as Now in Android uses.** Rejected as premature: NIA needs it
  because features navigate to each other's typed routes across a large graph. `core:navigation`
  gives the same guarantee here for one module instead of eighteen.

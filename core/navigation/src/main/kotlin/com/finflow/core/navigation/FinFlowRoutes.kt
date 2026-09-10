package com.finflow.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

/**
 * Every destination key in the app, in one module (APP_SPEC.md §26).
 *
 * Keys live here rather than in the feature that renders the screen so that one feature can
 * navigate to another without depending on it: the dashboard needs to send the user to the
 * transaction form, but it must not be able to call into `feature:transactions`. A key is a
 * `@Serializable` object with no behaviour, so sharing it couples nothing.
 *
 * The `NavGraphBuilder.xScreen()` builders stay in their own feature — those reference the
 * screen composable, which is exactly what must not be shared.
 *
 * Navigation arguments are ids and primitives only, never domain objects: the destination
 * loads what it needs from the data layer, so there is one source of truth.
 */
@Serializable
data object AuthRouteKey

@Serializable
data object DashboardRouteKey

@Serializable
data object TransactionsRouteKey

@Serializable
data object AccountsRouteKey

@Serializable
data object CategoriesRouteKey

@Serializable
data object BudgetsRouteKey

@Serializable
data object GoalsRouteKey

@Serializable
data object AnalyticsRouteKey

@Serializable
data object SettingsRouteKey

fun NavController.navigateToAuth(navOptions: NavOptions? = null) =
    navigate(AuthRouteKey, navOptions)

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) =
    navigate(DashboardRouteKey, navOptions)

fun NavController.navigateToTransactions(navOptions: NavOptions? = null) =
    navigate(TransactionsRouteKey, navOptions)

fun NavController.navigateToAccounts(navOptions: NavOptions? = null) =
    navigate(AccountsRouteKey, navOptions)

fun NavController.navigateToCategories(navOptions: NavOptions? = null) =
    navigate(CategoriesRouteKey, navOptions)

fun NavController.navigateToBudgets(navOptions: NavOptions? = null) =
    navigate(BudgetsRouteKey, navOptions)

fun NavController.navigateToGoals(navOptions: NavOptions? = null) =
    navigate(GoalsRouteKey, navOptions)

fun NavController.navigateToAnalytics(navOptions: NavOptions? = null) =
    navigate(AnalyticsRouteKey, navOptions)

fun NavController.navigateToSettings(navOptions: NavOptions? = null) =
    navigate(SettingsRouteKey, navOptions)

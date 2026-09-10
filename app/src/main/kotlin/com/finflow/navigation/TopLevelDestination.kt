package com.finflow.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.navigation.AccountsRouteKey
import com.finflow.core.navigation.AnalyticsRouteKey
import com.finflow.core.navigation.BudgetsRouteKey
import com.finflow.core.navigation.DashboardRouteKey
import com.finflow.core.navigation.SettingsRouteKey
import com.finflow.core.navigation.TransactionsRouteKey
import kotlin.reflect.KClass

/**
 * The destinations reachable from the bottom bar. Keeping this in `:app` is what lets
 * feature modules stay unaware of each other — only the app knows the full graph.
 */
enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val routeClass: KClass<*>,
) {
    DASHBOARD(
        label = "Home",
        icon = FinFlowIcons.Dashboard,
        route = DashboardRouteKey,
        routeClass = DashboardRouteKey::class,
    ),
    TRANSACTIONS(
        label = "Activity",
        icon = FinFlowIcons.Transactions,
        route = TransactionsRouteKey,
        routeClass = TransactionsRouteKey::class,
    ),
    ACCOUNTS(
        label = "Accounts",
        icon = FinFlowIcons.Account,
        route = AccountsRouteKey,
        routeClass = AccountsRouteKey::class,
    ),
    BUDGETS(
        label = "Budgets",
        icon = FinFlowIcons.Budget,
        route = BudgetsRouteKey,
        routeClass = BudgetsRouteKey::class,
    ),
    ANALYTICS(
        label = "Insights",
        icon = FinFlowIcons.Analytics,
        route = AnalyticsRouteKey,
        routeClass = AnalyticsRouteKey::class,
    ),
    SETTINGS(
        label = "Settings",
        icon = FinFlowIcons.Settings,
        route = SettingsRouteKey,
        routeClass = SettingsRouteKey::class,
    ),
}

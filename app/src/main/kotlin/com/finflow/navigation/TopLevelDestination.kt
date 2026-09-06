package com.finflow.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.feature.analytics.navigation.AnalyticsRouteKey
import com.finflow.feature.budgets.navigation.BudgetsRouteKey
import com.finflow.feature.dashboard.navigation.DashboardRouteKey
import com.finflow.feature.settings.navigation.SettingsRouteKey
import com.finflow.feature.transactions.navigation.TransactionsRouteKey
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

package com.finflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.finflow.feature.analytics.navigation.analyticsScreen
import com.finflow.feature.auth.navigation.AuthRouteKey
import com.finflow.feature.auth.navigation.authScreen
import com.finflow.feature.budgets.navigation.budgetsScreen
import com.finflow.feature.dashboard.navigation.DashboardRouteKey
import com.finflow.feature.dashboard.navigation.dashboardScreen
import com.finflow.feature.dashboard.navigation.navigateToDashboard
import com.finflow.feature.goals.navigation.goalsScreen
import com.finflow.feature.settings.navigation.settingsScreen
import com.finflow.feature.transactions.navigation.transactionsScreen

@Composable
fun FinFlowNavHost(
    navController: NavHostController,
    isAuthenticated: Boolean,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) DashboardRouteKey else AuthRouteKey,
        modifier = modifier,
    ) {
        authScreen(
            onSignedIn = {
                // Auth is popped inclusively so back from the dashboard leaves the app
                // rather than returning to a sign-in form for an authenticated user.
                navController.navigateToDashboard(
                    navOptions {
                        popUpTo(AuthRouteKey) { inclusive = true }
                        launchSingleTop = true
                    },
                )
            },
            onMessage = onMessage,
        )
        dashboardScreen()
        transactionsScreen()
        budgetsScreen()
        goalsScreen()
        analyticsScreen()
        settingsScreen()
    }
}

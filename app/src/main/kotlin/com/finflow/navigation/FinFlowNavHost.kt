package com.finflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.finflow.core.navigation.AuthGraphRouteKey
import com.finflow.core.navigation.DashboardRouteKey
import com.finflow.core.navigation.LoginRouteKey
import com.finflow.core.navigation.MainGraphRouteKey
import com.finflow.core.navigation.navigateToMainGraph
import com.finflow.core.navigation.navigateToSignUp
import com.finflow.feature.accounts.navigation.accountsScreen
import com.finflow.feature.analytics.navigation.analyticsScreen
import com.finflow.feature.auth.navigation.authScreens
import com.finflow.feature.budgets.navigation.budgetsScreen
import com.finflow.feature.categories.navigation.categoriesScreen
import com.finflow.feature.dashboard.navigation.dashboardScreen
import com.finflow.feature.goals.navigation.goalsScreen
import com.finflow.feature.settings.navigation.settingsScreen
import com.finflow.feature.transactions.navigation.transactionsScreen

/**
 * The root graph, with the two children APP_SPEC.md §6 specifies: an auth graph and a main
 * graph. `:app` composes it, because only `:app` knows the full set of features (§5).
 *
 * The nesting is what makes the sign-in transition correct. Popping `AuthGraphRouteKey`
 * inclusively removes the whole signed-out half of the stack in one operation, so back from
 * the dashboard leaves the app rather than returning an authenticated user to a sign-in
 * form. Popping a single auth *screen* would only work while the auth graph has one screen
 * in it, which is exactly the kind of assumption that breaks the first time a "forgot
 * password" destination is added.
 */
@Composable
fun FinFlowNavHost(
    navController: NavHostController,
    isAuthenticated: Boolean,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) MainGraphRouteKey else AuthGraphRouteKey,
        modifier = modifier,
    ) {
        navigation<AuthGraphRouteKey>(startDestination = LoginRouteKey) {
            authScreens(
                onSignedIn = {
                    navController.navigateToMainGraph(
                        navOptions {
                            popUpTo(AuthGraphRouteKey) { inclusive = true }
                            launchSingleTop = true
                        },
                    )
                },
                onNavigateToSignUp = {
                    navController.navigateToSignUp(
                        navOptions { launchSingleTop = true },
                    )
                },
                // Pops rather than navigates, so the footer link and the system back button
                // leave the same single-entry stack behind instead of growing it each time
                // the user bounces between the two forms.
                onNavigateBackToLogin = { navController.popBackStack() },
                onMessage = onMessage,
            )
        }

        navigation<MainGraphRouteKey>(startDestination = DashboardRouteKey) {
            dashboardScreen()
            transactionsScreen()
            accountsScreen(onMessage = onMessage)
            budgetsScreen()
            goalsScreen()
            analyticsScreen()
            // Registered so the destination exists and its module stays wired, but not yet in
            // the bottom bar — Categories and Goals get their entry points from Settings and
            // the Dashboard once those screens are built.
            categoriesScreen()
            settingsScreen()
        }
    }
}

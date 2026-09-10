package com.finflow.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finflow.core.designsystem.component.FinFlowBottomBar
import com.finflow.core.designsystem.component.FinFlowBottomBarItem
import com.finflow.core.navigation.DashboardRouteKey
import com.finflow.navigation.FinFlowNavHost
import com.finflow.navigation.TopLevelDestination
import kotlinx.coroutines.launch

@Composable
fun FinFlowApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    isAuthenticated: Boolean = true,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentTopLevel = TopLevelDestination.entries
        .firstOrNull { destination -> currentDestination.matches(destination) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentTopLevel != null) {
                FinFlowBottomBar(
                    items = TopLevelDestination.entries.map { it.toBarItem() },
                    selectedKey = currentTopLevel.name,
                    onItemSelected = { item ->
                        navController.navigateToTopLevel(TopLevelDestination.valueOf(item.key))
                    },
                )
            }
        },
    ) { innerPadding ->
        FinFlowNavHost(
            navController = navController,
            isAuthenticated = isAuthenticated,
            onMessage = { message ->
                scope.launch { snackbarHostState.showSnackbar(message) }
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

private fun TopLevelDestination.toBarItem() = FinFlowBottomBarItem(
    key = name,
    label = label,
    icon = icon,
)

private fun NavDestination?.matches(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true

private val NavDestination.hierarchy: Sequence<NavDestination>
    get() = generateSequence(this) { it.parent }

/**
 * Standard bottom-bar behaviour: single instance, state saved and restored per tab.
 *
 * It pops to [DashboardRouteKey] by name rather than to the *root* graph's start
 * destination. Those are the same destination only while the app happens to start signed in;
 * when it starts on the auth graph they are not, and popping to a destination that is no
 * longer on the stack silently loses per-tab state restoration. The main graph's start
 * destination is the thing this actually means, so it says so.
 */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(DashboardRouteKey) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

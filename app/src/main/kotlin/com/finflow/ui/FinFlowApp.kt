package com.finflow.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finflow.core.designsystem.component.FinFlowBottomBar
import com.finflow.core.designsystem.component.FinFlowBottomBarItem
import com.finflow.navigation.FinFlowNavHost
import com.finflow.navigation.TopLevelDestination

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
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

/** Standard bottom-bar behaviour: single instance, state saved and restored per tab. */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

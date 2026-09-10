package com.finflow.feature.accounts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.AccountsRouteKey
import com.finflow.feature.accounts.presentation.AccountsRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.accountsScreen(onMessage: (String) -> Unit) {
    composable<AccountsRouteKey> {
        AccountsRoute(onMessage = onMessage)
    }
}

package com.finflow.feature.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.transactions.presentation.TransactionsRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Transactions destination. */
@Serializable
data object TransactionsRouteKey

fun NavController.navigateToTransactions(navOptions: NavOptions? = null) =
    navigate(TransactionsRouteKey, navOptions)

fun NavGraphBuilder.transactionsScreen() {
    composable<TransactionsRouteKey> {
        TransactionsRoute()
    }
}

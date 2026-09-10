package com.finflow.feature.transactions.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.TransactionsRouteKey
import com.finflow.feature.transactions.presentation.TransactionsRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.transactionsScreen() {
    composable<TransactionsRouteKey> {
        TransactionsRoute()
    }
}

package com.finflow.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.AuthRouteKey
import com.finflow.feature.auth.presentation.AuthRoute

/** Registers the destination. The route key itself lives in `core:navigation`. */
fun NavGraphBuilder.authScreen(
    onSignedIn: () -> Unit,
    onMessage: (String) -> Unit,
) {
    composable<AuthRouteKey> {
        AuthRoute(
            onSignedIn = onSignedIn,
            onMessage = onMessage,
        )
    }
}

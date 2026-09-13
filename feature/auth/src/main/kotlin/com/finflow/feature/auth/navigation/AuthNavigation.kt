package com.finflow.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.finflow.core.navigation.LoginRouteKey
import com.finflow.core.navigation.SignUpRouteKey
import com.finflow.feature.auth.presentation.LoginRoute
import com.finflow.feature.auth.presentation.SignUpRoute

/**
 * Registers both auth destinations. The route keys themselves live in `core:navigation`.
 *
 * Navigation *between* the two is passed in rather than performed here: this module has no
 * `NavController`, and the app owns graph composition (APP_SPEC.md §6).
 */
fun NavGraphBuilder.authScreens(
    onSignedIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateBackToLogin: () -> Unit,
    onMessage: (String) -> Unit,
) {
    composable<LoginRouteKey> {
        LoginRoute(
            onSignedIn = onSignedIn,
            onNavigateToSignUp = onNavigateToSignUp,
            onMessage = onMessage,
        )
    }

    composable<SignUpRouteKey> {
        SignUpRoute(
            onSignedIn = onSignedIn,
            onNavigateBack = onNavigateBackToLogin,
            onMessage = onMessage,
        )
    }
}
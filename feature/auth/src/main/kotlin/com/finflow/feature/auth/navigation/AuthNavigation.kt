package com.finflow.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.finflow.feature.auth.presentation.AuthRoute
import kotlinx.serialization.Serializable

/** Type-safe route for the Sign in destination. */
@Serializable
data object AuthRouteKey

fun NavController.navigateToAuth(navOptions: NavOptions? = null) =
    navigate(AuthRouteKey, navOptions)

fun NavGraphBuilder.authScreen() {
    composable<AuthRouteKey> {
        AuthRoute()
    }
}

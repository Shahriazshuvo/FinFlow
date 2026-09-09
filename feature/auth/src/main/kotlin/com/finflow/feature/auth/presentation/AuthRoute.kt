package com.finflow.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finflow.core.ui.util.ObserveAsEvents

/**
 * The only stateful composable in the feature: it owns the ViewModel, collects state
 * lifecycle-aware, and turns one-shot effects into calls back into navigation.
 */
@Composable
fun AuthRoute(
    onSignedIn: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            AuthEffect.NavigateToHome -> onSignedIn()
            is AuthEffect.ShowMessage -> onMessage(effect.message)
        }
    }

    AuthScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
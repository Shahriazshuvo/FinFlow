package com.finflow.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finflow.core.ui.util.ObserveAsEvents
import com.finflow.feature.auth.presentation.contract.LoginEffect
import com.finflow.feature.auth.presentation.screen.LoginScreen
import com.finflow.feature.auth.presentation.viewmodel.LoginViewModel

/**
 * The only stateful composable in sign-in: it owns the ViewModel, collects state
 * lifecycle-aware, and turns one-shot effects into navigation and snackbar messages.
 *
 * [LoginViewModel] stays `internal`, so it is resolved in the body rather than exposed as a
 * default parameter of a public function.
 */
@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            LoginEffect.NavigateToHome -> onSignedIn()
            LoginEffect.NavigateToSignUp -> onNavigateToSignUp()
            is LoginEffect.ShowMessage -> onMessage(effect.message)
        }
    }

    LoginScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
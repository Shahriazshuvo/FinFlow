package com.finflow.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finflow.core.ui.util.ObserveAsEvents
import com.finflow.feature.auth.presentation.contract.SignUpEffect
import com.finflow.feature.auth.presentation.screen.SignUpScreen
import com.finflow.feature.auth.presentation.viewmodel.SignUpViewModel

/**
 * The only stateful composable in sign-up. [SignUpViewModel] stays `internal`, so it is
 * resolved in the body rather than exposed as a default parameter of a public function.
 */
@Composable
fun SignUpRoute(
    onSignedIn: () -> Unit,
    onNavigateBack: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SignUpViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            SignUpEffect.NavigateToHome -> onSignedIn()
            SignUpEffect.NavigateBackToLogin -> onNavigateBack()
            is SignUpEffect.ShowMessage -> onMessage(effect.message)
        }
    }

    SignUpScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
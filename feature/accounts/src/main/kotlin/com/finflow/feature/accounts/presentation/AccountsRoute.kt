package com.finflow.feature.accounts.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finflow.core.ui.util.ObserveAsEvents
import com.finflow.feature.accounts.presentation.contract.AccountsEffect
import com.finflow.feature.accounts.presentation.screen.AccountsScreen
import com.finflow.feature.accounts.presentation.viewmodel.AccountsViewModel

/**
 * The only stateful composable in the feature: it owns the ViewModel, collects state
 * lifecycle-aware, and turns one-shot effects into snackbar messages.
 *
 * [AccountsViewModel] stays `internal`, so it is resolved in the body rather than exposed as a
 * default parameter of a public function.
 */
@Composable
fun AccountsRoute(
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AccountsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is AccountsEffect.ShowMessage -> onMessage(effect.message)
        }
    }

    AccountsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

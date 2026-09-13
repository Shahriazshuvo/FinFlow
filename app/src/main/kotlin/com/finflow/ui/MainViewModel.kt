package com.finflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Decides which half of the root graph the app opens in.
 *
 * This exists because the start destination cannot be guessed. `NavHost` reads
 * `startDestination` once, when it first composes, so the answer has to be known before the
 * graph is built rather than corrected afterwards — which is why [FinFlowApp] holds a loader
 * while this is still [SessionState.Unknown] instead of composing the graph and navigating.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = observeSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SessionState.Unknown,
        )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiEffect

/**
 * One-shot events from [com.finflow.feature.auth.presentation.viewmodel.AuthViewModel].
 * Never state — these must not survive a rotation.
 */
sealed interface AuthEffect : UiEffect {
    /** Emitted when the session becomes authenticated, whether by sign in or by restore. */
    data object NavigateToHome : AuthEffect

    data class ShowMessage(val message: String) : AuthEffect
}
package com.finflow.feature.auth.presentation

import com.finflow.core.ui.mvi.UiEffect

/** One-shot events from [AuthViewModel]. Never state — these must not survive a rotation. */
sealed interface AuthEffect : UiEffect {
    /** Emitted when the session becomes authenticated, whether by sign in or by restore. */
    data object NavigateToHome : AuthEffect

    data class ShowMessage(val message: String) : AuthEffect
}
package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiEffect

sealed interface SignUpEffect : UiEffect {
    /** Emitted when the new account produces a session, i.e. no email confirmation is required. */
    data object NavigateToHome : SignUpEffect

    data object NavigateBackToLogin : SignUpEffect

    data class ShowMessage(val message: String) : SignUpEffect
}
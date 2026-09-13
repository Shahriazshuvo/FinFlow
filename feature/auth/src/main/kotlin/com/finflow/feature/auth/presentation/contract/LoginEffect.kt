package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiEffect

sealed interface LoginEffect : UiEffect {
    /** Emitted when the session becomes authenticated, whether by sign in or by restore. */
    data object NavigateToHome : LoginEffect

    data object NavigateToSignUp : LoginEffect

    data class ShowMessage(val message: String) : LoginEffect
}

package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiIntent

sealed interface SignUpIntent : UiIntent {
    data class DisplayNameChanged(val value: String) : SignUpIntent

    data class EmailChanged(val value: String) : SignUpIntent

    data class PasswordChanged(val value: String) : SignUpIntent

    data class ConfirmPasswordChanged(val value: String) : SignUpIntent

    data object Submitted : SignUpIntent

    /** The footer link back to sign in, and the back arrow, which mean the same thing. */
    data object SignInClicked : SignUpIntent

    data object ErrorDismissed : SignUpIntent
}
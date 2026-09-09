package com.finflow.feature.auth.presentation

import com.finflow.core.ui.mvi.UiIntent

/** Everything the sign in / sign up screen can send to [AuthViewModel]. */
sealed interface AuthIntent : UiIntent {
    data class EmailChanged(val value: String) : AuthIntent

    data class PasswordChanged(val value: String) : AuthIntent

    data class ConfirmPasswordChanged(val value: String) : AuthIntent

    data class DisplayNameChanged(val value: String) : AuthIntent

    data object ModeToggled : AuthIntent

    data object Submitted : AuthIntent

    data object ErrorDismissed : AuthIntent
}
package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiIntent

sealed interface LoginIntent : UiIntent {
    data class EmailChanged(val value: String) : LoginIntent

    data class PasswordChanged(val value: String) : LoginIntent

    data object Submitted : LoginIntent

    /** The footer link. Navigation is an effect, so the screen only reports the tap. */
    data object SignUpClicked : LoginIntent

    data object ErrorDismissed : LoginIntent
}

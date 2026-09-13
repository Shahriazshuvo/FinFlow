package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiState

/**
 * Sign in (APP_SPEC.md §13). Sign up is a separate destination with its own state, so this
 * carries only the two credentials and never fields it cannot use — see
 * `docs/adr/0011-split-login-and-signup.md`.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    /**
     * An error that belongs to the submission rather than to a field — wrong credentials,
     * offline, a server failure. Rendered above the button, not under an input.
     */
    val formError: String? = null,
    val isSubmitting: Boolean = false,
    /**
     * True until the stored session has been restored. The screen shows a loader rather
     * than flashing the sign-in form on every cold start of an already-authenticated app.
     */
    val isRestoringSession: Boolean = true,
) : UiState {

    /**
     * Only guards against submitting an obviously empty form. The real rules live in
     * `AuthValidator` behind the use case, so every entry point enforces the same ones.
     */
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank() && password.isNotBlank()
}
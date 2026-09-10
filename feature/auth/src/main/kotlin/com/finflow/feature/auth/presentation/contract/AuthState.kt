package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiState

/**
 * One screen serves both sign in and sign up (APP_SPEC.md §13). The extra fields simply
 * stay out of the layout in [Mode.SignIn], which keeps the credential handling in one
 * place instead of two screens that drift apart.
 */
data class AuthState(
    val mode: Mode = Mode.SignIn,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    /**
     * True until the stored session has been restored. The screen shows a loader rather
     * than flashing the sign-in form on every cold start of an already-authenticated app.
     */
    val isRestoringSession: Boolean = true,
) : UiState {

    enum class Mode { SignIn, SignUp }

    val isSignUp: Boolean get() = mode == Mode.SignUp

    /**
     * Only guards against submitting an obviously empty form. Real validation belongs to
     * `AuthValidator` behind the use case, so every entry point enforces the same rules.
     */
    val canSubmit: Boolean
        get() = !isSubmitting &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            (!isSignUp || (displayName.isNotBlank() && confirmPassword.isNotBlank()))
}
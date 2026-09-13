package com.finflow.feature.auth.presentation.contract

import com.finflow.core.ui.mvi.UiState

/**
 * Create an account (APP_SPEC.md §13).
 *
 * Errors are per-field rather than one message for the form, because this form has four inputs
 * and "Passwords do not match" above a submit button does not say which box to fix.
 * `SignUpUseCase` validates in a fixed order and returns one error at a time, so the ViewModel
 * runs the same `AuthValidator` per field first and keeps [formError] for what is left: a
 * server or connectivity failure that belongs to no input.
 */
data class SignUpState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val formError: String? = null,
    val isSubmitting: Boolean = false,
) : UiState {

    val canSubmit: Boolean
        get() = !isSubmitting &&
            displayName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank()
}
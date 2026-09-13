package com.finflow.feature.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.validation.AuthValidator
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.domain.usecase.auth.SignUpUseCase
import com.finflow.core.model.SessionState
import com.finflow.core.ui.error.toUserMessage
import com.finflow.core.ui.mvi.MviViewModel
import com.finflow.feature.auth.presentation.contract.SignUpEffect
import com.finflow.feature.auth.presentation.contract.SignUpIntent
import com.finflow.feature.auth.presentation.contract.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Creating the account is all this does. The `handle_new_user` trigger creates the profile, the
 * default Cash account and the starter categories server-side (APP_SPEC.md §13), so there is no
 * follow-up write to make here.
 */
@HiltViewModel
internal class SignUpViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    private val signUp: SignUpUseCase,
    private val validator: AuthValidator,
) : MviViewModel<SignUpState, SignUpIntent, SignUpEffect>(SignUpState()) {

    init {
        observeSession()
            .onEach { session ->
                if (session is SessionState.SignedIn) {
                    sendEffect(SignUpEffect.NavigateToHome)
                }
            }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.DisplayNameChanged -> setState {
                copy(displayName = intent.value, displayNameError = null, formError = null)
            }

            is SignUpIntent.EmailChanged -> setState {
                copy(email = intent.value, emailError = null, formError = null)
            }

            is SignUpIntent.PasswordChanged -> setState {
                copy(
                    password = intent.value,
                    passwordError = null,
                    // The confirmation is only wrong relative to this field, so editing the
                    // password clears a stale mismatch rather than leaving it accusing a box
                    // the user did not touch.
                    confirmPasswordError = null,
                    formError = null,
                )
            }

            is SignUpIntent.ConfirmPasswordChanged -> setState {
                copy(confirmPassword = intent.value, confirmPasswordError = null, formError = null)
            }

            SignUpIntent.SignInClicked -> sendEffect(SignUpEffect.NavigateBackToLogin)

            SignUpIntent.ErrorDismissed -> setState {
                copy(
                    displayNameError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    formError = null,
                )
            }

            SignUpIntent.Submitted -> submit()
        }
    }

    private suspend fun submit() {
        if (!currentState.canSubmit) return

        val state = currentState
        // Validate for *placement* before submitting. SignUpUseCase runs the same rules and
        // stays the authority, but it short-circuits on the first failure and returns one
        // message with no indication of which of the four inputs it belongs to. Running the
        // validator per field here is what puts each error under its own box, and restates
        // no rule: the regex and the length minimum still live only in AuthValidator.
        val displayNameError = validator.validateDisplayName(state.displayName).errorMessage()
        val emailError = validator.validateEmail(state.email).errorMessage()
        val passwordError = validator.validatePassword(state.password).errorMessage()
        val confirmPasswordError = validator
            .validatePasswordConfirmation(state.password, state.confirmPassword)
            .errorMessage()

        val hasFieldError = displayNameError != null ||
            emailError != null ||
            passwordError != null ||
            confirmPasswordError != null

        if (hasFieldError) {
            setState {
                copy(
                    displayNameError = displayNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                )
            }
            return
        }

        setState { copy(isSubmitting = true, formError = null) }

        val result = signUp(
            email = state.email,
            password = state.password,
            confirmPassword = state.confirmPassword,
            displayName = state.displayName,
        )

        setState { copy(isSubmitting = false) }

        when (result) {
            // A blank userId means Supabase accepted the account but issued no session,
            // because the project requires email confirmation. Nothing will emit SignedIn,
            // so without this branch the spinner would stop on a screen that says nothing
            // and goes nowhere. Navigation stays with the session flow for the other case.
            is AppResult.Success -> if (result.data.userId.isBlank()) {
                sendEffect(
                    SignUpEffect.ShowMessage(
                        "Account created. Check your email to confirm it, then sign in.",
                    ),
                )
                sendEffect(SignUpEffect.NavigateBackToLogin)
            }

            is AppResult.Failure -> setState { copy(formError = result.error.toSignUpMessage()) }
        }
    }
}

private fun AppResult<*>.errorMessage(): String? =
    ((this as? AppResult.Failure)?.error as? AppError.Validation)?.message

/**
 * [AppError.Duplicate] on this screen always means the email is taken — it is the only unique
 * column sign-up writes — so it says so instead of falling through to the generic wording.
 */
private fun AppError.toSignUpMessage(): String = when (this) {
    is AppError.Duplicate -> "An account with that email already exists."
    AppError.Offline -> "No connection. Check your network and try again."
    else -> toUserMessage()
}
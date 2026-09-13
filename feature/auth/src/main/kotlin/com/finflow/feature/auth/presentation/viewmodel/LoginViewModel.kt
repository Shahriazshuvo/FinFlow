package com.finflow.feature.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.validation.AuthValidator
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.domain.usecase.auth.SignInUseCase
import com.finflow.core.model.SessionState
import com.finflow.core.ui.error.toUserMessage
import com.finflow.core.ui.mvi.MviViewModel
import com.finflow.feature.auth.presentation.contract.LoginEffect
import com.finflow.feature.auth.presentation.contract.LoginIntent
import com.finflow.feature.auth.presentation.contract.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Navigation is driven by the session flow rather than by the sign-in call returning, so a
 * restored session on cold start and a fresh sign in take exactly the same path out of this
 * screen. That leaves one place where "the user is now authenticated" is decided.
 */
@HiltViewModel
internal class LoginViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    private val signIn: SignInUseCase,
    private val validator: AuthValidator,
) : MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    init {
        observeSession()
            .onEach { session ->
                setState { copy(isRestoringSession = session is SessionState.Unknown) }
                if (session is SessionState.SignedIn) {
                    sendEffect(LoginEffect.NavigateToHome)
                }
            }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged ->
                setState { copy(email = intent.value, emailError = null, formError = null) }

            is LoginIntent.PasswordChanged ->
                setState { copy(password = intent.value, passwordError = null, formError = null) }

            LoginIntent.SignUpClicked -> sendEffect(LoginEffect.NavigateToSignUp)

            LoginIntent.ErrorDismissed -> setState {
                copy(emailError = null, passwordError = null, formError = null)
            }

            LoginIntent.Submitted -> submit()
        }
    }

    private suspend fun submit() {
        if (!currentState.canSubmit) return

        val state = currentState
        // Validate for *placement* before submitting: the use case runs the same rules and
        // stays the authority, but it returns one message with no indication of which input
        // it belongs to. Running the validator per field here is what lets the error land
        // under the box the user has to fix, without restating any rule.
        val emailError = validator.validateEmail(state.email).errorMessage()
        val passwordError = validator.validatePassword(state.password).errorMessage()
        if (emailError != null || passwordError != null) {
            setState { copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        setState {
            copy(isSubmitting = true, emailError = null, passwordError = null, formError = null)
        }

        val result = signIn(email = state.email, password = state.password)

        setState { copy(isSubmitting = false) }

        // Success needs no navigation here — the session flow above emits SignedIn and
        // sends NavigateToHome, so both entry paths behave identically.
        if (result is AppResult.Failure) {
            setState { copy(formError = result.error.toLoginMessage()) }
        }
    }
}

private fun AppResult<*>.errorMessage(): String? =
    ((this as? AppResult.Failure)?.error as? AppError.Validation)?.message

/**
 * Sign-in is the one screen where [AppError.Unauthorized] does not mean "your session
 * expired" — it means the credentials were wrong — so it overrides that single case and
 * defers everything else to the shared wording in `core:ui`.
 */
private fun AppError.toLoginMessage(): String = when (this) {
    AppError.Unauthorized -> "That email and password do not match an account."
    AppError.Offline -> "No connection. Check your network and try again."
    else -> toUserMessage()
}
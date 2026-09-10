package com.finflow.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.domain.usecase.auth.SignInUseCase
import com.finflow.core.domain.usecase.auth.SignUpUseCase
import com.finflow.core.model.SessionState
import com.finflow.core.ui.error.toUserMessage
import com.finflow.core.ui.mvi.MviViewModel
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
class AuthViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    private val signIn: SignInUseCase,
    private val signUp: SignUpUseCase,
) : MviViewModel<AuthState, AuthIntent, AuthEffect>(AuthState()) {

    init {
        observeSession()
            .onEach { session ->
                setState { copy(isRestoringSession = session is SessionState.Unknown) }
                if (session is SessionState.SignedIn) {
                    sendEffect(AuthEffect.NavigateToHome)
                }
            }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged ->
                setState { copy(email = intent.value, errorMessage = null) }

            is AuthIntent.PasswordChanged ->
                setState { copy(password = intent.value, errorMessage = null) }

            is AuthIntent.ConfirmPasswordChanged ->
                setState { copy(confirmPassword = intent.value, errorMessage = null) }

            is AuthIntent.DisplayNameChanged ->
                setState { copy(displayName = intent.value, errorMessage = null) }

            AuthIntent.ModeToggled -> setState {
                copy(
                    mode = if (isSignUp) AuthState.Mode.SignIn else AuthState.Mode.SignUp,
                    confirmPassword = "",
                    displayName = "",
                    errorMessage = null,
                )
            }

            AuthIntent.ErrorDismissed -> setState { copy(errorMessage = null) }

            AuthIntent.Submitted -> submit()
        }
    }

    private suspend fun submit() {
        if (!currentState.canSubmit) return
        setState { copy(isSubmitting = true, errorMessage = null) }

        val state = currentState
        val result = if (state.isSignUp) {
            signUp(
                email = state.email,
                password = state.password,
                confirmPassword = state.confirmPassword,
                displayName = state.displayName,
            )
        } else {
            signIn(email = state.email, password = state.password)
        }

        setState { copy(isSubmitting = false) }

        // Success needs no navigation here — the session flow above emits SignedIn and
        // sends NavigateToHome, so both entry paths behave identically.
        if (result is AppResult.Failure) {
            setState { copy(errorMessage = result.error.toMessage()) }
        }
    }
}

/**
 * Sign-in is the one screen where [AppError.Unauthorized] does not mean "your session
 * expired" — it means the credentials were wrong — so it overrides that single case and
 * defers everything else to the shared wording in `core:ui`.
 */
private fun AppError.toMessage(): String = when (this) {
    AppError.Unauthorized -> "That email and password do not match an account."
    AppError.NetworkUnavailable -> "No connection. Check your network and try again."
    else -> toUserMessage()
}
package com.finflow.feature.auth.presentation.viewmodel

import app.cash.turbine.test
import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.domain.usecase.auth.SignInUseCase
import com.finflow.core.domain.usecase.auth.SignUpUseCase
import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import com.finflow.core.testing.MainDispatcherRule
import com.finflow.feature.auth.presentation.contract.AuthEffect
import com.finflow.feature.auth.presentation.contract.AuthIntent
import com.finflow.feature.auth.presentation.contract.AuthState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * The MVI contract from APP_SPEC.md §7 as this screen implements it: intents reduce to
 * state, failures surface as a message rather than a crash, and "the user is authenticated"
 * is decided in exactly one place — the session flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatcher get() = mainDispatcherRule.dispatcher
    private val sessionState = MutableStateFlow<SessionState>(SessionState.Unknown)

    private val observeSession = mockk<ObserveSessionUseCase>()
    private val signIn = mockk<SignInUseCase>()
    private val signUp = mockk<SignUpUseCase>()

    @Before
    fun setUp() {
        every { observeSession() } returns sessionState
    }

    private fun viewModel() = AuthViewModel(observeSession, signIn, signUp)

    @Test
    fun `starts restoring until the session resolves`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.state.test {
            assertTrue(awaitItem().isRestoringSession)

            sessionState.value = SessionState.SignedOut
            assertFalse(awaitItem().isRestoringSession)
        }
    }

    @Test
    fun `typing updates state and clears the previous error`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut
        coEvery { signIn(any(), any()) } returns
            AppResult.Failure(AppError.Validation("Enter a valid email address"))

        viewModel.onIntent(AuthIntent.EmailChanged("nope"))
        viewModel.onIntent(AuthIntent.PasswordChanged("password123"))
        viewModel.onIntent(AuthIntent.Submitted)
        runCurrent()
        assertEquals("Enter a valid email address", viewModel.state.value.errorMessage)

        viewModel.onIntent(AuthIntent.EmailChanged("ada@finflow.app"))
        runCurrent()

        assertEquals("ada@finflow.app", viewModel.state.value.email)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `toggling to sign up clears the sign-up-only fields`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(AuthIntent.ModeToggled)
        viewModel.onIntent(AuthIntent.DisplayNameChanged("Ada"))
        viewModel.onIntent(AuthIntent.ConfirmPasswordChanged("password123"))
        runCurrent()
        assertTrue(viewModel.state.value.isSignUp)

        viewModel.onIntent(AuthIntent.ModeToggled)
        runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isSignUp)
        assertEquals("", state.displayName)
        assertEquals("", state.confirmPassword)
    }

    @Test
    fun `a failed sign in surfaces a message and stops submitting`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut
        coEvery { signIn(any(), any()) } returns AppResult.Failure(AppError.Unauthorized)

        viewModel.onIntent(AuthIntent.EmailChanged("ada@finflow.app"))
        viewModel.onIntent(AuthIntent.PasswordChanged("password123"))
        viewModel.onIntent(AuthIntent.Submitted)
        runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isSubmitting)
        assertEquals("That email and password do not match an account.", state.errorMessage)
    }

    @Test
    fun `an authenticated session navigates home`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.effect.test {
            sessionState.value = SessionState.SignedIn(
                UserSession(userId = "user-1", email = "ada@finflow.app"),
            )
            runCurrent()

            assertEquals(AuthEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `submitting an empty form does nothing`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut

        viewModel.onIntent(AuthIntent.Submitted)
        runCurrent()

        assertFalse(viewModel.state.value.isSubmitting)
        assertNull(viewModel.state.value.errorMessage)
    }
}
package com.finflow.feature.auth.presentation.viewmodel

import app.cash.turbine.test
import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.validation.AuthValidator
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.domain.usecase.auth.SignInUseCase
import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import com.finflow.core.testing.MainDispatcherRule
import com.finflow.feature.auth.presentation.contract.LoginEffect
import com.finflow.feature.auth.presentation.contract.LoginIntent
import io.mockk.coEvery
import io.mockk.coVerify
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
 * The MVI contract from APP_SPEC.md §7 as sign-in implements it: intents reduce to state,
 * failures surface as a message rather than a crash, and "the user is authenticated" is
 * decided in exactly one place — the session flow.
 *
 * [AuthValidator] is the real one, not a mock. It has no dependencies, and the point of these
 * tests is that the screen puts the *real* rules under the right field.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatcher get() = mainDispatcherRule.dispatcher
    private val sessionState = MutableStateFlow<SessionState>(SessionState.Unknown)

    private val observeSession = mockk<ObserveSessionUseCase>()
    private val signIn = mockk<SignInUseCase>()
    private val validator = AuthValidator()

    @Before
    fun setUp() {
        every { observeSession() } returns sessionState
    }

    private fun viewModel() = LoginViewModel(observeSession, signIn, validator)

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

        viewModel.onIntent(LoginIntent.EmailChanged("nope"))
        viewModel.onIntent(LoginIntent.PasswordChanged("password123"))
        viewModel.onIntent(LoginIntent.Submitted)
        runCurrent()
        assertEquals("Enter a valid email address", viewModel.state.value.emailError)

        viewModel.onIntent(LoginIntent.EmailChanged("ada@finflow.app"))
        runCurrent()

        assertEquals("ada@finflow.app", viewModel.state.value.email)
        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun `a malformed email never reaches the use case`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut

        viewModel.onIntent(LoginIntent.EmailChanged("nope"))
        viewModel.onIntent(LoginIntent.PasswordChanged("password123"))
        viewModel.onIntent(LoginIntent.Submitted)
        runCurrent()

        coVerify(exactly = 0) { signIn(any(), any()) }
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `a failed sign in surfaces a form error and stops submitting`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut
        coEvery { signIn(any(), any()) } returns AppResult.Failure(AppError.Unauthorized)

        viewModel.onIntent(LoginIntent.EmailChanged("ada@finflow.app"))
        viewModel.onIntent(LoginIntent.PasswordChanged("password123"))
        viewModel.onIntent(LoginIntent.Submitted)
        runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isSubmitting)
        assertEquals("That email and password do not match an account.", state.formError)
        // Wrong credentials belong to the submission, not to either box.
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `an authenticated session navigates home`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.effect.test {
            sessionState.value = SessionState.SignedIn(
                UserSession(userId = "user-1", email = "ada@finflow.app"),
            )
            runCurrent()

            assertEquals(LoginEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `the footer link asks to navigate to sign up`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut

        viewModel.effect.test {
            viewModel.onIntent(LoginIntent.SignUpClicked)
            runCurrent()

            assertEquals(LoginEffect.NavigateToSignUp, awaitItem())
        }
    }

    @Test
    fun `submitting an empty form does nothing`() = runTest(dispatcher) {
        val viewModel = viewModel()
        sessionState.value = SessionState.SignedOut

        viewModel.onIntent(LoginIntent.Submitted)
        runCurrent()

        assertFalse(viewModel.state.value.isSubmitting)
        assertNull(viewModel.state.value.formError)
        coVerify(exactly = 0) { signIn(any(), any()) }
    }
}
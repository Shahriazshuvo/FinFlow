package com.finflow.feature.auth.presentation.viewmodel

import app.cash.turbine.test
import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.validation.AuthValidator
import com.finflow.core.domain.usecase.auth.ObserveSessionUseCase
import com.finflow.core.domain.usecase.auth.SignUpUseCase
import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import com.finflow.core.testing.MainDispatcherRule
import com.finflow.feature.auth.presentation.contract.SignUpEffect
import com.finflow.feature.auth.presentation.contract.SignUpIntent
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Sign-up's half of the APP_SPEC.md §7 contract, plus the one case the screen exists to get
 * right: a `Success` that carries no session because the project requires email confirmation.
 *
 * [AuthValidator] is the real one. These tests assert that each real rule lands under the
 * field it belongs to, which a stubbed validator could not show.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatcher get() = mainDispatcherRule.dispatcher
    private val sessionState = MutableStateFlow<SessionState>(SessionState.SignedOut)

    private val observeSession = mockk<ObserveSessionUseCase>()
    private val signUp = mockk<SignUpUseCase>()
    private val validator = AuthValidator()

    @Before
    fun setUp() {
        every { observeSession() } returns sessionState
    }

    private fun viewModel() = SignUpViewModel(observeSession, signUp, validator)

    private fun SignUpViewModel.fillValidForm() {
        onIntent(SignUpIntent.DisplayNameChanged("Ada Lovelace"))
        onIntent(SignUpIntent.EmailChanged("ada@finflow.app"))
        onIntent(SignUpIntent.PasswordChanged("password123"))
        onIntent(SignUpIntent.ConfirmPasswordChanged("password123"))
    }

    @Test
    fun `a session created by sign up navigates home`() = runTest(dispatcher) {
        val viewModel = viewModel()
        coEvery { signUp(any(), any(), any(), any()) } returns AppResult.Success(
            UserSession(userId = "user-1", email = "ada@finflow.app"),
        )

        viewModel.effect.test {
            viewModel.fillValidForm()
            viewModel.onIntent(SignUpIntent.Submitted)
            runCurrent()

            // Navigation is the session flow's job, exactly as it is on sign-in.
            sessionState.value = SessionState.SignedIn(
                UserSession(userId = "user-1", email = "ada@finflow.app"),
            )
            runCurrent()

            assertEquals(SignUpEffect.NavigateToHome, awaitItem())
        }
    }

    /**
     * The defect this screen was rebuilt around. Supabase returns a user with no session when
     * email confirmation is on, so nothing will ever emit `SignedIn`. Without an explicit
     * branch the spinner stops on a screen that says nothing and goes nowhere.
     */
    @Test
    fun `a sign up awaiting email confirmation explains itself and returns to login`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            coEvery { signUp(any(), any(), any(), any()) } returns AppResult.Success(
                UserSession(userId = "", email = "ada@finflow.app"),
            )

            viewModel.effect.test {
                viewModel.fillValidForm()
                viewModel.onIntent(SignUpIntent.Submitted)
                runCurrent()

                val message = awaitItem() as SignUpEffect.ShowMessage
                assertEquals(
                    "Account created. Check your email to confirm it, then sign in.",
                    message.message,
                )
                assertEquals(SignUpEffect.NavigateBackToLogin, awaitItem())
            }

            assertFalse(viewModel.state.value.isSubmitting)
            assertNull(viewModel.state.value.formError)
        }

    @Test
    fun `each validation failure lands under its own field`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SignUpIntent.DisplayNameChanged("Ada Lovelace"))
        viewModel.onIntent(SignUpIntent.EmailChanged("ada@"))
        viewModel.onIntent(SignUpIntent.PasswordChanged("short"))
        viewModel.onIntent(SignUpIntent.ConfirmPasswordChanged("shorter"))
        viewModel.onIntent(SignUpIntent.Submitted)
        runCurrent()

        val state = viewModel.state.value
        assertEquals("Enter a valid email address", state.emailError)
        assertEquals("Password must be at least 8 characters", state.passwordError)
        assertEquals("Passwords do not match", state.confirmPasswordError)
        // All three at once, rather than one per round trip like the use case would give.
        coVerify(exactly = 0) { signUp(any(), any(), any(), any()) }
    }

    /**
     * The blank-field rules in `AuthValidator` ("Enter your name", "Enter your email",
     * "Enter your password") are deliberately unreachable from this screen: [canSubmit]
     * already refuses a form with an empty box, so the disabled button is the feedback and no
     * error text is ever needed for that case. The validator still owns the rules, so they
     * apply to any other entry point and start firing here the moment one gains real content.
     */
    @Test
    fun `an incomplete form cannot be submitted at all`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SignUpIntent.DisplayNameChanged("   "))
        viewModel.onIntent(SignUpIntent.EmailChanged("ada@finflow.app"))
        viewModel.onIntent(SignUpIntent.PasswordChanged("password123"))
        viewModel.onIntent(SignUpIntent.ConfirmPasswordChanged("password123"))
        runCurrent()

        assertFalse(viewModel.state.value.canSubmit)

        viewModel.onIntent(SignUpIntent.Submitted)
        runCurrent()

        assertNull(viewModel.state.value.displayNameError)
        coVerify(exactly = 0) { signUp(any(), any(), any(), any()) }
    }

    @Test
    fun `editing the password clears a stale confirmation mismatch`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.fillValidForm()
        viewModel.onIntent(SignUpIntent.ConfirmPasswordChanged("different"))
        viewModel.onIntent(SignUpIntent.Submitted)
        runCurrent()
        assertEquals("Passwords do not match", viewModel.state.value.confirmPasswordError)

        viewModel.onIntent(SignUpIntent.PasswordChanged("different"))
        runCurrent()

        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `a duplicate email is named rather than described generically`() = runTest(dispatcher) {
        val viewModel = viewModel()
        coEvery { signUp(any(), any(), any(), any()) } returns
            AppResult.Failure(AppError.Duplicate("email"))

        viewModel.fillValidForm()
        viewModel.onIntent(SignUpIntent.Submitted)
        runCurrent()

        val state = viewModel.state.value
        assertFalse(state.isSubmitting)
        assertEquals("An account with that email already exists.", state.formError)
    }

    @Test
    fun `the footer link asks to go back to login`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onIntent(SignUpIntent.SignInClicked)
            runCurrent()

            assertEquals(SignUpEffect.NavigateBackToLogin, awaitItem())
        }
    }

    @Test
    fun `submitting an empty form does nothing`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SignUpIntent.Submitted)
        runCurrent()

        assertFalse(viewModel.state.value.isSubmitting)
        assertNull(viewModel.state.value.formError)
        coVerify(exactly = 0) { signUp(any(), any(), any(), any()) }
    }
}
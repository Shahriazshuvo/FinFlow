package com.finflow.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.FinFlowButton
import com.finflow.core.designsystem.component.FinFlowPasswordField
import com.finflow.core.designsystem.component.FinFlowTextField
import com.finflow.core.designsystem.component.LoadingState
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme
import com.finflow.feature.auth.presentation.components.AuthFooterPrompt
import com.finflow.feature.auth.presentation.components.AuthFormCard
import com.finflow.feature.auth.presentation.components.AuthFormError
import com.finflow.feature.auth.presentation.components.AuthHeader
import com.finflow.feature.auth.presentation.contract.LoginIntent
import com.finflow.feature.auth.presentation.contract.LoginState

@Composable
internal fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isRestoringSession) {
        LoadingState(modifier = modifier.fillMaxSize())
        return
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FinFlowTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                // Tablets and foldables would otherwise stretch a four-field form the full
                // width of the window, which reads as a table rather than a form.
                modifier = Modifier.widthIn(max = FinFlowTheme.dimens.maxContentWidth),
                verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.xl),
            ) {
                AuthHeader(
                    title = "Welcome back",
                    subtitle = "Sign in to pick up where you left off.",
                )

                AuthFormCard {
                    FinFlowTextField(
                        value = state.email,
                        onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                        label = "Email",
                        placeholder = "you@example.com",
                        errorMessage = state.emailError,
                        enabled = !state.isSubmitting,
                        leadingIcon = FinFlowIcons.Email,
                        keyboardType = KeyboardType.Email,
                    )

                    FinFlowPasswordField(
                        value = state.password,
                        onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                        label = "Password",
                        errorMessage = state.passwordError,
                        enabled = !state.isSubmitting,
                        leadingIcon = FinFlowIcons.Password,
                        imeAction = ImeAction.Done,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
                ) {
                    state.formError?.let { AuthFormError(message = it) }

                    FinFlowButton(
                        text = "Sign in",
                        onClick = { onIntent(LoginIntent.Submitted) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canSubmit,
                        isLoading = state.isSubmitting,
                    )

                    AuthFooterPrompt(
                        prompt = "New here?",
                        actionLabel = "Create an account",
                        onAction = { onIntent(LoginIntent.SignUpClicked) },
                        enabled = !state.isSubmitting,
                    )
                }
            }
        }
    }
}

@Preview(name = "Login", showBackground = true)
@Composable
private fun LoginScreenPreview() {
    FinFlowTheme {
        LoginScreen(
            state = LoginState(
                email = "ada@finflow.app",
                password = "password123",
                isRestoringSession = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Login · error", showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    FinFlowTheme {
        LoginScreen(
            state = LoginState(
                email = "ada@finflow.app",
                password = "wrong",
                formError = "That email and password do not match an account.",
                isRestoringSession = false,
            ),
            onIntent = {},
        )
    }
}
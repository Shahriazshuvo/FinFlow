package com.finflow.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.FinFlowButton
import com.finflow.core.designsystem.component.FinFlowPasswordField
import com.finflow.core.designsystem.component.FinFlowTextButton
import com.finflow.core.designsystem.component.FinFlowTextField
import com.finflow.core.designsystem.component.LoadingState
import com.finflow.core.designsystem.theme.FinFlowTheme

@Composable
internal fun AuthScreen(
    state: AuthState,
    onIntent: (AuthIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isRestoringSession) {
        LoadingState(modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FinFlowTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(
            FinFlowTheme.spacing.md,
            Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = if (state.isSignUp) "Create your account" else "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
        )

        if (state.isSignUp) {
            FinFlowTextField(
                value = state.displayName,
                onValueChange = { onIntent(AuthIntent.DisplayNameChanged(it)) },
                label = "Name",
                enabled = !state.isSubmitting,
            )
        }

        FinFlowTextField(
            value = state.email,
            onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
            label = "Email",
            enabled = !state.isSubmitting,
            keyboardType = KeyboardType.Email,
        )

        FinFlowPasswordField(
            value = state.password,
            onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
            label = "Password",
            enabled = !state.isSubmitting,
            imeAction = if (state.isSignUp) ImeAction.Next else ImeAction.Done,
        )

        if (state.isSignUp) {
            FinFlowPasswordField(
                value = state.confirmPassword,
                onValueChange = { onIntent(AuthIntent.ConfirmPasswordChanged(it)) },
                label = "Confirm password",
                enabled = !state.isSubmitting,
            )
        }

        // The use case validates in one pass and returns a single message, so the form
        // carries one error rather than pretending to know which field was at fault.
        state.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        FinFlowButton(
            text = if (state.isSignUp) "Create account" else "Sign in",
            onClick = { onIntent(AuthIntent.Submitted) },
            enabled = state.canSubmit,
            isLoading = state.isSubmitting,
        )

        FinFlowTextButton(
            text = if (state.isSignUp) {
                "Already have an account? Sign in"
            } else {
                "New here? Create an account"
            },
            onClick = { onIntent(AuthIntent.ModeToggled) },
            enabled = !state.isSubmitting,
        )
    }
}

@Preview
@Composable
private fun AuthScreenSignInPreview() {
    FinFlowTheme {
        AuthScreen(
            state = AuthState(isRestoringSession = false, email = "ada@finflow.app"),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun AuthScreenSignUpPreview() {
    FinFlowTheme {
        AuthScreen(
            state = AuthState(
                mode = AuthState.Mode.SignUp,
                isRestoringSession = false,
                errorMessage = "Passwords do not match",
            ),
            onIntent = {},
        )
    }
}

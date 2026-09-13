package com.finflow.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.finflow.core.designsystem.component.FinFlowButton
import com.finflow.core.designsystem.component.FinFlowPasswordField
import com.finflow.core.designsystem.component.FinFlowTextField
import com.finflow.core.designsystem.icon.FinFlowIcons
import com.finflow.core.designsystem.theme.FinFlowTheme
import com.finflow.feature.auth.presentation.components.AuthFooterPrompt
import com.finflow.feature.auth.presentation.components.AuthFormCard
import com.finflow.feature.auth.presentation.components.AuthFormError
import com.finflow.feature.auth.presentation.components.AuthHeader
import com.finflow.feature.auth.presentation.contract.SignUpIntent
import com.finflow.feature.auth.presentation.contract.SignUpState

@Composable
internal fun SignUpScreen(
    state: SignUpState,
    onIntent: (SignUpIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        ) {
            Column(
                modifier = Modifier.widthIn(max = FinFlowTheme.dimens.maxContentWidth),
                verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.xl),
            ) {
                // Not a TopAppBar: this screen has no title bar in the design, and a bar
                // would put a second heading above the serif one that already names it.
                IconButton(
                    onClick = { onIntent(SignUpIntent.SignInClicked) },
                    enabled = !state.isSubmitting,
                ) {
                    Icon(
                        imageVector = FinFlowIcons.Back,
                        contentDescription = "Back to sign in",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                AuthHeader(
                    title = "Create account",
                    subtitle = "Track every account, budget and goal in one place.",
                )

                AuthFormCard {
                    FinFlowTextField(
                        value = state.displayName,
                        onValueChange = { onIntent(SignUpIntent.DisplayNameChanged(it)) },
                        label = "Full name",
                        placeholder = "Ada Lovelace",
                        errorMessage = state.displayNameError,
                        enabled = !state.isSubmitting,
                        leadingIcon = FinFlowIcons.Person,
                    )

                    FinFlowTextField(
                        value = state.email,
                        onValueChange = { onIntent(SignUpIntent.EmailChanged(it)) },
                        label = "Email",
                        placeholder = "you@example.com",
                        errorMessage = state.emailError,
                        enabled = !state.isSubmitting,
                        leadingIcon = FinFlowIcons.Email,
                        keyboardType = KeyboardType.Email,
                    )

                    FinFlowPasswordField(
                        value = state.password,
                        onValueChange = { onIntent(SignUpIntent.PasswordChanged(it)) },
                        label = "Password",
                        errorMessage = state.passwordError,
                        enabled = !state.isSubmitting,
                        leadingIcon = FinFlowIcons.Password,
                        imeAction = ImeAction.Next,
                    )

                    FinFlowPasswordField(
                        value = state.confirmPassword,
                        onValueChange = { onIntent(SignUpIntent.ConfirmPasswordChanged(it)) },
                        label = "Confirm password",
                        errorMessage = state.confirmPasswordError,
                        enabled = !state.isSubmitting,
                        leadingIcon = FinFlowIcons.Password,
                        imeAction = ImeAction.Done,
                    )

                    // Stated up front rather than only as an error after a rejected submit.
                    if (state.passwordError == null) {
                        Text(
                            text = "Use at least 8 characters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(FinFlowTheme.spacing.md),
                ) {
                    state.formError?.let { AuthFormError(message = it) }

                    FinFlowButton(
                        text = "Create account",
                        onClick = { onIntent(SignUpIntent.Submitted) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canSubmit,
                        isLoading = state.isSubmitting,
                    )

                    AuthFooterPrompt(
                        prompt = "Already have an account?",
                        actionLabel = "Sign in",
                        onAction = { onIntent(SignUpIntent.SignInClicked) },
                        enabled = !state.isSubmitting,
                    )
                }
            }
        }
    }
}

@Preview(name = "Sign up", showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    FinFlowTheme {
        SignUpScreen(
            state = SignUpState(
                displayName = "Ada Lovelace",
                email = "ada@finflow.app",
                password = "password123",
                confirmPassword = "password123",
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Sign up · field errors", showBackground = true)
@Composable
private fun SignUpScreenErrorPreview() {
    FinFlowTheme {
        SignUpScreen(
            state = SignUpState(
                displayName = "Ada Lovelace",
                email = "ada@",
                password = "short",
                confirmPassword = "shorter",
                emailError = "Enter a valid email address",
                passwordError = "Password must be at least 8 characters",
                confirmPasswordError = "Passwords do not match",
            ),
            onIntent = {},
        )
    }
}
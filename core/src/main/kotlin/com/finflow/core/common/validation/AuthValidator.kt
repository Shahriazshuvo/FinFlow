package com.finflow.core.common.validation

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthValidator @Inject constructor() {

    fun validateEmail(email: String): AppResult<String> {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> AppResult.Failure(AppError.Validation("Enter your email"))
            !EMAIL_REGEX.matches(trimmed) ->
                AppResult.Failure(AppError.Validation("Enter a valid email address"))
            else -> AppResult.Success(trimmed)
        }
    }

    fun validatePassword(password: String): AppResult<String> = when {
        password.isEmpty() -> AppResult.Failure(AppError.Validation("Enter your password"))
        password.length < MIN_PASSWORD_LENGTH -> AppResult.Failure(
            AppError.Validation("Password must be at least $MIN_PASSWORD_LENGTH characters"),
        )
        else -> AppResult.Success(password)
    }

    fun validatePasswordConfirmation(
        password: String,
        confirmation: String,
    ): AppResult<String> = if (password == confirmation) {
        AppResult.Success(confirmation)
    } else {
        AppResult.Failure(AppError.Validation("Passwords do not match"))
    }

    fun validateDisplayName(name: String): AppResult<String> {
        val trimmed = name.trim()
        return if (trimmed.isEmpty()) {
            AppResult.Failure(AppError.Validation("Enter your name"))
        } else {
            AppResult.Success(trimmed)
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

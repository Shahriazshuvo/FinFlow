package com.finflow.core.domain.usecase.auth

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.flatMap
import com.finflow.core.common.validation.AuthValidator
import com.finflow.core.domain.repository.AuthRepository
import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<SessionState> = authRepository.sessionState
}

/**
 * Validation happens here rather than in the ViewModel so both sign-in and any future
 * entry point (deep link, biometric re-auth) enforce the same rules.
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validator: AuthValidator,
) {
    suspend operator fun invoke(email: String, password: String): AppResult<UserSession> =
        validator.validateEmail(email).flatMap { validEmail ->
            validator.validatePassword(password).flatMap { validPassword ->
                authRepository.signIn(validEmail, validPassword)
            }
        }
}

/**
 * The `handle_new_user` trigger creates the profile, a default Cash account and the starter
 * categories server-side, so signup here is purely an auth concern.
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validator: AuthValidator,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
    ): AppResult<UserSession> =
        validator.validateDisplayName(displayName).flatMap { validName ->
            validator.validateEmail(email).flatMap { validEmail ->
                validator.validatePassword(password).flatMap { validPassword ->
                    validator.validatePasswordConfirmation(validPassword, confirmPassword)
                        .flatMap { authRepository.signUp(validEmail, validPassword, validName) }
                }
            }
        }
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = authRepository.signOut()
}

/** Convenience for callers that need the id and treat "signed out" as an error. */
class RequireUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<String> =
        authRepository.currentUserId()
            ?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.Unauthorized)
}

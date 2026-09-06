package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits [SessionState.Unknown] until the stored session has been restored. */
    val sessionState: Flow<SessionState>

    suspend fun currentUserId(): String?

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): AppResult<UserSession>

    suspend fun signIn(email: String, password: String): AppResult<UserSession>

    suspend fun signOut(): AppResult<Unit>
}

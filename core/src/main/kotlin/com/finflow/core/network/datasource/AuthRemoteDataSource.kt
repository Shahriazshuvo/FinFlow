package com.finflow.core.network.datasource

import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase Auth. Session persistence and refresh are handled by the SDK; this exposes it
 * as domain [SessionState] so nothing above `core:network` imports a Supabase type.
 */
@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val client: SupabaseClient,
) {

    val sessionState: Flow<SessionState> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = status.session.user
                if (user == null) {
                    SessionState.SignedOut
                } else {
                    SessionState.SignedIn(
                        UserSession(userId = user.id, email = user.email.orEmpty()),
                    )
                }
            }
            is SessionStatus.NotAuthenticated -> SessionState.SignedOut
            else -> SessionState.Unknown
        }
    }

    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    /**
     * `full_name` goes into user metadata because the `handle_new_user` trigger reads
     * `raw_user_meta_data ->> 'full_name'` when it creates the profile row.
     */
    suspend fun signUp(email: String, password: String, fullName: String): UserSession {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject { put("full_name", fullName) }
        }
        // Email confirmation may be required, in which case there is no session yet.
        val user = client.auth.currentUserOrNull()
        return UserSession(userId = user?.id.orEmpty(), email = email)
    }

    suspend fun signIn(email: String, password: String): UserSession {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = client.auth.currentUserOrNull()
        return UserSession(userId = user?.id.orEmpty(), email = email)
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}

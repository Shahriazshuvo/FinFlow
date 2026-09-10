package com.finflow.core.network.auth

import com.finflow.core.security.EncryptedKeyValueStore
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the Supabase session through `core:security` instead of Supabase's default
 * `SettingsSessionManager`, which writes it to plain `SharedPreferences`.
 *
 * The session carries a refresh token — a bearer credential that mints access tokens until it
 * is revoked — so it is the one piece of local state that genuinely warrants encryption at
 * rest (APP_SPEC.md §31). Everything else FinFlow stores locally is the user's own financial
 * data, protected by the device lock and by RLS on the server.
 */
@Singleton
internal class EncryptedSessionManager @Inject constructor(
    private val store: EncryptedKeyValueStore,
) : SessionManager {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) {
        store.put(SESSION_KEY, json.encodeToString(session))
    }

    /**
     * The interface declares this non-null, so "no session" is an exception here and
     * [loadSessionOrNull] is the total version. Supabase calls the nullable one when
     * restoring on launch.
     */
    override suspend fun loadSession(): UserSession =
        checkNotNull(loadSessionOrNull()) { "No stored session" }

    /**
     * A session that cannot be decrypted or parsed is treated as absent rather than fatal:
     * the store drops it and the user signs in again. A corrupt token is not worth crashing
     * the app on launch over.
     */
    override suspend fun loadSessionOrNull(): UserSession? {
        val stored = store.get(SESSION_KEY) ?: return null
        return runCatching { json.decodeFromString<UserSession>(stored) }
            .onFailure { store.remove(SESSION_KEY) }
            .getOrNull()
    }

    override suspend fun deleteSession() {
        store.remove(SESSION_KEY)
    }

    private companion object {
        const val SESSION_KEY = "supabase_session"
    }
}

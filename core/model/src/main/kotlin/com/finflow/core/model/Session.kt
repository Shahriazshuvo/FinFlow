package com.finflow.core.model

data class UserSession(
    val userId: String,
    val email: String,
)

/**
 * Auth state as the app sees it. [Unknown] is the pre-restore state, so the UI can show a
 * splash instead of flashing the sign-in screen on every cold start.
 */
sealed interface SessionState {
    data object Unknown : SessionState

    data object SignedOut : SessionState

    data class SignedIn(val session: UserSession) : SessionState
}

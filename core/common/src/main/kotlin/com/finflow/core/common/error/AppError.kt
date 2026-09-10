package com.finflow.core.common.error

/**
 * The only error vocabulary the domain and presentation layers know about.
 *
 * Supabase, Ktor, Room and IO exceptions are translated into one of these inside the data
 * layer (APP_SPEC.md §20), so nothing above the repository boundary ever sees a
 * framework exception type.
 */
sealed interface AppError {
    data object NetworkUnavailable : AppError

    /** No usable session. The user must sign in again. */
    data object Unauthorized : AppError

    /**
     * A session exists but the row is not the user's — an RLS policy refused it. Distinct
     * from [Unauthorized] because signing in again would not help, and because a
     * `Forbidden` in a fintech app is worth noticing rather than retrying.
     */
    data object Forbidden : AppError

    data object NotFound : AppError

    /**
     * A uniqueness constraint rejected the write. [field] names the form field to attach
     * the message to, so the screen can mark the offending input instead of raising a
     * snackbar the user cannot act on.
     *
     * The two that occur: `categories(user_id, type, lower(name))` and
     * `budgets(user_id, category_id, month)` — see `docs/supabase/schema.sql`.
     */
    data class Conflict(val field: String?) : AppError

    data class Validation(val message: String) : AppError

    data class Unknown(val message: String?) : AppError
}

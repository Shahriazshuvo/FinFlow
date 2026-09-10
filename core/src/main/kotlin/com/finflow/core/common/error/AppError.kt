package com.finflow.core.common.error

/**
 * The only error vocabulary the domain and presentation layers know about.
 *
 * Supabase, Ktor, Room and IO exceptions are translated into one of these inside the data
 * layer (APP_SPEC.md §15), so nothing above the repository boundary ever sees a
 * framework exception type.
 *
 * The categories are the ones §15 names, in that order, plus [Forbidden]. §14 requires an RLS
 * refusal to be recognisable as one: it is not [Unauthorized], because signing in again would
 * not help, and in a fintech app it is worth noticing rather than retrying.
 */
sealed interface AppError {
    /** No usable session. The user must sign in again. */
    data object Unauthorized : AppError

    /**
     * A session exists but the row is not the user's — an RLS policy refused it. Distinct
     * from [Unauthorized] because signing in again would not help, and because a
     * `Forbidden` in a fintech app is worth noticing rather than retrying.
     */
    data object Forbidden : AppError

    /**
     * The server was reached and the request still failed — a 5xx, or a response that could
     * not be parsed. Distinct from [Offline] because retrying is worth it and the user is not
     * being told to check their connection when their connection is fine.
     */
    data class Network(val message: String?) : AppError

    /**
     * No connectivity. Never surfaced by a write: a write lands in Room first (§8), so the
     * user's data is already safe and the only honest message is "this will sync later".
     */
    data object Offline : AppError

    data class Validation(val message: String) : AppError

    /**
     * A uniqueness constraint rejected the write. [field] names the form field to attach
     * the message to, so the screen can mark the offending input instead of raising a
     * snackbar the user cannot act on.
     *
     * The two that occur: `categories(user_id, type, lower(name))` and
     * `budgets(user_id, category_id, month)` — see `docs/supabase/schema.sql`.
     */
    data class Duplicate(val field: String?) : AppError

    data object NotFound : AppError

    /** Room or SQLite refused the operation — a disk failure, or a constraint no query guards. */
    data class Database(val message: String?) : AppError

    data class Unknown(val message: String?) : AppError
}

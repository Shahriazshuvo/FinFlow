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

    data object Unauthorized : AppError

    data class Validation(val message: String) : AppError

    data class Unknown(val message: String?) : AppError
}

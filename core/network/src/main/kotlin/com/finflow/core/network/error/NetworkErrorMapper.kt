package com.finflow.core.network.error

import com.finflow.core.common.error.AppError
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Every Supabase/Ktor failure is translated here, so nothing above `core:network` ever
 * catches a transport exception (APP_SPEC.md §20).
 */
@Singleton
class NetworkErrorMapper @Inject constructor() {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is UnauthorizedRestException -> AppError.Unauthorized
        is NotFoundRestException -> AppError.NotFound
        is BadRequestRestException -> throwable.asConflictOrValidation()
        is RestException -> throwable.asConflictOrValidation()
        // Ktor wraps connectivity failures; IOException covers the rest.
        is HttpRequestException, is IOException -> AppError.NetworkUnavailable
        else -> AppError.Unknown(throwable.message)
    }

    /**
     * PostgREST reports a unique-index violation as SQLSTATE 23505 in the response body,
     * and an RLS refusal as 42501. Both arrive as a generic `RestException`, so the code
     * has to be read out of the message — losing that distinction would leave the category
     * and budget forms unable to say *which* field is duplicated.
     */
    private fun RestException.asConflictOrValidation(): AppError {
        val body = message.orEmpty()
        return when {
            UNIQUE_VIOLATION in body -> AppError.Conflict(body.conflictingField())
            RLS_VIOLATION in body -> AppError.Forbidden
            else -> AppError.Validation(
                message ?: "The server rejected this request",
            )
        }
    }

    /** Maps the index named in the error back to the form field the user can fix. */
    private fun String.conflictingField(): String? = when {
        contains("categories_user_type_name") -> "name"
        contains("budgets_user_category_month") -> "category"
        else -> null
    }

    private companion object {
        const val UNIQUE_VIOLATION = "23505"
        const val RLS_VIOLATION = "42501"
    }
}

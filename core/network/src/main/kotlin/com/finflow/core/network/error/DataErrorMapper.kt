package com.finflow.core.network.error

import android.database.sqlite.SQLiteException
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
 * Every data-layer failure is translated here into the [AppError] vocabulary, so nothing above
 * the repository boundary ever catches a framework exception (APP_SPEC.md §15).
 *
 * It covers Room as well as Supabase because the block it wraps covers both: a repository's
 * `syncWith` runs `TableSyncRunner`, which pushes to PostgREST *and* writes the result back to
 * Room inside one `runCatchingApp`. A mapper that only understood transport exceptions would
 * report a disk failure as [AppError.Unknown].
 */
@Singleton
class DataErrorMapper @Inject constructor() {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is UnauthorizedRestException -> AppError.Unauthorized
        is NotFoundRestException -> AppError.NotFound
        is BadRequestRestException -> throwable.asDuplicateOrValidation()
        is RestException -> throwable.asDuplicateOrServerFailure()
        // Ktor wraps connectivity failures; IOException covers the rest.
        is HttpRequestException, is IOException -> AppError.Offline
        is SQLiteException -> AppError.Database(throwable.message)
        else -> AppError.Unknown(throwable.message)
    }

    /**
     * PostgREST reports a unique-index violation as SQLSTATE 23505 in the response body,
     * and an RLS refusal as 42501. Both arrive as a generic `RestException`, so the code
     * has to be read out of the message — losing that distinction would leave the category
     * and budget forms unable to say *which* field is duplicated.
     */
    private fun RestException.asDuplicateOrValidation(): AppError = when {
        UNIQUE_VIOLATION in message.orEmpty() ->
            AppError.Duplicate(message.orEmpty().conflictingField())
        RLS_VIOLATION in message.orEmpty() -> AppError.Forbidden
        else -> AppError.Validation(message ?: "The server rejected this request")
    }

    /**
     * The same two SQLSTATEs can arrive on a non-4xx `RestException`. Anything else at this
     * point reached the server and failed there, which is [AppError.Network] rather than
     * [AppError.Validation] — the user has nothing to correct, and a retry is worthwhile.
     */
    private fun RestException.asDuplicateOrServerFailure(): AppError = when {
        UNIQUE_VIOLATION in message.orEmpty() ->
            AppError.Duplicate(message.orEmpty().conflictingField())
        RLS_VIOLATION in message.orEmpty() -> AppError.Forbidden
        else -> AppError.Network(message)
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

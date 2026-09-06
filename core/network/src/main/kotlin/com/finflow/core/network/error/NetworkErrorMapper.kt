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
        is NotFoundRestException -> AppError.Unknown(throwable.message)
        is BadRequestRestException -> AppError.Validation(
            throwable.message ?: "The server rejected this request",
        )
        is RestException -> AppError.Unknown(throwable.message)
        // Ktor wraps connectivity failures; IOException covers the rest.
        is HttpRequestException, is IOException -> AppError.NetworkUnavailable
        else -> AppError.Unknown(throwable.message)
    }
}

package com.finflow.core.common.result

import com.finflow.core.common.error.AppError
import kotlin.coroutines.cancellation.CancellationException

/** Success-or-[AppError] wrapper used by every suspending repository and use case. */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

val AppResult<*>.isSuccess: Boolean get() = this is AppResult.Success

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

fun AppResult<*>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> transform(data)
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Success) action(data)
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Failure) action(error)
}

inline fun <T, R> AppResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (AppError) -> R,
): R = when (this) {
    is AppResult.Success -> onSuccess(data)
    is AppResult.Failure -> onFailure(error)
}

/**
 * Runs [block], converting a thrown exception into [AppResult.Failure] via [mapError].
 * [CancellationException] is always rethrown so structured concurrency keeps working.
 */
inline fun <T> runCatchingApp(
    mapError: (Throwable) -> AppError = { AppError.Unknown(it.message) },
    block: () -> T,
): AppResult<T> = try {
    AppResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    AppResult.Failure(mapError(throwable))
}

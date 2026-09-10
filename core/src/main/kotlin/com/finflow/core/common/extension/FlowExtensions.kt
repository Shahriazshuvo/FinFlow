package com.finflow.core.common.extension

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Wraps a cold data stream so downstream collectors get failures as state instead of a
 * cancelled flow. Used by every "observe" use case.
 */
fun <T> Flow<T>.asResult(
    mapError: (Throwable) -> AppError = { AppError.Unknown(it.message) },
): Flow<AppResult<T>> = map<T, AppResult<T>> { AppResult.Success(it) }
    .catch { emit(AppResult.Failure(mapError(it))) }

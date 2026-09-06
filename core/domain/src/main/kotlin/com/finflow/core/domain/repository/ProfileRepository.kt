package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<Profile?>

    suspend fun updateFullName(fullName: String): AppResult<Unit>

    suspend fun updateCurrencyCode(currencyCode: String): AppResult<Unit>
}

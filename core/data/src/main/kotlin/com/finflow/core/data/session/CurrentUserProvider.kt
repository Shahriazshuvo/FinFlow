package com.finflow.core.data.session

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.datastore.FinFlowPreferencesDataSource
import com.finflow.core.network.datasource.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the signed-in user id for the data layer.
 *
 * The cached id in DataStore is authoritative rather than the Supabase client, because at
 * cold start the SDK has not restored its session yet — and an offline-first app must be
 * able to read its own data before any network call succeeds.
 */
@Singleton
internal class CurrentUserProvider @Inject constructor(
    private val preferences: FinFlowPreferencesDataSource,
    private val authRemote: AuthRemoteDataSource,
) {

    val userIdFlow: Flow<String?> = preferences.userPreferences.map { it.userId }

    suspend fun userIdOrNull(): String? =
        preferences.userPreferences.first().userId ?: authRemote.currentUserId()

    suspend fun requireUserId(): AppResult<String> =
        userIdOrNull()?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.Unauthorized)
}

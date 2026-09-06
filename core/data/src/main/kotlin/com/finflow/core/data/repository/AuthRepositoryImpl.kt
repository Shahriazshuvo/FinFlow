package com.finflow.core.data.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.database.datasource.AccountLocalDataSource
import com.finflow.core.database.datasource.BudgetLocalDataSource
import com.finflow.core.database.datasource.CategoryLocalDataSource
import com.finflow.core.database.datasource.GoalLocalDataSource
import com.finflow.core.database.datasource.ProfileLocalDataSource
import com.finflow.core.database.datasource.TransactionLocalDataSource
import com.finflow.core.datastore.FinFlowPreferencesDataSource
import com.finflow.core.domain.repository.AuthRepository
import com.finflow.core.model.SessionState
import com.finflow.core.model.UserSession
import com.finflow.core.network.datasource.AuthRemoteDataSource
import com.finflow.core.network.error.NetworkErrorMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase Auth plus the local bookkeeping that has to happen around it.
 *
 * Signup does not create a profile or seed categories — the `handle_new_user` trigger does
 * that server-side, and the first sync pulls the result down.
 */
@Singleton
internal class AuthRepositoryImpl @Inject constructor(
    private val authRemote: AuthRemoteDataSource,
    private val preferences: FinFlowPreferencesDataSource,
    private val errorMapper: NetworkErrorMapper,
    private val profileLocal: ProfileLocalDataSource,
    private val accountLocal: AccountLocalDataSource,
    private val categoryLocal: CategoryLocalDataSource,
    private val transactionLocal: TransactionLocalDataSource,
    private val budgetLocal: BudgetLocalDataSource,
    private val goalLocal: GoalLocalDataSource,
) : AuthRepository {

    override val sessionState: Flow<SessionState> = authRemote.sessionState

    override suspend fun currentUserId(): String? = authRemote.currentUserId()

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): AppResult<UserSession> = runCatchingApp(errorMapper::map) {
        authRemote.signUp(email, password, displayName).also { session ->
            preferences.setUserId(session.userId.takeIf(String::isNotEmpty))
        }
    }

    override suspend fun signIn(email: String, password: String): AppResult<UserSession> =
        runCatchingApp(errorMapper::map) {
            authRemote.signIn(email, password).also { session ->
                preferences.setUserId(session.userId.takeIf(String::isNotEmpty))
            }
        }

    /**
     * Local data is wiped on sign-out. Financial records for one account must never be
     * visible to the next person who signs in on the same device.
     */
    override suspend fun signOut(): AppResult<Unit> = runCatchingApp(errorMapper::map) {
        authRemote.signOut()
        clearLocalData()
    }

    private suspend fun clearLocalData() {
        transactionLocal.clear()
        budgetLocal.clear()
        goalLocal.clear()
        categoryLocal.clear()
        accountLocal.clear()
        profileLocal.clear()
        preferences.clear()
    }
}

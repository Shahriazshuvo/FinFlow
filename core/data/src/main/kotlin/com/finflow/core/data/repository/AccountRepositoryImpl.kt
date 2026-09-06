package com.finflow.core.data.repository

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.getOrNull
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.data.sync.Syncable
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.data.sync.TableSyncRunner
import com.finflow.core.database.datasource.AccountLocalDataSource
import com.finflow.core.domain.repository.AccountRepository
import com.finflow.core.model.Account
import com.finflow.core.model.AccountBalance
import com.finflow.core.model.AccountDraft
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.SyncTable
import com.finflow.core.network.datasource.AccountRemoteDataSource
import com.finflow.core.network.error.NetworkErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccountRepositoryImpl @Inject constructor(
    private val local: AccountLocalDataSource,
    private val remote: AccountRemoteDataSource,
    private val currentUser: CurrentUserProvider,
    private val errorMapper: NetworkErrorMapper,
    private val syncTrigger: SyncTrigger,
    private val clock: Clock,
) : AccountRepository, Syncable {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAccounts(): Flow<List<Account>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observe(userId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAccountBalances(): Flow<List<AccountBalance>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observeWithBalances(userId)
        }

    override fun observeAccount(id: String): Flow<Account?> = local.observeById(id)

    override suspend fun save(draft: AccountDraft): AppResult<String> {
        val userId = currentUser.requireUserId().getOrNull()
            ?: return AppResult.Failure(AppError.Unauthorized)
        val now = clock.instant()
        val existing = draft.id?.let { local.getById(it) }

        val account = Account(
            id = existing?.id ?: draft.id ?: UUID.randomUUID().toString(),
            userId = userId,
            name = draft.name.trim(),
            type = draft.type,
            openingBalance = draft.openingBalance,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            syncStatus = when (existing?.syncStatus) {
                null, SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            },
        )

        local.upsert(account, now)
        syncTrigger.requestSync()
        return AppResult.Success(account.id)
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        val existing = local.getById(id)
            ?: return AppResult.Failure(AppError.Unknown("Account not found"))
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            local.deleteHard(id)
        } else {
            local.markDeleted(id, clock.instant())
            syncTrigger.requestSync()
        }
        return AppResult.Success(Unit)
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        val userId = currentUser.userIdOrNull() ?: return false
        val result = runCatchingApp(errorMapper::map) {
            TableSyncRunner(
                table = SyncTable.ACCOUNTS,
                clock = clock,
                idOf = Account::id,
                updatedAtOf = Account::updatedAt,
                getPending = local::getPending,
                pushUpsert = remote::upsert,
                markSynced = local::markSynced,
                deleteLocal = local::deleteHard,
                fetchSince = remote::fetchSince,
                applyRemote = local::upsertFromRemote,
            ).run(userId, synchronizer)
        }
        return result is AppResult.Success
    }
}

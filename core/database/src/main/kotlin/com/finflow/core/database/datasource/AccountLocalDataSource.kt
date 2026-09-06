package com.finflow.core.database.datasource

import com.finflow.core.database.dao.AccountDao
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.mapper.toDomain
import com.finflow.core.database.mapper.toEntity
import com.finflow.core.model.Account
import com.finflow.core.model.AccountBalance
import com.finflow.core.model.Money
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountLocalDataSource @Inject constructor(
    private val dao: AccountDao,
) {

    fun observe(userId: String): Flow<List<Account>> =
        dao.observeAll(userId).map { entities -> entities.map { it.toDomain() } }

    /** Accounts joined with the balance SQL computes from their transactions. */
    fun observeWithBalances(userId: String): Flow<List<AccountBalance>> = combine(
        dao.observeAll(userId),
        dao.observeBalances(userId),
    ) { accounts, balances ->
        val balanceById = balances.associate { it.accountId to it.balanceMinor }
        accounts.map { entity ->
            AccountBalance(
                account = entity.toDomain(),
                balance = Money(balanceById[entity.id] ?: entity.openingBalanceMinor),
            )
        }
    }

    fun observeById(id: String): Flow<Account?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: String): Account? = dao.getById(id)?.toDomain()

    suspend fun upsert(account: Account, now: Instant) {
        val previous = dao.getById(account.id)?.sync
        dao.upsert(account.toEntity(localUpdatedAt = now, previous = previous))
    }

    /** Remote write: the row already matches the server, so it lands as SYNCED. */
    suspend fun upsertFromRemote(accounts: List<Account>, now: Instant) {
        dao.upsertAll(
            accounts.map { record ->
                record.copy(syncStatus = SyncStatus.SYNCED).toEntity(
                    localUpdatedAt = now,
                    previous = SyncMetadata(
                        syncStatus = SyncStatus.SYNCED,
                        localUpdatedAt = now,
                        remoteUpdatedAt = record.updatedAt,
                        lastSyncedAt = now,
                        deletedAt = null,
                    ),
                )
            },
        )
    }

    suspend fun markDeleted(id: String, now: Instant) =
        dao.markDeleted(id = id, deletedAt = now, now = now)

    suspend fun getPending(): List<PendingRecord<Account>> = dao.getPending().map { entity ->
        PendingRecord(
            id = entity.id,
            record = entity.toDomain(),
            syncStatus = entity.sync.syncStatus,
            deletedAt = entity.sync.deletedAt,
        )
    }

    suspend fun markSynced(id: String, syncedAt: Instant, remoteUpdatedAt: Instant?) =
        dao.markSynced(id = id, syncedAt = syncedAt, remoteUpdatedAt = remoteUpdatedAt)

    suspend fun deleteHard(id: String) = dao.deleteHard(id)

    suspend fun clear() = dao.clear()
}

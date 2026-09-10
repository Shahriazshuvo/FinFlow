package com.finflow.core.database.mapper

import com.finflow.core.database.entity.AccountEntity
import com.finflow.core.database.entity.BudgetEntity
import com.finflow.core.database.entity.CategoryEntity
import com.finflow.core.database.entity.GoalEntity
import com.finflow.core.database.entity.ProfileEntity
import com.finflow.core.database.entity.SyncMetadata
import com.finflow.core.database.entity.TransactionEntity
import com.finflow.core.model.Account
import com.finflow.core.model.Budget
import com.finflow.core.model.Category
import com.finflow.core.model.Goal
import com.finflow.core.model.Money
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.Profile
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.Transaction
import java.time.Instant

/**
 * Entity <-> domain translation. These are `internal` on purpose: Room entities must not
 * escape `core:database` (APP_SPEC.md §8), so only the local data sources in this module
 * can use them.
 */

internal fun ProfileEntity.toDomain() = Profile(
    id = id,
    fullName = fullName,
    currencyCode = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = sync.syncStatus,
)

internal fun Profile.toEntity(
    localUpdatedAt: Instant,
    previous: SyncMetadata? = null,
) = ProfileEntity(
    id = id,
    fullName = fullName,
    currencyCode = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sync = previous.next(syncStatus, localUpdatedAt),
)

internal fun AccountEntity.toDomain() = Account(
    id = id,
    userId = userId,
    name = name,
    type = type,
    openingBalance = Money(openingBalanceMinor),
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = sync.syncStatus,
)

internal fun Account.toEntity(
    localUpdatedAt: Instant,
    previous: SyncMetadata? = null,
) = AccountEntity(
    id = id,
    userId = userId,
    name = name,
    type = type,
    openingBalanceMinor = openingBalance.minorUnits,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sync = previous.next(syncStatus, localUpdatedAt),
)

internal fun CategoryEntity.toDomain() = Category(
    id = id,
    userId = userId,
    name = name,
    type = type,
    color = color,
    icon = icon,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = sync.syncStatus,
)

internal fun Category.toEntity(
    localUpdatedAt: Instant,
    previous: SyncMetadata? = null,
) = CategoryEntity(
    id = id,
    userId = userId,
    name = name,
    type = type,
    color = color,
    icon = icon,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sync = previous.next(syncStatus, localUpdatedAt),
)

internal fun TransactionEntity.toDomain() = Transaction(
    id = id,
    userId = userId,
    accountId = accountId,
    categoryId = categoryId,
    type = type,
    amount = Money(amountMinor),
    note = note,
    transactionDate = transactionDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = sync.syncStatus,
)

internal fun Transaction.toEntity(
    localUpdatedAt: Instant,
    previous: SyncMetadata? = null,
) = TransactionEntity(
    id = id,
    userId = userId,
    accountId = accountId,
    categoryId = categoryId,
    type = type,
    amountMinor = amount.minorUnits,
    note = note,
    transactionDate = transactionDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sync = previous.next(syncStatus, localUpdatedAt),
)

internal fun BudgetEntity.toDomain() = Budget(
    id = id,
    userId = userId,
    categoryId = categoryId,
    month = month,
    amount = Money(amountMinor),
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = sync.syncStatus,
)

internal fun Budget.toEntity(
    localUpdatedAt: Instant,
    previous: SyncMetadata? = null,
) = BudgetEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    month = month,
    amountMinor = amount.minorUnits,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sync = previous.next(syncStatus, localUpdatedAt),
)

internal fun GoalEntity.toDomain() = Goal(
    id = id,
    userId = userId,
    name = name,
    targetAmount = Money(targetAmountMinor),
    currentAmount = Money(currentAmountMinor),
    targetDate = targetDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = sync.syncStatus,
)

internal fun Goal.toEntity(
    localUpdatedAt: Instant,
    previous: SyncMetadata? = null,
) = GoalEntity(
    id = id,
    userId = userId,
    name = name,
    targetAmountMinor = targetAmount.minorUnits,
    currentAmountMinor = currentAmount.minorUnits,
    targetDate = targetDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sync = previous.next(syncStatus, localUpdatedAt),
)

/**
 * Carries forward the remote bookkeeping (`remoteUpdatedAt`, `lastSyncedAt`, `deletedAt`)
 * from the row already in the database, so a local edit never erases what the sync engine
 * knows about the server's copy.
 */
private fun SyncMetadata?.next(status: SyncStatus, localUpdatedAt: Instant) = SyncMetadata(
    syncStatus = status,
    localUpdatedAt = localUpdatedAt,
    remoteUpdatedAt = this?.remoteUpdatedAt,
    lastSyncedAt = this?.lastSyncedAt,
    deletedAt = this?.deletedAt,
)

internal fun <E, D> E.toPendingRecord(
    id: String,
    sync: SyncMetadata,
    toDomain: (E) -> D,
): PendingRecord<D> = PendingRecord(
    id = id,
    record = toDomain(this),
    syncStatus = sync.syncStatus,
    deletedAt = sync.deletedAt,
)

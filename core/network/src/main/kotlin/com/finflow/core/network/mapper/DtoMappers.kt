package com.finflow.core.network.mapper

import com.finflow.core.model.Account
import com.finflow.core.model.AccountType
import com.finflow.core.model.Budget
import com.finflow.core.model.Category
import com.finflow.core.model.Goal
import com.finflow.core.model.Money
import com.finflow.core.model.Profile
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionType
import com.finflow.core.network.dto.AccountDto
import com.finflow.core.network.dto.BudgetDto
import com.finflow.core.network.dto.CategoryDto
import com.finflow.core.network.dto.GoalDto
import com.finflow.core.network.dto.ProfileDto
import com.finflow.core.network.dto.TransactionDto
import com.finflow.core.network.util.toInstant
import com.finflow.core.network.util.toPostgresTimestamp
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * DTO <-> domain translation.
 *
 * Money crosses this boundary as `numeric(12,2)`: [Money] holds integer minor units
 * locally, so the conversion is a plain scale-2 shift in both directions and never loses a
 * cent. Anything arriving from the server is by definition SYNCED — the local replication
 * bookkeeping is applied by `core:database`, not here.
 */

private const val MONTH_DAY_OF_MONTH = 1

internal fun ProfileDto.toDomain() = Profile(
    id = id,
    fullName = fullName.orEmpty(),
    currencyCode = currencyCode,
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    syncStatus = SyncStatus.SYNCED,
)

internal fun AccountDto.toDomain() = Account(
    id = id,
    userId = userId,
    name = name,
    type = AccountType.fromWire(type),
    openingBalance = Money.ofMajorUnits(openingBalance),
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    syncStatus = SyncStatus.SYNCED,
)

internal fun Account.toDto(deletedAt: Instant? = null) = AccountDto(
    id = id,
    userId = userId,
    name = name,
    type = type.wireValue,
    openingBalance = openingBalance.toMajorUnits(),
    createdAt = createdAt.toPostgresTimestamp(),
    updatedAt = updatedAt.toPostgresTimestamp(),
    deletedAt = deletedAt?.toPostgresTimestamp(),
)

internal fun CategoryDto.toDomain() = Category(
    id = id,
    userId = userId,
    name = name,
    type = TransactionType.fromWire(type),
    color = color,
    icon = icon,
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    syncStatus = SyncStatus.SYNCED,
)

internal fun Category.toDto(deletedAt: Instant? = null) = CategoryDto(
    id = id,
    userId = userId,
    name = name,
    type = type.wireValue,
    color = color,
    icon = icon,
    createdAt = createdAt.toPostgresTimestamp(),
    updatedAt = updatedAt.toPostgresTimestamp(),
    deletedAt = deletedAt?.toPostgresTimestamp(),
)

internal fun TransactionDto.toDomain() = Transaction(
    id = id,
    userId = userId,
    accountId = accountId,
    categoryId = categoryId,
    type = TransactionType.fromWire(type),
    amount = Money.ofMajorUnits(amount),
    note = note,
    transactionDate = LocalDate.parse(transactionDate),
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    syncStatus = SyncStatus.SYNCED,
)

internal fun Transaction.toDto(deletedAt: Instant? = null) = TransactionDto(
    id = id,
    userId = userId,
    accountId = accountId,
    categoryId = categoryId,
    type = type.wireValue,
    amount = amount.toMajorUnits(),
    note = note,
    transactionDate = transactionDate.toString(),
    createdAt = createdAt.toPostgresTimestamp(),
    updatedAt = updatedAt.toPostgresTimestamp(),
    deletedAt = deletedAt?.toPostgresTimestamp(),
)

internal fun BudgetDto.toDomain() = Budget(
    id = id,
    userId = userId,
    categoryId = categoryId,
    month = YearMonth.from(LocalDate.parse(month)),
    amount = Money.ofMajorUnits(amount),
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    syncStatus = SyncStatus.SYNCED,
)

internal fun Budget.toDto(deletedAt: Instant? = null) = BudgetDto(
    id = id,
    userId = userId,
    categoryId = categoryId,
    // Postgres stores the month as the first day, matching date_trunc('month', ...).
    month = month.atDay(MONTH_DAY_OF_MONTH).toString(),
    amount = amount.toMajorUnits(),
    createdAt = createdAt.toPostgresTimestamp(),
    updatedAt = updatedAt.toPostgresTimestamp(),
    deletedAt = deletedAt?.toPostgresTimestamp(),
)

internal fun GoalDto.toDomain() = Goal(
    id = id,
    userId = userId,
    name = name,
    targetAmount = Money.ofMajorUnits(targetAmount),
    currentAmount = Money.ofMajorUnits(currentAmount),
    targetDate = targetDate?.let(LocalDate::parse),
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
    syncStatus = SyncStatus.SYNCED,
)

internal fun Goal.toDto(deletedAt: Instant? = null) = GoalDto(
    id = id,
    userId = userId,
    name = name,
    targetAmount = targetAmount.toMajorUnits(),
    currentAmount = currentAmount.toMajorUnits(),
    targetDate = targetDate?.toString(),
    createdAt = createdAt.toPostgresTimestamp(),
    updatedAt = updatedAt.toPostgresTimestamp(),
    deletedAt = deletedAt?.toPostgresTimestamp(),
)

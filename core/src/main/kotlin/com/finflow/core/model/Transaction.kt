package com.finflow.core.model

import java.time.Instant
import java.time.LocalDate

/**
 * `public.transactions`. `category_id` is `not null` in Postgres, and the RLS insert policy
 * additionally requires the account and category to belong to the same user.
 */
data class Transaction(
    val id: String,
    val userId: String,
    val accountId: String,
    val categoryId: String,
    val type: TransactionType,
    val amount: Money,
    val note: String?,
    val transactionDate: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
) {
    /** Income counts up, expense counts down, so balances are a plain sum. */
    val signedAmount: Money
        get() = when (type) {
            TransactionType.INCOME -> amount
            TransactionType.EXPENSE -> -amount
        }
}

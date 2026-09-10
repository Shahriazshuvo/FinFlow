package com.finflow.core.model

import java.time.Instant

/**
 * `public.accounts`. Currency is a profile-level setting, so accounts do not carry their
 * own currency code.
 */
data class Account(
    val id: String,
    val userId: String,
    val name: String,
    val type: AccountType,
    val openingBalance: Money,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

/**
 * An account together with the balance derived from its transactions (APP_SPEC.md §11).
 *
 * The backend has no `current_balance` column and this type does not add one: the balance is
 * `openingBalance + income - expense` over rows whose `deleted_at` is null, computed by
 * `AccountDao` and never stored.
 */
data class AccountWithBalance(
    val account: Account,
    val balance: Money,
)

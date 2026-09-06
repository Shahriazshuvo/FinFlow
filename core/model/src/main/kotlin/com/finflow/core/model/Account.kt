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

/** An account together with the balance derived from its transactions. */
data class AccountBalance(
    val account: Account,
    val balance: Money,
)

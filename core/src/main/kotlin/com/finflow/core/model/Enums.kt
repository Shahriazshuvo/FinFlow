package com.finflow.core.model

/** Serialized to Postgres as lowercase to satisfy `check (type in ('income','expense'))`. */
enum class TransactionType {
    INCOME,
    EXPENSE,
    ;

    val wireValue: String get() = name.lowercase()

    companion object {
        fun fromWire(value: String): TransactionType = valueOf(value.uppercase())
    }
}

/** Mirrors `check (type in ('cash','bank','card','wallet'))` on `public.accounts`. */
enum class AccountType {
    CASH,
    BANK,
    CARD,
    WALLET,
    ;

    val wireValue: String get() = name.lowercase()

    companion object {
        fun fromWire(value: String): AccountType = valueOf(value.uppercase())
    }
}

/**
 * Local-only replication state. Never sent to Supabase — the remote tracks `updated_at`
 * and `deleted_at`, and this is how the device remembers what it still owes the server.
 */
enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    ;

    val isPending: Boolean get() = this != SYNCED
}

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

package com.finflow.core.data.sync

/** Tables that carry their own incremental-pull watermark. */
enum class SyncTable {
    PROFILES,
    ACCOUNTS,
    CATEGORIES,
    TRANSACTIONS,
    BUDGETS,
    GOALS,
}

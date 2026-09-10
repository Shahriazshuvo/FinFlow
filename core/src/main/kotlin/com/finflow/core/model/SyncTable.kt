package com.finflow.core.model

/** Tables that carry their own incremental-pull watermark. */
enum class SyncTable {
    PROFILES,
    ACCOUNTS,
    CATEGORIES,
    TRANSACTIONS,
    BUDGETS,
    GOALS,
}

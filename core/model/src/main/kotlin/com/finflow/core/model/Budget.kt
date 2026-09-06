package com.finflow.core.model

import java.time.Instant
import java.time.YearMonth

/**
 * `public.budgets`. Postgres stores `month` as a `date` normalised to the first of the
 * month (matching `date_trunc('month', ...)` in the `budget_usage` view); the domain works
 * in [YearMonth] and converts at the network boundary.
 */
data class Budget(
    val id: String,
    val userId: String,
    val categoryId: String,
    val month: YearMonth,
    val amount: Money,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

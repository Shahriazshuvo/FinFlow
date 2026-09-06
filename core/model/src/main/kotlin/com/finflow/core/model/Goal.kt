package com.finflow.core.model

import java.time.Instant
import java.time.LocalDate

/** `public.goals`. */
data class Goal(
    val id: String,
    val userId: String,
    val name: String,
    val targetAmount: Money,
    val currentAmount: Money,
    val targetDate: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
) {
    val remainingAmount: Money
        get() = (targetAmount - currentAmount).let { if (it.isNegative) Money.ZERO else it }

    /** Progress in `0f..1f`; a met or exceeded goal reports `1f`. */
    val progress: Float
        get() = currentAmount.ratioOf(targetAmount).coerceIn(0f, 1f)

    val isReached: Boolean get() = currentAmount >= targetAmount
}

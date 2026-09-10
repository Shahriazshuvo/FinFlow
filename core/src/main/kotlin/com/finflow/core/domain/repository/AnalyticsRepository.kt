package com.finflow.core.domain.repository

import com.finflow.core.model.CategorySpending
import com.finflow.core.model.MonthlySummary
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface AnalyticsRepository {
    fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary>

    fun observeCategorySpending(
        month: YearMonth,
        type: TransactionType = TransactionType.EXPENSE,
    ): Flow<List<CategorySpending>>

    /** Trailing [months] of summaries, oldest first — the net-savings trend series. */
    fun observeSummaryTrend(monthCount: Int): Flow<List<MonthlySummary>>
}

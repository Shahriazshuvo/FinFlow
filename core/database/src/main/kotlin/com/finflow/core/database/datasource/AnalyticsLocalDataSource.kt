package com.finflow.core.database.datasource

import com.finflow.core.database.dao.TransactionDao
import com.finflow.core.model.CategorySpending
import com.finflow.core.model.Money
import com.finflow.core.model.MonthlySummary
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics are computed from Room (APP_SPEC.md §18), so the charts keep working offline
 * and stay consistent with whatever the transaction list is showing.
 */
@Singleton
class AnalyticsLocalDataSource @Inject constructor(
    private val transactionDao: TransactionDao,
) {

    fun observeMonthlySummary(userId: String, month: YearMonth): Flow<MonthlySummary> =
        transactionDao.observeTotalsByType(
            userId = userId,
            from = month.atDay(1),
            to = month.atEndOfMonth(),
        ).map { rows ->
            val byType = rows.associate { it.type to Money(it.totalMinor) }
            MonthlySummary(
                month = month,
                income = byType[TransactionType.INCOME] ?: Money.ZERO,
                expense = byType[TransactionType.EXPENSE] ?: Money.ZERO,
            )
        }

    fun observeCategorySpending(
        userId: String,
        month: YearMonth,
        type: TransactionType = TransactionType.EXPENSE,
    ): Flow<List<CategorySpending>> = transactionDao.observeTotalsByCategory(
        userId = userId,
        type = type,
        from = month.atDay(1),
        to = month.atEndOfMonth(),
    ).map { rows ->
        rows.map { row ->
            CategorySpending(
                categoryId = row.categoryId,
                categoryName = row.categoryName,
                categoryColor = row.categoryColor,
                amount = Money(row.totalMinor),
            )
        }
    }
}

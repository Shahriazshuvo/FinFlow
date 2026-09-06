package com.finflow.core.data.repository

import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.database.datasource.AnalyticsLocalDataSource
import com.finflow.core.domain.repository.AnalyticsRepository
import com.finflow.core.model.CategorySpending
import com.finflow.core.model.Money
import com.finflow.core.model.MonthlySummary
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics are aggregated from Room rather than the Supabase views, so every chart works
 * offline and always agrees with the transaction list on screen (APP_SPEC.md §18). The
 * `monthly_income_expense` / `budget_usage` views remain the server-side cross-check.
 */
@Singleton
internal class AnalyticsRepositoryImpl @Inject constructor(
    private val local: AnalyticsLocalDataSource,
    private val currentUser: CurrentUserProvider,
    private val clock: Clock,
) : AnalyticsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(MonthlySummary(month, Money.ZERO, Money.ZERO))
            } else {
                local.observeMonthlySummary(userId, month)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCategorySpending(
        month: YearMonth,
        type: TransactionType,
    ): Flow<List<CategorySpending>> = currentUser.userIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else local.observeCategorySpending(userId, month, type)
    }

    override fun observeSummaryTrend(monthCount: Int): Flow<List<MonthlySummary>> {
        val currentMonth = YearMonth.now(clock.withZone(ZoneId.systemDefault()))
        val months = (monthCount - 1 downTo 0).map { currentMonth.minusMonths(it.toLong()) }
        return combine(months.map(::observeMonthlySummary)) { it.toList() }
    }
}

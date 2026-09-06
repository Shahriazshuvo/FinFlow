package com.finflow.core.domain.usecase.analytics

import com.finflow.core.domain.repository.AnalyticsRepository
import com.finflow.core.model.CategorySpending
import com.finflow.core.model.MonthlySummary
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class ObserveMonthlySummaryUseCase @Inject constructor(
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(month: YearMonth): Flow<MonthlySummary> =
        repository.observeMonthlySummary(month)
}

class ObserveCategorySpendingUseCase @Inject constructor(
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(
        month: YearMonth,
        type: TransactionType = TransactionType.EXPENSE,
    ): Flow<List<CategorySpending>> = repository.observeCategorySpending(month, type)
}

class ObserveTopCategoriesUseCase @Inject constructor(
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(month: YearMonth, limit: Int = DEFAULT_LIMIT): Flow<List<CategorySpending>> =
        repository.observeCategorySpending(month, TransactionType.EXPENSE)
            .map { spending -> spending.take(limit) }

    private companion object {
        const val DEFAULT_LIMIT = 5
    }
}

class ObserveSavingsTrendUseCase @Inject constructor(
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(monthCount: Int = DEFAULT_MONTHS): Flow<List<MonthlySummary>> =
        repository.observeSummaryTrend(monthCount)

    private companion object {
        const val DEFAULT_MONTHS = 6
    }
}

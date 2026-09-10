package com.finflow.core.domain.usecase.budget

import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.repository.BudgetRepository
import com.finflow.core.model.Budget
import com.finflow.core.model.BudgetDraft
import com.finflow.core.model.BudgetUsage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class ObserveBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    operator fun invoke(month: YearMonth): Flow<List<Budget>> = repository.observeBudgets(month)
}

class ObserveBudgetUsageUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    operator fun invoke(month: YearMonth): Flow<List<BudgetUsage>> =
        repository.observeBudgetUsage(month)
}

/** Feeds the dashboard's "budget usage warning" card (APP_SPEC.md §13). */
class ObserveBudgetWarningsUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    operator fun invoke(month: YearMonth): Flow<List<BudgetUsage>> =
        repository.observeBudgetUsage(month)
            .map { usages -> usages.filter { it.isOverBudget || it.isNearLimit } }
}

class SaveBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(draft: BudgetDraft): AppResult<String> = repository.save(draft)
}

class DeleteBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.delete(id)
}

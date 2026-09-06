package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Budget
import com.finflow.core.model.BudgetDraft
import com.finflow.core.model.BudgetUsage
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun observeBudgets(month: YearMonth): Flow<List<Budget>>

    /** Budgets joined with the spend computed from that month's expense transactions. */
    fun observeBudgetUsage(month: YearMonth): Flow<List<BudgetUsage>>

    fun observeBudget(id: String): Flow<Budget?>

    suspend fun save(draft: BudgetDraft): AppResult<String>

    suspend fun delete(id: String): AppResult<Unit>
}

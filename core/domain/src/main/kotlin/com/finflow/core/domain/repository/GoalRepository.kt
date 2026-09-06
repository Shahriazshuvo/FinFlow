package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Goal
import com.finflow.core.model.GoalDraft
import com.finflow.core.model.Money
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>

    fun observeGoal(id: String): Flow<Goal?>

    suspend fun save(draft: GoalDraft): AppResult<String>

    suspend fun addContribution(id: String, amount: Money): AppResult<Unit>

    suspend fun delete(id: String): AppResult<Unit>
}

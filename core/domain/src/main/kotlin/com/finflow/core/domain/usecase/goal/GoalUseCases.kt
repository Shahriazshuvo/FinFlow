package com.finflow.core.domain.usecase.goal

import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.repository.GoalRepository
import com.finflow.core.model.Goal
import com.finflow.core.model.GoalDraft
import com.finflow.core.model.Money
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGoalsUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(): Flow<List<Goal>> = repository.observeGoals()
}

class ObserveGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(id: String): Flow<Goal?> = repository.observeGoal(id)
}

class SaveGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(draft: GoalDraft): AppResult<String> = repository.save(draft)
}

class AddGoalContributionUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String, amount: Money): AppResult<Unit> =
        repository.addContribution(id, amount)
}

class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.delete(id)
}

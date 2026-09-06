package com.finflow.core.domain.usecase.category

import com.finflow.core.common.result.AppResult
import com.finflow.core.domain.repository.CategoryRepository
import com.finflow.core.model.Category
import com.finflow.core.model.CategoryDraft
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    operator fun invoke(type: TransactionType? = null): Flow<List<Category>> =
        repository.observeCategories(type)
}

class SaveCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(draft: CategoryDraft): AppResult<String> = repository.save(draft)
}

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.delete(id)
}

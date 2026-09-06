package com.finflow.core.domain.repository

import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Category
import com.finflow.core.model.CategoryDraft
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Default categories are seeded server-side by the `handle_new_user` trigger, so there is
 * deliberately no "create defaults" entry point here.
 */
interface CategoryRepository {
    fun observeCategories(type: TransactionType? = null): Flow<List<Category>>

    fun observeCategory(id: String): Flow<Category?>

    suspend fun save(draft: CategoryDraft): AppResult<String>

    suspend fun delete(id: String): AppResult<Unit>
}

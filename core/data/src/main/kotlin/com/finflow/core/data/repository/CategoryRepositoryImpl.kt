package com.finflow.core.data.repository

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.common.result.getOrNull
import com.finflow.core.common.result.runCatchingApp
import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.data.sync.Syncable
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.data.sync.TableSyncRunner
import com.finflow.core.database.datasource.CategoryLocalDataSource
import com.finflow.core.domain.repository.CategoryRepository
import com.finflow.core.model.Category
import com.finflow.core.model.CategoryDraft
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.SyncTable
import com.finflow.core.model.TransactionType
import com.finflow.core.network.datasource.CategoryRemoteDataSource
import com.finflow.core.network.error.DataErrorMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CategoryRepositoryImpl @Inject constructor(
    private val local: CategoryLocalDataSource,
    private val remote: CategoryRemoteDataSource,
    private val currentUser: CurrentUserProvider,
    private val errorMapper: DataErrorMapper,
    private val syncTrigger: SyncTrigger,
    private val clock: Clock,
) : CategoryRepository, Syncable {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCategories(type: TransactionType?): Flow<List<Category>> =
        currentUser.userIdFlow.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else local.observe(userId, type)
        }

    override fun observeCategory(id: String): Flow<Category?> = local.observeById(id)

    override suspend fun save(draft: CategoryDraft): AppResult<String> {
        val userId = currentUser.requireUserId().getOrNull()
            ?: return AppResult.Failure(AppError.Unauthorized)
        val now = clock.instant()
        val existing = draft.id?.let { local.getById(it) }
        val name = draft.name.trim()

        // Checked here rather than left to Postgres: an offline create has to be rejected
        // at the form, not silently accepted and then bounced hours later by the unique
        // index during sync.
        if (local.hasNameConflict(userId, draft.type, name, existing?.id)) {
            return AppResult.Failure(AppError.Duplicate(field = "name"))
        }

        val category = Category(
            id = existing?.id ?: draft.id ?: UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            type = draft.type,
            color = draft.color,
            icon = draft.icon,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            syncStatus = when (existing?.syncStatus) {
                null, SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            },
        )

        local.upsert(category, now)
        syncTrigger.requestSync()
        return AppResult.Success(category.id)
    }

    override suspend fun delete(id: String): AppResult<Unit> {
        val existing = local.getById(id)
            ?: return AppResult.Failure(AppError.Unknown("Category not found"))
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            local.deleteHard(id)
        } else {
            local.markDeleted(id, clock.instant())
            syncTrigger.requestSync()
        }
        return AppResult.Success(Unit)
    }

    /** Pull order is imposed on this, not on Dagger's set iteration. */
    override val table: SyncTable = SyncTable.CATEGORIES

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        val userId = currentUser.userIdOrNull() ?: return false
        val result = runCatchingApp(errorMapper::map) {
            TableSyncRunner(
                table = SyncTable.CATEGORIES,
                clock = clock,
                idOf = Category::id,
                updatedAtOf = Category::updatedAt,
                getPending = local::getPending,
                pushUpsert = remote::upsert,
                markSynced = local::markSynced,
                deleteLocal = local::deleteHard,
                fetchSince = remote::fetchSince,
                applyRemote = local::upsertFromRemote,
            ).run(userId, synchronizer)
        }
        return result is AppResult.Success
    }
}

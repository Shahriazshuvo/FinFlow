package com.finflow.core.database.datasource

import com.finflow.core.database.dao.SyncStateDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncStateLocalDataSource @Inject internal constructor(
    private val dao: SyncStateDao,
) {
    val hasPendingChanges: Flow<Boolean> = dao.observePendingCount().map { it > 0 }
}

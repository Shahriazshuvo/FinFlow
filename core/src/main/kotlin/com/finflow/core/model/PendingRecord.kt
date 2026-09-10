package com.finflow.core.model

import java.time.Instant

/**
 * A locally-modified record awaiting push, paired with the replication bookkeeping the
 * sync engine needs. This is how `core:data` drives sync without `core:database` ever
 * exposing a Room entity.
 */
data class PendingRecord<T>(
    val id: String,
    val record: T,
    val syncStatus: SyncStatus,
    val deletedAt: Instant?,
)

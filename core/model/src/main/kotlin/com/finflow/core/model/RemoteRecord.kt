package com.finflow.core.model

import java.time.Instant

/**
 * A record as the server has it. [deletedAt] is the tombstone: a pull deliberately fetches
 * soft-deleted rows, because that is the only way a device learns about a record another
 * device removed (APP_SPEC.md §12).
 */
data class RemoteRecord<T>(
    val record: T,
    val deletedAt: Instant?,
)

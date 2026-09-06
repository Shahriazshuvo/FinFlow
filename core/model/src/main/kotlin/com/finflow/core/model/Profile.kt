package com.finflow.core.model

import java.time.Instant

/**
 * `public.profiles`. Created automatically by the `handle_new_user` trigger on signup, so
 * the app only ever reads and updates it — never inserts.
 */
data class Profile(
    val id: String,
    val fullName: String,
    val currencyCode: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

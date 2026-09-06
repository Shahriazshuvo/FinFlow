package com.finflow.core.model

import java.time.Instant

/**
 * `public.categories`. The signup trigger seeds a starter set (Salary, Food, Transport,
 * ...), so the app never has to create defaults itself.
 */
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val type: TransactionType,
    val color: String?,
    val icon: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

package com.finflow.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.finflow.core.model.AccountType
import java.time.Instant

/**
 * Mirrors `public.accounts`. Postgres stores `opening_balance` as `numeric(12,2)`; locally
 * it is held as integer minor units so every sum and balance is exact. The two are a
 * lossless 1:1 mapping at two decimal places, converted at the network boundary.
 */
@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["sync_status"]),
    ],
)
data class AccountEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: AccountType,
    @ColumnInfo(name = "opening_balance_minor")
    val openingBalanceMinor: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @Embedded
    val sync: SyncMetadata,
)

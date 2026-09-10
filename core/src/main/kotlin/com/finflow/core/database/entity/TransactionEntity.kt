package com.finflow.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.finflow.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate

/**
 * Mirrors `public.transactions`. `category_id` is `not null` there, and neither foreign key
 * declares an `on delete` action, so both are NO_ACTION here as well — categories and
 * accounts are soft-deleted rather than removed.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["user_id", "transaction_date"]),
        Index(value = ["user_id", "category_id"]),
        Index(value = ["user_id", "account_id"]),
        Index(value = ["sync_status"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
)
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "account_id")
    val accountId: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "type")
    val type: TransactionType,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,
    @ColumnInfo(name = "note")
    val note: String?,
    /** ISO date text so `BETWEEN` range filters sort correctly. */
    @ColumnInfo(name = "transaction_date")
    val transactionDate: LocalDate,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @Embedded
    val sync: SyncMetadata,
)

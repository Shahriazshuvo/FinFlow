package com.finflow.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.YearMonth

/** Mirrors `public.budgets`, including its unique (user, category, month) constraint. */
@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["user_id", "month"]),
        Index(value = ["category_id"]),
        Index(value = ["sync_status"]),
        Index(value = ["user_id", "category_id", "month"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
)
internal data class BudgetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    /** ISO `yyyy-MM`; Postgres holds the first day of the month as a `date`. */
    @ColumnInfo(name = "month")
    val month: YearMonth,
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @Embedded
    val sync: SyncMetadata,
)

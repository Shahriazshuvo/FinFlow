package com.finflow.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/** Mirrors `public.goals`. */
@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["sync_status"]),
    ],
)
data class GoalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "target_amount_minor")
    val targetAmountMinor: Long,
    @ColumnInfo(name = "current_amount_minor")
    val currentAmountMinor: Long,
    @ColumnInfo(name = "target_date")
    val targetDate: LocalDate?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @Embedded
    val sync: SyncMetadata,
)

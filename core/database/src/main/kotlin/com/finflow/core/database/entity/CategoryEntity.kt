package com.finflow.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.finflow.core.model.TransactionType
import java.time.Instant

/** Mirrors `public.categories`. */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["user_id", "type"]),
        Index(value = ["sync_status"]),
    ],
)
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: TransactionType,
    @ColumnInfo(name = "color")
    val color: String?,
    @ColumnInfo(name = "icon")
    val icon: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @Embedded
    val sync: SyncMetadata,
)

package com.finflow.core.database.model

import androidx.room.ColumnInfo
import com.finflow.core.model.TransactionType

/** Projection rows returned by the aggregate DAO queries (APP_SPEC.md §13). */

internal data class TypeTotalRow(
    @ColumnInfo(name = "type") val type: TransactionType,
    @ColumnInfo(name = "total_minor") val totalMinor: Long,
)

internal data class CategoryTotalRow(
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_color") val categoryColor: String?,
    @ColumnInfo(name = "total_minor") val totalMinor: Long,
)

internal data class AccountBalanceRow(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "balance_minor") val balanceMinor: Long,
)

internal data class BudgetUsageRow(
    @ColumnInfo(name = "budget_id") val budgetId: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_color") val categoryColor: String?,
    @ColumnInfo(name = "spent_minor") val spentMinor: Long,
)

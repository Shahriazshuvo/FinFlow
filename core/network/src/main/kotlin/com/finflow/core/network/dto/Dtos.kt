package com.finflow.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * Wire shapes for the Supabase tables, matching `docs/supabase/schema.sql` column for
 * column. These stay `internal` to `core:network`: domain and UI never see a DTO
 * (APP_SPEC.md §8).
 *
 * `type` values are lowercase to satisfy the Postgres check constraints, and money columns
 * are `numeric(12,2)` decoded as [BigDecimal] — never a floating point number.
 */

@Serializable
internal data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

/** Fields the app is allowed to update on `profiles`; the row itself is trigger-created. */
@Serializable
internal data class ProfileUpdateDto(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("currency_code") val currencyCode: String? = null,
)

@Serializable
internal data class AccountDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("opening_balance") val openingBalance: BigDecimal,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
internal data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("color") val color: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
internal data class TransactionDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("type") val type: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amount") val amount: BigDecimal,
    @SerialName("note") val note: String? = null,
    @SerialName("transaction_date") val transactionDate: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
internal data class BudgetDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("category_id") val categoryId: String,
    /** A `date` normalised to the first of the month, e.g. `2026-09-01`. */
    @SerialName("month") val month: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amount") val amount: BigDecimal,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
internal data class GoalDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("target_amount") val targetAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("current_amount") val currentAmount: BigDecimal,
    @SerialName("target_date") val targetDate: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

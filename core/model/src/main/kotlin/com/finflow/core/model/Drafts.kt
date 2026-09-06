package com.finflow.core.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * What a form submits. Drafts carry only what the user actually chose — ids, timestamps
 * and ownership are filled in by the repository, so the UI never fabricates a `user_id` or
 * a `created_at`.
 *
 * A null [id] means "create"; a non-null [id] means "update that record".
 */

data class TransactionDraft(
    val id: String? = null,
    val accountId: String,
    val categoryId: String,
    val type: TransactionType,
    val amount: Money,
    val note: String?,
    val transactionDate: LocalDate,
)

data class AccountDraft(
    val id: String? = null,
    val name: String,
    val type: AccountType,
    val openingBalance: Money,
)

data class CategoryDraft(
    val id: String? = null,
    val name: String,
    val type: TransactionType,
    val color: String?,
    val icon: String?,
)

data class BudgetDraft(
    val id: String? = null,
    val categoryId: String,
    val month: YearMonth,
    val amount: Money,
)

data class GoalDraft(
    val id: String? = null,
    val name: String,
    val targetAmount: Money,
    val currentAmount: Money,
    val targetDate: LocalDate?,
)

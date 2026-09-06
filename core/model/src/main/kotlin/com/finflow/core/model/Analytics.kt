package com.finflow.core.model

import java.time.YearMonth

/** Mirrors the `monthly_income_expense` view, computed locally from Room so it works offline. */
data class MonthlySummary(
    val month: YearMonth,
    val income: Money,
    val expense: Money,
) {
    val net: Money get() = income - expense
}

/** Mirrors the `monthly_category_spending` view. */
data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val categoryColor: String?,
    val amount: Money,
) {
    /** Share of a period total, filled in by the use case that knows the total. */
    fun shareOf(total: Money): Float = amount.ratioOf(total).coerceIn(0f, 1f)
}

/** Mirrors the `budget_usage` view. */
data class BudgetUsage(
    val budget: Budget,
    val categoryName: String,
    val categoryColor: String?,
    val spent: Money,
) {
    val remaining: Money
        get() = (budget.amount - spent).let { if (it.isNegative) Money.ZERO else it }

    val usageRatio: Float get() = spent.ratioOf(budget.amount)

    val isOverBudget: Boolean get() = spent > budget.amount

    /** Crossed the warning threshold but not yet over — drives the dashboard warning. */
    val isNearLimit: Boolean get() = !isOverBudget && usageRatio >= NEAR_LIMIT_THRESHOLD

    private companion object {
        const val NEAR_LIMIT_THRESHOLD = 0.8f
    }
}

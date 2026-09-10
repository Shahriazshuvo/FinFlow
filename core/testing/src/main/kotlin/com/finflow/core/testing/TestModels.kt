package com.finflow.core.testing

import com.finflow.core.model.Account
import com.finflow.core.model.AccountType
import com.finflow.core.model.Budget
import com.finflow.core.model.Category
import com.finflow.core.model.Goal
import com.finflow.core.model.Money
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * Domain models with every field defaulted, so a test names only what it is actually
 * asserting on.
 *
 * `testTransaction(amount = Money(500))` says "this test is about the amount";
 * a full constructor call buries that in nine irrelevant arguments and has to be edited
 * every time a field is added to the model.
 */
const val TEST_USER_ID: String = "user-1"

fun testAccount(
    id: String = "account-1",
    userId: String = TEST_USER_ID,
    name: String = "Cash",
    type: AccountType = AccountType.CASH,
    openingBalance: Money = Money.ZERO,
    createdAt: Instant = TEST_INSTANT,
    updatedAt: Instant = TEST_INSTANT,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
): Account = Account(id, userId, name, type, openingBalance, createdAt, updatedAt, syncStatus)

fun testCategory(
    id: String = "category-1",
    userId: String = TEST_USER_ID,
    name: String = "Food",
    type: TransactionType = TransactionType.EXPENSE,
    color: String? = null,
    icon: String? = null,
    createdAt: Instant = TEST_INSTANT,
    updatedAt: Instant = TEST_INSTANT,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
): Category = Category(id, userId, name, type, color, icon, createdAt, updatedAt, syncStatus)

fun testTransaction(
    id: String = "transaction-1",
    userId: String = TEST_USER_ID,
    accountId: String = "account-1",
    categoryId: String = "category-1",
    type: TransactionType = TransactionType.EXPENSE,
    amount: Money = Money(1_000),
    note: String? = null,
    transactionDate: LocalDate = TEST_DATE,
    createdAt: Instant = TEST_INSTANT,
    updatedAt: Instant = TEST_INSTANT,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
): Transaction = Transaction(
    id, userId, accountId, categoryId, type, amount, note,
    transactionDate, createdAt, updatedAt, syncStatus,
)

fun testBudget(
    id: String = "budget-1",
    userId: String = TEST_USER_ID,
    categoryId: String = "category-1",
    month: YearMonth = YearMonth.of(2026, 1),
    amount: Money = Money(50_000),
    createdAt: Instant = TEST_INSTANT,
    updatedAt: Instant = TEST_INSTANT,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
): Budget = Budget(id, userId, categoryId, month, amount, createdAt, updatedAt, syncStatus)

fun testGoal(
    id: String = "goal-1",
    userId: String = TEST_USER_ID,
    name: String = "Emergency fund",
    targetAmount: Money = Money(100_000),
    currentAmount: Money = Money.ZERO,
    targetDate: LocalDate? = null,
    createdAt: Instant = TEST_INSTANT,
    updatedAt: Instant = TEST_INSTANT,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
): Goal = Goal(
    id, userId, name, targetAmount, currentAmount, targetDate,
    createdAt, updatedAt, syncStatus,
)

package com.finflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `major units round-trip without losing a cent`() {
        val money = Money.ofMajorUnits("1240.55")

        assertEquals(124055L, money.minorUnits)
        assertEquals(BigDecimal("1240.55"), money.toMajorUnits())
    }

    @Test
    fun `repeated addition of a third does not drift`() {
        // The reason money is never a Double: 0.1 + 0.2 != 0.3 in binary floating point.
        val tenCents = Money.ofMajorUnits("0.10")
        val twentyCents = Money.ofMajorUnits("0.20")

        assertEquals(Money.ofMajorUnits("0.30"), tenCents + twentyCents)
    }

    @Test
    fun `summing a hundred amounts is exact`() {
        val total = List(100) { Money.ofMajorUnits("0.01") }.sum()

        assertEquals(Money.ofMajorUnits("1.00"), total)
    }

    @Test
    fun `half-even rounding is applied beyond two decimals`() {
        // Banker's rounding: exact halves go to the nearest even cent, so a long run of
        // them does not accumulate an upward bias.
        assertEquals(Money(0L), Money.ofMajorUnits("0.005"))
        assertEquals(Money(2L), Money.ofMajorUnits("0.015"))
        assertEquals(Money(2L), Money.ofMajorUnits("0.025"))
    }

    @Test
    fun `ratio of zero total is zero rather than a divide by zero`() {
        assertEquals(0f, Money.ofMajorUnits("10.00").ratioOf(Money.ZERO), 0f)
    }

    @Test
    fun `subtraction can go negative and reports its sign`() {
        val result = Money.ofMajorUnits("5.00") - Money.ofMajorUnits("8.50")

        assertEquals(Money.ofMajorUnits("-3.50"), result)
        assertTrue(result.isNegative)
        assertFalse(result.isPositive)
        assertEquals(Money.ofMajorUnits("3.50"), result.absolute())
    }

    @Test
    fun `expenses are signed downwards for balance arithmetic`() {
        val expense = transaction(TransactionType.EXPENSE, "25.00")
        val income = transaction(TransactionType.INCOME, "100.00")

        assertEquals(Money.ofMajorUnits("75.00"), income.signedAmount + expense.signedAmount)
    }

    private fun transaction(type: TransactionType, amount: String) = Transaction(
        id = "id",
        userId = "user",
        accountId = "account",
        categoryId = "category",
        type = type,
        amount = Money.ofMajorUnits(amount),
        note = null,
        transactionDate = java.time.LocalDate.of(2026, 9, 3),
        createdAt = java.time.Instant.EPOCH,
        updatedAt = java.time.Instant.EPOCH,
    )
}

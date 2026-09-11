package com.finflow.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.absoluteValue

/**
 * A monetary amount held as integer minor units (cents, paisa, ...).
 *
 * Money is never represented as a floating point number anywhere in FinFlow: amounts are
 * `Long` in Room, `numeric(12,2)` in Postgres, and this value class in the domain. That removes
 * rounding drift from every sum, budget comparison and analytics aggregate.
 *
 * The currency is a property of the account/profile, not of the amount, so it is not
 * carried here; formatting is done by `CurrencyFormatter` in `core:common`.
 */
@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

    val isZero: Boolean get() = minorUnits == 0L
    val isPositive: Boolean get() = minorUnits > 0L
    val isNegative: Boolean get() = minorUnits < 0L

    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money = Money(minorUnits - other.minorUnits)

    operator fun times(factor: Int): Money = Money(minorUnits * factor)

    operator fun unaryMinus(): Money = Money(-minorUnits)

    fun absolute(): Money = Money(minorUnits.absoluteValue)

    /**
     * This amount as a fraction of [total], clamped to `0f` when [total] is zero so callers
     * can feed it straight into a progress indicator.
     */
    fun ratioOf(total: Money): Float =
        if (total.minorUnits == 0L) 0f else minorUnits.toFloat() / total.minorUnits.toFloat()

    fun toMajorUnits(fractionDigits: Int = DEFAULT_FRACTION_DIGITS): BigDecimal =
        BigDecimal.valueOf(minorUnits, fractionDigits)

    override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

    override fun toString(): String = "Money(${toMajorUnits()})"

    companion object {
        const val DEFAULT_FRACTION_DIGITS: Int = 2

        val ZERO: Money = Money(0L)

        fun ofMajorUnits(
            amount: BigDecimal,
            fractionDigits: Int = DEFAULT_FRACTION_DIGITS,
        ): Money = Money(amount.setScale(fractionDigits, RoundingMode.HALF_EVEN).unscaledValue().toLong())

        fun ofMajorUnits(
            amount: String,
            fractionDigits: Int = DEFAULT_FRACTION_DIGITS,
        ): Money = ofMajorUnits(BigDecimal(amount), fractionDigits)
    }
}

fun Iterable<Money>.sum(): Money = fold(Money.ZERO, Money::plus)

package com.finflow.core.common.formatter

import com.finflow.core.model.Money
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyFormatter @Inject constructor() {

    /** e.g. `$1,240.50`. Falls back to the plain amount for an unknown currency code. */
    fun format(
        money: Money,
        currencyCode: String,
        locale: Locale = Locale.getDefault(),
    ): String {
        val currency = currencyOrNull(currencyCode)
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            if (currency != null) {
                this.currency = currency
                maximumFractionDigits = Money.DEFAULT_FRACTION_DIGITS
                minimumFractionDigits = Money.DEFAULT_FRACTION_DIGITS
            }
        }
        return format.format(money.toMajorUnits())
    }

    /** Same as [format] but with an explicit sign, for transaction rows. */
    fun formatSigned(
        money: Money,
        currencyCode: String,
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatted = format(money.absolute(), currencyCode, locale)
        return when {
            money.isNegative -> "-$formatted"
            money.isPositive -> "+$formatted"
            else -> formatted
        }
    }

    fun symbol(currencyCode: String, locale: Locale = Locale.getDefault()): String =
        currencyOrNull(currencyCode)?.getSymbol(locale) ?: currencyCode

    private fun currencyOrNull(currencyCode: String): Currency? = try {
        Currency.getInstance(currencyCode)
    } catch (_: IllegalArgumentException) {
        null
    }
}

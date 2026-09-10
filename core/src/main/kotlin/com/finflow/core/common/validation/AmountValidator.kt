package com.finflow.core.common.validation

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Money
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmountValidator @Inject constructor() {

    /** Parses user input into [Money], rejecting blank, malformed and non-positive amounts. */
    fun validate(rawInput: String): AppResult<Money> {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return AppResult.Failure(AppError.Validation("Enter an amount"))
        }
        val parsed = trimmed.toBigDecimalOrNull()
            ?: return AppResult.Failure(AppError.Validation("Enter a valid amount"))
        if (parsed <= BigDecimal.ZERO) {
            return AppResult.Failure(AppError.Validation("Amount must be greater than zero"))
        }
        if (parsed > MAX_AMOUNT) {
            return AppResult.Failure(AppError.Validation("Amount is too large"))
        }
        return AppResult.Success(Money.ofMajorUnits(parsed))
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? = try {
        BigDecimal(this)
    } catch (_: NumberFormatException) {
        null
    }

    private companion object {
        val MAX_AMOUNT: BigDecimal = BigDecimal("1000000000")
    }
}

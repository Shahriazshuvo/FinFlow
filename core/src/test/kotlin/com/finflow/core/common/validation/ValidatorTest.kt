package com.finflow.core.common.validation

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountValidatorTest {

    private val validator = AmountValidator()

    @Test
    fun `valid amount is parsed into minor units`() {
        val result = validator.validate(" 12.34 ")

        assertEquals(AppResult.Success(Money(1234L)), result)
    }

    @Test
    fun `blank input is rejected`() {
        assertValidationFails(validator.validate("  "), "Enter an amount")
    }

    @Test
    fun `non numeric input is rejected`() {
        assertValidationFails(validator.validate("12a"), "Enter a valid amount")
    }

    @Test
    fun `zero and negative amounts are rejected to match the Postgres check constraint`() {
        assertValidationFails(validator.validate("0"), "Amount must be greater than zero")
        assertValidationFails(validator.validate("-5"), "Amount must be greater than zero")
    }

    private fun assertValidationFails(result: AppResult<*>, message: String) {
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.Validation(message), (result as AppResult.Failure).error)
    }
}

class AuthValidatorTest {

    private val validator = AuthValidator()

    @Test
    fun `email is trimmed and accepted`() {
        assertEquals(
            AppResult.Success("user@finflow.app"),
            validator.validateEmail("  user@finflow.app "),
        )
    }

    @Test
    fun `malformed email is rejected`() {
        assertTrue(validator.validateEmail("user@") is AppResult.Failure)
        assertTrue(validator.validateEmail("user.finflow.app") is AppResult.Failure)
    }

    @Test
    fun `short password is rejected`() {
        assertTrue(validator.validatePassword("abc123") is AppResult.Failure)
        assertTrue(validator.validatePassword("abcd1234") is AppResult.Success)
    }

    @Test
    fun `mismatched confirmation is rejected`() {
        assertTrue(
            validator.validatePasswordConfirmation("abcd1234", "abcd12345") is AppResult.Failure,
        )
    }
}

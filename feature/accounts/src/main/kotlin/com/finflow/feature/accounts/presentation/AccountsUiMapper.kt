package com.finflow.feature.accounts.presentation

import com.finflow.core.common.formatter.CurrencyFormatter
import com.finflow.core.model.AccountWithBalance
import com.finflow.core.model.AccountType
import com.finflow.core.model.Money
import javax.inject.Inject

/**
 * Domain models in, render-ready strings out. This is the only place currency formatting
 * happens for this feature, so a `Money` never reaches a composable.
 */
internal class AccountsUiMapper @Inject constructor(
    private val currencyFormatter: CurrencyFormatter,
) {

    fun toUiModels(
        balances: List<AccountWithBalance>,
        currencyCode: String,
    ): List<AccountUiModel> = balances.map { item ->
        AccountUiModel(
            id = item.account.id,
            name = item.account.name,
            type = item.account.type,
            typeLabel = item.account.type.label,
            balance = currencyFormatter.format(item.balance, currencyCode),
            openingBalanceInput = item.account.openingBalance.toMajorUnits().toPlainString(),
            isPendingSync = item.account.syncStatus.isPending,
        )
    }

    fun totalBalance(balances: List<AccountWithBalance>, currencyCode: String): String {
        val total = balances.fold(Money.ZERO) { acc, item -> acc + item.balance }
        return currencyFormatter.format(total, currencyCode)
    }
}

internal val AccountType.label: String
    get() = when (this) {
        AccountType.CASH -> "Cash"
        AccountType.BANK -> "Bank"
        AccountType.CARD -> "Card"
        AccountType.WALLET -> "Wallet"
    }
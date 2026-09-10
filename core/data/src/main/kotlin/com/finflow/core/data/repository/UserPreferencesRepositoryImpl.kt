package com.finflow.core.data.repository

import com.finflow.core.datastore.FinFlowPreferencesDataSource
import com.finflow.core.domain.repository.UserPreferencesRepository
import com.finflow.core.model.ThemePreference
import com.finflow.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin adapter over `core:datastore`. It narrows the whole preferences object down to the
 * individual fields callers actually observe, so a theme change does not re-emit to a
 * screen that only cares about the currency.
 */
@Singleton
internal class UserPreferencesRepositoryImpl @Inject constructor(
    private val preferences: FinFlowPreferencesDataSource,
) : UserPreferencesRepository {

    override val currencyCode: Flow<String> =
        preferences.userPreferences.map { it.currencyCode }.distinctUntilChanged()

    override val themePreference: Flow<ThemePreference> =
        preferences.userPreferences.map { it.themePreference }.distinctUntilChanged()

    override val onboardingCompleted: Flow<Boolean> =
        preferences.userPreferences.map { it.onboardingCompleted }.distinctUntilChanged()

    override val lastTransactionFilter: Flow<TransactionFilter> =
        preferences.userPreferences.map { it.lastTransactionFilter }.distinctUntilChanged()

    override suspend fun setCurrencyCode(currencyCode: String) =
        preferences.setCurrencyCode(currencyCode)

    override suspend fun setThemePreference(theme: ThemePreference) =
        preferences.setThemePreference(theme)

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        preferences.setOnboardingCompleted(completed)

    override suspend fun setLastTransactionFilter(filter: TransactionFilter) =
        preferences.setLastTransactionFilter(filter)
}

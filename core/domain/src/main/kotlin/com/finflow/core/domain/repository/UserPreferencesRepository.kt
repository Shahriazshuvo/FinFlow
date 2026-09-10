package com.finflow.core.domain.repository

import com.finflow.core.model.ThemePreference
import com.finflow.core.model.TransactionFilter
import kotlinx.coroutines.flow.Flow

/**
 * Device-local settings, as the domain sees them (APP_SPEC.md §16).
 *
 * This exists so a feature never reaches into `core:datastore` directly, for the same
 * reason it never reaches into Room: the storage mechanism is an implementation detail and
 * the wall that keeps it that way is the feature convention plugin's classpath.
 */
interface UserPreferencesRepository {
    val currencyCode: Flow<String>

    val themePreference: Flow<ThemePreference>

    val onboardingCompleted: Flow<Boolean>

    /** The transaction list's filter as the user last left it. */
    val lastTransactionFilter: Flow<TransactionFilter>

    suspend fun setCurrencyCode(currencyCode: String)

    suspend fun setThemePreference(theme: ThemePreference)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setLastTransactionFilter(filter: TransactionFilter)
}

package com.finflow.core.domain.repository

import com.finflow.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * Device-local settings, as the domain sees them.
 *
 * This exists so a feature never reaches into `core:datastore` directly, for the same
 * reason it never reaches into Room: the storage mechanism is an implementation detail and
 * the wall that keeps it that way is the feature convention plugin's classpath.
 */
interface UserPreferencesRepository {
    val currencyCode: Flow<String>

    val themePreference: Flow<ThemePreference>

    suspend fun setCurrencyCode(currencyCode: String)

    suspend fun setThemePreference(theme: ThemePreference)
}

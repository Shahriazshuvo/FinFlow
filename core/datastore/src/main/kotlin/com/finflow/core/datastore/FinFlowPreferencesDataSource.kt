package com.finflow.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.finflow.core.model.SyncTable
import com.finflow.core.model.ThemePreference
import com.finflow.core.model.TransactionFilter
import com.finflow.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App settings plus the per-table sync watermarks. Watermarks live here rather than in
 * Room because they are device state, not user data: wiping the database on sign-out must
 * not silently convince the app it is already up to date.
 */
@Singleton
class FinFlowPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            themePreference = preferences[Keys.THEME]
                ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
            currencyCode = preferences[Keys.CURRENCY] ?: UserPreferences.DEFAULT_CURRENCY,
            userId = preferences[Keys.USER_ID],
            onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
            lastTransactionFilter = TransactionFilter(
                type = preferences[Keys.FILTER_TYPE]
                    ?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() },
                categoryIds = preferences[Keys.FILTER_CATEGORY_IDS].orEmpty(),
                accountIds = preferences[Keys.FILTER_ACCOUNT_IDS].orEmpty(),
            ),
        )
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setCurrencyCode(currencyCode: String) {
        dataStore.edit { it[Keys.CURRENCY] = currencyCode }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    /**
     * Persists only the durable dimensions of [filter] — see [UserPreferences.lastTransactionFilter]
     * for why the query and the date window are dropped rather than stored.
     */
    suspend fun setLastTransactionFilter(filter: TransactionFilter) {
        dataStore.edit { preferences ->
            val type = filter.type
            if (type == null) {
                preferences.remove(Keys.FILTER_TYPE)
            } else {
                preferences[Keys.FILTER_TYPE] = type.name
            }
            preferences[Keys.FILTER_CATEGORY_IDS] = filter.categoryIds
            preferences[Keys.FILTER_ACCOUNT_IDS] = filter.accountIds
        }
    }

    suspend fun setUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId == null) preferences.remove(Keys.USER_ID) else preferences[Keys.USER_ID] = userId
        }
    }

    fun lastSyncedAt(table: SyncTable): Flow<Instant?> = dataStore.data.map { preferences ->
        preferences[Keys.watermark(table)]?.let(Instant::ofEpochMilli)
    }

    suspend fun setLastSyncedAt(table: SyncTable, instant: Instant) {
        dataStore.edit { it[Keys.watermark(table)] = instant.toEpochMilli() }
    }

    /** Most recent successful pull across all tables, for the "last synced" label. */
    val lastSyncedAt: Flow<Instant?> = dataStore.data.map { preferences ->
        SyncTable.entries
            .mapNotNull { preferences[Keys.watermark(it)] }
            .maxOrNull()
            ?.let(Instant::ofEpochMilli)
    }

    /** Called on sign-out so the next account starts from a clean slate. */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme_preference")
        val CURRENCY = stringPreferencesKey("currency_code")
        val USER_ID = stringPreferencesKey("user_id")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FILTER_TYPE = stringPreferencesKey("last_filter_type")
        val FILTER_CATEGORY_IDS = stringSetPreferencesKey("last_filter_category_ids")
        val FILTER_ACCOUNT_IDS = stringSetPreferencesKey("last_filter_account_ids")

        fun watermark(table: SyncTable) =
            longPreferencesKey("last_synced_at_${table.name.lowercase()}")
    }
}

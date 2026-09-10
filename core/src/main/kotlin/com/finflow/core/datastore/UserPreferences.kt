package com.finflow.core.datastore

import com.finflow.core.model.ThemePreference
import com.finflow.core.model.TransactionFilter

/**
 * Everything DataStore holds, as one value (APP_SPEC.md §16).
 *
 * Preferences only: no relational finance data lives here. The sync watermarks are the one
 * neighbour, and they are kept out of this object because they are per-table and read on a
 * different cadence — see `FinFlowPreferencesDataSource.lastSyncedAt`.
 */
data class UserPreferences(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val currencyCode: String = DEFAULT_CURRENCY,
    val userId: String? = null,
    val onboardingCompleted: Boolean = false,
    /**
     * The transaction list's filter, restored on next launch.
     *
     * Only the durable dimensions are persisted — type, categories, accounts. The search
     * query and the date window are deliberately *not*: restoring either one silently on a
     * cold start hides transactions the user did not ask to hide, which reads as data loss
     * rather than as a remembered preference.
     */
    val lastTransactionFilter: TransactionFilter = TransactionFilter.None,
) {
    companion object {
        const val DEFAULT_CURRENCY = "USD"
    }
}

package com.finflow.core.datastore

import com.finflow.core.model.ThemePreference

data class UserPreferences(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val currencyCode: String = DEFAULT_CURRENCY,
    val userId: String? = null,
) {
    companion object {
        const val DEFAULT_CURRENCY = "USD"
    }
}

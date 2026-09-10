package com.finflow.core.domain.usecase.preferences

import com.finflow.core.domain.repository.UserPreferencesRepository
import com.finflow.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * The currency every amount on screen is formatted in. A `UiMapper` pairs this with a
 * `Money` to produce the string a composable renders.
 */
class ObserveCurrencyCodeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.currencyCode
}

class ObserveThemePreferenceUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<ThemePreference> = repository.themePreference
}

class SetCurrencyCodeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(currencyCode: String) =
        repository.setCurrencyCode(currencyCode)
}

class SetThemePreferenceUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(theme: ThemePreference) =
        repository.setThemePreference(theme)
}

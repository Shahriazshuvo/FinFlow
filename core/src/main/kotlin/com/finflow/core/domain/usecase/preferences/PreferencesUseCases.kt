package com.finflow.core.domain.usecase.preferences

import com.finflow.core.domain.repository.UserPreferencesRepository
import com.finflow.core.model.ThemePreference
import com.finflow.core.model.TransactionFilter
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

/** Decides whether the app opens on onboarding or on the dashboard (APP_SPEC.md §16). */
class ObserveOnboardingCompletedUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.onboardingCompleted
}

class SetOnboardingCompletedUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(completed: Boolean = true) =
        repository.setOnboardingCompleted(completed)
}

/**
 * Restores the transaction list's filter across launches. Only the durable dimensions come
 * back — the search query and date window are not persisted, so a cold start never hides
 * rows the user did not choose to hide.
 */
class ObserveLastTransactionFilterUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<TransactionFilter> = repository.lastTransactionFilter
}

class SetLastTransactionFilterUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(filter: TransactionFilter) =
        repository.setLastTransactionFilter(filter)
}

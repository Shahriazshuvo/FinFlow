package com.finflow.core.ui.mvi

/** Immutable snapshot a screen renders. Implementations must be data classes. */
interface UiState

/** A user action or lifecycle signal sent from the UI to the ViewModel. */
interface UiIntent

/** A one-shot event: navigation, snackbar, share, permission request. */
interface UiEffect

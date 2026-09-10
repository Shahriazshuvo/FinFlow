package com.finflow.core.ui.error

import com.finflow.core.common.error.AppError

/**
 * The one place [AppError] becomes something a person can read (APP_SPEC.md §20).
 *
 * It lives in `core:ui` rather than in each feature so that the same failure does not get
 * three different wordings across three screens. Validation messages pass straight through
 * — they are produced by the validators in `core:common` and are already user-facing.
 *
 * [AppError.Conflict] deliberately has no good generic wording: a duplicate is only
 * meaningful next to the field that is duplicated, so screens should attach it to the input
 * with [conflictMessage] instead of raising it as a snackbar.
 */
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Validation -> message
    AppError.NetworkUnavailable -> "No connection. Your changes are saved and will sync later."
    AppError.Unauthorized -> "Your session has expired. Please sign in again."
    AppError.Forbidden -> "You do not have access to that."
    AppError.NotFound -> "That item no longer exists."
    is AppError.Conflict -> conflictMessage(field)
    is AppError.Unknown -> message ?: "Something went wrong. Please try again."
}

/** Wording for a uniqueness violation, by the form field it belongs to. */
fun conflictMessage(field: String?): String = when (field) {
    "name" -> "A category with this name already exists."
    "category" -> "This category already has a budget for that month."
    else -> "That already exists."
}
package com.finflow.core.ui.error

import com.finflow.core.common.error.AppError

/**
 * The one place [AppError] becomes something a person can read (APP_SPEC.md §15).
 *
 * It lives in `core:ui` rather than in each feature so that the same failure does not get
 * three different wordings across three screens. Validation messages pass straight through
 * — they are produced by the validators in `core:common` and are already user-facing.
 *
 * [AppError.Duplicate] deliberately has no good generic wording: a duplicate is only
 * meaningful next to the field that is duplicated, so screens should attach it to the input
 * with [duplicateMessage] instead of raising it as a snackbar.
 */
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Validation -> message
    AppError.Offline -> "You're offline. Changes will sync later."
    is AppError.Network -> "We couldn't reach the server. Please try again."
    AppError.Unauthorized -> "Your session has expired. Please sign in again."
    AppError.Forbidden -> "You do not have permission or your session expired."
    AppError.NotFound -> "That item no longer exists."
    is AppError.Duplicate -> duplicateMessage(field)
    is AppError.Database -> "We couldn't save that on this device. Please try again."
    is AppError.Unknown -> message ?: "Something went wrong. Please try again."
}

/** Wording for a uniqueness violation, by the form field it belongs to. */
fun duplicateMessage(field: String?): String = when (field) {
    "name" -> "A category with this name already exists."
    "category" -> "This category already has a budget for that month."
    else -> "That already exists."
}

package com.finflow.core.common.formatter

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateFormatter @Inject constructor() {

    fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern(DATE_PATTERN, locale).format(date)

    fun formatDayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> formatDate(date)
    }

    fun formatMonth(month: YearMonth, locale: Locale = Locale.getDefault()): String =
        DateTimeFormatter.ofPattern(MONTH_PATTERN, locale).format(month)

    fun formatDateTime(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): String = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, locale)
        .withZone(zoneId)
        .format(instant)

    private companion object {
        const val DATE_PATTERN = "d MMM yyyy"
        const val MONTH_PATTERN = "MMMM yyyy"
        const val DATE_TIME_PATTERN = "d MMM yyyy, HH:mm"
    }
}

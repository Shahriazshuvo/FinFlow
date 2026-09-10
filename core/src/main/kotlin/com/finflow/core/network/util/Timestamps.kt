package com.finflow.core.network.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Postgres `timestamptz` comes back as an ISO offset string (`2026-09-03T10:00:00.123+00:00`)
 * which `Instant.parse` cannot read, so offsets are parsed explicitly.
 */
internal fun String.toInstant(): Instant = OffsetDateTime.parse(this).toInstant()

internal fun String?.toInstantOrNull(): Instant? = this?.let { it.toInstant() }

internal fun Instant.toPostgresTimestamp(): String =
    DateTimeFormatter.ISO_INSTANT.format(this)

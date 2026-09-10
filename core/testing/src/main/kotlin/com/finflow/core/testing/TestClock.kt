package com.finflow.core.testing

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Time never advances on its own in a test.
 *
 * Sync and pending-write assertions compare `updatedAt` against an exact value, so a
 * repository under test must be handed a fixed clock — `Instant.now()` would make those
 * assertions flaky by microseconds. `CLAUDE.md` makes injecting `Clock` a hard rule; this is
 * the value to inject.
 */
val TEST_INSTANT: Instant = Instant.parse("2026-01-15T10:00:00Z")

val TEST_DATE: LocalDate = LocalDate.of(2026, 1, 15)

val TEST_CLOCK: Clock = Clock.fixed(TEST_INSTANT, ZoneOffset.UTC)

/** A clock pinned to an offset from [TEST_INSTANT], for "what happens after N seconds" cases. */
fun testClockAt(secondsFromStart: Long): Clock =
    Clock.fixed(TEST_INSTANT.plusSeconds(secondsFromStart), ZoneOffset.UTC)

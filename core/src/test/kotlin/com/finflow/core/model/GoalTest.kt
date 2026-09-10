package com.finflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GoalTest {

    @Test
    fun `progress is clamped and remaining never goes negative`() {
        val goal = goal(target = "1000.00", current = "1500.00")

        assertEquals(1f, goal.progress, 0f)
        assertEquals(Money.ZERO, goal.remainingAmount)
        assertTrue(goal.isReached)
    }

    @Test
    fun `partial progress is reported as a fraction`() {
        val goal = goal(target = "1000.00", current = "250.00")

        assertEquals(0.25f, goal.progress, 0.0001f)
        assertEquals(Money.ofMajorUnits("750.00"), goal.remainingAmount)
    }

    private fun goal(target: String, current: String) = Goal(
        id = "id",
        userId = "user",
        name = "Emergency fund",
        targetAmount = Money.ofMajorUnits(target),
        currentAmount = Money.ofMajorUnits(current),
        targetDate = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}

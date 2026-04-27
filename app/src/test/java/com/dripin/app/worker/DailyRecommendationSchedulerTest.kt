package com.dripin.app.worker

import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyRecommendationSchedulerTest {
    @Test
    fun schedules_daily_alarm_after_settings_change() {
        val triggerTimes = mutableListOf<Long>()
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { },
            scheduleAt = { triggerAtMillis -> triggerTimes += triggerAtMillis },
        )

        scheduler.scheduleNextRun(LocalTime.of(21, 0), ZoneId.of("Asia/Shanghai"))

        assertEquals(1, triggerTimes.size)
    }

    @Test
    fun cancels_existing_work_when_notifications_are_disabled() {
        var cancelled = false
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { cancelled = true },
            scheduleAt = { error("scheduleAt should not run when only cancelling") },
        )

        scheduler.cancel()

        assertTrue(cancelled)
    }

    @Test
    fun rolls_over_to_next_day_when_target_time_has_passed() {
        val triggerTimes = mutableListOf<Long>()
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { },
            scheduleAt = { triggerAtMillis -> triggerTimes += triggerAtMillis },
            clock = Clock.fixed(Instant.parse("2026-04-14T14:30:00Z"), ZoneOffset.UTC),
        )

        scheduler.scheduleNextRun(LocalTime.of(21, 0), ZoneId.of("Asia/Shanghai"))

        assertEquals(listOf(Instant.parse("2026-04-15T13:00:00Z").toEpochMilli()), triggerTimes)
    }
}

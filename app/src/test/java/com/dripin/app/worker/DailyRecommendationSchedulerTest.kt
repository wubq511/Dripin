package com.dripin.app.worker

import androidx.work.OneTimeWorkRequest
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
    fun schedules_unique_daily_work_after_settings_change() {
        val requests = mutableListOf<OneTimeWorkRequest>()
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { },
            enqueue = { requests += it },
        )

        scheduler.scheduleNextRun(LocalTime.of(21, 0), ZoneId.of("Asia/Shanghai"))

        assertEquals(1, requests.size)
    }

    @Test
    fun cancels_existing_work_when_notifications_are_disabled() {
        var cancelled = false
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { cancelled = true },
            enqueue = { error("enqueue should not run when only cancelling") },
        )

        scheduler.cancel()

        assertTrue(cancelled)
    }

    @Test
    fun rolls_over_to_next_day_when_target_time_has_passed() {
        val requests = mutableListOf<OneTimeWorkRequest>()
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { },
            enqueue = { requests += it },
            clock = Clock.fixed(Instant.parse("2026-04-14T14:30:00Z"), ZoneOffset.UTC),
        )

        scheduler.scheduleNextRun(LocalTime.of(21, 0), ZoneId.of("Asia/Shanghai"))

        assertEquals(1, requests.size)
        assertEquals(22 * 60 * 60 * 1000L + 30 * 60 * 1000L, requests.single().workSpec.initialDelay)
    }
}

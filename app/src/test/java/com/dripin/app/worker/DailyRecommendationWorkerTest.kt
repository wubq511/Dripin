package com.dripin.app.worker

import androidx.work.ListenableWorker
import com.dripin.app.core.model.NotificationDeliveryStatus
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.repository.NotificationDeliveryLog
import com.dripin.app.data.repository.RecommendationStore
import com.dripin.app.data.repository.TodayBatch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyRecommendationWorkerTest {
    @Test
    fun notification_capability_treats_missing_channel_as_usable() {
        val capability = NotificationCapabilitySnapshot(
            runtimePermissionGranted = true,
            appNotificationsEnabled = true,
            channelExists = false,
            channelBlocked = false,
        )

        assertTrue(capability.canDeliverNotifications)
        assertTrue(capability.primaryIssue() == null)
    }

    @Test
    fun notification_capability_reports_permission_before_other_issues() {
        val capability = NotificationCapabilitySnapshot(
            runtimePermissionGranted = false,
            appNotificationsEnabled = false,
            channelExists = true,
            channelBlocked = true,
        )

        assertEquals(
            NotificationCapabilityIssue.RuntimePermissionDenied,
            capability.primaryIssue(),
        )
    }

    @Test
    fun notification_capability_reports_blocked_channel_when_permission_is_granted() {
        val capability = NotificationCapabilitySnapshot(
            runtimePermissionGranted = true,
            appNotificationsEnabled = true,
            channelExists = true,
            channelBlocked = true,
        )

        assertFalse(capability.canDeliverNotifications)
        assertEquals(
            NotificationCapabilityIssue.ChannelBlocked,
            capability.primaryIssue(),
        )
    }

    @Test
    fun worker_skips_notification_when_no_batch_generated() = runBlocking {
        val notifier = FakeRecommendationNotifier()
        val scheduler = RecordingScheduler()
        val store = FakeRecommendationStore(batch = null)

        val result = runDailyRecommendationWork(
            preferences = UserPreferences(dailyPushTime = LocalTime.of(21, 0)),
            today = LocalDate.parse("2026-04-14"),
            zoneId = ZoneId.of("Asia/Shanghai"),
            recommendationStore = store,
            scheduler = scheduler,
            notifier = notifier,
        )

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(notifier.wasNotified)
        assertTrue(scheduler.scheduled)
    }

    @Test
    fun worker_notifies_when_batch_is_created() = runBlocking {
        val notifier = FakeRecommendationNotifier()
        val scheduler = RecordingScheduler()
        val store = FakeRecommendationStore(batch = TodayBatch(id = 1L, itemIds = listOf(2L, 1L)))

        runDailyRecommendationWork(
            preferences = UserPreferences(dailyPushTime = LocalTime.of(21, 0)),
            today = LocalDate.parse("2026-04-14"),
            zoneId = ZoneId.of("Asia/Shanghai"),
            recommendationStore = store,
            scheduler = scheduler,
            notifier = notifier,
        )

        assertTrue(notifier.wasNotified)
        assertEquals(2, notifier.lastCount)
        assertTrue(scheduler.scheduled)
    }

    @Test
    fun worker_records_posted_notification_delivery_result() = runBlocking {
        val notifier = FakeRecommendationNotifier()
        val scheduler = RecordingScheduler()
        val store = FakeRecommendationStore(batch = TodayBatch(id = 7L, itemIds = listOf(2L, 1L)))
        val today = LocalDate.parse("2026-04-14")

        runDailyRecommendationWork(
            preferences = UserPreferences(dailyPushTime = LocalTime.of(21, 0)),
            today = today,
            zoneId = ZoneId.of("Asia/Shanghai"),
            recommendationStore = store,
            scheduler = scheduler,
            notifier = notifier,
            clock = Clock.fixed(Instant.parse("2026-04-14T13:00:00Z"), ZoneOffset.UTC),
        )

        val log = store.recordedLogs.single()
        assertEquals(today, log.recommendedDate)
        assertEquals(Instant.parse("2026-04-14T13:00:00Z"), log.attemptedAt)
        assertEquals(2, log.itemCount)
        assertEquals(NotificationDeliveryStatus.POSTED, log.status)
        assertEquals(null, log.issue)
        assertEquals(7L, log.batchId)
    }

    @Test
    fun worker_records_blocked_notification_delivery_result() = runBlocking {
        val notifier = FakeRecommendationNotifier().apply {
            result = NotificationPostResult.Blocked(NotificationCapabilityIssue.RuntimePermissionDenied)
        }
        val scheduler = RecordingScheduler()
        val store = FakeRecommendationStore(batch = TodayBatch(id = 8L, itemIds = listOf(2L)))

        runDailyRecommendationWork(
            preferences = UserPreferences(dailyPushTime = LocalTime.of(21, 0)),
            today = LocalDate.parse("2026-04-15"),
            zoneId = ZoneId.of("Asia/Shanghai"),
            recommendationStore = store,
            scheduler = scheduler,
            notifier = notifier,
            clock = Clock.fixed(Instant.parse("2026-04-15T13:00:00Z"), ZoneOffset.UTC),
        )

        val log = store.recordedLogs.single()
        assertEquals(NotificationDeliveryStatus.BLOCKED, log.status)
        assertEquals(NotificationCapabilityIssue.RuntimePermissionDenied.name, log.issue)
        assertEquals(1, log.itemCount)
        assertEquals(8L, log.batchId)
    }
}

private class FakeRecommendationNotifier : RecommendationNotifier {
    var wasNotified = false
    var lastCount = 0
    var result: NotificationPostResult = NotificationPostResult.Posted

    override fun showDailyRecommendation(count: Int): NotificationPostResult {
        wasNotified = true
        lastCount = count
        return result
    }
}

private class RecordingScheduler : SchedulerController {
    var scheduled = false
    var cancelled = false

    override fun scheduleNextRun(time: LocalTime, zoneId: ZoneId) {
        scheduled = true
    }

    override fun cancel() {
        cancelled = true
    }
}

private class FakeRecommendationStore(
    private val batch: TodayBatch?,
) : RecommendationStore {
    val recordedLogs = mutableListOf<NotificationDeliveryLog>()

    override suspend fun generateTodayBatch(
        preferences: UserPreferences,
        today: LocalDate,
    ): TodayBatch? = batch

    override suspend fun getTodayBatch(today: LocalDate): TodayBatch? = null

    override suspend fun getTodayItems(today: LocalDate): List<SavedItemEntity> = emptyList()

    override fun observeTodayItems(today: LocalDate): Flow<List<SavedItemEntity>> = emptyFlow()

    override suspend fun markItemRead(itemId: Long) = Unit

    override suspend fun recordNotificationDelivery(log: NotificationDeliveryLog) {
        recordedLogs += log
    }

    override fun observeNotificationDeliveryLogs(limit: Int): Flow<List<NotificationDeliveryLog>> = emptyFlow()
}

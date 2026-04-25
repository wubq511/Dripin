package com.dripin.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.dripin.app.core.model.NotificationDeliveryStatus
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.data.repository.NotificationDeliveryLog
import com.dripin.app.data.preferences.userPreferencesDataStore
import com.dripin.app.data.repository.RecommendationRepository
import com.dripin.app.data.repository.RecommendationStore
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class DailyRecommendationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val zoneId = ZoneId.systemDefault()
        val preferencesRepository = UserPreferencesRepository(applicationContext.userPreferencesDataStore)
        val preferences = preferencesRepository.preferences.first()
        val database = AppDatabase.build(applicationContext)
        val recommendationStore = RecommendationRepository(
            savedItemDao = database.savedItemDao(),
            recommendationDao = database.dailyRecommendationDao(),
        )

        return runDailyRecommendationWork(
            preferences = preferences,
            today = LocalDate.now(zoneId),
            zoneId = zoneId,
            recommendationStore = recommendationStore,
            scheduler = DailyRecommendationScheduler.create(applicationContext),
            notifier = AndroidRecommendationNotifier(applicationContext),
            clock = Clock.systemUTC(),
        )
    }
}

suspend fun runDailyRecommendationWork(
    preferences: UserPreferences,
    today: LocalDate,
    zoneId: ZoneId,
    recommendationStore: RecommendationStore,
    scheduler: SchedulerController,
    notifier: RecommendationNotifier,
    clock: Clock = Clock.systemUTC(),
): ListenableWorker.Result {
    if (!preferences.notificationsEnabled) {
        recommendationStore.recordNotificationDelivery(
            NotificationDeliveryLog(
                recommendedDate = today,
                attemptedAt = clock.instant(),
                itemCount = 0,
                status = NotificationDeliveryStatus.SKIPPED,
                issue = "NOTIFICATIONS_DISABLED",
                batchId = null,
            ),
        )
        scheduler.cancel()
        return ListenableWorker.Result.success()
    }

    recommendationStore.reconcileTodayBatchPushState(today)
    val existingBatch = recommendationStore.getTodayBatch(today)
    val batch = existingBatch ?: recommendationStore.generateTodayBatch(
        preferences = preferences,
        today = today,
    )

    if (batch != null && !recommendationStore.hasPostedNotificationForBatch(batch.id)) {
        val unreadItemCount = recommendationStore.getTodayItems(today).count { !it.isRead }
        val attemptedAt = clock.instant()
        if (unreadItemCount > 0) {
            val notificationResult = notifier.showDailyRecommendation(unreadItemCount)
            if (notificationResult == NotificationPostResult.Posted) {
                recommendationStore.markBatchPosted(
                    batchId = batch.id,
                    deliveredAt = attemptedAt,
                )
            }
            recommendationStore.recordNotificationDelivery(
                NotificationDeliveryLog(
                    recommendedDate = today,
                    attemptedAt = attemptedAt,
                    itemCount = unreadItemCount,
                    status = notificationResult.toDeliveryStatus(),
                    issue = notificationResult.toDeliveryIssue(),
                    batchId = batch.id,
                ),
            )
        } else {
            recommendationStore.recordNotificationDelivery(
                NotificationDeliveryLog(
                    recommendedDate = today,
                    attemptedAt = attemptedAt,
                    itemCount = 0,
                    status = NotificationDeliveryStatus.SKIPPED,
                    issue = "NO_UNREAD_RECOMMENDATIONS",
                    batchId = batch.id,
                ),
            )
        }
    } else if (existingBatch == null && batch == null) {
        recommendationStore.recordNotificationDelivery(
            NotificationDeliveryLog(
                recommendedDate = today,
                attemptedAt = clock.instant(),
                itemCount = 0,
                status = NotificationDeliveryStatus.SKIPPED,
                issue = "NO_RECOMMENDATIONS",
                batchId = null,
            ),
        )
    }

    scheduler.scheduleNextRun(
        time = preferences.dailyPushTime,
        zoneId = zoneId,
    )
    return ListenableWorker.Result.success()
}

private fun NotificationPostResult.toDeliveryStatus(): NotificationDeliveryStatus = when (this) {
    NotificationPostResult.Posted -> NotificationDeliveryStatus.POSTED
    is NotificationPostResult.Blocked -> NotificationDeliveryStatus.BLOCKED
    is NotificationPostResult.Failed -> NotificationDeliveryStatus.FAILED
}

private fun NotificationPostResult.toDeliveryIssue(): String? = when (this) {
    NotificationPostResult.Posted -> null
    is NotificationPostResult.Blocked -> issue.name
    is NotificationPostResult.Failed -> reason
}

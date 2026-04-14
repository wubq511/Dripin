package com.dripin.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.data.preferences.userPreferencesDataStore
import com.dripin.app.data.repository.RecommendationRepository
import com.dripin.app.data.repository.RecommendationStore
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
): ListenableWorker.Result {
    if (!preferences.notificationsEnabled) {
        scheduler.cancel()
        return ListenableWorker.Result.success()
    }

    val existingBatch = recommendationStore.getTodayBatch(today)
    val batch = existingBatch ?: recommendationStore.generateTodayBatch(
        preferences = preferences,
        today = today,
    )

    if (existingBatch == null && batch != null && batch.itemIds.isNotEmpty()) {
        notifier.showDailyRecommendation(batch.itemIds.size)
    }

    scheduler.scheduleNextRun(
        time = preferences.dailyPushTime,
        zoneId = zoneId,
    )
    return ListenableWorker.Result.success()
}

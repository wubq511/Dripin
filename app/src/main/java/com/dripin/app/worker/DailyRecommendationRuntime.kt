package com.dripin.app.worker

import android.content.Context
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.data.preferences.userPreferencesDataStore
import com.dripin.app.data.repository.RecommendationRepository
import com.dripin.app.data.repository.SettingsRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

internal object DailyRecommendationRuntime {
    suspend fun runNow(context: Context) {
        val appContext = context.applicationContext
        val zoneId = ZoneId.systemDefault()
        val preferencesRepository = UserPreferencesRepository(appContext.userPreferencesDataStore)
        val database = AppDatabase.build(appContext)
        val recommendationStore = RecommendationRepository(
            savedItemDao = database.savedItemDao(),
            recommendationDao = database.dailyRecommendationDao(),
        )

        runDailyRecommendationWork(
            preferences = preferencesRepository.preferences.first(),
            today = LocalDate.now(zoneId),
            zoneId = zoneId,
            recommendationStore = recommendationStore,
            scheduler = DailyRecommendationScheduler.create(appContext),
            notifier = AndroidRecommendationNotifier(appContext),
            clock = Clock.systemUTC(),
        )
    }

    suspend fun syncSchedule(context: Context) {
        val appContext = context.applicationContext
        val preferencesRepository = UserPreferencesRepository(appContext.userPreferencesDataStore)
        SettingsRepository(
            preferencesRepository = preferencesRepository,
            scheduler = DailyRecommendationScheduler.create(appContext),
        ).syncSchedule()
    }
}

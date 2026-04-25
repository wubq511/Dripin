package com.dripin.app.data.repository

import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.worker.DailyRecommendationScheduler
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val preferencesRepository: UserPreferencesRepository,
    private val scheduler: DailyRecommendationScheduler,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    val preferences: Flow<UserPreferences> = preferencesRepository.preferences

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
        syncSchedule()
    }

    suspend fun setDailyPushTime(time: LocalTime) {
        preferencesRepository.updateNotificationTime(time)
        syncSchedule()
    }

    suspend fun setDailyPushCount(count: Int) {
        preferencesRepository.setDailyPushCount(count)
    }

    suspend fun setRepeatPushedUnreadItems(enabled: Boolean) {
        preferencesRepository.setRepeatUnreadPushedItems(enabled)
    }

    suspend fun setRecommendationSortMode(mode: RecommendationSortMode) {
        preferencesRepository.setRecommendationSortMode(mode)
    }

    suspend fun syncSchedule() {
        val current = preferences.first()
        if (current.notificationsEnabled) {
            scheduler.scheduleNextRun(
                time = current.dailyPushTime,
                zoneId = zoneId,
                catchUpIfDue = true,
            )
        } else {
            scheduler.cancel()
        }
    }
}

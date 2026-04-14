package com.dripin.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dripin.app.core.model.RecommendationSortMode
import java.io.IOException
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { stored ->
            UserPreferences(
                notificationsEnabled = stored[UserPreferenceKeys.NotificationsEnabled] ?: true,
                dailyPushTime = stored.readDailyPushTime(),
                dailyPushCount = (stored[UserPreferenceKeys.DailyPushCount] ?: 3).coerceIn(1, 10),
                repeatPushedUnreadItems = stored[UserPreferenceKeys.RepeatPushedUnreadItems] ?: true,
                recommendationSortMode = stored.readRecommendationSortMode(),
            )
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.NotificationsEnabled] = enabled
        }
    }

    suspend fun updateNotificationTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.DailyPushHour] = time.hour
            preferences[UserPreferenceKeys.DailyPushMinute] = time.minute
        }
    }

    suspend fun setDailyPushCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.DailyPushCount] = count.coerceIn(1, 10)
        }
    }

    suspend fun setRepeatUnreadPushedItems(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.RepeatPushedUnreadItems] = enabled
        }
    }

    suspend fun setRecommendationSortMode(mode: RecommendationSortMode) {
        dataStore.edit { preferences ->
            preferences[UserPreferenceKeys.RecommendationSortMode] = mode.name
        }
    }

    private fun Preferences.readDailyPushTime(): LocalTime {
        val hour = this[UserPreferenceKeys.DailyPushHour] ?: 21
        val minute = this[UserPreferenceKeys.DailyPushMinute] ?: 0
        return if (hour in 0..23 && minute in 0..59) {
            LocalTime.of(hour, minute)
        } else {
            LocalTime.of(21, 0)
        }
    }

    private fun Preferences.readRecommendationSortMode(): RecommendationSortMode {
        return this[UserPreferenceKeys.RecommendationSortMode]
            ?.let { storedValue -> runCatching { RecommendationSortMode.valueOf(storedValue) }.getOrNull() }
            ?: RecommendationSortMode.OLDEST_SAVED_FIRST
    }
}

internal object UserPreferenceKeys {
    val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
    val DailyPushHour = intPreferencesKey("daily_push_hour")
    val DailyPushMinute = intPreferencesKey("daily_push_minute")
    val DailyPushCount = intPreferencesKey("daily_push_count")
    val RepeatPushedUnreadItems = booleanPreferencesKey("repeat_pushed_unread_items")
    val RecommendationSortMode = stringPreferencesKey("recommendation_sort_mode")
}

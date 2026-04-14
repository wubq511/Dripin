package com.dripin.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import com.dripin.app.core.model.RecommendationSortMode
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UserPreferencesRepositoryTest {
    @Test
    fun saves_notification_time_and_repeat_rule() = runBlocking {
        val repository = UserPreferencesRepository(FakePreferencesDataStore())

        repository.updateNotificationTime(LocalTime.of(21, 0))
        repository.setRepeatUnreadPushedItems(false)

        val preferences = repository.preferences.first()
        assertEquals(LocalTime.of(21, 0), preferences.dailyPushTime)
        assertFalse(preferences.repeatPushedUnreadItems)
    }

    @Test
    fun falls_back_to_defaults_for_invalid_stored_values() = runBlocking {
        val repository = UserPreferencesRepository(
            FakePreferencesDataStore(
                preferencesOf(
                    UserPreferenceKeys.DailyPushHour to 99,
                    UserPreferenceKeys.DailyPushMinute to -2,
                    UserPreferenceKeys.RecommendationSortMode to "BROKEN",
                ),
            ),
        )

        val preferences = repository.preferences.first()
        assertEquals(LocalTime.of(21, 0), preferences.dailyPushTime)
        assertEquals(RecommendationSortMode.OLDEST_SAVED_FIRST, preferences.recommendationSortMode)
    }
}

private class FakePreferencesDataStore(
    initial: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

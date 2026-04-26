package com.dripin.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.work.OneTimeWorkRequest
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.worker.DailyRecommendationScheduler
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun syncSchedule_rolls_over_to_tomorrow_when_daily_time_has_already_passed() = runBlocking {
        val requests = mutableListOf<OneTimeWorkRequest>()
        val preferencesRepository = UserPreferencesRepository(FakePreferencesDataStore())
        val scheduler = DailyRecommendationScheduler(
            cancelExisting = { },
            enqueue = { requests += it },
            clock = Clock.fixed(Instant.parse("2026-04-14T07:00:00Z"), ZoneOffset.UTC),
        )
        val repository = SettingsRepository(
            preferencesRepository = preferencesRepository,
            scheduler = scheduler,
            zoneId = ZoneId.of("Asia/Shanghai"),
        )
        preferencesRepository.updateNotificationTime(LocalTime.of(12, 0))

        repository.syncSchedule()

        assertEquals(1, requests.size)
        assertEquals(21 * 60 * 60 * 1000L, requests.single().workSpec.initialDelay)
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

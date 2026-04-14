package com.dripin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.repository.SavedItemRepository
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.data.preferences.userPreferencesDataStore
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.worker.DailyRecommendationScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dripin.db",
        ).build()
    }

    private val repository by lazy {
        SavedItemRepository(
            savedItemDao = database.savedItemDao(),
            tagDao = database.tagDao(),
        )
    }

    private val settingsRepository by lazy {
        SettingsRepository(
            preferencesRepository = UserPreferencesRepository(applicationContext.userPreferencesDataStore),
            scheduler = DailyRecommendationScheduler.create(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            settingsRepository.syncSchedule()
        }
        setContent {
            DripinApp(
                repository = repository,
                settingsRepository = settingsRepository,
            )
        }
    }
}

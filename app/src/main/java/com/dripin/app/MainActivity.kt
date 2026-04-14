package com.dripin.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.data.preferences.userPreferencesDataStore
import com.dripin.app.data.repository.RecommendationRepository
import com.dripin.app.data.repository.SavedItemRepository
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.worker.DailyRecommendationScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val launchIntentState = mutableStateOf<Intent?>(null)

    private val database by lazy {
        AppDatabase.build(applicationContext)
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

    private val recommendationRepository by lazy {
        RecommendationRepository(
            savedItemDao = database.savedItemDao(),
            recommendationDao = database.dailyRecommendationDao(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchIntentState.value = intent
        lifecycleScope.launch {
            settingsRepository.syncSchedule()
        }
        setContent {
            DripinApp(
                repository = repository,
                settingsRepository = settingsRepository,
                recommendationRepository = recommendationRepository,
                launchIntent = launchIntentState.value,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchIntentState.value = intent
    }
}

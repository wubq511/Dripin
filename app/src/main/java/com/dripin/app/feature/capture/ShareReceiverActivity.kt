package com.dripin.app.feature.capture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.dripin4.ui.app.DripRuntimeApp
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.metadata.LinkMetadataFetcher
import com.dripin.app.data.preferences.UserPreferencesRepository
import com.dripin.app.data.preferences.userPreferencesDataStore
import com.dripin.app.data.repository.ContextPersistedImageStore
import com.dripin.app.data.repository.RecommendationRepository
import com.dripin.app.data.repository.SavedItemRepository
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.worker.DailyRecommendationScheduler
import okhttp3.OkHttpClient

class ShareReceiverActivity : ComponentActivity() {
    private val database by lazy {
        AppDatabase.build(applicationContext)
    }

    private val repository by lazy {
        SavedItemRepository(
            savedItemDao = database.savedItemDao(),
            tagDao = database.tagDao(),
            imageStore = ContextPersistedImageStore(applicationContext),
        )
    }

    private val metadataFetcher by lazy {
        LinkMetadataFetcher(OkHttpClient())
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
        val sourcePackage = callingPackage ?: referrer?.host

        val payload = ShareIntentParser.parse(
            intent = intent,
            sourcePackage = sourcePackage,
            sourceLabel = sourcePackage?.let(::resolveSourceLabel),
        )

        setContent {
            DripRuntimeApp(
                repository = repository,
                settingsRepository = settingsRepository,
                recommendationRepository = recommendationRepository,
                metadataFetcher = metadataFetcher,
                initialCapturePayload = payload,
                onCaptureFinished = { finishAndRemoveTask() },
            )
        }
    }

    private fun resolveSourceLabel(packageName: String): String? {
        return runCatching {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        }.getOrNull()
    }
}

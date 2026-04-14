package com.dripin.app

import android.content.Intent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.dripin.app.core.designsystem.theme.DripinTheme
import com.dripin.app.data.repository.RecommendationStore
import com.dripin.app.data.repository.SavedItemStore
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.navigation.DripinNavGraph

@Composable
fun DripinApp(
    repository: SavedItemStore,
    settingsRepository: SettingsRepository,
    recommendationRepository: RecommendationStore,
    launchIntent: Intent? = null,
) {
    DripinTheme {
        Surface {
            DripinNavGraph(
                repository = repository,
                settingsRepository = settingsRepository,
                recommendationRepository = recommendationRepository,
                launchIntent = launchIntent,
            )
        }
    }
}

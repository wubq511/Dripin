package com.dripin.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.dripin.app.core.designsystem.theme.DripinTheme
import com.dripin.app.data.repository.SavedItemStore
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.navigation.DripinNavGraph

@Composable
fun DripinApp(
    repository: SavedItemStore,
    settingsRepository: SettingsRepository,
) {
    DripinTheme {
        Surface {
            DripinNavGraph(
                repository = repository,
                settingsRepository = settingsRepository,
            )
        }
    }
}

package com.dripin.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.repository.SettingsRepository
import java.time.LocalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val dailyPushTime: LocalTime = LocalTime.of(21, 0),
    val dailyPushCount: Int = 3,
    val repeatPushedUnreadItems: Boolean = true,
    val recommendationSortMode: RecommendationSortMode = RecommendationSortMode.OLDEST_SAVED_FIRST,
)

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.preferences.collect { preferences ->
                _uiState.value = preferences.toUiState()
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        scope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }

    fun setDailyPushTime(time: LocalTime) {
        scope.launch {
            repository.setDailyPushTime(time)
        }
    }

    fun setDailyPushCount(count: Int) {
        scope.launch {
            repository.setDailyPushCount(count)
        }
    }

    fun setRepeatUnreadPushedItems(enabled: Boolean) {
        scope.launch {
            repository.setRepeatPushedUnreadItems(enabled)
        }
    }

    fun setRecommendationSortMode(mode: RecommendationSortMode) {
        scope.launch {
            repository.setRecommendationSortMode(mode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private fun UserPreferences.toUiState(): SettingsUiState = SettingsUiState(
        notificationsEnabled = notificationsEnabled,
        dailyPushTime = dailyPushTime,
        dailyPushCount = dailyPushCount,
        repeatPushedUnreadItems = repeatPushedUnreadItems,
        recommendationSortMode = recommendationSortMode,
    )
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        check(modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return SettingsViewModel(
            repository = repository,
            workerDispatcher = workerDispatcher,
        ) as T
    }
}

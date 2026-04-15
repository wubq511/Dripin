package com.dripin.app.feature.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.repository.RecommendationStore
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class TodayCardModel(
    val id: Long,
    val rank: Int,
    val title: String,
    val contentType: ContentType,
    val sourceLabel: String,
    val textPreview: String?,
    val note: String?,
    val rawUrl: String?,
    val imageUri: String?,
    val isRead: Boolean = false,
)

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val cards: List<TodayCardModel> = emptyList(),
)

class TodayViewModel(
    private val repository: RecommendationStore,
    private val preferencesProvider: suspend () -> UserPreferences = { UserPreferences() },
    private val today: LocalDate = LocalDate.now(Clock.systemDefaultZone().withZone(ZoneId.systemDefault())),
    private val currentTimeProvider: () -> LocalTime = { LocalTime.now() },
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _uiState = MutableStateFlow(TodayUiState(date = today))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            maybeGenerateBatchIfDue()
            repository.observeTodayItems(today).collect { items ->
                _uiState.value = TodayUiState(
                    date = today,
                    cards = items.mapIndexed { index, item -> item.toCardModel(rank = index + 1) },
                )
                if (items.isEmpty()) {
                    maybeGenerateBatchIfDue()
                }
            }
        }
    }

    fun markRead(itemId: Long) {
        scope.launch {
            repository.markItemRead(itemId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    fun refresh() {
        scope.launch {
            maybeGenerateBatchIfDue()
        }
    }

    private suspend fun maybeGenerateBatchIfDue() {
        if (repository.getTodayItems(today).isNotEmpty()) return
        val preferences = preferencesProvider()
        if (!preferences.notificationsEnabled) return
        if (currentTimeProvider().isBefore(preferences.dailyPushTime)) return
        repository.generateTodayBatch(
            preferences = preferences,
            today = today,
        )
    }

    private fun SavedItemEntity.toCardModel(rank: Int): TodayCardModel = TodayCardModel(
        id = id,
        rank = rank,
        title = title ?: "(无标题)",
        contentType = contentType,
        sourceLabel = sourcePlatform ?: sourceDomain ?: "未知来源",
        textPreview = textContent,
        note = note,
        rawUrl = rawUrl,
        imageUri = primaryImageUri,
        isRead = isRead,
    )
}

class TodayViewModelFactory(
    private val repository: RecommendationStore,
    private val preferencesProvider: suspend () -> UserPreferences = { UserPreferences() },
    private val today: LocalDate = LocalDate.now(),
    private val currentTimeProvider: () -> LocalTime = { LocalTime.now() },
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        check(modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return TodayViewModel(
            repository = repository,
            preferencesProvider = preferencesProvider,
            today = today,
            currentTimeProvider = currentTimeProvider,
            workerDispatcher = workerDispatcher,
        ) as T
    }
}

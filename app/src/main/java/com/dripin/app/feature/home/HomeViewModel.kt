package com.dripin.app.feature.home

import androidx.lifecycle.ViewModel
import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.HomeFilterState
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.repository.SavedItemStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val filterState: HomeFilterState = HomeFilterState(),
    val items: List<SavedItemEntity> = emptyList(),
)

class HomeViewModel(
    private val repository: SavedItemStore,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val filterState = MutableStateFlow(HomeFilterState())
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            combine(repository.observeItems(), filterState) { items, filters ->
                HomeUiState(
                    filterState = filters,
                    items = items.filterWith(filters),
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onContentTypeChanged(contentType: ContentType?) {
        filterState.update { it.copy(contentType = contentType) }
    }

    fun onReadFilterChanged(readFilter: ReadFilter) {
        filterState.update { it.copy(readFilter = readFilter) }
    }

    fun onPushFilterChanged(pushFilter: PushFilter) {
        filterState.update { it.copy(pushFilter = pushFilter) }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private fun List<SavedItemEntity>.filterWith(filterState: HomeFilterState): List<SavedItemEntity> {
        return filter { item ->
            val matchesContentType = filterState.contentType == null || item.contentType == filterState.contentType
            val matchesReadState = when (filterState.readFilter) {
                ReadFilter.ALL -> true
                ReadFilter.READ -> item.isRead
                ReadFilter.UNREAD -> !item.isRead
            }
            val matchesPushState = when (filterState.pushFilter) {
                PushFilter.ALL -> true
                PushFilter.PUSHED -> item.pushCount > 0
                PushFilter.UNPUSHED -> item.pushCount == 0
            }
            matchesContentType && matchesReadState && matchesPushState
        }
    }
}

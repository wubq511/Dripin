package com.dripin.app.feature.detail

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val item: SavedItemEntity? = null,
    val tags: List<String> = emptyList(),
    val titleDraft: String = "",
    val noteDraft: String = "",
    val tagDraft: String = "",
)

class DetailViewModel(
    private val itemId: Long,
    private val repository: SavedItemStore,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(titleDraft = value) }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(noteDraft = value) }
    }

    fun onTagDraftChanged(value: String) {
        _uiState.update { it.copy(tagDraft = value) }
    }

    fun addTag() {
        val tag = uiState.value.tagDraft.trim()
        if (tag.isBlank()) return

        _uiState.update {
            it.copy(
                tags = (it.tags + tag).distinctBy { value -> value.lowercase() },
                tagDraft = "",
            )
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(tags = state.tags.filterNot { it.equals(tag, ignoreCase = true) })
        }
    }

    fun markRead() {
        scope.launch {
            repository.setReadState(itemId, isRead = true)
            refresh()
        }
    }

    fun markUnread() {
        scope.launch {
            repository.setReadState(itemId, isRead = false)
            refresh()
        }
    }

    fun saveEdits() {
        scope.launch {
            repository.updateItemContent(
                itemId = itemId,
                title = uiState.value.titleDraft.ifBlank { null },
                note = uiState.value.noteDraft.ifBlank { null },
            )
            repository.replaceTags(itemId, uiState.value.tags)
            refresh()
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private fun refresh() {
        scope.launch {
            val item = repository.getItem(itemId) ?: return@launch
            val autoTagNames = listOfNotNull(
                item.sourceDomain,
                item.sourcePlatform,
                item.topicCategory,
            ).map(String::lowercase).toSet()
            val tags = repository.getTags(itemId)
                .filterNot { it.lowercase() in autoTagNames }
            _uiState.value = DetailUiState(
                item = item,
                tags = tags,
                titleDraft = item.title.orEmpty(),
                noteDraft = item.note.orEmpty(),
            )
        }
    }
}

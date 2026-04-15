package com.dripin.app.feature.capture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dripin.app.core.common.SourcePlatformClassifier
import com.dripin.app.core.common.TopicClassifier
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.metadata.LinkMetadataReader
import com.dripin.app.data.repository.LinkSaveRequest
import com.dripin.app.data.repository.SaveResult
import com.dripin.app.data.repository.SavedItemStore
import java.util.Locale
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class SaveItemViewModel(
    initialPayload: IncomingSharePayload,
    private val metadataFetcher: LinkMetadataReader,
    private val repository: SavedItemStore,
    private val sourcePlatformClassifier: SourcePlatformClassifier = SourcePlatformClassifier,
    private val topicClassifier: TopicClassifier = TopicClassifier,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private var hasUserEditedTitle = false

    private val _uiState = MutableStateFlow(initialState(initialPayload))
    val uiState: StateFlow<SaveItemUiState> = _uiState.asStateFlow()

    init {
        refreshDuplicateState()
        fetchMetadataIfNeeded()
    }

    fun onTitleChanged(value: String) {
        hasUserEditedTitle = true
        savedStateHandle["title"] = value
        _uiState.update { it.copy(title = value) }
    }

    fun onNoteChanged(value: String) {
        savedStateHandle["note"] = value
        _uiState.update { it.copy(note = value) }
    }

    fun onDraftTagChanged(value: String) {
        _uiState.update { it.copy(draftTag = value) }
    }

    fun addDraftTag() {
        val tag = uiState.value.draftTag.trim()
        if (tag.isBlank()) return

        _uiState.update { state ->
            state.copy(
                userTags = (state.userTags + tag).distinctBy { it.lowercase(Locale.getDefault()) },
                draftTag = "",
            )
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(userTags = state.userTags.filterNot { it.equals(tag, ignoreCase = true) })
        }
    }

    fun setContentType(contentType: ContentType) {
        if (uiState.value.contentType == contentType) return

        _uiState.update { current ->
            deriveState(current.copy(contentType = contentType))
                .copy(duplicateExistingItemId = null)
        }
        refreshDuplicateState()
        fetchMetadataIfNeeded()
    }

    fun onSharedUrlChanged(value: String) {
        if (!hasUserEditedTitle) {
            savedStateHandle["title"] = ""
        }
        _uiState.update { current ->
            deriveState(
                current.copy(
                    sharedUrl = value.trim().ifBlank { null },
                    title = if (hasUserEditedTitle) current.title else "",
                    duplicateExistingItemId = null,
                ),
            )
        }
        refreshDuplicateState()
        fetchMetadataIfNeeded()
    }

    fun onSharedTextChanged(value: String) {
        _uiState.update { current ->
            deriveState(current.copy(sharedText = value.ifBlank { null }))
        }
    }

    fun onSharedImageUriChanged(value: String?) {
        _uiState.update { current ->
            deriveState(current.copy(sharedImageUri = value))
        }
    }

    fun save() {
        if (_uiState.value.isSaving || !_uiState.value.canSave) return

        scope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            when (state.contentType) {
                ContentType.LINK -> {
                    val url = state.sharedUrl.orEmpty()
                    val result = repository.upsertSharedLink(
                        LinkSaveRequest(
                            rawUrl = url,
                            title = state.title.ifBlank { null },
                            textContent = state.sharedText,
                            note = state.note.ifBlank { null },
                            sourceAppPackage = state.sourceAppPackage,
                            sourceAppLabel = state.sourceAppLabel,
                            tags = state.userTags,
                        ),
                    )
                    val itemId = when (result) {
                        is SaveResult.Created -> result.itemId
                        is SaveResult.UpdatedExisting -> result.itemId
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            completedItemId = itemId,
                            duplicateExistingItemId = itemId.takeIf { result is SaveResult.UpdatedExisting }
                                ?: it.duplicateExistingItemId,
                        )
                    }
                }

                ContentType.TEXT -> {
                    val itemId = repository.saveText(
                        text = state.sharedText.orEmpty(),
                        title = state.title.ifBlank { null },
                        note = state.note.ifBlank { null },
                        sourceAppPackage = state.sourceAppPackage,
                        sourceAppLabel = state.sourceAppLabel,
                        tags = state.userTags,
                    )
                    _uiState.update { it.copy(isSaving = false, completedItemId = itemId) }
                }

                ContentType.IMAGE -> {
                    val itemId = repository.saveImage(
                        imageUri = state.sharedImageUri.orEmpty(),
                        title = state.title.ifBlank { null },
                        note = state.note.ifBlank { null },
                        sourceAppPackage = state.sourceAppPackage,
                        sourceAppLabel = state.sourceAppLabel,
                        tags = state.userTags,
                    )
                    _uiState.update { it.copy(isSaving = false, completedItemId = itemId) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.coroutineContext.cancel()
    }

    private fun initialState(payload: IncomingSharePayload): SaveItemUiState {
        return deriveState(
            SaveItemUiState(
                contentType = payload.contentType,
                isManualEntry = payload.isManualEntry,
                title = payload.initialTitle.orEmpty(),
                sharedUrl = payload.sharedUrl,
                sharedText = payload.sharedText,
                sharedImageUri = payload.sharedImageUri,
                sourceAppPackage = payload.sourceAppPackage,
                sourceAppLabel = payload.sourceAppLabel,
                note = "",
            ),
        )
    }

    private fun refreshDuplicateState() {
        val url = uiState.value.sharedUrl
        if (url.isNullOrBlank() || uiState.value.contentType != ContentType.LINK) {
            _uiState.update { it.copy(duplicateExistingItemId = null) }
            return
        }
        scope.launch {
            val existingItemId = repository.findExistingLinkId(url)
            _uiState.update { it.copy(duplicateExistingItemId = existingItemId) }
        }
    }

    private fun fetchMetadataIfNeeded() {
        val state = uiState.value
        if (state.contentType != ContentType.LINK) return
        if (state.sharedUrl.isNullOrBlank()) return
        if (state.title.isNotBlank()) return

        scope.launch {
            if (hasUserEditedTitle) return@launch
            val metadata = metadataFetcher.fetch(state.sharedUrl)
            val fetchedTitle = metadata?.title?.trim().orEmpty()
            val fallbackTitle = state.sourcePlatform?.let { "$it 内容" } ?: state.sourceDomain
            val resolvedTitle = fetchedTitle.ifBlank { fallbackTitle.orEmpty() }
            if (resolvedTitle.isBlank()) return@launch

            _uiState.update { current ->
                if (hasUserEditedTitle || current.title.isNotBlank()) {
                    current
                } else {
                    current.copy(title = resolvedTitle)
                }
            }
        }
    }

    private fun deriveState(state: SaveItemUiState): SaveItemUiState {
        val sourceDomain = state.sharedUrl?.toHttpUrlOrNull()?.host
        val sourcePlatform = sourcePlatformClassifier.classify(
            packageName = state.sourceAppPackage,
            domain = sourceDomain,
        )
        val topicCategory = topicClassifier.classify(
            title = state.title.takeIf(String::isNotBlank) ?: state.sharedText,
            domain = sourceDomain,
        )

        return state.copy(
            sourceDomain = sourceDomain,
            sourcePlatform = sourcePlatform,
            autoTags = listOfNotNull(sourceDomain, sourcePlatform, topicCategory).distinct(),
        )
    }

    class Factory(
        private val initialPayload: IncomingSharePayload,
        private val metadataFetcher: LinkMetadataReader,
        private val repository: SavedItemStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SaveItemViewModel::class.java)) {
                return SaveItemViewModel(
                    initialPayload = initialPayload,
                    metadataFetcher = metadataFetcher,
                    repository = repository,
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

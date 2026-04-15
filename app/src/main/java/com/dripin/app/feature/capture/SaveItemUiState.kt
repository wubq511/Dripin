package com.dripin.app.feature.capture

import com.dripin.app.core.model.ContentType

data class SaveItemUiState(
    val contentType: ContentType,
    val isManualEntry: Boolean = false,
    val title: String = "",
    val sharedUrl: String? = null,
    val sharedText: String? = null,
    val sharedImageUri: String? = null,
    val sourceAppPackage: String? = null,
    val sourceAppLabel: String? = null,
    val sourceDomain: String? = null,
    val sourcePlatform: String? = null,
    val note: String = "",
    val autoTags: List<String> = emptyList(),
    val userTags: List<String> = emptyList(),
    val draftTag: String = "",
    val isSaving: Boolean = false,
    val duplicateExistingItemId: Long? = null,
    val completedItemId: Long? = null,
) {
    val saveActionLabel: String
        get() = if (duplicateExistingItemId != null) "更新已有内容" else "保存"

    val canSave: Boolean
        get() = when (contentType) {
            ContentType.LINK -> !sharedUrl.isNullOrBlank()
            ContentType.TEXT -> !sharedText.isNullOrBlank()
            ContentType.IMAGE -> !sharedImageUri.isNullOrBlank()
        }
}

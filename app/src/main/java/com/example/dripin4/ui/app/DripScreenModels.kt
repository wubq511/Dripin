package com.example.dripin4.ui.app

import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import com.example.dripin4.ui.content.DripStrings

data class InboxScreenState(
    val contentFilters: List<InboxFilter>,
    val selectedContentFilter: InboxFilter,
    val readFilters: List<ReadFilter>,
    val selectedReadFilter: ReadFilter,
    val pushFilters: List<PushFilter>,
    val selectedPushFilter: PushFilter,
    val hasActiveFilters: Boolean,
    val items: List<InboxItemUi>,
)

data class TodayScreenState(
    val heroTimeText: String,
    val items: List<TodayItemUi>,
)

data class CaptureScreenState(
    val contentType: ContentType,
    val isManualEntry: Boolean,
    val sourceDetail: String,
    val title: String,
    val note: String,
    val selectedTags: Set<String>,
    val availableTags: List<String>,
    val draftTag: String,
    val autoTags: List<String>,
    val sharedUrl: String,
    val sharedText: String,
    val imageUris: List<String>,
    val canSave: Boolean,
    val isSaving: Boolean,
    val saveLabel: String,
    val duplicateMessage: String?,
)

data class DetailEditorState(
    val visible: Boolean,
    val titleDraft: String,
    val noteDraft: String,
    val rawUrlDraft: String,
    val textContentDraft: String,
    val imageUris: List<String>,
    val tags: List<String>,
    val tagDraft: String,
    val canSave: Boolean,
    val isRead: Boolean,
)

data class DetailScreenState(
    val item: InboxItemUi,
    val contentType: ContentType,
    val noteBody: String,
    val rawUrl: String?,
    val textContent: String?,
    val imageUris: List<String>,
    val primaryActionEnabled: Boolean,
    val editor: DetailEditorState,
)

data class SettingsScreenState(
    val dailyCount: Int,
    val groups: List<SettingsGroupUi>,
    val reminderSubtitle: String,
    val repeatUnreadEnabled: Boolean,
    val sortModeLabel: String,
)

internal fun DripAppState.toInboxScreenState(): InboxScreenState = InboxScreenState(
    contentFilters = inboxFilters,
    selectedContentFilter = selectedContentFilter,
    readFilters = inboxReadFilters,
    selectedReadFilter = selectedReadFilter,
    pushFilters = inboxPushFilters,
    selectedPushFilter = selectedPushFilter,
    hasActiveFilters = hasActiveInboxFilters(),
    items = filteredInboxItems(),
)

internal fun DripAppState.toTodayScreenState(): TodayScreenState = TodayScreenState(
    heroTimeText = "20:30",
    items = todayItems,
)

internal fun DripAppState.toCaptureScreenState(): CaptureScreenState = CaptureScreenState(
    contentType = ContentType.LINK,
    isManualEntry = false,
    sourceDetail = DripStrings.CaptureSourceDetail,
    title = captureTitle,
    note = captureNote,
    selectedTags = selectedCaptureTags,
    availableTags = captureTags,
    draftTag = "",
    autoTags = emptyList(),
    sharedUrl = "",
    sharedText = "",
    imageUris = emptyList(),
    canSave = true,
    isSaving = false,
    saveLabel = DripStrings.CaptureSave,
    duplicateMessage = null,
)

internal fun emptyDetailScreenState(): DetailScreenState = DetailScreenState(
    item = InboxItemUi(
        id = "",
        title = "",
        note = "",
        source = "",
        time = "",
        tag = "",
        kind = InboxKind.Article,
        contentType = ContentType.LINK,
        isRead = false,
        pushCount = 0,
    ),
    contentType = ContentType.LINK,
    noteBody = "",
    rawUrl = null,
    textContent = null,
    imageUris = emptyList(),
    primaryActionEnabled = false,
    editor = DetailEditorState(
        visible = false,
        titleDraft = "",
        noteDraft = "",
        rawUrlDraft = "",
        textContentDraft = "",
        imageUris = emptyList(),
        tags = emptyList(),
        tagDraft = "",
        canSave = false,
        isRead = false,
    ),
)

internal fun DripAppState.toDetailScreenState(): DetailScreenState {
    val detailItem = detailItemOrNull ?: return emptyDetailScreenState()

    return DetailScreenState(
        item = detailItem,
        contentType = when (detailItem.kind) {
            InboxKind.Article, InboxKind.Video -> ContentType.LINK
            InboxKind.Image -> ContentType.IMAGE
            InboxKind.Thread -> ContentType.TEXT
        },
        noteBody = detailItem.note,
        rawUrl = null,
        textContent = null,
        imageUris = emptyList(),
        primaryActionEnabled = false,
        editor = DetailEditorState(
            visible = false,
            titleDraft = detailItem.title,
            noteDraft = detailItem.note,
            rawUrlDraft = "",
            textContentDraft = "",
            imageUris = emptyList(),
            tags = listOf(detailItem.tag).filter(String::isNotBlank),
            tagDraft = "",
            canSave = true,
            isRead = detailItem.isRead,
        ),
    )
}

internal fun DripAppState.toSettingsScreenState(): SettingsScreenState = SettingsScreenState(
    dailyCount = dailyCount,
    groups = settingsGroups,
    reminderSubtitle = DripStrings.UtilityReminderSubtitle,
    repeatUnreadEnabled = true,
    sortModeLabel = "最早保存优先",
)

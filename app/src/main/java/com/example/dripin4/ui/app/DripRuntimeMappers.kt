package com.example.dripin4.ui.app

import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.NotificationDeliveryStatus
import com.dripin.app.data.repository.NotificationDeliveryLog
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.feature.capture.SaveItemUiState
import com.dripin.app.feature.detail.DetailUiState
import com.dripin.app.feature.recommendation.TodayCardModel
import com.dripin.app.feature.settings.SettingsUiState
import com.dripin.app.worker.NotificationCapabilitySnapshot
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

data class PrototypeSettingsUi(
    val dailyCount: Int,
    val reminderSubtitle: String,
    val groups: List<SettingsGroupUi>,
    val repeatUnreadEnabled: Boolean,
    val sortModeLabel: String,
)

internal enum class SystemNotificationAction {
    RequestPermission,
    OpenSettings,
}

internal fun SavedItemEntity.toInboxItemUi(
    now: Instant,
    zoneId: ZoneId,
): InboxItemUi {
    val kind = toInboxKind()
    val fallbackNote = when (contentType) {
        ContentType.LINK -> textContent?.takeIf(String::isNotBlank) ?: "保存了一条稍后回看的链接"
        ContentType.TEXT -> textContent?.takeIf(String::isNotBlank) ?: "保存了一段文字内容"
        ContentType.IMAGE -> "已保存 ${imageUris.size.coerceAtLeast(1)} 张图片"
    }

    return InboxItemUi(
        id = id.toString(),
        title = title.orEmpty().ifBlank { "(无标题)" },
        note = note?.takeIf(String::isNotBlank) ?: fallbackNote,
        source = sourcePlatform
            ?: sourceDomain
            ?: sourceAppLabel
            ?: "未知来源",
        time = createdAt.toRelativeTimeLabel(now = now, zoneId = zoneId),
        tag = topicCategory
            ?: sourcePlatform
            ?: contentType.toTagLabel(),
        kind = kind,
        contentType = contentType,
        isRead = isRead,
        pushCount = pushCount,
    )
}

internal fun TodayCardModel.toTodayItemUi(
    tag: String,
    savedDaysAgo: Long,
): TodayItemUi {
    val relativeDate = when {
        savedDaysAgo <= 0L -> "今天"
        savedDaysAgo == 1L -> "昨天"
        else -> "${savedDaysAgo} 天前"
    }
    return TodayItemUi(
        id = id.toString(),
        title = title,
        meta = "$relativeDate · $tag",
    )
}

internal fun SettingsUiState.toPrototypeSettingsUi(): PrototypeSettingsUi {
    val timeLabel = dailyPushTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    val groups = listOf(
        SettingsGroupUi(
            title = "推送节奏",
            rows = listOf(
                SettingsToggleUi(
                    key = SettingsToggleKey.DailyNotification,
                    label = "每日推送",
                    hint = "每天推送少量值得回看内容",
                    checked = notificationsEnabled,
                ),
            ),
        ),
        SettingsGroupUi(
            title = "收集行为",
            rows = listOf(
                SettingsToggleUi(
                    key = SettingsToggleKey.AutoTitle,
                    label = "重复推荐未读内容",
                    hint = if (repeatPushedUnreadItems) "已允许重复推送未读内容" else "仅推送未推送过的未读内容",
                    checked = repeatPushedUnreadItems,
                ),
                SettingsToggleUi(
                    key = SettingsToggleKey.SuggestCategory,
                    label = "推荐排序偏好",
                    hint = recommendationSortMode.label,
                    checked = recommendationSortMode == com.dripin.app.core.model.RecommendationSortMode.NEWEST_SAVED_FIRST,
                ),
            ),
        ),
    )

    return PrototypeSettingsUi(
        dailyCount = dailyPushCount,
        reminderSubtitle = "每天 $timeLabel",
        groups = groups,
        repeatUnreadEnabled = repeatPushedUnreadItems,
        sortModeLabel = recommendationSortMode.label,
    )
}

internal fun NotificationDeliveryLog.toNotificationHistoryUi(
    zoneId: ZoneId,
): NotificationHistoryUi {
    val attemptedAtLabel = attemptedAt.atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))

    return NotificationHistoryUi(
        id = id.toString(),
        statusLabel = when (status) {
            NotificationDeliveryStatus.POSTED -> "已发送"
            NotificationDeliveryStatus.BLOCKED -> "未发送"
            NotificationDeliveryStatus.FAILED -> "发送失败"
            NotificationDeliveryStatus.SKIPPED -> "已跳过"
        },
        attemptedAtLabel = attemptedAtLabel,
        countLabel = "${itemCount} 条内容",
        detail = issue.toNotificationHistoryDetail(),
        successful = status == NotificationDeliveryStatus.POSTED,
    )
}

internal fun SettingsUiState.toSettingsScreenState(): SettingsScreenState {
    val mapped = toPrototypeSettingsUi()
    return SettingsScreenState(
        dailyCount = mapped.dailyCount,
        groups = mapped.groups,
        reminderSubtitle = mapped.reminderSubtitle,
        repeatUnreadEnabled = mapped.repeatUnreadEnabled,
        sortModeLabel = mapped.sortModeLabel,
        systemNotification = notificationCapability.toSystemNotificationUi(),
    )
}

internal fun NotificationCapabilitySnapshot.resolveSystemNotificationAction(
    sdkInt: Int,
): SystemNotificationAction = if (sdkInt >= 33 && !runtimePermissionGranted) {
    SystemNotificationAction.RequestPermission
} else {
    SystemNotificationAction.OpenSettings
}

internal fun SaveItemUiState.toCaptureScreenState(
    availableTags: List<String>,
): CaptureScreenState {
    val dynamicSourceDetail = buildList {
        sourceAppLabel?.takeIf(String::isNotBlank)?.let(::add)
        sourcePlatform?.takeIf(String::isNotBlank)?.let(::add)
        sourceDomain?.takeIf(String::isNotBlank)?.let(::add)
        sharedUrl?.takeIf(String::isNotBlank)?.let(::add)
    }.distinct().joinToString(" · ")

    return CaptureScreenState(
        contentType = contentType,
        isManualEntry = isManualEntry,
        sourceDetail = dynamicSourceDetail.ifBlank { "手动整理一条稍后回看的内容" },
        title = title,
        note = note,
        selectedTags = userTags.toSet(),
        availableTags = (availableTags + userTags).distinct(),
        draftTag = draftTag,
        autoTags = autoTags,
        sharedUrl = sharedUrl.orEmpty(),
        sharedText = sharedText.orEmpty(),
        imageUris = sharedImageUris,
        canSave = canSave,
        isSaving = isSaving,
        saveLabel = saveActionLabel,
        duplicateMessage = duplicateExistingItemId?.let { "检测到相同链接，保存时会更新已有条目。" },
    )
}

internal fun DetailUiState.toDetailScreenState(
    now: Instant,
    zoneId: ZoneId,
): DetailScreenState {
    val item = checkNotNull(item) { "DetailUiState.item must not be null for screen mapping." }
    val displayItem = item.toInboxItemUi(now = now, zoneId = zoneId)

    return DetailScreenState(
        item = displayItem,
        contentType = item.contentType,
        noteBody = item.note.orEmpty(),
        rawUrl = item.rawUrl,
        textContent = item.textContent,
        imageUris = item.imageUris,
        primaryActionEnabled = !item.rawUrl.isNullOrBlank(),
        editor = DetailEditorState(
            visible = false,
            titleDraft = titleDraft,
            noteDraft = noteDraft,
            rawUrlDraft = rawUrlDraft,
            textContentDraft = textContentDraft,
            imageUris = imageUriDrafts,
            tags = tags,
            tagDraft = tagDraft,
            canSave = canSaveEdits,
            isRead = item.isRead,
        ),
    )
}

internal fun Instant.toRelativeTimeLabel(
    now: Instant,
    zoneId: ZoneId,
): String {
    val minutes = ChronoUnit.MINUTES.between(this, now)
    if (minutes < 60) {
        return "${minutes.coerceAtLeast(0)} 分钟前"
    }

    val hours = ChronoUnit.HOURS.between(this, now)
    if (hours < 24) {
        return "${hours.coerceAtLeast(1)} 小时前"
    }

    val days = ChronoUnit.DAYS.between(
        atZone(zoneId).toLocalDate(),
        now.atZone(zoneId).toLocalDate(),
    ).absoluteValue
    return when (days) {
        0L -> "${hours.coerceAtLeast(1)} 小时前"
        1L -> "昨天"
        else -> "${days} 天前"
    }
}

internal fun SavedItemEntity.toInboxKind(): InboxKind = when (contentType) {
    ContentType.LINK -> {
        val source = listOfNotNull(sourcePlatform, sourceDomain, rawUrl).joinToString(" ").lowercase()
        when {
            listOf("youtube", "youtu.be", "bilibili", "vimeo", "douyin", "tiktok").any(source::contains) -> InboxKind.Video
            listOf("podcast", "spotify", "music", "audio").any(source::contains) -> InboxKind.Thread
            else -> InboxKind.Article
        }
    }

    ContentType.TEXT -> InboxKind.Thread
    ContentType.IMAGE -> InboxKind.Image
}

private fun ContentType.toTagLabel(): String = when (this) {
    ContentType.LINK -> "链接"
    ContentType.TEXT -> "文字"
    ContentType.IMAGE -> "图片"
}

private fun NotificationCapabilitySnapshot.toSystemNotificationUi(): SystemNotificationUi {
    val systemNotificationEnabled = runtimePermissionGranted && appNotificationsEnabled
    val detail = when {
        !runtimePermissionGranted -> "系统还没授予通知权限，Dripin 现在发不出提醒。"
        !appNotificationsEnabled -> "系统里已经关闭 Dripin 通知，开启后才能收到提醒。"
        channelBlocked -> "系统通知已开启，但“每日推荐”频道被关闭，建议去通知设置里检查。"
        else -> "系统通知已开启，Dripin 可以正常发送提醒。"
    }

    return SystemNotificationUi(
        enabled = systemNotificationEnabled,
        statusLabel = if (systemNotificationEnabled) "已开启" else "未开启",
        detail = detail,
        actionLabel = if (systemNotificationEnabled) "管理通知" else "开启系统通知",
    )
}

private fun String?.toNotificationHistoryDetail(): String = when (this) {
    null -> "系统已接受通知"
    "RuntimePermissionDenied" -> "权限未授权"
    "AppNotificationsDisabled" -> "应用通知已关闭"
    "ChannelBlocked" -> "通知频道被关闭"
    "NOTIFICATIONS_DISABLED" -> "提醒功能已关闭"
    "NO_RECOMMENDATIONS" -> "本次没有可推送内容"
    else -> this
}

package com.example.dripin4.ui.app

import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.NotificationDeliveryStatus
import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.data.repository.NotificationDeliveryLog
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.feature.recommendation.TodayCardModel
import com.dripin.app.feature.settings.SettingsUiState
import com.dripin.app.worker.NotificationCapabilitySnapshot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DripRuntimeMappersTest {
    @Test
    fun savedLink_mapsIntoInboxCardContent() {
        val item = savedItem(
            id = 42L,
            contentType = ContentType.LINK,
            title = "OpenAI release",
            note = "Need to revisit",
            sourcePlatform = "GitHub",
            topicCategory = "设计",
            createdAt = Instant.parse("2026-04-21T12:00:00Z"),
        )

        val mapped = item.toInboxItemUi(
            now = Instant.parse("2026-04-21T12:08:00Z"),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals("42", mapped.id)
        assertEquals("OpenAI release", mapped.title)
        assertEquals("Need to revisit", mapped.note)
        assertEquals("GitHub", mapped.source)
        assertEquals("8 分钟前", mapped.time)
        assertEquals("设计", mapped.tag)
        assertEquals(InboxKind.Article, mapped.kind)
        assertEquals(ContentType.LINK, mapped.contentType)
        assertEquals(false, mapped.isRead)
        assertEquals(0, mapped.pushCount)
    }

    @Test
    fun savedImage_prefersImageKindAndFallbackNote() {
        val item = savedItem(
            id = 7L,
            contentType = ContentType.IMAGE,
            title = "Mood board",
            note = null,
            sourcePlatform = null,
            sourceDomain = null,
            topicCategory = null,
            createdAt = Instant.parse("2026-04-21T08:00:00Z"),
            imageUris = listOf("file:///images/7.jpg"),
        )

        val mapped = item.toInboxItemUi(
            now = Instant.parse("2026-04-21T10:00:00Z"),
            zoneId = ZoneOffset.UTC,
        )

        assertEquals(InboxKind.Image, mapped.kind)
        assertEquals("图片", mapped.tag)
        assertEquals("已保存 1 张图片", mapped.note)
        assertEquals("2 小时前", mapped.time)
        assertEquals(ContentType.IMAGE, mapped.contentType)
    }

    @Test
    fun todayCard_mapsIntoPrototypeTodayCardMeta() {
        val card = TodayCardModel(
            id = 99L,
            rank = 1,
            title = "A saved article",
            contentType = ContentType.LINK,
            sourceLabel = "GitHub",
            textPreview = null,
            note = "Important",
            rawUrl = "https://github.com/openai/openai",
            imageUri = null,
            isRead = false,
        )

        val mapped = card.toTodayItemUi(
            tag = "设计",
            savedDaysAgo = 2,
        )

        assertEquals("99", mapped.id)
        assertEquals("A saved article", mapped.title)
        assertEquals("2 天前 · 设计", mapped.meta)
    }

    @Test
    fun settingsUiState_mapsIntoSettingsScreenState() {
        val uiState = SettingsUiState(
            notificationsEnabled = false,
            dailyPushTime = LocalTime.of(20, 45),
            dailyPushCount = 5,
            repeatPushedUnreadItems = false,
            recommendationSortMode = RecommendationSortMode.NEWEST_SAVED_FIRST,
            notificationCapability = NotificationCapabilitySnapshot(
                runtimePermissionGranted = false,
                appNotificationsEnabled = true,
                channelExists = false,
                channelBlocked = false,
            ),
        )

        val mapped = uiState.toSettingsScreenState()

        assertEquals(5, mapped.dailyCount)
        assertEquals("每天 20:45", mapped.reminderSubtitle)
        assertEquals(false, mapped.groups.first().rows.first().checked)
        assertEquals(false, mapped.repeatUnreadEnabled)
        assertEquals("最新优先", mapped.sortModeLabel)
        assertEquals(false, mapped.systemNotification.enabled)
        assertEquals("未开启", mapped.systemNotification.statusLabel)
        assertEquals("系统还没授予通知权限，Dripin 现在发不出提醒。", mapped.systemNotification.detail)
        assertEquals("开启系统通知", mapped.systemNotification.actionLabel)
    }

    @Test
    fun notificationCapability_resolvesSystemNotificationActionByPlatform() {
        val runtimePermissionDenied = NotificationCapabilitySnapshot(
            runtimePermissionGranted = false,
            appNotificationsEnabled = true,
            channelExists = false,
            channelBlocked = false,
        )
        val alreadyEnabled = NotificationCapabilitySnapshot(
            runtimePermissionGranted = true,
            appNotificationsEnabled = true,
            channelExists = true,
            channelBlocked = false,
        )

        assertEquals(
            SystemNotificationAction.RequestPermission,
            runtimePermissionDenied.resolveSystemNotificationAction(sdkInt = 33),
        )
        assertEquals(
            SystemNotificationAction.OpenSettings,
            runtimePermissionDenied.resolveSystemNotificationAction(sdkInt = 32),
        )
        assertEquals(
            SystemNotificationAction.OpenSettings,
            alreadyEnabled.resolveSystemNotificationAction(sdkInt = 34),
        )
    }

    @Test
    fun notificationDeliveryLog_mapsIntoReadableHistoryRow() {
        val mapped = NotificationDeliveryLog(
            id = 12L,
            recommendedDate = LocalDate.parse("2026-04-15"),
            attemptedAt = Instant.parse("2026-04-15T13:00:00Z"),
            itemCount = 3,
            status = NotificationDeliveryStatus.BLOCKED,
            issue = "RuntimePermissionDenied",
            batchId = 7L,
        ).toNotificationHistoryUi(ZoneId.of("Asia/Shanghai"))

        assertEquals("12", mapped.id)
        assertEquals("未发送", mapped.statusLabel)
        assertEquals("04-15 21:00", mapped.attemptedAtLabel)
        assertEquals("3 条内容", mapped.countLabel)
        assertEquals("权限未授权", mapped.detail)
        assertEquals(false, mapped.successful)
    }

    @Test
    fun relativeTime_formatsYesterdayAndDaysAgo() {
        val createdAt = Instant.parse("2026-04-20T08:00:00Z")

        assertEquals(
            "昨天",
            createdAt.toRelativeTimeLabel(
                now = Instant.parse("2026-04-21T08:00:00Z"),
                zoneId = ZoneOffset.UTC,
            ),
        )
        assertEquals(
            "3 天前",
            createdAt.toRelativeTimeLabel(
                now = Instant.parse("2026-04-23T09:00:00Z"),
                zoneId = ZoneOffset.UTC,
            ),
        )
    }
}

private fun savedItem(
    id: Long,
    contentType: ContentType,
    title: String,
    note: String?,
    sourcePlatform: String? = null,
    sourceDomain: String? = "example.com",
    topicCategory: String? = "灵感",
    createdAt: Instant,
    imageUris: List<String> = emptyList(),
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = contentType,
    title = title,
    rawUrl = if (contentType == ContentType.LINK) "https://example.com/$id" else null,
    canonicalUrl = if (contentType == ContentType.LINK) "https://example.com/$id" else null,
    textContent = if (contentType == ContentType.TEXT) "Saved text content" else null,
    imageUris = imageUris,
    sourceAppPackage = "com.example.share",
    sourceAppLabel = "Example",
    sourcePlatform = sourcePlatform,
    sourceDomain = sourceDomain,
    topicCategory = topicCategory,
    note = note,
    createdAt = createdAt,
    updatedAt = createdAt,
    isRead = false,
    readAt = null,
    pushCount = 0,
    lastPushedAt = null,
    lastRecommendedDate = LocalDate.parse("2026-04-21"),
)

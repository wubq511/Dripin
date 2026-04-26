package com.example.dripin4.ui.app

import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class DripDestination {
    Inbox, Today, Capture, Detail, Settings
}

enum class InboxFilter {
    All, Link, Text, Image
}

enum class InboxKind {
    Article, Video, Image, Thread
}

data class InboxItemUi(
    val id: String,
    val title: String,
    val note: String,
    val source: String,
    val time: String,
    val tag: String,
    val kind: InboxKind,
    val contentType: ContentType,
    val isRead: Boolean,
    val pushCount: Int,
)

data class TodayItemUi(
    val id: String,
    val title: String,
    val meta: String
)

data class TodaySectionUi(
    val id: String,
    val label: String,
    val items: List<TodayItemUi>,
)

enum class SettingsToggleKey {
    DailyNotification,
    EveningWindow,
    AutoTitle,
    SuggestCategory
}

data class SettingsToggleUi(
    val key: SettingsToggleKey,
    val label: String,
    val hint: String,
    val checked: Boolean
)

data class SettingsGroupUi(
    val title: String,
    val rows: List<SettingsToggleUi>
)

@Stable
class DripAppState(initialDestination: DripDestination = DripDestination.Today) {
    internal var beforeNavigate: ((DripDestination) -> Unit)? = null

    var currentDestination by mutableStateOf(initialDestination)
        private set

    var selectedContentFilter by mutableStateOf(InboxFilter.All)
        private set

    var selectedReadFilter by mutableStateOf(ReadFilter.ALL)
        private set

    var selectedPushFilter by mutableStateOf(PushFilter.ALL)
        private set

    var selectedCaptureTags by mutableStateOf(setOf("设计"))
        private set

    var captureTitle by mutableStateOf("为未来注意力做整理，而不是即时消费")
        private set

    var captureNote by mutableStateOf("记录一点未来再看的理由…")
        private set

    var dailyCount by mutableIntStateOf(3)
        private set

    var deliveryDaily by mutableStateOf(true)
        private set

    var eveningWindow by mutableStateOf(true)
        private set

    var autoTitle by mutableStateOf(true)
        private set

    var suggestCategory by mutableStateOf(false)
        private set

    val inboxFilters = InboxFilter.entries.toList()
    val inboxReadFilters = ReadFilter.entries.toList()
    val inboxPushFilters = PushFilter.entries.toList()
    val captureTags = listOf("设计", "阅读", "研究", "灵感")

    val inboxItems = listOf(
        InboxItemUi(
            id = "inbox-1",
            title = "为未来注意力而设的安静系统，而不是内容过载。",
            note = "关注交互节奏和提醒语气。",
            source = "设计文章",
            time = "8 分钟前",
            tag = "设计",
            kind = InboxKind.Article,
            contentType = ContentType.LINK,
            isRead = false,
            pushCount = 0,
        ),
        InboxItemUi(
            id = "inbox-2",
            title = "更慢的信息流，更有意图的重新发现。",
            note = "和 Dripin 的轻收件流理念很一致。",
            source = "讨论串",
            time = "41 分钟前",
            tag = "系统",
            kind = InboxKind.Thread,
            contentType = ContentType.TEXT,
            isRead = true,
            pushCount = 1,
        ),
        InboxItemUi(
            id = "inbox-3",
            title = "高级移动产品里的形状语言。",
            note = "适合参考卡片层级和容器节奏。",
            source = "视频内容",
            time = "2 小时前",
            tag = "视觉",
            kind = InboxKind.Video,
            contentType = ContentType.LINK,
            isRead = false,
            pushCount = 2,
        ),
        InboxItemUi(
            id = "inbox-4",
            title = "适合轻量界面的安静图标包。",
            note = "可以启发后续组件的图标风格。",
            source = "图片素材",
            time = "5 小时前",
            tag = "素材",
            kind = InboxKind.Image,
            contentType = ContentType.IMAGE,
            isRead = true,
            pushCount = 0,
        )
    )

    val todaySections = listOf(
        TodaySectionUi(
            id = "sample-today",
            label = "今天",
            items = listOf(
                TodayItemUi(
                    id = "today-1",
                    title = "今晚 3 条待看内容",
                    meta = "低压力回看 · 20:30"
                ),
                TodayItemUi(
                    id = "today-2",
                    title = "一篇值得重看的文章",
                    meta = "2 天前 · 设计"
                ),
                TodayItemUi(
                    id = "today-3",
                    title = "一组简短的视觉参考",
                    meta = "昨天 · 灵感"
                )
            ),
        ),
    )

    private var selectedDetailId by mutableStateOf<String?>(null)

    val detailItemOrNull: InboxItemUi?
        get() = selectedDetailId?.let { detailId ->
            inboxItems.firstOrNull { it.id == detailId }
        }

    val settingsGroups: List<SettingsGroupUi>
        get() = listOf(
            SettingsGroupUi(
                title = "推送节奏",
                rows = listOf(
                    SettingsToggleUi(
                        key = SettingsToggleKey.DailyNotification,
                        label = "每日推送",
                        hint = "每天推送少量值得回看内容",
                        checked = deliveryDaily
                    ),
                    SettingsToggleUi(
                        key = SettingsToggleKey.EveningWindow,
                        label = "晚间专注时段",
                        hint = "20:30 · 柔和提醒模式",
                        checked = eveningWindow
                    )
                )
            ),
            SettingsGroupUi(
                title = "收集行为",
                rows = listOf(
                    SettingsToggleUi(
                        key = SettingsToggleKey.AutoTitle,
                        label = "重复推荐未读内容",
                        hint = "已允许重复推送未读内容",
                        checked = autoTitle
                    ),
                    SettingsToggleUi(
                        key = SettingsToggleKey.SuggestCategory,
                        label = "推荐排序偏好",
                        hint = if (suggestCategory) "最新优先" else "最早优先",
                        checked = suggestCategory
                    )
                )
            )
    )

    fun navigateTo(destination: DripDestination) {
        beforeNavigate?.invoke(destination)
        currentDestination = destination
    }

    fun openDetail(itemId: String) {
        selectedDetailId = itemId
        navigateTo(DripDestination.Detail)
    }

    fun toggleInboxContentFilter(filter: InboxFilter) {
        selectedContentFilter = filter
    }

    fun setInboxReadFilter(filter: ReadFilter) {
        selectedReadFilter = filter
    }

    fun setInboxPushFilter(filter: PushFilter) {
        selectedPushFilter = filter
    }

    fun hasActiveInboxFilters(): Boolean {
        return selectedContentFilter != InboxFilter.All ||
            selectedReadFilter != ReadFilter.ALL ||
            selectedPushFilter != PushFilter.ALL
    }

    fun filteredInboxItems(): List<InboxItemUi> {
        return inboxItems.filterWith(
            contentFilter = selectedContentFilter,
            readFilter = selectedReadFilter,
            pushFilter = selectedPushFilter,
        )
    }

    fun toggleCaptureTag(tag: String) {
        selectedCaptureTags = if (selectedCaptureTags.contains(tag)) {
            selectedCaptureTags - tag
        } else {
            selectedCaptureTags + tag
        }
    }

    fun updateCaptureTitle(newTitle: String) {
        captureTitle = newTitle
    }

    fun updateCaptureNote(newNote: String) {
        captureNote = newNote
    }

    fun onCaptureSave() {
        navigateTo(DripDestination.Inbox)
    }

    fun onCaptureCancel() {
        navigateTo(DripDestination.Inbox)
    }

    fun decreaseDailyCount() {
        dailyCount = (dailyCount - 1).coerceAtLeast(1)
    }

    fun increaseDailyCount() {
        dailyCount = (dailyCount + 1).coerceAtMost(7)
    }

    fun toggleSetting(key: SettingsToggleKey, checked: Boolean) {
        when (key) {
            SettingsToggleKey.DailyNotification -> deliveryDaily = checked
            SettingsToggleKey.EveningWindow -> eveningWindow = checked
            SettingsToggleKey.AutoTitle -> autoTitle = checked
            SettingsToggleKey.SuggestCategory -> suggestCategory = checked
        }
    }
}

internal fun List<InboxItemUi>.filterWith(
    contentFilter: InboxFilter,
    readFilter: ReadFilter,
    pushFilter: PushFilter,
): List<InboxItemUi> {
    return filter { item ->
        val matchesContentType = when (contentFilter) {
            InboxFilter.All -> true
            else -> item.contentType == contentFilter.toContentType()
        }
        val matchesReadState = when (readFilter) {
            ReadFilter.ALL -> true
            ReadFilter.READ -> item.isRead
            ReadFilter.UNREAD -> !item.isRead
        }
        val matchesPushState = when (pushFilter) {
            PushFilter.ALL -> true
            PushFilter.PUSHED -> item.pushCount > 0
            PushFilter.UNPUSHED -> item.pushCount == 0
        }
        matchesContentType && matchesReadState && matchesPushState
    }
}

internal fun InboxFilter.toContentType(): ContentType = when (this) {
    InboxFilter.All -> error("InboxFilter.All does not map to a concrete ContentType")
    InboxFilter.Link -> ContentType.LINK
    InboxFilter.Text -> ContentType.TEXT
    InboxFilter.Image -> ContentType.IMAGE
}

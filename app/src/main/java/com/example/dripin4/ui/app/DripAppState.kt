package com.example.dripin4.ui.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class DripDestination {
    Inbox, Today, Capture, Detail, Settings
}

enum class InboxFilter {
    All, Article, Video, Image, Thread, Audio
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
    val kind: InboxKind
)

data class TodayItemUi(
    val id: String,
    val title: String,
    val meta: String
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

    var selectedFilter by mutableStateOf(InboxFilter.All)
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

    val inboxFilters = InboxFilter.values().toList()
    val captureTags = listOf("设计", "阅读", "研究", "灵感")

    val inboxItems = listOf(
        InboxItemUi(
            id = "inbox-1",
            title = "为未来注意力而设的安静系统，而不是内容过载。",
            note = "关注交互节奏和提醒语气。",
            source = "设计文章",
            time = "8 分钟前",
            tag = "设计",
            kind = InboxKind.Article
        ),
        InboxItemUi(
            id = "inbox-2",
            title = "更慢的信息流，更有意图的重新发现。",
            note = "和 Dripin 的轻收件流理念很一致。",
            source = "讨论串",
            time = "41 分钟前",
            tag = "系统",
            kind = InboxKind.Thread
        ),
        InboxItemUi(
            id = "inbox-3",
            title = "高级移动产品里的形状语言。",
            note = "适合参考卡片层级和容器节奏。",
            source = "视频内容",
            time = "2 小时前",
            tag = "视觉",
            kind = InboxKind.Video
        ),
        InboxItemUi(
            id = "inbox-4",
            title = "适合轻量界面的安静图标包。",
            note = "可以启发后续组件的图标风格。",
            source = "图片素材",
            time = "5 小时前",
            tag = "素材",
            kind = InboxKind.Image
        )
    )

    val todayItems = listOf(
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
    )

    private var selectedDetailId by mutableStateOf(inboxItems.first().id)

    val detailItem: InboxItemUi
        get() = inboxItems.firstOrNull { it.id == selectedDetailId } ?: inboxItems.first()

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
                        label = "自动识别标题",
                        hint = "保持收集页轻量",
                        checked = autoTitle
                    ),
                    SettingsToggleUi(
                        key = SettingsToggleKey.SuggestCategory,
                        label = "智能建议分类",
                        hint = "仅在高置信度时触发",
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

    fun setInboxFilter(filter: InboxFilter) {
        selectedFilter = filter
    }

    fun filteredInboxItems(): List<InboxItemUi> {
        return when (selectedFilter) {
            InboxFilter.All -> inboxItems
            InboxFilter.Article -> inboxItems.filter { it.kind == InboxKind.Article }
            InboxFilter.Video -> inboxItems.filter { it.kind == InboxKind.Video }
            InboxFilter.Image -> inboxItems.filter { it.kind == InboxKind.Image }
            InboxFilter.Thread -> inboxItems.filter { it.kind == InboxKind.Thread }
            InboxFilter.Audio -> emptyList()
        }
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

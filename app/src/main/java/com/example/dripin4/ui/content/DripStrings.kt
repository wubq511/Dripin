package com.example.dripin4.ui.content

import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import com.example.dripin4.ui.app.InboxFilter
import com.example.dripin4.ui.app.InboxKind

object DripStrings {
    const val Brand = "Dripin"
    const val TopSubtitle = "未来收件流"

    const val InboxTitle = "收件流"
    const val InboxSubtitle = "轻雾整理你想稍后再看的内容"
    const val InboxEmptyTitle = "收件流还是空的"
    const val InboxEmptyBody = "先到收集页保存一点内容吧"
    const val InboxNoResultTitle = "没有匹配结果"
    const val InboxNoResultBody = "换个筛选试试，或回到全部"

    const val TodayTitle = "今日推送"
    const val TodaySubtitle = "今晚值得回看的轻量内容"
    const val TodayHeroChip = "精选"
    const val TodayHeroTitle = "慢一点看，更清晰"
    const val TodayHeroBody = "这里不是任务清单，而是你真正想回看的少量内容。"
    const val TodayItemLabel = "今日"
    const val TodayItemEffort = "低投入"

    const val CaptureTitle = "收集"
    const val CaptureSubtitle = "保存灵感，不打断当下"
    const val CaptureSource = "已识别为分享链接"
    const val CaptureSourceDetail = "来自设计灵感站的分享"
    const val CaptureTitleLabel = "标题"
    const val CaptureNoteLabel = "备注"
    const val CaptureNotePlaceholder = "写一点你保存它的理由"
    const val CaptureSave = "保存"
    const val CaptureCancel = "取消"

    const val DetailTitle = "内容详情"
    const val DetailSubtitle = "信息层级清晰，阅读更轻松"
    const val DetailSectionMeta = "元信息"
    const val DetailSectionPreview = "预览"
    const val DetailPrimaryAction = "打开原文"
    const val DetailSecondaryAction = "编辑笔记"

    const val SettingsTitle = "设置"
    const val SettingsSubtitle = "用柔和节奏控制提醒与偏好"
    const val SettingsDailyCount = "每日数量"
    const val SettingsLess = "少一点"
    const val SettingsMore = "多一点"
    const val SettingsDecrease = "减少"
    const val SettingsIncrease = "增加"

    const val GroupDelivery = "推送节奏"
    const val GroupCapture = "收集行为"
    const val GroupUtility = "工具项"

    const val UtilityReminderTitle = "提醒时间"
    const val UtilityReminderSubtitle = "每晚 20:30"
    const val UtilityArchiveTitle = "已保存归档"
    const val UtilityArchiveSubtitle = "回看旧内容，不制造压力"
    const val UtilityDismissedTitle = "清理已略过"
    const val UtilityDismissedSubtitle = "让今日保持轻量"

    const val InboxFilterAll = "全部"
    const val InboxFilterLink = "链接"
    const val InboxFilterText = "文字"
    const val InboxFilterImage = "图片"
    const val InboxReadFilterAll = "全部"
    const val InboxReadFilterRead = "已读"
    const val InboxReadFilterUnread = "未读"
    const val InboxPushFilterAll = "全部"
    const val InboxPushFilterPushed = "已推送"
    const val InboxPushFilterUnpushed = "未推送"

    const val InboxKindArticle = "文章"
    const val InboxKindVideo = "视频"
    const val InboxKindImage = "图片"
    const val InboxKindThread = "文字"

    const val CaptureTagDesign = "设计"
    const val CaptureTagReading = "阅读"
    const val CaptureTagResearch = "研究"
    const val CaptureTagInspiration = "灵感"

    fun inboxFilterLabel(filter: InboxFilter): String = when (filter) {
        InboxFilter.All -> InboxFilterAll
        InboxFilter.Link -> InboxFilterLink
        InboxFilter.Text -> InboxFilterText
        InboxFilter.Image -> InboxFilterImage
    }

    fun inboxReadFilterLabel(filter: ReadFilter): String = when (filter) {
        ReadFilter.ALL -> InboxReadFilterAll
        ReadFilter.READ -> InboxReadFilterRead
        ReadFilter.UNREAD -> InboxReadFilterUnread
    }

    fun inboxPushFilterLabel(filter: PushFilter): String = when (filter) {
        PushFilter.ALL -> InboxPushFilterAll
        PushFilter.PUSHED -> InboxPushFilterPushed
        PushFilter.UNPUSHED -> InboxPushFilterUnpushed
    }

    fun inboxKindLabel(kind: InboxKind): String = when (kind) {
        InboxKind.Article -> InboxKindArticle
        InboxKind.Video -> InboxKindVideo
        InboxKind.Image -> InboxKindImage
        InboxKind.Thread -> InboxKindThread
    }

    fun captureTagLabel(tag: String): String = when (tag) {
        "Design" -> CaptureTagDesign
        "Reading" -> CaptureTagReading
        "Research" -> CaptureTagResearch
        "Inspiration" -> CaptureTagInspiration
        else -> tag
    }
}

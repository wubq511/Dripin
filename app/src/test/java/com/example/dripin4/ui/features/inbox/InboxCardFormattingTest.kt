package com.example.dripin4.ui.features.inbox

import com.dripin.app.core.model.ContentType
import com.example.dripin4.ui.app.InboxItemUi
import com.example.dripin4.ui.app.InboxKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InboxCardFormattingTest {
    @Test
    fun contentContextLabel_prefersTextForTextItems() {
        val item = InboxItemUi(
            id = "item-0",
            title = "hi",
            note = "good",
            source = "未知来源",
            time = "0 分钟前",
            tag = "文字",
            kind = InboxKind.Thread,
            contentType = ContentType.TEXT,
            isRead = false,
            pushCount = 0,
        )

        assertEquals("文字", item.contentContextLabel())
    }

    @Test
    fun decorativeTagOrNull_hidesGenericContentLabels() {
        val item = InboxItemUi(
            id = "item-1",
            title = "hi",
            note = "test",
            source = "未知来源",
            time = "11 分钟前",
            tag = "文字",
            kind = InboxKind.Thread,
            contentType = ContentType.TEXT,
            isRead = false,
            pushCount = 0,
        )

        assertNull(item.decorativeTagOrNull())
    }

    @Test
    fun decorativeTagOrNull_keepsMeaningfulTopicLabels() {
        val item = InboxItemUi(
            id = "item-2",
            title = "为未来注意力而设的安静系统",
            note = "关注交互节奏",
            source = "设计文章",
            time = "8 分钟前",
            tag = "设计",
            kind = InboxKind.Article,
            contentType = ContentType.LINK,
            isRead = false,
            pushCount = 2,
        )

        assertEquals("设计", item.decorativeTagOrNull())
    }

    @Test
    fun pushStatusLabel_includesDeliveryCount() {
        val item = InboxItemUi(
            id = "item-3",
            title = "高级移动产品里的形状语言",
            note = "适合参考卡片层级",
            source = "视频内容",
            time = "2 小时前",
            tag = "视觉",
            kind = InboxKind.Video,
            contentType = ContentType.LINK,
            isRead = false,
            pushCount = 2,
        )

        assertEquals("已推送 2 次", item.pushStatusLabel())
    }

    @Test
    fun summaryTextOrNull_trimsBlankNotes() {
        val item = InboxItemUi(
            id = "item-4",
            title = "空摘要",
            note = "   ",
            source = "未知来源",
            time = "刚刚",
            tag = "",
            kind = InboxKind.Article,
            contentType = ContentType.LINK,
            isRead = false,
            pushCount = 0,
        )

        assertNull(item.summaryTextOrNull())
    }
}

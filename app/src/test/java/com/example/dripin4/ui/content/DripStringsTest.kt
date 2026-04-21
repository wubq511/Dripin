package com.example.dripin4.ui.content

import org.junit.Assert.assertEquals
import org.junit.Test

class DripStringsTest {
    @Test
    fun captureSaveLabel_isShortSaveText() {
        assertEquals("保存", DripStrings.CaptureSave)
    }

    @Test
    fun todayTitle_isTodayPush() {
        assertEquals("今日推送", DripStrings.TodayTitle)
    }

    @Test
    fun captureTitle_isCollect() {
        assertEquals("收集", DripStrings.CaptureTitle)
    }

    @Test
    fun settingsTitle_isSettings() {
        assertEquals("设置", DripStrings.SettingsTitle)
    }
}

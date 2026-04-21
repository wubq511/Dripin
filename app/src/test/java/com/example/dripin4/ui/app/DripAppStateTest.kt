package com.example.dripin4.ui.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DripAppStateTest {
    @Test
    fun deliveryToggleLabel_isDailyPush() {
        val appState = DripAppState()

        val deliveryLabel = appState.settingsGroups
            .first { it.title == "推送节奏" }
            .rows
            .first { it.key == SettingsToggleKey.DailyNotification }
            .label

        assertEquals("每日推送", deliveryLabel)
    }
}

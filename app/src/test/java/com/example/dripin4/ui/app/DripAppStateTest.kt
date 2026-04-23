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

    @Test
    fun searchInboxItems_matchesTitleNoteSourceAndTag() {
        val appState = DripAppState()

        assertEquals(
            listOf("inbox-3"),
            appState.inboxItems.searchInboxItems("视觉").map(InboxItemUi::id),
        )
        assertEquals(
            listOf("inbox-2"),
            appState.inboxItems.searchInboxItems("讨论串").map(InboxItemUi::id),
        )
        assertEquals(
            appState.inboxItems.map(InboxItemUi::id),
            appState.inboxItems.searchInboxItems("   ").map(InboxItemUi::id),
        )
    }
}

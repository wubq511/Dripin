package com.example.dripin4

import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.example.dripin4.ui.app.DripAppState
import com.example.dripin4.ui.app.DripDestination
import com.example.dripin4.ui.app.InboxFilter
import com.example.dripin4.ui.app.toDetailScreenState
import com.example.dripin4.ui.content.DripStrings

class PrototypeIntegrationTest {

    @Test
    fun dripAppState_defaultsToTodayDestination() {
        val state = DripAppState()
        assertEquals(DripDestination.Today, state.currentDestination)
    }

    @Test
    fun dripAppState_navigationAndInboxContentFilters_behaveAsExpected() {
        val state = DripAppState()
        state.navigateTo(DripDestination.Capture)
        assertEquals(DripDestination.Capture, state.currentDestination)

        assertEquals(
            listOf("全部", "链接", "文字", "图片"),
            state.inboxFilters.map(DripStrings::inboxFilterLabel),
        )
        assertTrue(state.filteredInboxItems().isNotEmpty())

        state.toggleInboxContentFilter(InboxFilter.Link)
        state.setInboxReadFilter(ReadFilter.UNREAD)
        state.setInboxPushFilter(PushFilter.UNPUSHED)

        val filteredItems = state.filteredInboxItems()
        assertEquals(1, filteredItems.size)
        assertEquals("inbox-1", filteredItems.single().id)
    }

    @Test
    fun dripAppState_defaultDetailScreenState_isEmptyUntilAnItemIsOpened() {
        val state = DripAppState()

        val detailState = state.toDetailScreenState()
        assertTrue(detailState.item.title.isBlank())
        assertTrue(detailState.noteBody.isBlank())
        assertTrue(detailState.imageUris.isEmpty())
        assertTrue(detailState.rawUrl.isNullOrBlank())
        assertTrue(detailState.textContent.isNullOrBlank())
        assertFalse(detailState.primaryActionEnabled)
    }
}

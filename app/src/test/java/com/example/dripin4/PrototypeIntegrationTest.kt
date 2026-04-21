package com.example.dripin4

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.example.dripin4.ui.app.DripAppState
import com.example.dripin4.ui.app.DripDestination
import com.example.dripin4.ui.app.InboxFilter

class PrototypeIntegrationTest {

    @Test
    fun dripAppState_defaultsToTodayDestination() {
        val state = DripAppState()
        assertEquals(DripDestination.Today, state.currentDestination)
    }

    @Test
    fun dripAppState_navigationAndNoResultFilter_behaveAsExpected() {
        val state = DripAppState()
        state.navigateTo(DripDestination.Capture)
        assertEquals(DripDestination.Capture, state.currentDestination)

        state.setInboxFilter(InboxFilter.Audio)
        assertTrue(state.filteredInboxItems().isEmpty())
    }
}

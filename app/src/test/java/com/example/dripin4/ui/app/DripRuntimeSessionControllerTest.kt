package com.example.dripin4.ui.app

import com.dripin.app.core.model.ContentType
import com.dripin.app.feature.capture.IncomingSharePayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DripRuntimeSessionControllerTest {
    @Test
    fun defaultController_startsWithReusableManualLinkCapture() {
        val controller = DripRuntimeSessionController()

        assertEquals(DripDestination.Today, controller.currentDestination)
        assertTrue(controller.capturePayload.isManualEntry)
        assertEquals(ContentType.LINK, controller.capturePayload.contentType)
        assertFalse(controller.shouldFinishCaptureHost)
    }

    @Test
    fun shareController_completionRequestsHostFinish() {
        val controller = DripRuntimeSessionController(
            initialCapturePayload = IncomingSharePayload(
                contentType = ContentType.TEXT,
                sharedText = "Shared note",
                isManualEntry = false,
            ),
        )

        assertEquals(DripDestination.Capture, controller.currentDestination)
        assertTrue(controller.shouldFinishCaptureHost)

        controller.onCaptureCompleted()

        assertEquals(DripDestination.Inbox, controller.currentDestination)
        assertTrue(controller.consumeFinishCaptureHost())
        assertFalse(controller.consumeFinishCaptureHost())
        assertTrue(controller.capturePayload.isManualEntry)
        assertEquals(ContentType.LINK, controller.capturePayload.contentType)
    }
}

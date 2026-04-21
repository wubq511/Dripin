package com.example.dripin4.ui.app

import com.dripin.app.core.model.ContentType
import com.dripin.app.feature.capture.IncomingSharePayload

class DripRuntimeSessionController(
    initialCapturePayload: IncomingSharePayload? = null,
) {
    var currentDestination: DripDestination = if (initialCapturePayload != null) {
        DripDestination.Capture
    } else {
        DripDestination.Today
    }
        private set

    var capturePayload: IncomingSharePayload = initialCapturePayload ?: manualCapturePayload()
        private set

    val shouldFinishCaptureHost: Boolean = initialCapturePayload != null

    private var pendingFinishCaptureHost = false

    fun navigateTo(destination: DripDestination) {
        if (destination == DripDestination.Capture && currentDestination != DripDestination.Capture) {
            ensureManualCapturePayload()
        }
        currentDestination = destination
    }

    fun openDetail() {
        currentDestination = DripDestination.Detail
    }

    fun onCaptureCancelled() {
        currentDestination = DripDestination.Inbox
        pendingFinishCaptureHost = shouldFinishCaptureHost
        ensureManualCapturePayload()
    }

    fun onCaptureCompleted() {
        currentDestination = DripDestination.Inbox
        pendingFinishCaptureHost = shouldFinishCaptureHost
        ensureManualCapturePayload()
    }

    fun replaceCapturePayload(payload: IncomingSharePayload) {
        capturePayload = payload
        currentDestination = DripDestination.Capture
    }

    fun consumeFinishCaptureHost(): Boolean {
        val shouldFinish = pendingFinishCaptureHost
        pendingFinishCaptureHost = false
        return shouldFinish
    }

    private fun ensureManualCapturePayload() {
        if (capturePayload.isManualEntry) return
        capturePayload = manualCapturePayload()
    }
}

private fun manualCapturePayload(): IncomingSharePayload = IncomingSharePayload(
    contentType = ContentType.LINK,
    isManualEntry = true,
)

package com.example.dripin4.ui.features

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.dripin4.ui.app.DripApp
import org.junit.Rule
import org.junit.Test

class CaptureUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureTagToggle_changesSelectedState() {
        rule.setContent { DripApp() }
        rule.onNodeWithTag("tab_capture").performClick()
        rule.onNodeWithTag("tag_研究").performClick()
        rule.onNodeWithTag("tag_研究_selected").assertIsDisplayed()
    }

    @Test
    fun captureScreen_showsSaveAndCancelControls() {
        rule.setContent { DripApp() }
        rule.onNodeWithTag("tab_capture").performClick()
        rule.onNodeWithTag("capture_cancel").assertIsDisplayed()
        rule.onNodeWithTag("capture_save").assertIsDisplayed()
    }
}

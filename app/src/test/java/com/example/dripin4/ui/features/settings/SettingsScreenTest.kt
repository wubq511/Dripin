package com.example.dripin4.ui.features.settings

import com.example.dripin4.ui.designsystem.DripTypography
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenTest {
    @Test
    fun systemNotificationStatusStyle_isSmallerThanPanelTitle() {
        assertTrue(
            systemNotificationStatusTextStyle().fontSize.value < DripTypography.bodyLarge.fontSize.value,
        )
    }
}

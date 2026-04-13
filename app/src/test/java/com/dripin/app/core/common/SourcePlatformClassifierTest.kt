package com.dripin.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class SourcePlatformClassifierTest {
    @Test
    fun maps_known_packages() {
        assertEquals("微信", SourcePlatformClassifier.classify("com.tencent.mm", null))
        assertEquals("GitHub", SourcePlatformClassifier.classify("com.github.android", null))
    }
}

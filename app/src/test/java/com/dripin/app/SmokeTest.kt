package com.dripin.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import dagger.hilt.android.HiltAndroidApp

class SmokeTest {
    @Test
    fun applicationClassExists() {
        assertEquals("com.dripin.app.DripinApplication", DripinApplication::class.qualifiedName)
    }

    @Test
    fun applicationClassIsAnnotatedForHilt() {
        assertTrue(DripinApplication::class.java.isAnnotationPresent(HiltAndroidApp::class.java))
    }

    @Test
    fun mainActivityClassExists() {
        assertEquals("com.dripin.app.MainActivity", MainActivity::class.qualifiedName)
    }
}

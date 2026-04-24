package com.dripin.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SmokeTest {
    @Test
    fun applicationClassExists() {
        assertEquals("com.dripin.app.DripinApplication", DripinApplication::class.qualifiedName)
    }

    @Test
    fun mainActivityClassExists() {
        assertEquals("com.dripin.app.MainActivity", MainActivity::class.qualifiedName)
    }
}

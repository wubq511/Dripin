package com.dripin.app

import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeTest {
    @Test
    fun applicationClassExists() {
        assertEquals("com.dripin.app.DripinApplication", DripinApplication::class.qualifiedName)
    }
}

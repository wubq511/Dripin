package com.dripin.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class TopicClassifierTest {
    @Test
    fun infers_development_from_title() {
        assertEquals("开发", TopicClassifier.classify("OpenAI repo release note", "github.com"))
    }
}

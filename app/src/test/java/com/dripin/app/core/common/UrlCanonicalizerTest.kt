package com.dripin.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlCanonicalizerTest {
    @Test
    fun strips_fragment_and_tracking_params() {
        val input = "https://GitHub.com/openai/openai?utm_source=x&tab=readme#top"

        assertEquals(
            "https://github.com/openai/openai?tab=readme",
            UrlCanonicalizer.canonicalize(input),
        )
    }

    @Test
    fun preserves_meaningful_query_params() {
        val input = "https://mp.weixin.qq.com/s?id=123&scene=1"

        assertEquals(
            "https://mp.weixin.qq.com/s?id=123&scene=1",
            UrlCanonicalizer.canonicalize(input),
        )
    }
}

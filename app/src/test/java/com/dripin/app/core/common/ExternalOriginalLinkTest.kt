package com.dripin.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalOriginalLinkTest {
    @Test
    fun x_status_link_prefers_native_status_deep_link_before_web_fallback() {
        val targets = ExternalOriginalLink.resolveTargets(
            "https://x.com/i/status/2047591003759247796",
        )

        assertEquals(
            listOf(
                ExternalOpenTarget(
                    url = "twitter://status?status_id=2047591003759247796",
                    packageName = "com.twitter.android",
                    className = "com.twitter.deeplink.implementation.UrlInterpreterActivity",
                ),
                ExternalOpenTarget(url = "https://x.com/i/status/2047591003759247796"),
            ),
            targets,
        )
    }

    @Test
    fun x_user_status_link_extracts_post_id_and_keeps_raw_web_fallback() {
        val targets = ExternalOriginalLink.resolveTargets(
            "https://twitter.com/openai/status/42?s=20",
        )

        assertEquals(
            listOf(
                ExternalOpenTarget(
                    url = "twitter://status?status_id=42",
                    packageName = "com.twitter.android",
                    className = "com.twitter.deeplink.implementation.UrlInterpreterActivity",
                ),
                ExternalOpenTarget(url = "https://twitter.com/openai/status/42?s=20"),
            ),
            targets,
        )
    }

    @Test
    fun regular_link_uses_original_url_only() {
        assertEquals(
            listOf(ExternalOpenTarget(url = "https://github.com/openai/openai")),
            ExternalOriginalLink.resolveTargets(" https://github.com/openai/openai "),
        )
    }
}

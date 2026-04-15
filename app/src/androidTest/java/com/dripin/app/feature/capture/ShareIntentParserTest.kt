package com.dripin.app.feature.capture

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dripin.app.core.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareIntentParserTest {
    @Test
    fun parses_plain_url_text_as_link() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://github.com/openai/openai")
        }

        val parsed = ShareIntentParser.parse(intent, "com.github.android", "GitHub")

        assertEquals(ContentType.LINK, parsed.contentType)
        assertEquals("https://github.com/openai/openai", parsed.sharedUrl)
    }

    @Test
    fun parses_mixed_share_text_into_link_and_prefilled_title() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "OpenAI 发布了新的模型说明\nhttps://x.com/openai/status/1234567890",
            )
        }

        val parsed = ShareIntentParser.parse(intent, "com.twitter.android", "X")

        assertEquals(ContentType.LINK, parsed.contentType)
        assertEquals("https://x.com/openai/status/1234567890", parsed.sharedUrl)
        assertEquals("OpenAI 发布了新的模型说明", parsed.initialTitle)
    }

    @Test
    fun parses_image_share_as_image_payload() {
        val uri = Uri.parse("content://media/external/images/media/1")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        val parsed = ShareIntentParser.parse(intent, "tv.danmaku.bili", "B站")

        assertEquals(ContentType.IMAGE, parsed.contentType)
        assertEquals(uri.toString(), parsed.sharedImageUri)
    }
}

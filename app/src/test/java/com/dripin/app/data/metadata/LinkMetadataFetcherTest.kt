package com.dripin.app.data.metadata

import kotlinx.coroutines.runBlocking
import java.net.UnknownServiceException
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkMetadataFetcherTest {
    @Test
    fun invalid_url_returns_null_without_starting_request() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 200,
                body = "<html></html>",
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch(
            "GPT Image 2 真的有点离谱 http://xhslink.com/o/5XyrHTmge6N 复制文本并前往【小红书】",
        )

        assertNull(metadata)
        assertTrue(interceptor.requests.isEmpty())
    }

    @Test
    fun cleartext_block_returns_null_without_crashing() = runBlocking {
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor { throw UnknownServiceException("CLEARTEXT communication not permitted") }
                .build(),
        )

        val metadata = fetcher.fetch("http://xhslink.com/o/5XyrHTmge6N")

        assertNull(metadata)
    }

    @Test
    fun xiaohongshu_short_link_uses_https_and_extracts_note_title() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 200,
                body = """
                    <html>
                      <head><title>小红书</title></head>
                      <body>
                        <div class="reds-text fs18 fw500 title">GPT Image 2 真的有点离谱 🤯</div>
                      </body>
                    </html>
                """.trimIndent(),
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch("http://xhslink.com/o/5XyrHTmge6N")

        assertEquals("GPT Image 2 真的有点离谱 🤯", metadata?.title)
        assertEquals("https", interceptor.requests.single().url.scheme)
        assertEquals("xhslink.com", interceptor.requests.single().url.host)
    }

    @Test
    fun xiaohongshu_site_title_is_not_used_as_note_title() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 200,
                body = "<html><head><title>小红书</title></head><body></body></html>",
            ),
            FakeResponse(
                code = 200,
                body = "<html><head><title>小红书</title></head><body></body></html>",
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch("http://xhslink.com/o/5XyrHTmge6N")

        assertNull(metadata)
        assertEquals(1, interceptor.requests.size)
    }

    @Test
    fun x_status_fetches_title_from_official_oembed() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 200,
                body = """
                    {
                      "author_name": "向阳乔木",
                      "html": "<blockquote class=\"twitter-tweet\"><p lang=\"zh\" dir=\"ltr\">DeepSeek大家发点实际测试案例吧，别只发新闻和参数了。<br><br>正在写一个网页小游戏，threejs用的还行。</p>&mdash; 向阳乔木 (@vista8) <a href=\"https://twitter.com/vista8/status/2047591003759247796\">April 24, 2026</a></blockquote>"
                    }
                """.trimIndent(),
                contentType = "application/json; charset=utf-8",
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch("https://x.com/i/status/2047591003759247796")

        assertEquals("向阳乔木：DeepSeek大家发点实际测试案例吧，别只发新闻和参数了。", metadata?.title)
        val requestUrl = interceptor.requests.single().url
        assertEquals("publish.x.com", requestUrl.host)
        assertEquals("/oembed", requestUrl.encodedPath)
        assertEquals("https://x.com/i/status/2047591003759247796", requestUrl.queryParameter("url"))
        assertEquals("1", requestUrl.queryParameter("omit_script"))
        assertEquals("1", requestUrl.queryParameter("hide_thread"))
    }

    @Test
    fun x_status_falls_back_to_generic_metadata_when_oembed_fails() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 404,
                body = "{}",
                contentType = "application/json; charset=utf-8",
            ),
            FakeResponse(
                code = 200,
                body = """
                    <html>
                      <head>
                        <meta property="og:title" content="Generic X Title">
                      </head>
                    </html>
                """.trimIndent(),
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch("https://x.com/i/status/42")

        assertEquals("Generic X Title", metadata?.title)
        assertEquals(listOf("publish.x.com", "x.com"), interceptor.requests.map { it.url.host })
    }

    @Test
    fun x_status_title_does_not_stop_inside_domain_names() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 200,
                body = """
                    {
                      "author_name": "Dripin",
                      "html": "<blockquote class=\"twitter-tweet\"><p>Try x.com links in Dripin. The fallback still works.</p></blockquote>"
                    }
                """.trimIndent(),
                contentType = "application/json; charset=utf-8",
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch("https://x.com/i/status/43")

        assertEquals("Dripin：Try x.com links in Dripin.", metadata?.title)
    }

    @Test
    fun regular_link_uses_open_graph_metadata_without_oembed() = runBlocking {
        val interceptor = RecordingInterceptor(
            FakeResponse(
                code = 200,
                body = """
                    <html>
                      <head>
                        <meta property="og:title" content="Regular Article">
                      </head>
                    </html>
                """.trimIndent(),
            ),
        )
        val fetcher = LinkMetadataFetcher(
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build(),
        )

        val metadata = fetcher.fetch("https://example.com/article")

        assertEquals("Regular Article", metadata?.title)
        assertEquals("example.com", interceptor.requests.single().url.host)
    }
}

private class RecordingInterceptor(
    vararg responses: FakeResponse,
) : Interceptor {
    private val responses = responses.toMutableList()
    val requests = mutableListOf<Request>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        requests += request
        val fakeResponse = if (responses.isNotEmpty()) {
            responses.removeAt(0)
        } else {
            error("No fake response configured for ${request.url}")
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(fakeResponse.code)
            .message(if (fakeResponse.code in 200..299) "OK" else "Error")
            .body(fakeResponse.body.toResponseBody(fakeResponse.contentType.toMediaType()))
            .build()
    }
}

private data class FakeResponse(
    val code: Int,
    val body: String,
    val contentType: String = "text/html; charset=utf-8",
)

package com.dripin.app.data.metadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

fun interface LinkMetadataReader {
    suspend fun fetch(url: String): LinkMetadata?
}

class LinkMetadataFetcher(
    private val client: OkHttpClient,
) : LinkMetadataReader {
    override suspend fun fetch(url: String): LinkMetadata? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0 Mobile Safari/537.36 Dripin/0.1",
            )
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null

            val html = response.body?.string().orEmpty()
            val document = Jsoup.parse(html)
            val title = document.selectFirst("meta[property=og:title]")?.attr("content")
                ?.ifBlank { null }
                ?: document.selectFirst("meta[name=twitter:title]")?.attr("content")
                ?.ifBlank { null }
                ?: document.selectFirst("meta[name=title]")?.attr("content")
                ?.ifBlank { null }
                ?: document.title().ifBlank { null }

            LinkMetadata(title = title)
        }
    }
}

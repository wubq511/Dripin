package com.dripin.app.data.metadata

import com.dripin.app.core.common.ExternalOriginalLink
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
        fetchXPostMetadata(url)?.let { return@withContext it }
        if (url.isXiaohongshuUrl()) {
            return@withContext fetchXiaohongshuMetadata(url)
        }
        fetchGenericMetadata(url)
    }

    private fun fetchXPostMetadata(url: String): LinkMetadata? {
        ExternalOriginalLink.extractXPostId(url) ?: return null

        val oembedUrl = "https://publish.x.com/oembed".toHttpUrl().newBuilder()
            .addQueryParameter("url", url)
            .addQueryParameter("omit_script", "1")
            .addQueryParameter("hide_thread", "1")
            .build()

        val request = metadataRequest(oembedUrl.toString())

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val payload = response.body.string()
                val authorName = extractJsonStringField(payload, "author_name")
                    ?.trim()
                    ?.ifBlank { null }
                val html = extractJsonStringField(payload, "html").orEmpty()
                val postText = extractXPostText(html)?.toTitleSnippet()
                val title = when {
                    authorName != null && postText != null -> "$authorName：$postText"
                    postText != null -> postText
                    authorName != null -> authorName
                    else -> null
                }

                return title?.let { LinkMetadata(title = it) }
            }
        } catch (_: IOException) {
            return null
        }
    }

    private fun fetchGenericMetadata(url: String): LinkMetadata? {
        val httpUrl = url.toHttpUrlOrNull() ?: return null
        val request = metadataRequest(httpUrl.toString())

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val html = response.body.string()
                val document = Jsoup.parse(html)
                val title = document.selectFirst("meta[property=og:title]")?.attr("content")
                    ?.ifBlank { null }
                    ?: document.selectFirst("meta[name=twitter:title]")?.attr("content")
                    ?.ifBlank { null }
                    ?: document.selectFirst("meta[name=title]")?.attr("content")
                    ?.ifBlank { null }
                    ?: document.title().ifBlank { null }

                return LinkMetadata(title = title)
            }
        } catch (_: IOException) {
            return null
        }
    }

    private fun fetchXiaohongshuMetadata(url: String): LinkMetadata? {
        val httpUrl = url.toHttpUrlOrNull() ?: return null
        if (!httpUrl.host.isXiaohongshuHost()) return null

        val metadataUrl = httpUrl.newBuilder()
            .scheme("https")
            .build()
        val request = metadataRequest(metadataUrl.toString())

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val document = Jsoup.parse(response.body.string())
                val title = document.extractXiaohongshuTitle() ?: return null
                return LinkMetadata(title = title)
            }
        } catch (_: IOException) {
            return null
        }
    }

    private fun metadataRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                UserAgent,
            )
            .build()
    }

    private fun extractXPostText(html: String): String? {
        if (html.isBlank()) return null

        val document = Jsoup.parseBodyFragment(html)
        return document.selectFirst("blockquote.twitter-tweet p")?.text()
            ?.ifBlank { null }
            ?: document.selectFirst("p")?.text()?.ifBlank { null }
    }

    private fun org.jsoup.nodes.Document.extractXiaohongshuTitle(): String? {
        return selectFirst("div.title")?.text()
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.ifBlank { null }
            ?.takeUnless { it == "小红书" }
    }

    private fun String.isXiaohongshuHost(): Boolean {
        val normalized = lowercase()
        return normalized == "xhslink.com" ||
            normalized == "www.xhslink.com" ||
            normalized == "xiaohongshu.com" ||
            normalized == "www.xiaohongshu.com"
    }

    private fun String.isXiaohongshuUrl(): Boolean {
        return toHttpUrlOrNull()?.host?.isXiaohongshuHost() == true
    }

    private fun extractJsonStringField(json: String, fieldName: String): String? {
        val quotedFieldName = "\"$fieldName\""
        var fieldIndex = json.indexOf(quotedFieldName)

        while (fieldIndex >= 0) {
            var cursor = fieldIndex + quotedFieldName.length
            cursor = json.skipWhitespace(cursor)
            if (cursor < json.length && json[cursor] == ':') {
                cursor = json.skipWhitespace(cursor + 1)
                if (cursor < json.length && json[cursor] == '"') {
                    return json.readJsonString(cursor)
                }
            }
            fieldIndex = json.indexOf(quotedFieldName, fieldIndex + quotedFieldName.length)
        }

        return null
    }

    private fun String.skipWhitespace(startIndex: Int): Int {
        var cursor = startIndex
        while (cursor < length && this[cursor].isWhitespace()) {
            cursor += 1
        }
        return cursor
    }

    private fun String.readJsonString(startQuoteIndex: Int): String? {
        if (getOrNull(startQuoteIndex) != '"') return null

        val output = StringBuilder()
        var cursor = startQuoteIndex + 1

        while (cursor < length) {
            when (val char = this[cursor]) {
                '"' -> return output.toString()
                '\\' -> {
                    val escaped = getOrNull(cursor + 1) ?: return null
                    when (escaped) {
                        '"', '\\', '/' -> output.append(escaped)
                        'b' -> output.append('\b')
                        'f' -> output.append('\u000C')
                        'n' -> output.append('\n')
                        'r' -> output.append('\r')
                        't' -> output.append('\t')
                        'u' -> {
                            val unicodeStart = cursor + 2
                            val unicodeEnd = unicodeStart + 4
                            if (unicodeEnd > length) return null
                            val codePoint = substring(unicodeStart, unicodeEnd).toIntOrNull(16)
                                ?: return null
                            output.append(codePoint.toChar())
                            cursor += 4
                        }
                        else -> return null
                    }
                    cursor += 2
                }
                else -> {
                    output.append(char)
                    cursor += 1
                }
            }
        }

        return null
    }

    private fun String.toTitleSnippet(): String {
        val compact = trim().replace(Regex("\\s+"), " ")
        val firstSentenceEnd = compact.firstSentenceEndIndex()
        val sentence = if (firstSentenceEnd != null) {
            compact.substring(0, firstSentenceEnd)
        } else {
            compact
        }

        return sentence.limitCodePoints(MaxXTitleTextLength)
    }

    private fun String.firstSentenceEndIndex(): Int? {
        for (index in indices) {
            when (this[index]) {
                '。', '！', '？', '!', '?' -> return index + 1
                '.' -> if (index == lastIndex || getOrNull(index + 1)?.isWhitespace() == true) {
                    return index + 1
                }
            }
        }

        return null
    }

    private fun String.limitCodePoints(maxLength: Int): String {
        if (codePointCount(0, length) <= maxLength) return this

        val endIndex = offsetByCodePoints(0, maxLength)
        return substring(0, endIndex).trimEnd() + "..."
    }

    private companion object {
        const val UserAgent =
            "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0 Mobile Safari/537.36 Dripin/0.1"
        const val MaxXTitleTextLength = 60
    }
}

package com.dripin.app.core.common

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class ExternalOpenTarget(
    val url: String,
    val packageName: String? = null,
    val className: String? = null,
)

object ExternalOriginalLink {
    private const val X_ANDROID_PACKAGE = "com.twitter.android"
    private const val X_STATUS_INTERPRETER_CLASS = "com.twitter.deeplink.implementation.UrlInterpreterActivity"
    private val xHosts = setOf(
        "x.com",
        "www.x.com",
        "twitter.com",
        "www.twitter.com",
        "m.twitter.com",
        "mobile.twitter.com",
    )
    private val xStatusSegments = setOf("status", "statuses")
    private val nativeXStatusRegex =
        Regex("""^twitter://status\?(?:status_id|id)=([0-9]{1,19})(?:[&#].*)?$""", RegexOption.IGNORE_CASE)

    fun resolveTargets(rawUrl: String): List<ExternalOpenTarget> {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return emptyList()

        val xPostId = extractXPostId(trimmed)
        return if (xPostId == null) {
            listOf(ExternalOpenTarget(url = trimmed))
        } else {
            listOf(
                ExternalOpenTarget(
                    url = "twitter://status?status_id=$xPostId",
                    packageName = X_ANDROID_PACKAGE,
                    className = X_STATUS_INTERPRETER_CLASS,
                ),
                ExternalOpenTarget(url = trimmed),
            ).distinct()
        }
    }

    internal fun extractXPostId(rawUrl: String): String? {
        nativeXStatusRegex.find(rawUrl.trim())?.let { match ->
            return match.groupValues[1]
        }

        val url = rawUrl.trim().toHttpUrlOrNull() ?: return null
        if (url.host.lowercase() !in xHosts) return null

        val segments = url.pathSegments
        val statusIndex = segments.indexOfFirst { it.lowercase() in xStatusSegments }
        if (statusIndex == -1 || statusIndex + 1 >= segments.size) return null

        return segments[statusIndex + 1]
            .takeIf { it.length in 1..19 && it.all(Char::isDigit) }
    }
}

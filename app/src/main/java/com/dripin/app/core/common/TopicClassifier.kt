package com.dripin.app.core.common

object TopicClassifier {
    fun classify(
        title: String?,
        domain: String?,
    ): String? {
        val haystack = listOfNotNull(title, domain)
            .joinToString(separator = " ")
            .lowercase()

        return when {
            haystack.isBlank() -> null
            listOf("repo", "release", "issue", "pull request", "github", "android", "kotlin").any(haystack::contains) -> "开发"
            listOf("video", "bilibili", "douyin", "youtube").any(haystack::contains) -> "视频"
            listOf("blog", "article", "newsletter", "medium", "post").any(haystack::contains) -> "文章"
            else -> null
        }
    }
}

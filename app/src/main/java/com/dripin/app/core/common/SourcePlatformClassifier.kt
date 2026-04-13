package com.dripin.app.core.common

object SourcePlatformClassifier {
    fun classify(
        packageName: String?,
        domain: String?,
    ): String? {
        val normalizedPackage = packageName?.trim()?.lowercase()
        val normalizedDomain = domain?.trim()?.lowercase()

        return when {
            normalizedPackage == "com.tencent.mm" -> "微信"
            normalizedPackage == "com.github.android" -> "GitHub"
            normalizedPackage == "tv.danmaku.bili" -> "B站"
            normalizedPackage == "com.ss.android.ugc.aweme" -> "抖音"
            normalizedPackage == "com.twitter.android" -> "X"
            normalizedDomain == "github.com" -> "GitHub"
            normalizedDomain == "x.com" || normalizedDomain == "twitter.com" -> "X"
            normalizedDomain == "mp.weixin.qq.com" -> "微信"
            normalizedDomain == "bilibili.com" || normalizedDomain == "www.bilibili.com" -> "B站"
            normalizedDomain == "douyin.com" || normalizedDomain == "www.douyin.com" -> "抖音"
            else -> null
        }
    }
}

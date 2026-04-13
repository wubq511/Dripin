package com.dripin.app.core.common

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object UrlCanonicalizer {
    private val ignoredPrefixes = listOf("utm_")
    private val ignoredNames = setOf("spm", "si")

    fun canonicalize(raw: String): String {
        val trimmed = raw.trim()
        val url = trimmed.toHttpUrlOrNull() ?: return trimmed
        val keptQueryParams = buildList {
            repeat(url.querySize) { index ->
                val name = url.queryParameterName(index)
                if (ignoredNames.contains(name) || ignoredPrefixes.any(name::startsWith)) {
                    return@repeat
                }

                add(name to url.queryParameterValue(index))
            }
        }.sortedWith(compareBy({ it.first }, { it.second.orEmpty() }))

        val builder = url.newBuilder()
            .fragment(null)
            .encodedQuery(null)

        keptQueryParams.forEach { (name, value) ->
            builder.addQueryParameter(name, value)
        }

        return builder.build().toString()
    }
}

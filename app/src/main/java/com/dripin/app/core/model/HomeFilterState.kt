package com.dripin.app.core.model

enum class ReadFilter {
    ALL,
    READ,
    UNREAD,
}

enum class PushFilter {
    ALL,
    PUSHED,
    UNPUSHED,
}

data class HomeFilterState(
    val contentType: ContentType? = null,
    val readFilter: ReadFilter = ReadFilter.ALL,
    val pushFilter: PushFilter = PushFilter.ALL,
)

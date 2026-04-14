package com.dripin.app.data.preferences

import com.dripin.app.core.model.RecommendationSortMode
import java.time.LocalTime

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val dailyPushTime: LocalTime = LocalTime.of(21, 0),
    val dailyPushCount: Int = 3,
    val repeatPushedUnreadItems: Boolean = true,
    val recommendationSortMode: RecommendationSortMode = RecommendationSortMode.OLDEST_SAVED_FIRST,
)

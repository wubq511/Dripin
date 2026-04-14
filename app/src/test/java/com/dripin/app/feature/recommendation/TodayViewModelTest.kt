package com.dripin.app.feature.recommendation

import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.repository.RecommendationStore
import com.dripin.app.data.repository.TodayBatch
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayViewModelTest {
    @Test
    fun exposes_today_cards_in_rank_order() = runBlocking {
        val viewModel = TodayViewModel(
            repository = FakeRecommendationStore(
                items = listOf(
                    fakeTodayItem(id = 2L, title = "Second"),
                    fakeTodayItem(id = 1L, title = "First"),
                ),
            ),
            today = LocalDate.parse("2026-04-14"),
            workerDispatcher = Dispatchers.Unconfined,
        )

        delay(10)

        assertEquals(listOf(2L, 1L), viewModel.uiState.value.cards.map { it.id })
    }
}

private class FakeRecommendationStore(
    private val items: List<SavedItemEntity>,
) : RecommendationStore {
    override suspend fun generateTodayBatch(
        preferences: UserPreferences,
        today: LocalDate,
    ): TodayBatch? = null

    override suspend fun getTodayBatch(today: LocalDate): TodayBatch? = null

    override suspend fun getTodayItems(today: LocalDate): List<SavedItemEntity> = items

    override suspend fun markItemRead(itemId: Long) = Unit
}

private fun fakeTodayItem(
    id: Long,
    title: String,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.LINK,
    title = title,
    rawUrl = "https://example.com/$id",
    canonicalUrl = "https://example.com/$id",
    textContent = null,
    imageUri = null,
    sourceAppPackage = "com.example.share",
    sourceAppLabel = "Example",
    sourcePlatform = "Example",
    sourceDomain = "example.com",
    topicCategory = "文章",
    note = null,
    createdAt = Instant.parse("2026-04-14T08:00:00Z"),
    updatedAt = Instant.parse("2026-04-14T08:00:00Z"),
    isRead = false,
    readAt = null,
    pushCount = 1,
    lastPushedAt = Instant.parse("2026-04-14T08:00:00Z"),
    lastRecommendedDate = LocalDate.parse("2026-04-14"),
)

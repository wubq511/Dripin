package com.dripin.app.feature.recommendation

import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.repository.NotificationDeliveryLog
import com.dripin.app.data.preferences.UserPreferences
import com.dripin.app.data.repository.RecommendationStore
import com.dripin.app.data.repository.TodayBatch
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayViewModelTest {
    @Test
    fun exposes_unread_pushed_cards_in_rank_order() = runBlocking {
        val viewModel = TodayViewModel(
            repository = FlowBackedRecommendationStore(
                initialItems = listOf(
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

    @Test
    fun filters_out_read_or_unpushed_cards() = runBlocking {
        val viewModel = TodayViewModel(
            repository = FlowBackedRecommendationStore(
                initialItems = listOf(
                    fakeTodayItem(id = 1L, title = "Unread pushed"),
                    fakeTodayItem(id = 2L, title = "Already read", isRead = true),
                    fakeTodayItem(id = 3L, title = "Never pushed", pushCount = 0, lastPushedAt = null),
                ),
            ),
            today = LocalDate.parse("2026-04-14"),
            workerDispatcher = Dispatchers.Unconfined,
        )

        delay(10)

        assertEquals(listOf(1L), viewModel.uiState.value.cards.map { it.id })
    }

    @Test
    fun flow_updates_today_cards_when_repository_emits_later() = runBlocking {
        val repository = MutableRecommendationStore()
        val viewModel = TodayViewModel(
            repository = repository,
            today = LocalDate.parse("2026-04-14"),
            workerDispatcher = Dispatchers.Unconfined,
        )

        delay(10)
        assertTrue(viewModel.uiState.value.cards.isEmpty())

        repository.emit(
            listOf(
                fakeTodayItem(id = 3L, title = "Third"),
                fakeTodayItem(id = 4L, title = "Fourth"),
            ),
        )
        delay(10)

        assertEquals(listOf(3L, 4L), viewModel.uiState.value.cards.map { it.id })
    }

    @Test
    fun does_not_generate_missing_batch_from_today_page() = runBlocking {
        val repository = FlowBackedRecommendationStore(initialItems = emptyList()).apply {
            generatedItems = listOf(fakeTodayItem(id = 8L, title = "Recovered"))
        }
        val viewModel = TodayViewModel(
            repository = repository,
            today = LocalDate.parse("2026-04-14"),
            workerDispatcher = Dispatchers.Unconfined,
        )

        delay(10)

        assertEquals(false, repository.generateCalled)
        assertTrue(viewModel.uiState.value.cards.isEmpty())
    }
}

private class FlowBackedRecommendationStore(
    initialItems: List<SavedItemEntity>,
) : RecommendationStore {
    private val items = MutableStateFlow(initialItems)

    var generateCalled = false
    var generatedItems: List<SavedItemEntity> = emptyList()

    override suspend fun generateTodayBatch(
        preferences: UserPreferences,
        today: LocalDate,
    ): TodayBatch? {
        generateCalled = true
        if (generatedItems.isEmpty()) return null
        items.value = generatedItems
        return TodayBatch(
            id = 1L,
            itemIds = generatedItems.map(SavedItemEntity::id),
        )
    }

    override suspend fun getTodayBatch(today: LocalDate): TodayBatch? = null

    override suspend fun getTodayItems(today: LocalDate): List<SavedItemEntity> = items.value

    override fun observeTodayItems(today: LocalDate): Flow<List<SavedItemEntity>> = items.asStateFlow()

    override fun observeUnreadPushedItems(): Flow<List<SavedItemEntity>> = items.asStateFlow()

    override suspend fun reconcileTodayBatchPushState(today: LocalDate) = Unit

    override suspend fun hasPostedNotificationForBatch(batchId: Long): Boolean = false

    override suspend fun markBatchPosted(batchId: Long, deliveredAt: Instant) = Unit

    override suspend fun markItemRead(itemId: Long) = Unit

    override suspend fun recordNotificationDelivery(log: NotificationDeliveryLog) = Unit

    override fun observeNotificationDeliveryLogs(limit: Int): Flow<List<NotificationDeliveryLog>> = MutableStateFlow(emptyList())
}

private class MutableRecommendationStore : RecommendationStore {
    private val items = MutableStateFlow<List<SavedItemEntity>>(emptyList())

    override suspend fun generateTodayBatch(
        preferences: UserPreferences,
        today: LocalDate,
    ): TodayBatch? = null

    override suspend fun getTodayBatch(today: LocalDate): TodayBatch? = null

    override suspend fun getTodayItems(today: LocalDate): List<SavedItemEntity> = items.value

    override fun observeTodayItems(today: LocalDate): Flow<List<SavedItemEntity>> = items.asStateFlow()

    override fun observeUnreadPushedItems(): Flow<List<SavedItemEntity>> = items.asStateFlow()

    override suspend fun reconcileTodayBatchPushState(today: LocalDate) = Unit

    override suspend fun hasPostedNotificationForBatch(batchId: Long): Boolean = false

    override suspend fun markBatchPosted(batchId: Long, deliveredAt: Instant) = Unit

    override suspend fun markItemRead(itemId: Long) = Unit

    override suspend fun recordNotificationDelivery(log: NotificationDeliveryLog) = Unit

    override fun observeNotificationDeliveryLogs(limit: Int): Flow<List<NotificationDeliveryLog>> = MutableStateFlow(emptyList())

    fun emit(value: List<SavedItemEntity>) {
        items.value = value
    }
}

private fun fakeTodayItem(
    id: Long,
    title: String,
    isRead: Boolean = false,
    pushCount: Int = 1,
    lastPushedAt: Instant? = Instant.parse("2026-04-14T08:00:00Z"),
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.LINK,
    title = title,
    rawUrl = "https://example.com/$id",
    canonicalUrl = "https://example.com/$id",
    textContent = null,
    imageUris = emptyList(),
    sourceAppPackage = "com.example.share",
    sourceAppLabel = "Example",
    sourcePlatform = "Example",
    sourceDomain = "example.com",
    topicCategory = "文章",
    note = null,
    createdAt = Instant.parse("2026-04-14T08:00:00Z"),
    updatedAt = Instant.parse("2026-04-14T08:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = pushCount,
    lastPushedAt = lastPushedAt,
    lastRecommendedDate = LocalDate.parse("2026-04-14"),
)

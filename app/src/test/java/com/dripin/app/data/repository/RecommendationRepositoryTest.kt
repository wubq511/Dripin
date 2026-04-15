package com.dripin.app.data.repository

import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.data.local.dao.DailyRecommendationDao
import com.dripin.app.data.local.dao.SavedItemDao
import com.dripin.app.data.local.entity.DailyRecommendationEntity
import com.dripin.app.data.local.entity.DailyRecommendationItemEntity
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.preferences.UserPreferences
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationRepositoryTest {
    @Test
    fun selects_unread_items_and_respects_repeat_rule() = runBlocking {
        val savedItemDao = FakeSavedItemDao(
            listOf(
                fakeLinkItem(id = 1L, isRead = false, pushCount = 0),
                fakeTextItem(id = 2L, isRead = false, pushCount = 3),
                fakeImageItem(id = 3L, isRead = true, pushCount = 0),
            ),
        )
        val recommendationDao = FakeDailyRecommendationDao(savedItemDao)
        val repository = RecommendationRepository(
            savedItemDao = savedItemDao,
            recommendationDao = recommendationDao,
            clock = Clock.fixed(Instant.parse("2026-04-14T09:00:00Z"), ZoneOffset.UTC),
        )

        val batch = repository.generateTodayBatch(
            preferences = UserPreferences(
                dailyPushCount = 2,
                repeatPushedUnreadItems = false,
                recommendationSortMode = RecommendationSortMode.OLDEST_SAVED_FIRST,
            ),
            today = LocalDate.parse("2026-04-14"),
        )

        assertEquals(listOf(1L), batch?.itemIds)
        assertEquals(1, savedItemDao.requireItem(1L).pushCount)
        assertEquals(LocalDate.parse("2026-04-14"), savedItemDao.requireItem(1L).lastRecommendedDate)
        assertNull(savedItemDao.requireItem(2L).lastRecommendedDate)
        assertNotNull(recommendationDao.getBatchForDate(LocalDate.parse("2026-04-14")))
    }

    @Test
    fun returns_existing_batch_when_today_was_already_generated() = runBlocking {
        val savedItemDao = FakeSavedItemDao(listOf(fakeLinkItem(id = 9L, isRead = false, pushCount = 1)))
        val recommendationDao = FakeDailyRecommendationDao(savedItemDao).apply {
            val batchId = insertBatch(
                DailyRecommendationEntity(
                    recommendedDate = LocalDate.parse("2026-04-14"),
                    createdAt = Instant.parse("2026-04-14T09:00:00Z"),
                    itemCount = 1,
                ),
            )
            insertBatchItems(
                listOf(
                    DailyRecommendationItemEntity(
                        batchId = batchId,
                        itemId = 9L,
                        displayOrder = 0,
                    ),
                ),
            )
        }
        val repository = RecommendationRepository(
            savedItemDao = savedItemDao,
            recommendationDao = recommendationDao,
        )

        val batch = repository.getTodayBatch(LocalDate.parse("2026-04-14"))

        assertEquals(listOf(9L), batch?.itemIds)
        assertTrue(recommendationDao.insertedBatches.isNotEmpty())
    }
}

private class FakeSavedItemDao(
    initialItems: List<SavedItemEntity>,
) : SavedItemDao {
    private val items = initialItems.associateBy { it.id }.toMutableMap()

    override suspend fun insert(item: SavedItemEntity): Long {
        val nextId = (items.keys.maxOrNull() ?: 0L) + 1L
        items[nextId] = item.copy(id = nextId)
        return nextId
    }

    override suspend fun update(item: SavedItemEntity) {
        items[item.id] = item
    }

    override suspend fun getById(itemId: Long): SavedItemEntity? = items[itemId]

    override suspend fun findByCanonicalUrl(canonicalUrl: String): SavedItemEntity? {
        return items.values.firstOrNull { it.canonicalUrl == canonicalUrl }
    }

    override fun observeAll(): Flow<List<SavedItemEntity>> = emptyFlow()

    override suspend fun getAllByOldestFirst(): List<SavedItemEntity> = items.values.sortedBy { it.createdAt }

    override suspend fun getAllByNewestFirst(): List<SavedItemEntity> = items.values.sortedByDescending { it.createdAt }

    fun requireItem(itemId: Long): SavedItemEntity = checkNotNull(items[itemId])
}

private class FakeDailyRecommendationDao(
    private val savedItemDao: FakeSavedItemDao,
) : DailyRecommendationDao {
    private val batches = linkedMapOf<Long, DailyRecommendationEntity>()
    private val batchItems = mutableListOf<DailyRecommendationItemEntity>()
    private var nextId = 1L

    val insertedBatches: List<DailyRecommendationEntity> get() = batches.values.toList()

    override suspend fun insertBatch(batch: DailyRecommendationEntity): Long {
        val id = nextId++
        batches[id] = batch.copy(id = id)
        return id
    }

    override suspend fun insertBatchItems(items: List<DailyRecommendationItemEntity>) {
        batchItems += items
    }

    override suspend fun getBatchForDate(date: LocalDate): DailyRecommendationEntity? {
        return batches.values.firstOrNull { it.recommendedDate == date }
    }

    override suspend fun getItemIdsForBatch(batchId: Long): List<Long> {
        return batchItems
            .filter { it.batchId == batchId }
            .sortedBy { it.displayOrder }
            .map { it.itemId }
    }

    override suspend fun getItemsForDate(date: LocalDate): List<SavedItemEntity> {
        val batch = getBatchForDate(date) ?: return emptyList()
        return getItemIdsForBatch(batch.id).mapNotNull { itemId -> savedItemDao.getById(itemId) }
    }

    override fun observeItemsForDate(date: LocalDate): Flow<List<SavedItemEntity>> {
        return MutableStateFlow(emptyList())
    }
}

private fun fakeLinkItem(
    id: Long,
    isRead: Boolean,
    pushCount: Int,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.LINK,
    title = "OpenAI Repo",
    rawUrl = "https://github.com/openai/openai",
    canonicalUrl = "https://github.com/openai/openai",
    textContent = null,
    imageUris = emptyList(),
    sourceAppPackage = "com.github.android",
    sourceAppLabel = "GitHub",
    sourcePlatform = "GitHub",
    sourceDomain = "github.com",
    topicCategory = "开发",
    note = null,
    createdAt = Instant.parse("2026-04-13T12:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = pushCount,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

private fun fakeTextItem(
    id: Long,
    isRead: Boolean,
    pushCount: Int,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.TEXT,
    title = "Article Summary",
    rawUrl = null,
    canonicalUrl = null,
    textContent = "Some text content",
    imageUris = emptyList(),
    sourceAppPackage = "com.tencent.mm",
    sourceAppLabel = "微信",
    sourcePlatform = "微信",
    sourceDomain = null,
    topicCategory = "文章",
    note = null,
    createdAt = Instant.parse("2026-04-13T13:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T13:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = pushCount,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

private fun fakeImageItem(
    id: Long,
    isRead: Boolean,
    pushCount: Int,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.IMAGE,
    title = "Mood Board",
    rawUrl = null,
    canonicalUrl = null,
    textContent = null,
    imageUris = listOf("content://image/$id"),
    sourceAppPackage = "com.xingin.xhs",
    sourceAppLabel = "小红书",
    sourcePlatform = "小红书",
    sourceDomain = null,
    topicCategory = "图片",
    note = null,
    createdAt = Instant.parse("2026-04-13T14:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T14:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = pushCount,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

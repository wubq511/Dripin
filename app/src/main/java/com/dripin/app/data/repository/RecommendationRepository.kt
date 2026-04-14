package com.dripin.app.data.repository

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

data class TodayBatch(
    val id: Long,
    val itemIds: List<Long>,
)

interface RecommendationStore {
    suspend fun generateTodayBatch(
        preferences: UserPreferences,
        today: LocalDate,
    ): TodayBatch?

    suspend fun getTodayBatch(today: LocalDate): TodayBatch?

    suspend fun getTodayItems(today: LocalDate): List<SavedItemEntity>

    suspend fun markItemRead(itemId: Long)
}

class RecommendationRepository(
    private val savedItemDao: SavedItemDao,
    private val recommendationDao: DailyRecommendationDao,
    private val clock: Clock = Clock.systemUTC(),
) : RecommendationStore {
    override suspend fun generateTodayBatch(
        preferences: UserPreferences,
        today: LocalDate,
    ): TodayBatch? {
        getTodayBatch(today)?.let { return it }

        val candidates = orderedCandidates(preferences.recommendationSortMode)
            .asSequence()
            .filter { !it.isRead }
            .filter(::hasUsablePayload)
            .filter { preferences.repeatPushedUnreadItems || it.pushCount == 0 }
            .take(preferences.dailyPushCount.coerceIn(1, 10))
            .toList()

        if (candidates.isEmpty()) {
            return null
        }

        val now = Instant.now(clock)
        val batchId = recommendationDao.insertBatch(
            DailyRecommendationEntity(
                recommendedDate = today,
                createdAt = now,
                itemCount = candidates.size,
            ),
        )
        recommendationDao.insertBatchItems(
            candidates.mapIndexed { index, item ->
                DailyRecommendationItemEntity(
                    batchId = batchId,
                    itemId = item.id,
                    displayOrder = index,
                )
            },
        )
        candidates.forEach { item ->
            savedItemDao.update(
                item.copy(
                    pushCount = item.pushCount + 1,
                    lastPushedAt = now,
                    lastRecommendedDate = today,
                    updatedAt = now,
                ),
            )
        }

        return TodayBatch(
            id = batchId,
            itemIds = candidates.map(SavedItemEntity::id),
        )
    }

    override suspend fun getTodayBatch(today: LocalDate): TodayBatch? {
        val batch = recommendationDao.getBatchForDate(today) ?: return null
        return TodayBatch(
            id = batch.id,
            itemIds = recommendationDao.getItemIdsForBatch(batch.id),
        )
    }

    override suspend fun getTodayItems(today: LocalDate): List<SavedItemEntity> {
        return recommendationDao.getItemsForDate(today)
    }

    override suspend fun markItemRead(itemId: Long) {
        val item = savedItemDao.getById(itemId) ?: return
        val now = Instant.now(clock)
        savedItemDao.update(
            item.copy(
                isRead = true,
                readAt = now,
                updatedAt = now,
            ),
        )
    }

    private suspend fun orderedCandidates(sortMode: RecommendationSortMode): List<SavedItemEntity> {
        return when (sortMode) {
            RecommendationSortMode.OLDEST_SAVED_FIRST -> savedItemDao.getAllByOldestFirst()
            RecommendationSortMode.NEWEST_SAVED_FIRST -> savedItemDao.getAllByNewestFirst()
        }
    }

    private fun hasUsablePayload(item: SavedItemEntity): Boolean {
        return !item.rawUrl.isNullOrBlank() ||
            !item.textContent.isNullOrBlank() ||
            !item.imageUri.isNullOrBlank()
    }
}

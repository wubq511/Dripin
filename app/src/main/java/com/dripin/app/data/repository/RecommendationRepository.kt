package com.dripin.app.data.repository

import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.core.model.NotificationDeliveryStatus
import com.dripin.app.data.local.dao.DailyRecommendationDao
import com.dripin.app.data.local.dao.SavedItemDao
import com.dripin.app.data.local.entity.DailyRecommendationEntity
import com.dripin.app.data.local.entity.DailyRecommendationItemEntity
import com.dripin.app.data.local.entity.NotificationDeliveryLogEntity
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.preferences.UserPreferences
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class NotificationDeliveryLog(
    val id: Long = 0,
    val recommendedDate: LocalDate,
    val attemptedAt: Instant,
    val itemCount: Int,
    val status: NotificationDeliveryStatus,
    val issue: String?,
    val batchId: Long?,
)

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

    fun observeTodayItems(today: LocalDate): Flow<List<SavedItemEntity>>

    fun observeUnreadPushedItems(): Flow<List<SavedItemEntity>>

    suspend fun reconcileTodayBatchPushState(today: LocalDate)

    suspend fun hasPostedNotificationForBatch(batchId: Long): Boolean

    suspend fun markBatchPosted(batchId: Long, deliveredAt: Instant)

    suspend fun markItemRead(itemId: Long)

    suspend fun recordNotificationDelivery(log: NotificationDeliveryLog)

    fun observeNotificationDeliveryLogs(limit: Int): Flow<List<NotificationDeliveryLog>>
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

    override fun observeTodayItems(today: LocalDate): Flow<List<SavedItemEntity>> {
        return recommendationDao.observeItemsForDate(today)
    }

    override fun observeUnreadPushedItems(): Flow<List<SavedItemEntity>> {
        return recommendationDao.observeUnreadPushedItems()
    }

    override suspend fun reconcileTodayBatchPushState(today: LocalDate) {
        val batch = recommendationDao.getBatchForDate(today) ?: return
        if (recommendationDao.hasPostedNotificationForBatch(batch.id)) return

        val now = Instant.now(clock)
        recommendationDao.getItemIdsForBatch(batch.id).forEach { itemId ->
            val item = savedItemDao.getById(itemId) ?: return@forEach
            if (item.lastRecommendedDate != today || item.pushCount == 0) return@forEach

            val repairedPushCount = (item.pushCount - 1).coerceAtLeast(0)
            savedItemDao.update(
                item.copy(
                    pushCount = repairedPushCount,
                    lastPushedAt = item.lastPushedAt.takeUnless { repairedPushCount == 0 },
                    updatedAt = now,
                ),
            )
        }
    }

    override suspend fun hasPostedNotificationForBatch(batchId: Long): Boolean {
        return recommendationDao.hasPostedNotificationForBatch(batchId)
    }

    override suspend fun markBatchPosted(batchId: Long, deliveredAt: Instant) {
        recommendationDao.getItemIdsForBatch(batchId).forEach { itemId ->
            val item = savedItemDao.getById(itemId) ?: return@forEach
            if (item.isRead) return@forEach
            savedItemDao.update(
                item.copy(
                    pushCount = item.pushCount + 1,
                    lastPushedAt = deliveredAt,
                    updatedAt = deliveredAt,
                ),
            )
        }
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

    override suspend fun recordNotificationDelivery(log: NotificationDeliveryLog) {
        recommendationDao.insertNotificationDeliveryLog(log.toEntity())
    }

    override fun observeNotificationDeliveryLogs(limit: Int): Flow<List<NotificationDeliveryLog>> {
        return recommendationDao.observeNotificationDeliveryLogs(limit)
            .map { logs -> logs.map(NotificationDeliveryLogEntity::toModel) }
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
            item.imageUris.isNotEmpty()
    }
}

private fun NotificationDeliveryLog.toEntity(): NotificationDeliveryLogEntity = NotificationDeliveryLogEntity(
    id = id,
    recommendedDate = recommendedDate,
    attemptedAt = attemptedAt,
    itemCount = itemCount,
    status = status,
    issue = issue,
    batchId = batchId,
)

private fun NotificationDeliveryLogEntity.toModel(): NotificationDeliveryLog = NotificationDeliveryLog(
    id = id,
    recommendedDate = recommendedDate,
    attemptedAt = attemptedAt,
    itemCount = itemCount,
    status = status,
    issue = issue,
    batchId = batchId,
)

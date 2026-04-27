package com.dripin.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dripin.app.data.local.entity.DailyRecommendationEntity
import com.dripin.app.data.local.entity.DailyRecommendationItemEntity
import com.dripin.app.data.local.entity.NotificationDeliveryLogEntity
import com.dripin.app.data.local.entity.SavedItemEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class NotificationDeliveryLogItemTitle(
    val logId: Long,
    val title: String,
    val displayOrder: Int,
)

@Dao
interface DailyRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBatch(batch: DailyRecommendationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchItems(items: List<DailyRecommendationItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationDeliveryLog(log: NotificationDeliveryLogEntity): Long

    @Query(
        """
        SELECT * FROM daily_recommendations
        WHERE recommendedDate = :date
        LIMIT 1
        """,
    )
    suspend fun getBatchForDate(date: LocalDate): DailyRecommendationEntity?

    @Query(
        """
        SELECT itemId FROM daily_recommendation_items
        WHERE batchId = :batchId
        ORDER BY displayOrder ASC
        """,
    )
    suspend fun getItemIdsForBatch(batchId: Long): List<Long>

    @Query(
        """
        SELECT saved_items.* FROM saved_items
        INNER JOIN daily_recommendation_items
            ON daily_recommendation_items.itemId = saved_items.id
        INNER JOIN daily_recommendations
            ON daily_recommendations.id = daily_recommendation_items.batchId
        WHERE daily_recommendations.recommendedDate = :date
        ORDER BY daily_recommendation_items.displayOrder ASC
        """,
    )
    suspend fun getItemsForDate(date: LocalDate): List<SavedItemEntity>

    @Query(
        """
        SELECT saved_items.* FROM saved_items
        INNER JOIN daily_recommendation_items
            ON daily_recommendation_items.itemId = saved_items.id
        INNER JOIN daily_recommendations
            ON daily_recommendations.id = daily_recommendation_items.batchId
        WHERE daily_recommendations.recommendedDate = :date
        ORDER BY daily_recommendation_items.displayOrder ASC
        """,
    )
    fun observeItemsForDate(date: LocalDate): Flow<List<SavedItemEntity>>

    @Query(
        """
        SELECT * FROM saved_items
        WHERE pushCount > 0
            AND isRead = 0
            AND lastPushedAt IS NOT NULL
        ORDER BY lastPushedAt DESC, updatedAt DESC
        """,
    )
    fun observeUnreadPushedItems(): Flow<List<SavedItemEntity>>

    @Query(
        """
        SELECT * FROM notification_delivery_logs
        ORDER BY attemptedAt DESC
        LIMIT :limit
        """,
    )
    fun observeNotificationDeliveryLogs(limit: Int): Flow<List<NotificationDeliveryLogEntity>>

    @Query(
        """
        SELECT notification_delivery_logs.id AS logId,
            COALESCE(NULLIF(saved_items.title, ''), '(无标题)') AS title,
            daily_recommendation_items.displayOrder AS displayOrder
        FROM notification_delivery_logs
        INNER JOIN daily_recommendation_items
            ON daily_recommendation_items.batchId = notification_delivery_logs.batchId
        INNER JOIN saved_items
            ON saved_items.id = daily_recommendation_items.itemId
        WHERE notification_delivery_logs.id IN (:logIds)
        ORDER BY notification_delivery_logs.attemptedAt DESC,
            daily_recommendation_items.displayOrder ASC
        """,
    )
    fun observeNotificationDeliveryLogItemTitles(logIds: List<Long>): Flow<List<NotificationDeliveryLogItemTitle>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM notification_delivery_logs
            WHERE batchId = :batchId AND status = 'POSTED'
        )
        """,
    )
    suspend fun hasPostedNotificationForBatch(batchId: Long): Boolean
}

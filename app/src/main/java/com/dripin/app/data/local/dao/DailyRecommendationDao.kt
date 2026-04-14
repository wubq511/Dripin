package com.dripin.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dripin.app.data.local.entity.DailyRecommendationEntity
import com.dripin.app.data.local.entity.DailyRecommendationItemEntity
import com.dripin.app.data.local.entity.SavedItemEntity
import java.time.LocalDate

@Dao
interface DailyRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBatch(batch: DailyRecommendationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchItems(items: List<DailyRecommendationItemEntity>)

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
}

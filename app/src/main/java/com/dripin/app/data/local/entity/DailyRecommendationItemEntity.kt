package com.dripin.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "daily_recommendation_items",
    primaryKeys = ["batchId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyRecommendationEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["batchId", "displayOrder"]),
        Index(value = ["itemId"]),
    ],
)
data class DailyRecommendationItemEntity(
    val batchId: Long,
    val itemId: Long,
    val displayOrder: Int,
)

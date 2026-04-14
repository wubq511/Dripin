package com.dripin.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "daily_recommendations",
    indices = [Index(value = ["recommendedDate"], unique = true)],
)
data class DailyRecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recommendedDate: LocalDate,
    val createdAt: Instant,
    val itemCount: Int,
)

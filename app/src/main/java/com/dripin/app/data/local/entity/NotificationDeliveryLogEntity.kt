package com.dripin.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dripin.app.core.model.NotificationDeliveryStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "notification_delivery_logs",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecommendationEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["attemptedAt"]),
        Index(value = ["recommendedDate"]),
        Index(value = ["batchId"]),
    ],
)
data class NotificationDeliveryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recommendedDate: LocalDate,
    val attemptedAt: Instant,
    val itemCount: Int,
    val status: NotificationDeliveryStatus,
    val issue: String?,
    val batchId: Long?,
)

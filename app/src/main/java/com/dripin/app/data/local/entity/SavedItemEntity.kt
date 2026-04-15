package com.dripin.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.dripin.app.core.model.ContentType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "saved_items",
    indices = [Index(value = ["canonicalUrl"], unique = true)],
)
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentType: ContentType,
    val title: String?,
    val rawUrl: String?,
    val canonicalUrl: String?,
    val textContent: String?,
    @ColumnInfo(name = "imageUri") val imageUris: List<String> = emptyList(),
    val sourceAppPackage: String?,
    val sourceAppLabel: String?,
    val sourcePlatform: String?,
    val sourceDomain: String?,
    val topicCategory: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isRead: Boolean,
    val readAt: Instant?,
    val pushCount: Int,
    val lastPushedAt: Instant?,
    val lastRecommendedDate: LocalDate?,
) {
    val primaryImageUri: String?
        get() = imageUris.firstOrNull()
}

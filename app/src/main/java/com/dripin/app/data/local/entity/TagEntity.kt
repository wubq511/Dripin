package com.dripin.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dripin.app.core.model.TagType
import java.time.Instant

@Entity(
    tableName = "tags",
    indices = [Index(value = ["normalizedName", "type"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val type: TagType,
    val createdAt: Instant,
)

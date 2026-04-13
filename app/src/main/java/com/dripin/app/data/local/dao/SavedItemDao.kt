package com.dripin.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dripin.app.data.local.entity.SavedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedItemDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: SavedItemEntity): Long

    @Update
    suspend fun update(item: SavedItemEntity)

    @Query("SELECT * FROM saved_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: Long): SavedItemEntity?

    @Query("SELECT * FROM saved_items WHERE canonicalUrl = :canonicalUrl LIMIT 1")
    suspend fun findByCanonicalUrl(canonicalUrl: String): SavedItemEntity?

    @Query("SELECT * FROM saved_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedItemEntity>>
}

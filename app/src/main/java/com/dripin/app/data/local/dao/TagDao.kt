package com.dripin.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dripin.app.data.local.entity.ItemTagCrossRef
import com.dripin.app.data.local.entity.TagEntity

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Query(
        """
        SELECT * FROM tags
        WHERE normalizedName = :normalizedName AND type = :typeName
        LIMIT 1
        """,
    )
    suspend fun findByNormalizedNameAndType(
        normalizedName: String,
        typeName: String,
    ): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<ItemTagCrossRef>)

    @Query("DELETE FROM item_tag_cross_refs WHERE itemId = :itemId")
    suspend fun deleteCrossRefsForItem(itemId: Long)

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN item_tag_cross_refs ON tags.id = item_tag_cross_refs.tagId
        WHERE item_tag_cross_refs.itemId = :itemId
        ORDER BY tags.name COLLATE NOCASE
        """,
    )
    suspend fun getTagsForItem(itemId: Long): List<TagEntity>
}

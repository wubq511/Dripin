package com.dripin.app.data.repository

import com.dripin.app.data.local.dao.SavedItemDao
import com.dripin.app.data.local.dao.TagDao
import com.dripin.app.data.local.entity.ItemTagCrossRef
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.local.entity.TagEntity
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SavedItemRepositoryTest {
    @Test
    fun save_image_persists_source_label_and_deduplicates_auto_tags() = runBlocking {
        val savedItemDao = RepositorySavedItemDaoFake()
        val tagDao = FakeTagDao()
        val persistedImageStore = FakePersistedImageStore()
        val repository = SavedItemRepository(
            savedItemDao = savedItemDao,
            tagDao = tagDao,
            imageStore = persistedImageStore,
            clock = Clock.fixed(Instant.parse("2026-04-14T09:00:00Z"), ZoneOffset.UTC),
        )

        val itemId = repository.saveImage(
            imageUri = "content://media/external/images/media/99",
            title = "Shared Image",
            note = "from timeline",
            sourceAppPackage = "com.twitter.android",
            sourceAppLabel = "X",
            tags = listOf("X", "配图"),
        )

        val item = requireNotNull(savedItemDao.getById(itemId))
        val tags = repository.getTags(itemId)

        assertEquals("file:///data/user/0/com.dripin.app/files/shared-images/persisted-99.jpg", item.imageUri)
        assertEquals("X", item.sourceAppLabel)
        assertEquals("X", item.sourcePlatform)
        assertFalse(tags.groupBy { it.lowercase() }.values.any { it.size > 1 })
    }
}

private class FakePersistedImageStore : PersistedImageStore {
    override suspend fun persist(sourceUri: String): String {
        return "file:///data/user/0/com.dripin.app/files/shared-images/persisted-99.jpg"
    }
}

private class RepositorySavedItemDaoFake : SavedItemDao {
    private val items = linkedMapOf<Long, SavedItemEntity>()
    private var nextId = 1L

    override suspend fun insert(item: SavedItemEntity): Long {
        val id = nextId++
        items[id] = item.copy(id = id)
        return id
    }

    override suspend fun update(item: SavedItemEntity) {
        items[item.id] = item
    }

    override suspend fun getById(itemId: Long): SavedItemEntity? = items[itemId]

    override suspend fun findByCanonicalUrl(canonicalUrl: String): SavedItemEntity? {
        return items.values.firstOrNull { it.canonicalUrl == canonicalUrl }
    }

    override fun observeAll(): Flow<List<SavedItemEntity>> = emptyFlow()

    override suspend fun getAllByOldestFirst(): List<SavedItemEntity> = items.values.sortedBy { it.createdAt }

    override suspend fun getAllByNewestFirst(): List<SavedItemEntity> = items.values.sortedByDescending { it.createdAt }
}

private class FakeTagDao : TagDao {
    private val tags = linkedMapOf<Long, TagEntity>()
    private val refs = mutableListOf<ItemTagCrossRef>()
    private var nextId = 1L

    override suspend fun insert(tag: TagEntity): Long {
        val existing = tags.values.firstOrNull {
            it.normalizedName == tag.normalizedName && it.type == tag.type
        }
        if (existing != null) return -1L

        val id = nextId++
        tags[id] = tag.copy(id = id)
        return id
    }

    override suspend fun findByNormalizedNameAndType(
        normalizedName: String,
        typeName: String,
    ): TagEntity? {
        return tags.values.firstOrNull {
            it.normalizedName == normalizedName && it.type.name == typeName
        }
    }

    override suspend fun insertCrossRefs(refs: List<ItemTagCrossRef>) {
        this.refs += refs.distinct()
    }

    override suspend fun deleteCrossRefsForItem(itemId: Long) {
        refs.removeAll { it.itemId == itemId }
    }

    override suspend fun getTagsForItem(itemId: Long): List<TagEntity> {
        val tagIds = refs.filter { it.itemId == itemId }.map { it.tagId }
        return tagIds.mapNotNull(tags::get)
    }
}

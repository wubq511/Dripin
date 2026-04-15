package com.dripin.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.DailyRecommendationEntity
import com.dripin.app.data.local.entity.DailyRecommendationItemEntity
import com.dripin.app.data.local.entity.SavedItemEntity
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun canonical_url_is_unique() = runBlocking {
        val dao = database.savedItemDao()
        val item = SavedItemEntity(
            id = 0,
            contentType = ContentType.LINK,
            title = "One",
            rawUrl = "https://github.com/openai/openai",
            canonicalUrl = "https://github.com/openai/openai",
            textContent = null,
            imageUris = emptyList(),
            sourceAppPackage = null,
            sourceAppLabel = null,
            sourcePlatform = "GitHub",
            sourceDomain = "github.com",
            topicCategory = "开发",
            note = null,
            createdAt = Instant.parse("2026-04-13T12:00:00Z"),
            updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
            isRead = false,
            readAt = null,
            pushCount = 0,
            lastPushedAt = null,
            lastRecommendedDate = null,
        )

        dao.insert(item)

        var threwConstraint = false
        try {
            dao.insert(item.copy(id = 0, title = "Two"))
        } catch (_: SQLiteConstraintException) {
            threwConstraint = true
        }

        assertTrue(threwConstraint)
    }

    @Test
    fun legacy_single_image_value_reads_back_as_one_image_list() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO saved_items (
                contentType, title, rawUrl, canonicalUrl, textContent, imageUri,
                sourceAppPackage, sourceAppLabel, sourcePlatform, sourceDomain, topicCategory, note,
                createdAt, updatedAt, isRead, readAt, pushCount, lastPushedAt, lastRecommendedDate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                ContentType.IMAGE.name,
                "Legacy image",
                null,
                null,
                null,
                "content://legacy/image/1",
                "com.tencent.mm",
                "微信",
                "微信",
                null,
                "图片",
                null,
                "2026-04-13T12:00:00Z",
                "2026-04-13T12:00:00Z",
                0,
                null,
                0,
                null,
                null,
            ),
        )

        val item = database.savedItemDao().getAllByNewestFirst().single()
        assertEquals(listOf("content://legacy/image/1"), item.imageUris)
    }

    @Test
    fun today_items_flow_emits_when_batch_is_inserted() = runBlocking {
        val savedItemDao = database.savedItemDao()
        val recommendationDao = database.dailyRecommendationDao()
        val today = LocalDate.parse("2026-04-15")
        val itemId = savedItemDao.insert(
            SavedItemEntity(
                contentType = ContentType.LINK,
                title = "Observable",
                rawUrl = "https://example.com/observable",
                canonicalUrl = "https://example.com/observable",
                textContent = null,
                imageUris = emptyList(),
                sourceAppPackage = "com.example.share",
                sourceAppLabel = "Example",
                sourcePlatform = "Example",
                sourceDomain = "example.com",
                topicCategory = "文章",
                note = null,
                createdAt = Instant.parse("2026-04-15T08:00:00Z"),
                updatedAt = Instant.parse("2026-04-15T08:00:00Z"),
                isRead = false,
                readAt = null,
                pushCount = 0,
                lastPushedAt = null,
                lastRecommendedDate = null,
            ),
        )

        val observed = async {
            withTimeout(2_000) {
                recommendationDao.observeItemsForDate(today)
                    .dropWhile { it.isEmpty() }
                    .first()
            }
        }

        val batchId = recommendationDao.insertBatch(
            DailyRecommendationEntity(
                recommendedDate = today,
                createdAt = Instant.parse("2026-04-15T09:00:00Z"),
                itemCount = 1,
            ),
        )
        recommendationDao.insertBatchItems(
            listOf(
                DailyRecommendationItemEntity(
                    batchId = batchId,
                    itemId = itemId,
                    displayOrder = 0,
                ),
            ),
        )

        val emission = observed.await()
        assertEquals(listOf(itemId), emission.map(SavedItemEntity::id))
    }
}

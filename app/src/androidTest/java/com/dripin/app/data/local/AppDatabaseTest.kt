package com.dripin.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertTrue
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
            imageUri = null,
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
}

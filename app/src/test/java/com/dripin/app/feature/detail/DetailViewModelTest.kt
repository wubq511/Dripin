package com.dripin.app.feature.detail

import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.repository.LinkSaveRequest
import com.dripin.app.data.repository.SaveResult
import com.dripin.app.data.repository.SavedItemStore
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailViewModelTest {
    @Test
    fun mark_read_updates_repository() = runBlocking {
        val repository = FakeSavedItemStore(listOf(fakeLinkItem(id = 1L, isRead = false)))
        val viewModel = DetailViewModel(
            itemId = 1L,
            repository = repository,
            workerDispatcher = Dispatchers.Unconfined,
        )

        viewModel.markRead()
        delay(10)

        assertTrue(repository.requireItem(1L).isRead)
    }
}

private class FakeSavedItemStore(
    items: List<SavedItemEntity>,
) : SavedItemStore {
    private val state = MutableStateFlow(items)

    override fun observeItems(): Flow<List<SavedItemEntity>> = state.asStateFlow()

    override suspend fun getItem(itemId: Long): SavedItemEntity? = state.value.firstOrNull { it.id == itemId }

    override suspend fun getTags(itemId: Long): List<String> = listOf("github.com", "GitHub")

    override suspend fun setReadState(
        itemId: Long,
        isRead: Boolean,
    ) {
        state.value = state.value.map { item ->
            if (item.id == itemId) item.copy(isRead = isRead) else item
        }
    }

    override suspend fun updateItemContent(
        itemId: Long,
        title: String?,
        note: String?,
    ) = Unit

    override suspend fun replaceTags(
        itemId: Long,
        tags: List<String>,
    ) = Unit

    override suspend fun findExistingLinkId(rawUrl: String): Long? = null

    override suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult = SaveResult.Created(1L)

    override suspend fun saveText(
        text: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        tags: List<String>,
    ): Long = 2L

    override suspend fun saveImage(
        imageUri: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        tags: List<String>,
    ): Long = 3L

    fun requireItem(itemId: Long): SavedItemEntity = checkNotNull(state.value.firstOrNull { it.id == itemId })
}

private fun fakeLinkItem(
    id: Long,
    isRead: Boolean,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.LINK,
    title = "OpenAI Repo",
    rawUrl = "https://github.com/openai/openai",
    canonicalUrl = "https://github.com/openai/openai",
    textContent = null,
    imageUri = null,
    sourceAppPackage = "com.github.android",
    sourceAppLabel = "GitHub",
    sourcePlatform = "GitHub",
    sourceDomain = "github.com",
    topicCategory = "开发",
    note = "Important",
    createdAt = Instant.parse("2026-04-13T12:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = 0,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

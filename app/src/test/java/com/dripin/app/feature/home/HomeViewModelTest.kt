package com.dripin.app.feature.home

import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.ReadFilter
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
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun filter_state_limits_visible_items() = runBlocking {
        val repository = FakeSavedItemStore(
            listOf(
                fakeLinkItem(id = 1L, isRead = false, pushCount = 0),
                fakeTextItem(id = 2L, isRead = true, pushCount = 2),
            ),
        )
        val viewModel = HomeViewModel(
            repository = repository,
            workerDispatcher = Dispatchers.Unconfined,
        )

        viewModel.onReadFilterChanged(ReadFilter.UNREAD)
        delay(10)

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(ContentType.LINK, viewModel.uiState.value.items.first().contentType)
    }
}

private class FakeSavedItemStore(
    items: List<SavedItemEntity>,
) : SavedItemStore {
    private val state = MutableStateFlow(items)

    override fun observeItems(): Flow<List<SavedItemEntity>> = state.asStateFlow()

    override suspend fun getItem(itemId: Long): SavedItemEntity? = state.value.firstOrNull { it.id == itemId }

    override suspend fun getTags(itemId: Long): List<String> = emptyList()

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

    override suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult = SaveResult.Created(99L)

    override suspend fun saveText(
        text: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        tags: List<String>,
    ): Long = 98L

    override suspend fun saveImage(
        imageUri: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        tags: List<String>,
    ): Long = 97L
}

private fun fakeLinkItem(
    id: Long,
    isRead: Boolean,
    pushCount: Int,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.LINK,
    title = "Link Item",
    rawUrl = "https://github.com/openai/openai",
    canonicalUrl = "https://github.com/openai/openai",
    textContent = null,
    imageUri = null,
    sourceAppPackage = "com.github.android",
    sourceAppLabel = "GitHub",
    sourcePlatform = "GitHub",
    sourceDomain = "github.com",
    topicCategory = "开发",
    note = null,
    createdAt = Instant.parse("2026-04-13T12:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = pushCount,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

private fun fakeTextItem(
    id: Long,
    isRead: Boolean,
    pushCount: Int,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.TEXT,
    title = "Text Item",
    rawUrl = null,
    canonicalUrl = null,
    textContent = "Some text content",
    imageUri = null,
    sourceAppPackage = "com.tencent.mm",
    sourceAppLabel = "微信",
    sourcePlatform = "微信",
    sourceDomain = null,
    topicCategory = "文章",
    note = "Saved note",
    createdAt = Instant.parse("2026-04-13T13:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T13:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = pushCount,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

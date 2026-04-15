package com.dripin.app.feature.detail

import com.dripin.app.core.common.UrlCanonicalizer
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
import org.junit.Assert.assertEquals
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

    @Test
    fun save_edits_updates_link_content_fields() = runBlocking {
        val repository = FakeSavedItemStore(listOf(fakeLinkItem(id = 1L, isRead = false)))
        val viewModel = DetailViewModel(
            itemId = 1L,
            repository = repository,
            workerDispatcher = Dispatchers.Unconfined,
        )

        viewModel.onTitleChanged("Updated title")
        viewModel.onNoteChanged("Updated note")
        viewModel.onRawUrlChanged("https://example.com/article?utm_source=newsletter&tab=readme")
        viewModel.onTextContentChanged("Updated link note")
        viewModel.saveEdits()
        delay(10)

        val item = repository.requireItem(1L)
        assertEquals("Updated title", item.title)
        assertEquals("Updated note", item.note)
        assertEquals("https://example.com/article?utm_source=newsletter&tab=readme", item.rawUrl)
        assertEquals("https://example.com/article?tab=readme", item.canonicalUrl)
        assertEquals("Updated link note", item.textContent)
        assertEquals("https://example.com/article?utm_source=newsletter&tab=readme", repository.lastUpdateContent?.rawUrl)
        assertEquals("Updated link note", repository.lastUpdateContent?.textContent)
        assertEquals("Updated title", viewModel.uiState.value.titleDraft)
        assertEquals("Updated link note", viewModel.uiState.value.textContentDraft)
    }

    @Test
    fun save_edits_updates_text_content_fields() = runBlocking {
        val repository = FakeSavedItemStore(listOf(fakeTextItem(id = 2L, isRead = true)))
        val viewModel = DetailViewModel(
            itemId = 2L,
            repository = repository,
            workerDispatcher = Dispatchers.Unconfined,
        )

        viewModel.onTextContentChanged("Edited body")
        viewModel.saveEdits()
        delay(10)

        val item = repository.requireItem(2L)
        assertEquals("Edited body", item.textContent)
        assertEquals("Edited body", repository.lastUpdateContent?.textContent)
        assertEquals("Edited body", viewModel.uiState.value.textContentDraft)
    }

    @Test
    fun save_edits_updates_image_collection() = runBlocking {
        val repository = FakeSavedItemStore(listOf(fakeImageItem(id = 3L, isRead = false)))
        val viewModel = DetailViewModel(
            itemId = 3L,
            repository = repository,
            workerDispatcher = Dispatchers.Unconfined,
        )

        viewModel.addImageUris(listOf("content://image/4"))
        viewModel.removeImageUri("content://image/1")
        viewModel.saveEdits()
        delay(10)

        val item = repository.requireItem(3L)
        assertEquals(listOf("content://image/4"), item.imageUris)
        assertEquals(listOf("content://image/4"), repository.lastUpdateContent?.imageUris)
    }
}

private class FakeSavedItemStore(
    items: List<SavedItemEntity>,
) : SavedItemStore {
    private val state = MutableStateFlow(items)
    var lastUpdateContent: UpdateContentRequest? = null

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
        rawUrl: String?,
        textContent: String?,
        imageUris: List<String>?,
    ) {
        lastUpdateContent = UpdateContentRequest(
            itemId = itemId,
            title = title,
            note = note,
            rawUrl = rawUrl,
            textContent = textContent,
            imageUris = imageUris,
        )
        state.value = state.value.map { item ->
            if (item.id != itemId) return@map item

            item.copy(
                title = title ?: item.title,
                note = note ?: item.note,
                rawUrl = rawUrl ?: item.rawUrl,
                canonicalUrl = rawUrl?.let(UrlCanonicalizer::canonicalize) ?: item.canonicalUrl,
                textContent = textContent ?: item.textContent,
                imageUris = imageUris ?: item.imageUris,
            )
        }
    }

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
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long = 2L

    override suspend fun saveImages(
        imageUris: List<String>,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long = 3L

    fun requireItem(itemId: Long): SavedItemEntity = checkNotNull(state.value.firstOrNull { it.id == itemId })
}

private data class UpdateContentRequest(
    val itemId: Long,
    val title: String?,
    val note: String?,
    val rawUrl: String?,
    val textContent: String?,
    val imageUris: List<String>?,
)

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
    imageUris = emptyList(),
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

private fun fakeTextItem(
    id: Long,
    isRead: Boolean,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.TEXT,
    title = "Meeting notes",
    rawUrl = null,
    canonicalUrl = null,
    textContent = "Original body",
    imageUris = emptyList(),
    sourceAppPackage = "com.dripin.app",
    sourceAppLabel = "Dripin",
    sourcePlatform = "Dripin",
    sourceDomain = null,
    topicCategory = "笔记",
    note = "Original note",
    createdAt = Instant.parse("2026-04-13T12:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = 0,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

private fun fakeImageItem(
    id: Long,
    isRead: Boolean,
): SavedItemEntity = SavedItemEntity(
    id = id,
    contentType = ContentType.IMAGE,
    title = "Gallery",
    rawUrl = null,
    canonicalUrl = null,
    textContent = null,
    imageUris = listOf("content://image/1"),
    sourceAppPackage = "com.xingin.xhs",
    sourceAppLabel = "小红书",
    sourcePlatform = "小红书",
    sourceDomain = null,
    topicCategory = "图片",
    note = "Original note",
    createdAt = Instant.parse("2026-04-13T12:00:00Z"),
    updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
    isRead = isRead,
    readAt = null,
    pushCount = 0,
    lastPushedAt = null,
    lastRecommendedDate = null,
)

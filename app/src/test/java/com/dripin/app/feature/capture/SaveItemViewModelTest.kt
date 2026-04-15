package com.dripin.app.feature.capture

import androidx.lifecycle.SavedStateHandle
import com.dripin.app.core.common.SourcePlatformClassifier
import com.dripin.app.core.common.TopicClassifier
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.metadata.LinkMetadata
import com.dripin.app.data.metadata.LinkMetadataReader
import com.dripin.app.data.repository.LinkSaveRequest
import com.dripin.app.data.repository.SaveResult
import com.dripin.app.data.repository.SavedItemStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveItemViewModelTest {
    @Test
    fun link_without_title_fetches_metadata_and_keeps_manual_override() = runBlocking {
        val metadataFetcher = LinkMetadataReader {
            delay(50)
            LinkMetadata(title = "Fetched Title")
        }
        val viewModel = SaveItemViewModel(
            initialPayload = IncomingSharePayload(
                contentType = ContentType.LINK,
                sharedUrl = "https://github.com/openai/openai",
                sourceAppPackage = "com.github.android",
                sourceAppLabel = "GitHub",
            ),
            metadataFetcher = metadataFetcher,
            repository = FakeSavedItemStore(),
            sourcePlatformClassifier = SourcePlatformClassifier,
            topicClassifier = TopicClassifier,
            savedStateHandle = SavedStateHandle(),
            workerDispatcher = Dispatchers.Unconfined,
        )

        viewModel.onTitleChanged("Manual Title")
        delay(100)

        assertEquals("Manual Title", viewModel.uiState.value.title)
        assertEquals("GitHub", viewModel.uiState.value.sourcePlatform)
    }

    @Test
    fun manual_entry_requires_content_before_save_becomes_available() = runBlocking {
        val viewModel = SaveItemViewModel(
            initialPayload = IncomingSharePayload(
                contentType = ContentType.LINK,
                isManualEntry = true,
            ),
            metadataFetcher = LinkMetadataReader { null },
            repository = FakeSavedItemStore(),
            sourcePlatformClassifier = SourcePlatformClassifier,
            topicClassifier = TopicClassifier,
            savedStateHandle = SavedStateHandle(),
            workerDispatcher = Dispatchers.Unconfined,
        )

        assertTrue(viewModel.uiState.value.isManualEntry)
        assertFalse(viewModel.uiState.value.canSave)

        viewModel.onSharedUrlChanged("https://mp.weixin.qq.com/s/abc123")
        delay(10)

        assertTrue(viewModel.uiState.value.canSave)
        assertEquals("微信", viewModel.uiState.value.sourcePlatform)

        viewModel.setContentType(ContentType.TEXT)
        assertFalse(viewModel.uiState.value.canSave)

        viewModel.onSharedTextChanged("手工补一段稍后再看")
        assertTrue(viewModel.uiState.value.canSave)
    }

    private class FakeSavedItemStore : SavedItemStore {
        override fun observeItems(): Flow<List<SavedItemEntity>> = flowOf(emptyList())

        override suspend fun getItem(itemId: Long): SavedItemEntity? = null

        override suspend fun getTags(itemId: Long): List<String> = emptyList()

        override suspend fun setReadState(
            itemId: Long,
            isRead: Boolean,
        ) = Unit

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

        override suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult =
            SaveResult.Created(itemId = 1L)

        override suspend fun saveText(
            text: String,
            title: String?,
            note: String?,
            sourceAppPackage: String?,
            sourceAppLabel: String?,
            tags: List<String>,
        ): Long = 2L

        override suspend fun saveImage(
            imageUri: String,
            title: String?,
            note: String?,
            sourceAppPackage: String?,
            sourceAppLabel: String?,
            tags: List<String>,
        ): Long = 3L
    }
}

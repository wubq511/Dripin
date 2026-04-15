package com.dripin.app.data.repository

import android.content.Context
import android.net.Uri
import com.dripin.app.core.common.SourcePlatformClassifier
import com.dripin.app.core.common.TopicClassifier
import com.dripin.app.core.common.UrlCanonicalizer
import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.TagType
import com.dripin.app.data.local.dao.SavedItemDao
import com.dripin.app.data.local.dao.TagDao
import com.dripin.app.data.local.entity.ItemTagCrossRef
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.local.entity.TagEntity
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class LinkSaveRequest(
    val rawUrl: String,
    val title: String? = null,
    val textContent: String? = null,
    val note: String? = null,
    val sourceAppPackage: String? = null,
    val sourceAppLabel: String? = null,
    val tags: List<String> = emptyList(),
)

sealed interface SaveResult {
    data class Created(val itemId: Long) : SaveResult

    data class UpdatedExisting(val itemId: Long) : SaveResult
}

interface SavedItemStore {
    fun observeItems(): Flow<List<SavedItemEntity>>

    suspend fun getItem(itemId: Long): SavedItemEntity?

    suspend fun getTags(itemId: Long): List<String>

    suspend fun setReadState(
        itemId: Long,
        isRead: Boolean,
    )

    suspend fun updateItemContent(
        itemId: Long,
        title: String?,
        note: String?,
        rawUrl: String? = null,
        textContent: String? = null,
        imageUris: List<String>? = null,
    )

    suspend fun replaceTags(
        itemId: Long,
        tags: List<String>,
    )

    suspend fun findExistingLinkId(rawUrl: String): Long?

    suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult

    suspend fun saveText(
        text: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long

    suspend fun saveImages(
        imageUris: List<String>,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long
}

fun interface PersistedImageStore {
    suspend fun persist(sourceUri: String): String
}

class ContextPersistedImageStore(
    private val context: Context,
) : PersistedImageStore {
    override suspend fun persist(sourceUri: String): String = withContext(Dispatchers.IO) {
        if (!sourceUri.startsWith("content://", ignoreCase = true)) {
            return@withContext sourceUri
        }

        val source = Uri.parse(sourceUri)
        val extension = context.contentResolver.getType(source)
            ?.substringAfter('/', missingDelimiterValue = "")
            ?.ifBlank { "jpg" }
            ?: "jpg"
        val outputDir = context.filesDir.resolve("shared-images").apply { mkdirs() }
        val outputFile = outputDir.resolve("persisted-${System.currentTimeMillis()}.$extension")

        runCatching {
            context.contentResolver.openInputStream(source)?.use { input ->
                outputFile.outputStream().use { output -> input.copyTo(output) }
            }
        }.getOrNull()

        if (outputFile.exists()) Uri.fromFile(outputFile).toString() else sourceUri
    }
}

class SavedItemRepository(
    private val savedItemDao: SavedItemDao,
    private val tagDao: TagDao,
    private val imageStore: PersistedImageStore = PersistedImageStore { it },
    private val clock: Clock = Clock.systemUTC(),
) : SavedItemStore {
    override fun observeItems(): Flow<List<SavedItemEntity>> = savedItemDao.observeAll()

    override suspend fun getItem(itemId: Long): SavedItemEntity? = savedItemDao.getById(itemId)

    override suspend fun getTags(itemId: Long): List<String> {
        return tagDao.getTagsForItem(itemId)
            .map(TagEntity::name)
            .distinctBy(String::lowercase)
    }

    override suspend fun setReadState(
        itemId: Long,
        isRead: Boolean,
    ) {
        val item = savedItemDao.getById(itemId) ?: return
        val now = Instant.now(clock)
        savedItemDao.update(
            item.copy(
                isRead = isRead,
                readAt = now.takeIf { isRead },
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateItemContent(
        itemId: Long,
        title: String?,
        note: String?,
        rawUrl: String?,
        textContent: String?,
        imageUris: List<String>?,
    ) {
        val item = savedItemDao.getById(itemId) ?: return
        val now = Instant.now(clock)
        val normalizedTitle = title.normalizeBlank()
        val normalizedNote = note.normalizeBlank()
        val normalizedRawUrl = rawUrl.normalizeBlank()
        val normalizedTextContent = textContent.normalizeBlank()
        val normalizedImageUris = imageUris?.normalizeImageUris()
        val existingAutoTagNames = listOfNotNull(
            item.sourceDomain,
            item.sourcePlatform,
            item.topicCategory,
        ).map(String::lowercase).toSet()
        val explicitTags = getTags(itemId).filterNot { it.lowercase() in existingAutoTagNames }
        val updatedItem = when (item.contentType) {
            ContentType.LINK -> {
                val updatedCanonicalUrl = normalizedRawUrl?.let(UrlCanonicalizer::canonicalize)
                val updatedSourceDomain = updatedCanonicalUrl?.toHttpUrlOrNull()?.host
                val updatedSourcePlatform = SourcePlatformClassifier.classify(
                    packageName = null,
                    domain = updatedSourceDomain,
                )
                val updatedTopicCategory = TopicClassifier.classify(
                    title = normalizedTitle ?: normalizedTextContent,
                    domain = updatedSourceDomain,
                )
                item.copy(
                    title = normalizedTitle,
                    note = normalizedNote,
                    rawUrl = normalizedRawUrl,
                    canonicalUrl = updatedCanonicalUrl,
                    textContent = normalizedTextContent,
                    sourceDomain = updatedSourceDomain,
                    sourcePlatform = updatedSourcePlatform,
                    topicCategory = updatedTopicCategory,
                    updatedAt = now,
                )
            }

            ContentType.TEXT -> {
                val updatedTopicCategory = TopicClassifier.classify(
                    title = normalizedTitle ?: normalizedTextContent,
                    domain = null,
                )
                item.copy(
                    title = normalizedTitle,
                    note = normalizedNote,
                    textContent = normalizedTextContent,
                    topicCategory = updatedTopicCategory,
                    updatedAt = now,
                )
            }

            ContentType.IMAGE -> item.copy(
                title = normalizedTitle,
                note = normalizedNote,
                imageUris = normalizedImageUris?.persisted() ?: item.imageUris,
                updatedAt = now,
            )
        }
        savedItemDao.update(updatedItem)
        syncTags(
            itemId = itemId,
            explicitTags = explicitTags,
            sourceDomain = updatedItem.sourceDomain,
            sourcePlatform = updatedItem.sourcePlatform,
            topicCategory = updatedItem.topicCategory,
            now = now,
            replaceExisting = true,
        )
    }

    override suspend fun replaceTags(
        itemId: Long,
        tags: List<String>,
    ) {
        val item = savedItemDao.getById(itemId) ?: return
        syncTags(
            itemId = itemId,
            explicitTags = tags,
            sourceDomain = item.sourceDomain,
            sourcePlatform = item.sourcePlatform,
            topicCategory = item.topicCategory,
            now = Instant.now(clock),
            replaceExisting = true,
        )
    }

    override suspend fun findExistingLinkId(rawUrl: String): Long? {
        val canonicalUrl = UrlCanonicalizer.canonicalize(rawUrl)
        return savedItemDao.findByCanonicalUrl(canonicalUrl)?.id
    }

    override suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult {
        val canonicalUrl = UrlCanonicalizer.canonicalize(request.rawUrl)
        val sourceDomain = canonicalUrl.toHttpUrlOrNull()?.host
        val sourcePlatform = SourcePlatformClassifier.classify(
            packageName = request.sourceAppPackage,
            domain = sourceDomain,
        )
        val topicCategory = TopicClassifier.classify(
            title = request.title ?: request.textContent,
            domain = sourceDomain,
        )
        val now = Instant.now(clock)

        val existing = savedItemDao.findByCanonicalUrl(canonicalUrl)
        return if (existing == null) {
            val itemId = savedItemDao.insert(
                SavedItemEntity(
                    contentType = ContentType.LINK,
                    title = request.title,
                    rawUrl = request.rawUrl,
                    canonicalUrl = canonicalUrl,
                    textContent = request.textContent,
                    imageUris = emptyList(),
                    sourceAppPackage = request.sourceAppPackage,
                    sourceAppLabel = request.sourceAppLabel,
                    sourcePlatform = sourcePlatform,
                    sourceDomain = sourceDomain,
                    topicCategory = topicCategory,
                    note = request.note,
                    createdAt = now,
                    updatedAt = now,
                    isRead = false,
                    readAt = null,
                    pushCount = 0,
                    lastPushedAt = null,
                    lastRecommendedDate = null,
                ),
            )

            syncTags(
                itemId = itemId,
                explicitTags = request.tags,
                sourceDomain = sourceDomain,
                sourcePlatform = sourcePlatform,
                topicCategory = topicCategory,
                now = now,
                replaceExisting = true,
            )
            SaveResult.Created(itemId)
        } else {
            savedItemDao.update(
                existing.copy(
                    title = request.title ?: existing.title,
                    rawUrl = request.rawUrl,
                    canonicalUrl = canonicalUrl,
                    textContent = request.textContent ?: existing.textContent,
                    sourceAppPackage = request.sourceAppPackage ?: existing.sourceAppPackage,
                    sourceAppLabel = request.sourceAppLabel ?: existing.sourceAppLabel,
                    sourcePlatform = sourcePlatform ?: existing.sourcePlatform,
                    sourceDomain = sourceDomain ?: existing.sourceDomain,
                    topicCategory = topicCategory ?: existing.topicCategory,
                    note = request.note ?: existing.note,
                    updatedAt = now,
                ),
            )

            syncTags(
                itemId = existing.id,
                explicitTags = request.tags,
                sourceDomain = sourceDomain ?: existing.sourceDomain,
                sourcePlatform = sourcePlatform ?: existing.sourcePlatform,
                topicCategory = topicCategory ?: existing.topicCategory,
                now = now,
                replaceExisting = true,
            )
            SaveResult.UpdatedExisting(existing.id)
        }
    }

    override suspend fun saveText(
        text: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long = saveStandaloneItem(
        contentType = ContentType.TEXT,
        title = title,
        textContent = text,
        imageUris = emptyList(),
        note = note,
        sourceAppPackage = sourceAppPackage,
        sourceAppLabel = sourceAppLabel,
        tags = tags,
    )

    override suspend fun saveImages(
        imageUris: List<String>,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long = saveStandaloneItem(
        contentType = ContentType.IMAGE,
        title = title,
        textContent = null,
        imageUris = imageUris.normalizeImageUris().persisted(),
        note = note,
        sourceAppPackage = sourceAppPackage,
        sourceAppLabel = sourceAppLabel,
        tags = tags,
    )

    private suspend fun saveStandaloneItem(
        contentType: ContentType,
        title: String?,
        textContent: String?,
        imageUris: List<String>,
        note: String?,
        sourceAppPackage: String?,
        sourceAppLabel: String?,
        tags: List<String>,
    ): Long {
        val topicCategory = TopicClassifier.classify(title = title ?: textContent, domain = null)
        val sourcePlatform = SourcePlatformClassifier.classify(
            packageName = sourceAppPackage,
            domain = null,
        )
        val now = Instant.now(clock)
        val itemId = savedItemDao.insert(
            SavedItemEntity(
                contentType = contentType,
                title = title,
                rawUrl = null,
                canonicalUrl = null,
                textContent = textContent,
                imageUris = imageUris,
                sourceAppPackage = sourceAppPackage,
                sourceAppLabel = sourceAppLabel,
                sourcePlatform = sourcePlatform,
                sourceDomain = null,
                topicCategory = topicCategory,
                note = note,
                createdAt = now,
                updatedAt = now,
                isRead = false,
                readAt = null,
                pushCount = 0,
                lastPushedAt = null,
                lastRecommendedDate = null,
            ),
        )

        syncTags(
            itemId = itemId,
            explicitTags = tags,
            sourceDomain = null,
            sourcePlatform = sourcePlatform,
            topicCategory = topicCategory,
            now = now,
            replaceExisting = true,
        )

        return itemId
    }

    private suspend fun syncTags(
        itemId: Long,
        explicitTags: List<String>,
        sourceDomain: String?,
        sourcePlatform: String?,
        topicCategory: String?,
        now: Instant,
        replaceExisting: Boolean,
    ) {
        val autoTags = buildList {
            sourceDomain?.takeIf(String::isNotBlank)?.let { add(TagDraft(it, TagType.SOURCE_DOMAIN)) }
            sourcePlatform?.takeIf(String::isNotBlank)?.let { add(TagDraft(it, TagType.SOURCE_PLATFORM)) }
            topicCategory?.takeIf(String::isNotBlank)?.let { add(TagDraft(it, TagType.TOPIC)) }
        }
        val autoTagNames = autoTags.map(TagDraft::normalizedName).toSet()
        val userTags = explicitTags
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { TagDraft(label = it, type = TagType.USER) }
            .distinctBy(TagDraft::normalizedName)
            .filterNot { it.normalizedName in autoTagNames }

        val mergedTags = (autoTags + userTags).distinctBy { it.normalizedName to it.type }

        if (replaceExisting) {
            tagDao.deleteCrossRefsForItem(itemId)
        }
        if (mergedTags.isEmpty()) return

        val tagIds = mergedTags.map { draft ->
            val insertedId = tagDao.insert(
                TagEntity(
                    name = draft.label,
                    normalizedName = draft.normalizedName,
                    type = draft.type,
                    createdAt = now,
                ),
            )

            if (insertedId != -1L) {
                insertedId
            } else {
                requireNotNull(
                    tagDao.findByNormalizedNameAndType(
                        normalizedName = draft.normalizedName,
                        typeName = draft.type.name,
                    ),
                ).id
            }
        }

        tagDao.insertCrossRefs(tagIds.map { tagId -> ItemTagCrossRef(itemId = itemId, tagId = tagId) })
    }

    private data class TagDraft(
        val label: String,
        val type: TagType,
    ) {
        val normalizedName: String = label.trim().lowercase()
    }

    private suspend fun List<String>.persisted(): List<String> = buildList(size) {
        for (uri in this@persisted) {
            add(imageStore.persist(uri))
        }
    }
}

private fun String?.normalizeBlank(): String? = this?.trim()?.ifBlank { null }

private fun List<String>.normalizeImageUris(): List<String> = map(String::trim)
    .filter(String::isNotBlank)
    .distinct()

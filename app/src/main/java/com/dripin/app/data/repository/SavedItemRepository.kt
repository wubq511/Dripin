package com.dripin.app.data.repository

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

class SavedItemRepository(
    private val savedItemDao: SavedItemDao,
    private val tagDao: TagDao,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult {
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
                    imageUri = null,
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
            )
            SaveResult.UpdatedExisting(existing.id)
        }
    }

    suspend fun saveText(
        text: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        tags: List<String>,
    ): Long = saveStandaloneItem(
        contentType = ContentType.TEXT,
        title = title,
        textContent = text,
        imageUri = null,
        note = note,
        sourceAppPackage = sourceAppPackage,
        tags = tags,
    )

    suspend fun saveImage(
        imageUri: String,
        title: String?,
        note: String?,
        sourceAppPackage: String?,
        tags: List<String>,
    ): Long = saveStandaloneItem(
        contentType = ContentType.IMAGE,
        title = title,
        textContent = null,
        imageUri = imageUri,
        note = note,
        sourceAppPackage = sourceAppPackage,
        tags = tags,
    )

    private suspend fun saveStandaloneItem(
        contentType: ContentType,
        title: String?,
        textContent: String?,
        imageUri: String?,
        note: String?,
        sourceAppPackage: String?,
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
                imageUri = imageUri,
                sourceAppPackage = sourceAppPackage,
                sourceAppLabel = null,
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
    ) {
        val autoTags = buildList {
            sourceDomain?.takeIf(String::isNotBlank)?.let { add(TagDraft(it, TagType.SOURCE_DOMAIN)) }
            sourcePlatform?.takeIf(String::isNotBlank)?.let { add(TagDraft(it, TagType.SOURCE_PLATFORM)) }
            topicCategory?.takeIf(String::isNotBlank)?.let { add(TagDraft(it, TagType.TOPIC)) }
            explicitTags
                .map(String::trim)
                .filter(String::isNotBlank)
                .forEach { add(TagDraft(it, TagType.USER)) }
        }.distinctBy { it.normalizedName to it.type }

        tagDao.deleteCrossRefsForItem(itemId)
        if (autoTags.isEmpty()) return

        val tagIds = autoTags.map { draft ->
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
}

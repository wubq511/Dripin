package com.dripin.app.data.local

import androidx.room.TypeConverter
import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.NotificationDeliveryStatus
import com.dripin.app.core.model.TagType
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate

class Converters {
    private companion object {
        const val IMAGE_URI_LIST_PREFIX = "__dripin_image_list__:"
    }

    @TypeConverter
    fun fromContentType(value: ContentType?): String? = value?.name

    @TypeConverter
    fun toContentType(value: String?): ContentType? = value?.let(ContentType::valueOf)

    @TypeConverter
    fun fromTagType(value: TagType?): String? = value?.name

    @TypeConverter
    fun toTagType(value: String?): TagType? = value?.let(TagType::valueOf)

    @TypeConverter
    fun fromNotificationDeliveryStatus(value: NotificationDeliveryStatus?): String? = value?.name

    @TypeConverter
    fun toNotificationDeliveryStatus(value: String?): NotificationDeliveryStatus? =
        value?.let(NotificationDeliveryStatus::valueOf)

    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromImageUriList(value: List<String>?): String? {
        if (value == null) return null
        return IMAGE_URI_LIST_PREFIX + value.joinToString("|") {
            URLEncoder.encode(it, StandardCharsets.UTF_8.name())
        }
    }

    @TypeConverter
    fun toImageUriList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        if (!value.startsWith(IMAGE_URI_LIST_PREFIX)) return listOf(value)
        return value.removePrefix(IMAGE_URI_LIST_PREFIX)
            .split('|')
            .mapNotNull { encoded ->
                URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
                    .trim()
                    .ifBlank { null }
            }
    }
}

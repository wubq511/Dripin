package com.dripin.app.data.local

import androidx.room.TypeConverter
import com.dripin.app.core.model.ContentType
import com.dripin.app.core.model.TagType
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromContentType(value: ContentType?): String? = value?.name

    @TypeConverter
    fun toContentType(value: String?): ContentType? = value?.let(ContentType::valueOf)

    @TypeConverter
    fun fromTagType(value: TagType?): String? = value?.name

    @TypeConverter
    fun toTagType(value: String?): TagType? = value?.let(TagType::valueOf)

    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
}

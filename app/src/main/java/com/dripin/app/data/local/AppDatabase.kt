package com.dripin.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dripin.app.data.local.dao.SavedItemDao
import com.dripin.app.data.local.dao.TagDao
import com.dripin.app.data.local.entity.ItemTagCrossRef
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.local.entity.TagEntity

@Database(
    entities = [
        SavedItemEntity::class,
        TagEntity::class,
        ItemTagCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedItemDao(): SavedItemDao

    abstract fun tagDao(): TagDao
}

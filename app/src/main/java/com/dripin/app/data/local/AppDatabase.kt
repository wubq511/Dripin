package com.dripin.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dripin.app.data.local.dao.DailyRecommendationDao
import com.dripin.app.data.local.dao.SavedItemDao
import com.dripin.app.data.local.dao.TagDao
import com.dripin.app.data.local.entity.DailyRecommendationEntity
import com.dripin.app.data.local.entity.DailyRecommendationItemEntity
import com.dripin.app.data.local.entity.ItemTagCrossRef
import com.dripin.app.data.local.entity.NotificationDeliveryLogEntity
import com.dripin.app.data.local.entity.SavedItemEntity
import com.dripin.app.data.local.entity.TagEntity

@Database(
    entities = [
        SavedItemEntity::class,
        TagEntity::class,
        ItemTagCrossRef::class,
        DailyRecommendationEntity::class,
        DailyRecommendationItemEntity::class,
        NotificationDeliveryLogEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedItemDao(): SavedItemDao

    abstract fun tagDao(): TagDao

    abstract fun dailyRecommendationDao(): DailyRecommendationDao

    companion object {
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "dripin.db",
            ).addMigrations(Migration1To2, Migration2To3).build()
        }

        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_recommendations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recommendedDate TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        itemCount INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_daily_recommendations_recommendedDate
                    ON daily_recommendations(recommendedDate)
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_recommendation_items (
                        batchId INTEGER NOT NULL,
                        itemId INTEGER NOT NULL,
                        displayOrder INTEGER NOT NULL,
                        PRIMARY KEY(batchId, itemId),
                        FOREIGN KEY(batchId) REFERENCES daily_recommendations(id) ON DELETE CASCADE,
                        FOREIGN KEY(itemId) REFERENCES saved_items(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_daily_recommendation_items_batchId_displayOrder
                    ON daily_recommendation_items(batchId, displayOrder)
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_daily_recommendation_items_itemId
                    ON daily_recommendation_items(itemId)
                    """.trimIndent(),
                )
            }
        }

        val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notification_delivery_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recommendedDate TEXT NOT NULL,
                        attemptedAt TEXT NOT NULL,
                        itemCount INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        issue TEXT,
                        batchId INTEGER,
                        FOREIGN KEY(batchId) REFERENCES daily_recommendations(id) ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_notification_delivery_logs_attemptedAt
                    ON notification_delivery_logs(attemptedAt)
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_notification_delivery_logs_recommendedDate
                    ON notification_delivery_logs(recommendedDate)
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_notification_delivery_logs_batchId
                    ON notification_delivery_logs(batchId)
                    """.trimIndent(),
                )
            }
        }
    }
}

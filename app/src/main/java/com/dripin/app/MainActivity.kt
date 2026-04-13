package com.dripin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.repository.SavedItemRepository

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dripin.db",
        ).build()
    }

    private val repository by lazy {
        SavedItemRepository(
            savedItemDao = database.savedItemDao(),
            tagDao = database.tagDao(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DripinApp(repository = repository)
        }
    }
}

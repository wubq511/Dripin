package com.dripin.app.feature.capture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.dripin.app.core.designsystem.theme.DripinTheme
import com.dripin.app.data.local.AppDatabase
import com.dripin.app.data.metadata.LinkMetadataFetcher
import com.dripin.app.data.repository.SavedItemRepository
import okhttp3.OkHttpClient

class ShareReceiverActivity : ComponentActivity() {
    private val database by lazy {
        AppDatabase.build(applicationContext)
    }

    private val repository by lazy {
        SavedItemRepository(
            savedItemDao = database.savedItemDao(),
            tagDao = database.tagDao(),
        )
    }

    private val metadataFetcher by lazy {
        LinkMetadataFetcher(OkHttpClient())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val payload = ShareIntentParser.parse(
            intent = intent,
            sourcePackage = callingPackage,
            sourceLabel = null,
        )
        val viewModel = ViewModelProvider(
            this,
            SaveItemViewModel.Factory(
                initialPayload = payload,
                metadataFetcher = metadataFetcher,
                repository = repository,
            ),
        )[SaveItemViewModel::class.java]

        setContent {
            DripinTheme {
                SaveItemScreen(
                    viewModel = viewModel,
                    onDone = { finish() },
                )
            }
        }
    }
}

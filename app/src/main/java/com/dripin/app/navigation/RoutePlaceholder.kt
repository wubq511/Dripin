package com.dripin.app.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dripin.app.core.model.ContentType
import com.dripin.app.data.metadata.LinkMetadataFetcher
import com.dripin.app.data.repository.SavedItemStore
import com.dripin.app.feature.capture.IncomingSharePayload
import com.dripin.app.feature.capture.SaveItemScreen
import com.dripin.app.feature.capture.SaveItemViewModel
import okhttp3.OkHttpClient

@Composable
fun RoutePlaceholder(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SaveRouteScreen(
    repository: SavedItemStore,
    onDone: () -> Unit,
) {
    val metadataFetcher = remember { LinkMetadataFetcher(OkHttpClient()) }
    val factory = remember(repository, metadataFetcher) {
        SaveItemViewModel.Factory(
            initialPayload = IncomingSharePayload(
                contentType = ContentType.LINK,
                isManualEntry = true,
            ),
            metadataFetcher = metadataFetcher,
            repository = repository,
        )
    }
    val viewModel: SaveItemViewModel = viewModel(factory = factory)
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        viewModel.appendSharedImageUris(uris.map(Uri::toString))
    }

    SaveItemScreen(
        viewModel = viewModel,
        onDone = onDone,
        onPickImages = { imagePicker.launch("image/*") },
    )
}

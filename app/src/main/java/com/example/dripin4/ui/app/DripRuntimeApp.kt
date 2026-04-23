package com.example.dripin4.ui.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.data.repository.RecommendationStore
import com.dripin.app.data.repository.SavedItemStore
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.feature.capture.IncomingSharePayload
import com.dripin.app.feature.capture.SaveItemViewModel
import com.dripin.app.feature.detail.DetailViewModel
import com.dripin.app.feature.recommendation.TodayViewModel
import com.dripin.app.feature.recommendation.TodayViewModelFactory
import com.dripin.app.feature.settings.SettingsViewModel
import com.dripin.app.feature.settings.SettingsViewModelFactory
import com.dripin.app.worker.AndroidNotificationCapabilityReader
import com.example.dripin4.ui.designsystem.DripTheme
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DripRuntimeApp(
    repository: SavedItemStore,
    settingsRepository: SettingsRepository,
    recommendationRepository: RecommendationStore,
    metadataFetcher: com.dripin.app.data.metadata.LinkMetadataReader,
    launchIntent: Intent? = null,
    initialCapturePayload: IncomingSharePayload? = null,
    onCaptureFinished: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    val controller = remember(initialCapturePayload) { DripRuntimeSessionController(initialCapturePayload) }
    val appState = remember(controller.currentDestination) { DripAppState(initialDestination = controller.currentDestination) }

    var selectedDetailId by remember { mutableStateOf<String?>(null) }
    var detailEditorVisible by remember { mutableStateOf(false) }
    var captureSessionId by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    val savedItems by repository.observeItems().collectAsStateWithLifecycle(initialValue = emptyList())
    val inboxItems = remember(savedItems) {
        val now = Instant.now()
        savedItems.map { it.toInboxItemUi(now = now, zoneId = zoneId) }
    }

    val activeDetailId = selectedDetailId ?: savedItems.firstOrNull()?.id?.toString()

    appState.beforeNavigate = { destination ->
        when (destination) {
            DripDestination.Capture -> {
                val previousPayload = controller.capturePayload
                controller.navigateTo(destination)
                if (controller.capturePayload != previousPayload) {
                    captureSessionId += 1
                }
            }

            DripDestination.Detail -> {
                if (selectedDetailId == null) {
                    selectedDetailId = savedItems.firstOrNull()?.id?.toString()
                }
                detailEditorVisible = false
            }

            else -> Unit
        }
    }

    val captureViewModelFactory = remember(captureSessionId, metadataFetcher, repository) {
        SaveItemViewModel.Factory(
            initialPayload = controller.capturePayload,
            metadataFetcher = metadataFetcher,
            repository = repository,
        )
    }
    val captureViewModel: SaveItemViewModel = viewModel(
        key = "prototype-capture-$captureSessionId",
        factory = captureViewModelFactory,
    )
    val captureUiState by captureViewModel.uiState.collectAsStateWithLifecycle()
    val captureState = remember(captureUiState) {
        captureUiState.toCaptureScreenState(
            availableTags = appState.captureTags,
        )
    }

    val todayViewModelFactory = remember(recommendationRepository, settingsRepository) {
        TodayViewModelFactory(
            repository = recommendationRepository,
            preferencesProvider = { settingsRepository.preferences.first() },
        )
    }
    val todayViewModel: TodayViewModel = viewModel(factory = todayViewModelFactory)
    val todayUiState by todayViewModel.uiState.collectAsStateWithLifecycle()

    val settingsViewModelFactory = remember(settingsRepository, context) {
        SettingsViewModelFactory(
            repository = settingsRepository,
            notificationCapabilityReader = AndroidNotificationCapabilityReader(context),
        )
    }
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState = remember(settingsUiState) { settingsUiState.toSettingsScreenState() }
    val notificationHistory by recommendationRepository.observeNotificationDeliveryLogs(limit = 20)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val detailViewModelOrNull: DetailViewModel? = activeDetailId?.toLongOrNull()?.let { itemId ->
        viewModel(
            key = "prototype-detail-$itemId",
            factory = remember(itemId, repository) {
                object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return DetailViewModel(
                            itemId = itemId,
                            repository = repository,
                        ) as T
                    }
                }
            },
        )
    }
    val detailUiState = detailViewModelOrNull?.uiState?.collectAsStateWithLifecycle()?.value

    val captureImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            captureViewModel.onSharedImageUrisChanged(uris.map(Uri::toString))
        }
    }
    val detailImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            detailViewModelOrNull?.addImageUris(uris.map(Uri::toString))
        }
    }

    val detailState = remember(detailUiState, detailEditorVisible) {
        if (detailUiState?.item != null) {
            detailUiState.toDetailScreenState(
                now = Instant.now(),
                zoneId = zoneId,
            ).copy(
                editor = detailUiState.toDetailScreenState(
                    now = Instant.now(),
                    zoneId = zoneId,
                ).editor.copy(visible = detailEditorVisible),
            )
        } else {
            emptyDetailScreenState().copy(
                editor = emptyDetailScreenState().editor.copy(visible = detailEditorVisible),
            )
        }
    }

    val todayState = remember(todayUiState, settingsUiState) {
        val nowDate = todayUiState.date
        val items = todayUiState.cards.map { card ->
            val savedItem = savedItems.firstOrNull { it.id == card.id }
            val savedDaysAgo = savedItem?.createdAt
                ?.atZone(zoneId)
                ?.toLocalDate()
                ?.let { java.time.temporal.ChronoUnit.DAYS.between(it, nowDate).coerceAtLeast(0) }
                ?: 0L
            val tag = savedItem?.topicCategory ?: card.sourceLabel
            card.toTodayItemUi(
                tag = tag,
                savedDaysAgo = savedDaysAgo,
            )
        }
        TodayScreenState(
            heroTimeText = settingsUiState.dailyPushTime.toString().substring(0, 5),
            items = items,
        )
    }

    val filteredInboxItems = remember(
        inboxItems,
        appState.selectedContentFilter,
        appState.selectedReadFilter,
        appState.selectedPushFilter,
    ) {
        inboxItems.filterWith(
            contentFilter = appState.selectedContentFilter,
            readFilter = appState.selectedReadFilter,
            pushFilter = appState.selectedPushFilter,
        )
    }
    val inboxState = remember(
        inboxItems,
        filteredInboxItems,
        appState.selectedContentFilter,
        appState.selectedReadFilter,
        appState.selectedPushFilter,
    ) {
        InboxScreenState(
            contentFilters = appState.inboxFilters,
            selectedContentFilter = appState.selectedContentFilter,
            readFilters = appState.inboxReadFilters,
            selectedReadFilter = appState.selectedReadFilter,
            pushFilters = appState.inboxPushFilters,
            selectedPushFilter = appState.selectedPushFilter,
            hasActiveFilters = appState.hasActiveInboxFilters(),
            items = filteredInboxItems,
        )
    }

    LaunchedEffect(launchIntent) {
        if (launchIntent?.dataString == "dripin://today") {
            appState.navigateTo(DripDestination.Today)
        }
    }

    LaunchedEffect(captureUiState.completedItemId) {
        if (captureUiState.completedItemId != null) {
            controller.onCaptureCompleted()
            captureSessionId += 1
            appState.navigateTo(DripDestination.Inbox)
            if (controller.consumeFinishCaptureHost()) {
                onCaptureFinished?.invoke()
            }
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = settingsUiState.dailyPushTime.hour,
            initialMinute = settingsUiState.dailyPushTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("每日提醒时间") },
            text = { TimeInput(state = pickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.setDailyPushTime(
                            java.time.LocalTime.of(pickerState.hour, pickerState.minute),
                        )
                        showTimePicker = false
                    },
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            },
        )
    }

    DripTheme {
        DripAppScaffold(
            appState = appState,
            inboxState = inboxState,
            searchSourceItems = inboxItems,
            todayState = todayState,
            captureState = captureState,
            detailState = detailState,
            settingsState = settingsState,
            notificationHistory = notificationHistory.map { it.toNotificationHistoryUi(zoneId) },
            onInboxContentFilterSelected = appState::toggleInboxContentFilter,
            onInboxReadFilterSelected = appState::setInboxReadFilter,
            onInboxPushFilterSelected = appState::setInboxPushFilter,
            onOpenDetail = { itemId ->
                selectedDetailId = itemId
                detailEditorVisible = false
                appState.openDetail(itemId)
            },
            onCaptureTitleChanged = captureViewModel::onTitleChanged,
            onCaptureNoteChanged = captureViewModel::onNoteChanged,
            onCaptureTagToggled = { tag ->
                if (captureUiState.userTags.any { it.equals(tag, ignoreCase = true) }) {
                    captureViewModel.removeTag(tag)
                } else {
                    captureViewModel.onDraftTagChanged(tag)
                    captureViewModel.addDraftTag()
                }
            },
            onCaptureDraftTagChanged = captureViewModel::onDraftTagChanged,
            onCaptureAddDraftTag = captureViewModel::addDraftTag,
            onCaptureContentTypeChanged = captureViewModel::setContentType,
            onCaptureSharedUrlChanged = captureViewModel::onSharedUrlChanged,
            onCaptureSharedTextChanged = captureViewModel::onSharedTextChanged,
            onCapturePickImages = { captureImagePicker.launch("image/*") },
            onCaptureRemoveImage = captureViewModel::removeSharedImageUri,
            onCaptureSave = captureViewModel::save,
            onCaptureCancel = {
                controller.onCaptureCancelled()
                captureSessionId += 1
                appState.navigateTo(DripDestination.Inbox)
                if (controller.consumeFinishCaptureHost()) {
                    onCaptureFinished?.invoke()
                }
            },
            onDetailPrimaryAction = {
                val rawUrl = detailUiState?.item?.rawUrl ?: return@DripAppScaffold
                context.startActivity(Intent(Intent.ACTION_VIEW, rawUrl.toUri()))
                if (detailUiState?.item?.isRead == false) {
                    detailViewModelOrNull?.markRead()
                }
            },
            onDetailOpenEditor = { detailEditorVisible = true },
            onDetailDismissEditor = { detailEditorVisible = false },
            onDetailEditorTitleChanged = { detailViewModelOrNull?.onTitleChanged(it) },
            onDetailEditorNoteChanged = { detailViewModelOrNull?.onNoteChanged(it) },
            onDetailEditorRawUrlChanged = { detailViewModelOrNull?.onRawUrlChanged(it) },
            onDetailEditorTextChanged = { detailViewModelOrNull?.onTextContentChanged(it) },
            onDetailEditorRequestImages = { detailImagePicker.launch("image/*") },
            onDetailEditorRemoveImage = { detailViewModelOrNull?.removeImageUri(it) },
            onDetailEditorTagDraftChanged = { detailViewModelOrNull?.onTagDraftChanged(it) },
            onDetailEditorAddTag = { detailViewModelOrNull?.addTag() },
            onDetailEditorRemoveTag = { detailViewModelOrNull?.removeTag(it) },
            onDetailEditorToggleRead = {
                val item = detailUiState?.item ?: return@DripAppScaffold
                if (item.isRead) {
                    detailViewModelOrNull?.markUnread()
                } else {
                    detailViewModelOrNull?.markRead()
                }
            },
            onDetailEditorSave = {
                detailViewModelOrNull?.saveEdits()
                detailEditorVisible = false
            },
            onSettingsDecreaseDailyCount = {
                settingsViewModel.setDailyPushCount((settingsUiState.dailyPushCount - 1).coerceAtLeast(1))
            },
            onSettingsIncreaseDailyCount = {
                settingsViewModel.setDailyPushCount((settingsUiState.dailyPushCount + 1).coerceAtMost(10))
            },
            onSettingsToggle = { key, checked ->
                when (key) {
                    SettingsToggleKey.DailyNotification -> settingsViewModel.setNotificationsEnabled(checked)
                    SettingsToggleKey.EveningWindow -> showTimePicker = true
                    SettingsToggleKey.AutoTitle -> settingsViewModel.setRepeatUnreadPushedItems(checked)
                    SettingsToggleKey.SuggestCategory -> settingsViewModel.setRecommendationSortMode(
                        if (checked) RecommendationSortMode.NEWEST_SAVED_FIRST else RecommendationSortMode.OLDEST_SAVED_FIRST,
                    )
                }
            },
            onSettingsOpenReminderTime = { showTimePicker = true },
        )
    }
}

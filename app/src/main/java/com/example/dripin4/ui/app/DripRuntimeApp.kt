package com.example.dripin4.ui.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dripin.app.core.common.ExternalOriginalLink
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
import com.dripin.app.worker.PostNotificationsPermission
import com.example.dripin4.ui.designsystem.DripTheme
import java.time.Instant
import java.time.ZoneId

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
    val lifecycleOwner = LocalLifecycleOwner.current
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

    val todayViewModelFactory = remember(recommendationRepository) {
        TodayViewModelFactory(
            repository = recommendationRepository,
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
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        settingsViewModel.refreshNotificationStatus()
    }

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
        TodayScreenState(
            heroTimeText = settingsUiState.dailyPushTime.toString().substring(0, 5),
            items = todayUiState.cards.toTodayItemsUi(
                savedItems = savedItems,
                nowDate = nowDate,
                zoneId = zoneId,
            ),
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

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.refreshNotificationStatus()
    }

    DisposableEffect(lifecycleOwner, settingsViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.refreshNotificationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                openExternalOriginalLink(context, rawUrl)
            },
            onDetailToggleRead = {
                val item = detailUiState?.item ?: return@DripAppScaffold
                if (item.isRead) {
                    detailViewModelOrNull.markUnread()
                } else {
                    detailViewModelOrNull.markRead()
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
            onSettingsSystemNotificationAction = {
                when (settingsUiState.notificationCapability.resolveSystemNotificationAction(Build.VERSION.SDK_INT)) {
                    SystemNotificationAction.RequestPermission -> {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notificationPermissionLauncher.launch(PostNotificationsPermission)
                        } else {
                            openAppNotificationSettings(context)
                        }
                    }

                    SystemNotificationAction.OpenSettings -> {
                        openAppNotificationSettings(context)
                    }
                }
            },
        )
    }
}

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun openExternalOriginalLink(context: Context, rawUrl: String) {
    for (target in ExternalOriginalLink.resolveTargets(rawUrl)) {
        val intent = Intent(Intent.ACTION_VIEW, target.url.toUri()).apply {
            if (target.packageName != null && target.className != null) {
                setClassName(target.packageName, target.className)
            } else {
                target.packageName?.let(::setPackage)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            // Try the next target, usually the original web URL.
        }
    }
}

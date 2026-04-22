package com.example.dripin4.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dripin.app.core.model.PushFilter
import com.dripin.app.core.model.ReadFilter
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripTheme
import com.example.dripin4.ui.designsystem.components.DripBottomNav
import com.example.dripin4.ui.designsystem.components.DripBottomNavItem
import com.example.dripin4.ui.designsystem.components.DripTopBar
import com.example.dripin4.ui.designsystem.components.GlassScaffoldBackground
import com.example.dripin4.ui.designsystem.components.ProvideGlassBackdrop
import com.example.dripin4.ui.features.capture.CaptureScreen
import com.example.dripin4.ui.features.detail.DetailScreen
import com.example.dripin4.ui.features.inbox.InboxScreen
import com.example.dripin4.ui.features.settings.SettingsScreen
import com.example.dripin4.ui.features.today.TodayScreen
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun rememberDripAppState(): DripAppState = remember { DripAppState() }

@Composable
fun DripApp() {
    var savedDestinationName by rememberSaveable {
        androidx.compose.runtime.mutableStateOf(DripDestination.Today.name)
    }
    val appState = remember {
        DripAppState(initialDestination = DripDestination.valueOf(savedDestinationName))
    }
    LaunchedEffect(appState.currentDestination) {
        savedDestinationName = appState.currentDestination.name
    }
        DripTheme {
        DripAppScaffold(
            appState = appState,
            inboxState = appState.toInboxScreenState(),
            todayState = appState.toTodayScreenState(),
            captureState = appState.toCaptureScreenState(),
            detailState = appState.toDetailScreenState(),
            settingsState = appState.toSettingsScreenState(),
            onInboxContentFilterSelected = appState::toggleInboxContentFilter,
            onInboxReadFilterSelected = appState::setInboxReadFilter,
            onInboxPushFilterSelected = appState::setInboxPushFilter,
            onOpenDetail = appState::openDetail,
            onCaptureTitleChanged = appState::updateCaptureTitle,
            onCaptureNoteChanged = appState::updateCaptureNote,
            onCaptureTagToggled = appState::toggleCaptureTag,
            onCaptureDraftTagChanged = {},
            onCaptureAddDraftTag = {},
            onCaptureContentTypeChanged = {},
            onCaptureSharedUrlChanged = {},
            onCaptureSharedTextChanged = {},
            onCapturePickImages = {},
            onCaptureRemoveImage = {},
            onCaptureSave = appState::onCaptureSave,
            onCaptureCancel = appState::onCaptureCancel,
            onDetailPrimaryAction = {},
            onDetailOpenEditor = {},
            onDetailDismissEditor = {},
            onDetailEditorTitleChanged = {},
            onDetailEditorNoteChanged = {},
            onDetailEditorRawUrlChanged = {},
            onDetailEditorTextChanged = {},
            onDetailEditorRequestImages = {},
            onDetailEditorRemoveImage = {},
            onDetailEditorTagDraftChanged = {},
            onDetailEditorAddTag = {},
            onDetailEditorRemoveTag = {},
            onDetailEditorToggleRead = {},
            onDetailEditorSave = {},
            onSettingsDecreaseDailyCount = appState::decreaseDailyCount,
            onSettingsIncreaseDailyCount = appState::increaseDailyCount,
            onSettingsToggle = appState::toggleSetting,
            onSettingsOpenReminderTime = {},
        )
    }
}

@Composable
fun DripAppScaffold(
    appState: DripAppState,
    inboxState: InboxScreenState,
    todayState: TodayScreenState,
    captureState: CaptureScreenState,
    detailState: DetailScreenState,
    settingsState: SettingsScreenState,
    onInboxContentFilterSelected: (InboxFilter) -> Unit,
    onInboxReadFilterSelected: (ReadFilter) -> Unit,
    onInboxPushFilterSelected: (PushFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    onCaptureTitleChanged: (String) -> Unit,
    onCaptureNoteChanged: (String) -> Unit,
    onCaptureTagToggled: (String) -> Unit,
    onCaptureDraftTagChanged: (String) -> Unit,
    onCaptureAddDraftTag: () -> Unit,
    onCaptureContentTypeChanged: (com.dripin.app.core.model.ContentType) -> Unit,
    onCaptureSharedUrlChanged: (String) -> Unit,
    onCaptureSharedTextChanged: (String) -> Unit,
    onCapturePickImages: () -> Unit,
    onCaptureRemoveImage: (String) -> Unit,
    onCaptureSave: () -> Unit,
    onCaptureCancel: () -> Unit,
    onDetailPrimaryAction: () -> Unit,
    onDetailOpenEditor: () -> Unit,
    onDetailDismissEditor: () -> Unit,
    onDetailEditorTitleChanged: (String) -> Unit,
    onDetailEditorNoteChanged: (String) -> Unit,
    onDetailEditorRawUrlChanged: (String) -> Unit,
    onDetailEditorTextChanged: (String) -> Unit,
    onDetailEditorRequestImages: () -> Unit,
    onDetailEditorRemoveImage: (String) -> Unit,
    onDetailEditorTagDraftChanged: (String) -> Unit,
    onDetailEditorAddTag: () -> Unit,
    onDetailEditorRemoveTag: (String) -> Unit,
    onDetailEditorToggleRead: () -> Unit,
    onDetailEditorSave: () -> Unit,
    onSettingsDecreaseDailyCount: () -> Unit,
    onSettingsIncreaseDailyCount: () -> Unit,
    onSettingsToggle: (SettingsToggleKey, Boolean) -> Unit,
    onSettingsOpenReminderTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayBackdrop = rememberLayerBackdrop()
    val navItems = remember {
        listOf(
            DripBottomNavItem(
                destination = DripDestination.Inbox,
                label = "Inbox",
                icon = Icons.Outlined.Inbox,
                testTag = "tab_inbox"
            ),
            DripBottomNavItem(
                destination = DripDestination.Today,
                label = "Today",
                icon = Icons.Outlined.AutoAwesome,
                testTag = "tab_today"
            ),
            DripBottomNavItem(
                destination = DripDestination.Capture,
                label = "Capture",
                icon = Icons.Outlined.AddCircleOutline,
                testTag = "tab_capture"
            ),
            DripBottomNavItem(
                destination = DripDestination.Detail,
                label = "Detail",
                icon = Icons.AutoMirrored.Outlined.Subject,
                testTag = "tab_detail"
            ),
            DripBottomNavItem(
                destination = DripDestination.Settings,
                label = "Settings",
                icon = Icons.Outlined.Settings,
                testTag = "tab_settings"
            )
        )
    }

    ProvideGlassBackdrop(backdrop = todayBackdrop) {
        Box(modifier = modifier.fillMaxSize()) {
            GlassScaffoldBackground(
                modifier = Modifier.layerBackdrop(todayBackdrop),
                todayMode = true
            )
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                topBar = {
                    DripTopBar(
                        brand = DripStrings.Brand,
                        subtitle = DripStrings.TopSubtitle,
                        onSearch = {},
                        onBell = {},
                        todayGlassMode = true,
                        todayBackdrop = todayBackdrop
                    )
                },
                bottomBar = {
                    DripBottomNav(
                        items = navItems,
                        currentDestination = appState.currentDestination,
                        onDestinationSelected = appState::navigateTo,
                        todayGlassMode = true,
                        todayBackdrop = todayBackdrop
                    )
                }
            ) {
                ScreenHost(
                    appState = appState,
                    inboxState = inboxState,
                    todayState = todayState,
                    captureState = captureState,
                    detailState = detailState,
                    settingsState = settingsState,
                    onInboxContentFilterSelected = onInboxContentFilterSelected,
                    onInboxReadFilterSelected = onInboxReadFilterSelected,
                    onInboxPushFilterSelected = onInboxPushFilterSelected,
                    onOpenDetail = onOpenDetail,
                    onCaptureTitleChanged = onCaptureTitleChanged,
                    onCaptureNoteChanged = onCaptureNoteChanged,
                    onCaptureTagToggled = onCaptureTagToggled,
                    onCaptureDraftTagChanged = onCaptureDraftTagChanged,
                    onCaptureAddDraftTag = onCaptureAddDraftTag,
                    onCaptureContentTypeChanged = onCaptureContentTypeChanged,
                    onCaptureSharedUrlChanged = onCaptureSharedUrlChanged,
                    onCaptureSharedTextChanged = onCaptureSharedTextChanged,
                    onCapturePickImages = onCapturePickImages,
                    onCaptureRemoveImage = onCaptureRemoveImage,
                    onCaptureSave = onCaptureSave,
                    onCaptureCancel = onCaptureCancel,
                    onDetailPrimaryAction = onDetailPrimaryAction,
                    onDetailOpenEditor = onDetailOpenEditor,
                    onDetailDismissEditor = onDetailDismissEditor,
                    onDetailEditorTitleChanged = onDetailEditorTitleChanged,
                    onDetailEditorNoteChanged = onDetailEditorNoteChanged,
                    onDetailEditorRawUrlChanged = onDetailEditorRawUrlChanged,
                    onDetailEditorTextChanged = onDetailEditorTextChanged,
                    onDetailEditorRequestImages = onDetailEditorRequestImages,
                    onDetailEditorRemoveImage = onDetailEditorRemoveImage,
                    onDetailEditorTagDraftChanged = onDetailEditorTagDraftChanged,
                    onDetailEditorAddTag = onDetailEditorAddTag,
                    onDetailEditorRemoveTag = onDetailEditorRemoveTag,
                    onDetailEditorToggleRead = onDetailEditorToggleRead,
                    onDetailEditorSave = onDetailEditorSave,
                    onSettingsDecreaseDailyCount = onSettingsDecreaseDailyCount,
                    onSettingsIncreaseDailyCount = onSettingsIncreaseDailyCount,
                    onSettingsToggle = onSettingsToggle,
                    onSettingsOpenReminderTime = onSettingsOpenReminderTime,
                    todayBackdrop = todayBackdrop,
                    modifier = Modifier.padding(
                        PaddingValues(
                            start = 0.dp,
                            top = 0.dp,
                            end = 0.dp,
                            bottom = 0.dp
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun ScreenHost(
    appState: DripAppState,
    inboxState: InboxScreenState,
    todayState: TodayScreenState,
    captureState: CaptureScreenState,
    detailState: DetailScreenState,
    settingsState: SettingsScreenState,
    onInboxContentFilterSelected: (InboxFilter) -> Unit,
    onInboxReadFilterSelected: (ReadFilter) -> Unit,
    onInboxPushFilterSelected: (PushFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    onCaptureTitleChanged: (String) -> Unit,
    onCaptureNoteChanged: (String) -> Unit,
    onCaptureTagToggled: (String) -> Unit,
    onCaptureDraftTagChanged: (String) -> Unit,
    onCaptureAddDraftTag: () -> Unit,
    onCaptureContentTypeChanged: (com.dripin.app.core.model.ContentType) -> Unit,
    onCaptureSharedUrlChanged: (String) -> Unit,
    onCaptureSharedTextChanged: (String) -> Unit,
    onCapturePickImages: () -> Unit,
    onCaptureRemoveImage: (String) -> Unit,
    onCaptureSave: () -> Unit,
    onCaptureCancel: () -> Unit,
    onDetailPrimaryAction: () -> Unit,
    onDetailOpenEditor: () -> Unit,
    onDetailDismissEditor: () -> Unit,
    onDetailEditorTitleChanged: (String) -> Unit,
    onDetailEditorNoteChanged: (String) -> Unit,
    onDetailEditorRawUrlChanged: (String) -> Unit,
    onDetailEditorTextChanged: (String) -> Unit,
    onDetailEditorRequestImages: () -> Unit,
    onDetailEditorRemoveImage: (String) -> Unit,
    onDetailEditorTagDraftChanged: (String) -> Unit,
    onDetailEditorAddTag: () -> Unit,
    onDetailEditorRemoveTag: (String) -> Unit,
    onDetailEditorToggleRead: () -> Unit,
    onDetailEditorSave: () -> Unit,
    onSettingsDecreaseDailyCount: () -> Unit,
    onSettingsIncreaseDailyCount: () -> Unit,
    onSettingsToggle: (SettingsToggleKey, Boolean) -> Unit,
    onSettingsOpenReminderTime: () -> Unit,
    todayBackdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = appState.currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxSize(),
        label = "screen_transition"
    ) { destination ->
        when (destination) {
            DripDestination.Inbox -> InboxScreen(
                state = inboxState,
                onContentFilterSelected = onInboxContentFilterSelected,
                onReadFilterSelected = onInboxReadFilterSelected,
                onPushFilterSelected = onInboxPushFilterSelected,
                onOpenDetail = onOpenDetail
            )

            DripDestination.Today -> TodayScreen(
                state = todayState,
                onOpenDetail = onOpenDetail,
                backdrop = todayBackdrop
            )

            DripDestination.Capture -> CaptureScreen(
                state = captureState,
                onTitleChanged = onCaptureTitleChanged,
                onNoteChanged = onCaptureNoteChanged,
                onTagToggle = onCaptureTagToggled,
                onDraftTagChanged = onCaptureDraftTagChanged,
                onAddDraftTag = onCaptureAddDraftTag,
                onContentTypeChanged = onCaptureContentTypeChanged,
                onSharedUrlChanged = onCaptureSharedUrlChanged,
                onSharedTextChanged = onCaptureSharedTextChanged,
                onPickImages = onCapturePickImages,
                onRemoveImage = onCaptureRemoveImage,
                onSave = onCaptureSave,
                onCancel = onCaptureCancel,
            )
            DripDestination.Detail -> DetailScreen(
                state = detailState,
                onPrimaryAction = onDetailPrimaryAction,
                onOpenEditor = onDetailOpenEditor,
                onDismissEditor = onDetailDismissEditor,
                onEditorTitleChanged = onDetailEditorTitleChanged,
                onEditorNoteChanged = onDetailEditorNoteChanged,
                onEditorRawUrlChanged = onDetailEditorRawUrlChanged,
                onEditorTextChanged = onDetailEditorTextChanged,
                onEditorRequestImages = onDetailEditorRequestImages,
                onEditorRemoveImage = onDetailEditorRemoveImage,
                onEditorTagDraftChanged = onDetailEditorTagDraftChanged,
                onEditorAddTag = onDetailEditorAddTag,
                onEditorRemoveTag = onDetailEditorRemoveTag,
                onEditorToggleRead = onDetailEditorToggleRead,
                onEditorSave = onDetailEditorSave,
            )
            DripDestination.Settings -> SettingsScreen(
                state = settingsState,
                onDecreaseDailyCount = onSettingsDecreaseDailyCount,
                onIncreaseDailyCount = onSettingsIncreaseDailyCount,
                onToggleSetting = onSettingsToggle,
                onOpenReminderTime = onSettingsOpenReminderTime,
            )
        }
    }
}

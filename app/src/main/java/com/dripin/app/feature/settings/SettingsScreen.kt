package com.dripin.app.feature.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.DripinHero
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.FilterChipRow
import com.dripin.app.core.designsystem.component.MetaChip
import com.dripin.app.core.designsystem.component.SectionCard
import com.dripin.app.core.model.RecommendationSortMode
import com.dripin.app.worker.NotificationCapabilityIssue
import com.dripin.app.worker.NotificationCapabilitySnapshot
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showTimePicker by remember { mutableStateOf(false) }
    var pushCountDraft by remember(uiState.dailyPushCount) {
        mutableFloatStateOf(uiState.dailyPushCount.toFloat())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { viewModel.refreshNotificationStatus() }

    LaunchedEffect(viewModel) {
        viewModel.refreshNotificationStatus()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNotificationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.dailyPushCount) {
        pushCountDraft = uiState.dailyPushCount.toFloat()
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = uiState.dailyPushTime.hour,
            initialMinute = uiState.dailyPushTime.minute,
            is24Hour = true,
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(text = "每日提醒时间") },
            text = { TimeInput(state = pickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDailyPushTime(LocalTime.of(pickerState.hour, pickerState.minute))
                        showTimePicker = false
                    },
                ) {
                    Text(text = "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = "取消")
                }
            },
        )
    }

    val notificationCapability = uiState.notificationCapability
    val notificationStatusLabel = notificationStatusLabel(
        notificationsEnabled = uiState.notificationsEnabled,
        capability = notificationCapability,
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            DripinHero(
                eyebrow = "Rhythm",
                title = "把提醒调成\n你自己的节奏",
                subtitle = "克制、稳定、不打扰。",
                badge = notificationStatusLabel,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaChip(text = "时间 ${uiState.dailyPushTime.format(timeFormatter)}")
                    MetaChip(text = "数量 ${uiState.dailyPushCount}")
                }
            }
        }

        item {
            SectionCard(title = "通知状态") {
                NotificationStatusPanel(
                    notificationsEnabled = uiState.notificationsEnabled,
                    capability = notificationCapability,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenAppNotificationSettings = {
                        openAppNotificationSettings(context)
                    },
                    onOpenChannelSettings = {
                        openChannelNotificationSettings(context)
                    },
                )
            }
        }

        item {
            SectionCard(title = "推送节奏") {
                SettingSwitchRow(
                    title = "开启每日提醒",
                    body = "本地定时生成今日推荐",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled,
                )
                AnimatedVisibility(
                    visible = uiState.notificationsEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingValueRow(
                            title = "提醒时间",
                            body = uiState.dailyPushTime.format(timeFormatter),
                            actionLabel = "修改",
                            onActionClick = { showTimePicker = true },
                        )
                        DripinPanel(
                            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        ) {
                            Text(
                                text = "每日数量 ${pushCountDraft.roundToInt()} 条",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Slider(
                                value = pushCountDraft,
                                onValueChange = { pushCountDraft = it },
                                valueRange = 1f..10f,
                                steps = 8,
                                onValueChangeFinished = {
                                    viewModel.setDailyPushCount(pushCountDraft.roundToInt())
                                },
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = !uiState.notificationsEnabled) {
                    Text(
                        text = "提醒关闭时，不会安排下一次推荐。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(title = "推荐策略") {
                SettingSwitchRow(
                    title = "允许重复推荐未读内容",
                    body = "关闭后只推未推送过的未读内容",
                    checked = uiState.repeatPushedUnreadItems,
                    onCheckedChange = viewModel::setRepeatUnreadPushedItems,
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "排序偏好",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    FilterChipRow(
                        options = RecommendationSortMode.entries,
                        selected = uiState.recommendationSortMode,
                        labelOf = RecommendationSortMode::label,
                        onSelected = viewModel::setRecommendationSortMode,
                    )
                }
            }
        }

        item {
            SectionCard(title = "本地优先") {
                Text(
                    text = "内容、标签和提醒偏好都保存在本机。联网仅用于补全链接标题。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NotificationStatusPanel(
    notificationsEnabled: Boolean,
    capability: NotificationCapabilitySnapshot,
    onRequestPermission: () -> Unit,
    onOpenAppNotificationSettings: () -> Unit,
    onOpenChannelSettings: () -> Unit,
) {
    val issue = capability.primaryIssue()
    val statusText = when {
        !notificationsEnabled -> "应用提醒已关闭，系统状态不会触发每日推送。"
        issue == null -> if (capability.channelExists) {
            "系统通知、权限和每日推荐频道都可用。"
        } else {
            "系统通知可用，每日推荐频道会在下次推送时自动创建。"
        }
        issue == NotificationCapabilityIssue.RuntimePermissionDenied -> {
            "需要先授予系统通知权限，才能发送每日推荐。"
        }
        issue == NotificationCapabilityIssue.AppNotificationsDisabled -> {
            "应用的系统通知已被关闭，需要到系统设置里重新开启。"
        }
        issue == NotificationCapabilityIssue.ChannelBlocked -> {
            "每日推荐通知频道已被关闭，需要去频道设置里重新打开。"
        }
        else -> "通知状态需要进一步检查。"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (issue == null && notificationsEnabled) "通知可用" else "通知需要处理",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!capability.runtimePermissionGranted && Build.VERSION.SDK_INT >= 33) {
                OutlinedButton(onClick = onRequestPermission) {
                    Text(text = "请求权限")
                }
            }
            if (!capability.appNotificationsEnabled || capability.channelBlocked) {
                OutlinedButton(onClick = onOpenAppNotificationSettings) {
                    Text(text = "打开通知设置")
                }
            }
            if (capability.channelExists && capability.channelBlocked) {
                OutlinedButton(onClick = onOpenChannelSettings) {
                    Text(text = "检查频道")
                }
            }
        }
        if (!capability.channelExists && notificationsEnabled && capability.canDeliverNotifications) {
            Text(
                text = "频道尚未创建，下一次每日推送会自动生成。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingValueRow(
    title: String,
    body: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(onClick = onActionClick) {
            Text(text = actionLabel)
        }
    }
}

@Composable
private fun notificationStatusLabel(
    notificationsEnabled: Boolean,
    capability: NotificationCapabilitySnapshot,
): String {
    val issue = capability.primaryIssue()
    return when {
        !notificationsEnabled -> "已关闭"
        issue == null -> "可发送"
        issue == NotificationCapabilityIssue.RuntimePermissionDenied -> "缺少权限"
        issue == NotificationCapabilityIssue.AppNotificationsDisabled -> "通知已关"
        issue == NotificationCapabilityIssue.ChannelBlocked -> "频道受限"
        else -> "需检查"
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

private fun openChannelNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra(Settings.EXTRA_CHANNEL_ID, com.dripin.app.worker.RecommendationChannelId)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

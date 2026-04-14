package com.dripin.app.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dripin.app.core.designsystem.component.FilterChipRow
import com.dripin.app.core.designsystem.component.SectionCard
import com.dripin.app.core.model.RecommendationSortMode
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
    var showTimePicker by remember { mutableStateOf(false) }
    var pushCountDraft by remember(uiState.dailyPushCount) {
        mutableFloatStateOf(uiState.dailyPushCount.toFloat())
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = "推送节奏") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingSwitchRow(
                        title = "开启每日提醒",
                        body = "在本地定时生成一批今日推荐。",
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
                                body = "当前时间 ${uiState.dailyPushTime.format(timeFormatter)}",
                                actionLabel = "修改",
                                onActionClick = { showTimePicker = true },
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "每日数量 ${pushCountDraft.roundToInt()} 条",
                                    style = MaterialTheme.typography.titleSmall,
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
                                Text(
                                    text = "默认从未读内容里抽取，数量控制在 1 到 10 条之间。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = !uiState.notificationsEnabled) {
                        Text(
                            text = "提醒关闭时，应用仍会保留你的节奏设置，但不会生成下一次计划任务。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            SectionCard(title = "推荐策略") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingSwitchRow(
                        title = "允许重复推荐未读内容",
                        body = "关闭后，只推荐从未推送过的未读内容。",
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
        }

        item {
            SectionCard(title = "本地优先") {
                Text(
                    text = "所有内容、标签和提醒偏好都保存在本机。联网仅用于尝试补全链接标题，失败时回退为空或域名。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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

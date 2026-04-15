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
import com.dripin.app.core.designsystem.component.DripinHero
import com.dripin.app.core.designsystem.component.DripinPanel
import com.dripin.app.core.designsystem.component.FilterChipRow
import com.dripin.app.core.designsystem.component.MetaChip
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
                badge = if (uiState.notificationsEnabled) "已开启" else "已关闭",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaChip(text = "时间 ${uiState.dailyPushTime.format(timeFormatter)}")
                    MetaChip(text = "数量 ${uiState.dailyPushCount}")
                }
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

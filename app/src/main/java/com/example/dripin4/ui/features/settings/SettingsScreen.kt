package com.example.dripin4.ui.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.example.dripin4.ui.app.SettingsScreenState
import com.example.dripin4.ui.app.SettingsToggleKey
import com.example.dripin4.ui.content.DripStrings
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.GlassCard
import com.example.dripin4.ui.designsystem.components.GlassCardTone
import com.example.dripin4.ui.designsystem.components.GlassIconButton
import com.example.dripin4.ui.designsystem.components.GlassPageHeader
import com.example.dripin4.ui.designsystem.components.GlassPanel
import com.example.dripin4.ui.designsystem.components.GlassScaffold
import com.example.dripin4.ui.designsystem.components.GlassSectionHeading
import com.example.dripin4.ui.designsystem.components.GlassSwitch

@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    onDecreaseDailyCount: () -> Unit,
    onIncreaseDailyCount: () -> Unit,
    onToggleSetting: (SettingsToggleKey, Boolean) -> Unit,
    onOpenReminderTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deliveryGroup = state.groups.firstOrNull()
    val captureGroup = state.groups.drop(1).firstOrNull()

    GlassScaffold(
        testTag = "screen_settings",
        modifier = modifier,
        header = {
            GlassPageHeader(
                title = DripStrings.SettingsTitle,
            )
        },
    ) {
        item("delivery_settings") {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                tone = GlassCardTone.Neutral,
                contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
            ) {
                GlassSectionHeading(title = DripStrings.GroupDelivery)
                Spacer(modifier = Modifier.height(DripSpacing.XSmall))
                GlassPanel {
                    Column {
                        Text(
                            text = DripStrings.SettingsDailyCount,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DripColors.Ink,
                        )
                        Spacer(modifier = Modifier.height(DripSpacing.XXSmall))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GlassIconButton(
                                icon = Icons.Outlined.Remove,
                                contentDescription = DripStrings.SettingsDecrease,
                                onClick = onDecreaseDailyCount,
                                testTag = "settings_count_decrease",
                            )
                            Text(
                                text = "${state.dailyCount}",
                                style = MaterialTheme.typography.titleLarge,
                                color = DripColors.Ink,
                                modifier = Modifier.testTag("settings_count_value"),
                            )
                            GlassIconButton(
                                icon = Icons.Outlined.Add,
                                contentDescription = DripStrings.SettingsIncrease,
                                onClick = onIncreaseDailyCount,
                                testTag = "settings_count_increase",
                            )
                        }
                    }
                }

                deliveryGroup?.rows?.forEach { row ->
                    Spacer(modifier = Modifier.height(DripSpacing.XSmall))
                    DeliveryToggleRow(
                        label = row.label,
                        hint = row.hint,
                        checked = row.checked,
                        onCheckedChange = { onToggleSetting(row.key, it) },
                    )
                }

                Spacer(modifier = Modifier.height(DripSpacing.XSmall))
                UtilityRow(
                    title = DripStrings.UtilityReminderTitle,
                    subtitle = state.reminderSubtitle,
                    onClick = onOpenReminderTime,
                )
            }
        }

        captureGroup?.let { group ->
            item("advanced_settings") {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = GlassCardTone.Neutral,
                    contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
                ) {
                    GlassSectionHeading(title = group.title)
                    group.rows.forEach { row ->
                        Spacer(modifier = Modifier.height(DripSpacing.XSmall))
                        DeliveryToggleRow(
                            label = row.label,
                            hint = row.hint,
                            checked = row.checked,
                            onCheckedChange = { onToggleSetting(row.key, it) },
                        )
                    }
                    Spacer(modifier = Modifier.height(DripSpacing.XSmall))
                    UtilityRow(
                        title = "当前排序",
                        subtitle = state.sortModeLabel,
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryToggleRow(
    label: String,
    hint: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    GlassPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DripColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hint != null) {
                    Spacer(modifier = Modifier.height(DripSpacing.XXSmall))
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassPalette.TextTodaySubtitle,
                    )
                }
            }
            Spacer(modifier = Modifier.width(DripSpacing.XSmall))
            GlassSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun UtilityRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    GlassPanel {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = DripColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassPalette.TextTodaySubtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

package com.dripin.app.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
sealed class DripinDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : DripinDestination("home", "收集", Icons.Rounded.Inbox)
    data object Today : DripinDestination("today", "今日", Icons.Rounded.AutoAwesome)
    data object Settings : DripinDestination("settings", "设置", Icons.Rounded.Settings)
    data object Save : DripinDestination("save", "保存", Icons.Rounded.Inbox)
    data object Detail : DripinDestination("detail/{itemId}", "详情", Icons.Rounded.Inbox) {
        fun routeFor(itemId: Long): String = "detail/$itemId"
    }
}

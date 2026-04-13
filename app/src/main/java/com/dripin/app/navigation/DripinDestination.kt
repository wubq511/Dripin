package com.dripin.app.navigation

import androidx.compose.runtime.Immutable

@Immutable
sealed class DripinDestination(
    val route: String,
    val label: String,
) {
    data object Home : DripinDestination("home", "Home")
    data object Today : DripinDestination("today", "Today")
    data object Settings : DripinDestination("settings", "Settings")
    data object Save : DripinDestination("save", "Save")
    data object Detail : DripinDestination("detail/{itemId}", "Detail") {
        fun routeFor(itemId: Long): String = "detail/$itemId"
    }
}

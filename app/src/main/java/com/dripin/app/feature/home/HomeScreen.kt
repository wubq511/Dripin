package com.dripin.app.feature.home

import androidx.compose.runtime.Composable
import com.dripin.app.navigation.RoutePlaceholder

@Composable
fun HomeScreen() {
    RoutePlaceholder(
        title = "Home",
        body = "Your saved items will appear here.",
    )
}

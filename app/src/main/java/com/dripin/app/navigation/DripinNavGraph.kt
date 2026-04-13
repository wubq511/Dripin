package com.dripin.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dripin.app.core.designsystem.theme.DripinAccent
import com.dripin.app.core.designsystem.theme.DripinLine
import com.dripin.app.feature.home.HomeScreen
import com.dripin.app.feature.recommendation.TodayScreen
import com.dripin.app.feature.settings.SettingsScreen

private val bottomBarDestinations = listOf(
    DripinDestination.Home,
    DripinDestination.Today,
    DripinDestination.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DripinNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentDestination = bottomBarDestinations.firstOrNull { it.route == currentRoute }
        ?: DripinDestination.Home

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = currentDestination.label) },
            )
        },
        bottomBar = {
            NavigationBar {
                bottomBarDestinations.forEach { destination ->
                    val selected = currentRoute == destination.route
                    NavigationBarItem(
                        modifier = Modifier.testTag("nav-${destination.route}"),
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (selected) DripinAccent else DripinLine,
                                        shape = CircleShape,
                                    ),
                            )
                        },
                        label = { Text(text = destination.label) },
                        alwaysShowLabel = true,
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DripinDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(DripinDestination.Home.route) {
                HomeScreen()
            }
            composable(DripinDestination.Today.route) {
                TodayScreen()
            }
            composable(DripinDestination.Settings.route) {
                SettingsScreen()
            }
            composable(DripinDestination.Save.route) {
                SaveRouteScreen()
            }
        }
    }
}

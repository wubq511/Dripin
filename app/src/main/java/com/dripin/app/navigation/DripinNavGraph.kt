package com.dripin.app.navigation

import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.dripin.app.core.designsystem.theme.DripinAccent
import com.dripin.app.core.designsystem.theme.DripinLine
import com.dripin.app.data.repository.RecommendationStore
import com.dripin.app.data.repository.SavedItemStore
import com.dripin.app.data.repository.SettingsRepository
import com.dripin.app.feature.detail.DetailScreen
import com.dripin.app.feature.detail.DetailViewModel
import com.dripin.app.feature.home.HomeScreen
import com.dripin.app.feature.home.HomeViewModel
import com.dripin.app.feature.recommendation.TodayScreen
import com.dripin.app.feature.recommendation.TodayViewModel
import com.dripin.app.feature.recommendation.TodayViewModelFactory
import com.dripin.app.feature.settings.SettingsScreen
import com.dripin.app.feature.settings.SettingsViewModel
import com.dripin.app.feature.settings.SettingsViewModelFactory

private val bottomBarDestinations = listOf(
    DripinDestination.Home,
    DripinDestination.Today,
    DripinDestination.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DripinNavGraph(
    repository: SavedItemStore,
    settingsRepository: SettingsRepository,
    recommendationRepository: RecommendationStore,
    launchIntent: Intent? = null,
    navController: NavHostController = rememberNavController(),
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentDestination = bottomBarDestinations.firstOrNull { it.route == currentRoute }
        ?: DripinDestination.Home

    LaunchedEffect(launchIntent) {
        if (launchIntent?.dataString == "dripin://today") {
            navController.navigate(DripinDestination.Today.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

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
                val viewModel = remember(repository) { HomeViewModel(repository) }
                HomeScreen(
                    viewModel = viewModel,
                    onOpenItem = { itemId ->
                        navController.navigate(DripinDestination.Detail.routeFor(itemId))
                    },
                )
            }
            composable(
                route = DripinDestination.Today.route,
                deepLinks = listOf(navDeepLink { uriPattern = "dripin://today" }),
            ) {
                val context = LocalContext.current
                val factory = remember(recommendationRepository) { TodayViewModelFactory(recommendationRepository) }
                val viewModel: TodayViewModel = viewModel(factory = factory)
                TodayScreen(
                    viewModel = viewModel,
                    onOpenLink = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    },
                )
            }
            composable(DripinDestination.Settings.route) {
                val factory = remember(settingsRepository) { SettingsViewModelFactory(settingsRepository) }
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = viewModel)
            }
            composable(DripinDestination.Save.route) {
                SaveRouteScreen()
            }
            composable(
                route = DripinDestination.Detail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val context = LocalContext.current
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
                val viewModel = remember(itemId, repository) { DetailViewModel(itemId, repository) }
                DetailScreen(
                    viewModel = viewModel,
                    onOpenLink = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    },
                )
            }
        }
    }
}

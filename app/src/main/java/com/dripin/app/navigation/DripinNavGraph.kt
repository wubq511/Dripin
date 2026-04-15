package com.dripin.app.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.dripin.app.core.designsystem.component.DripinBackground
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
import com.dripin.app.worker.AndroidNotificationCapabilityReader
import kotlinx.coroutines.flow.first

private val bottomBarDestinations = listOf(
    DripinDestination.Home,
    DripinDestination.Today,
    DripinDestination.Settings,
)

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
    val showBottomBar = bottomBarDestinations.any { it.route == currentRoute }
    val showQuickAddFab = currentRoute == DripinDestination.Home.route

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

    DripinBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = DripinDestination.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomBar) 92.dp else 0.dp),
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
                val factory = remember(recommendationRepository, settingsRepository) {
                    TodayViewModelFactory(
                        repository = recommendationRepository,
                        preferencesProvider = { settingsRepository.preferences.first() },
                    )
                }
                val viewModel: TodayViewModel = viewModel(factory = factory)
                TodayScreen(
                    viewModel = viewModel,
                    onOpenLink = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    },
                )
            }
            composable(DripinDestination.Settings.route) {
                val context = LocalContext.current
                val factory = remember(settingsRepository, context) {
                    SettingsViewModelFactory(
                        repository = settingsRepository,
                        notificationCapabilityReader = AndroidNotificationCapabilityReader(context),
                    )
                }
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = viewModel)
            }
            composable(DripinDestination.Save.route) {
                SaveRouteScreen(
                    repository = repository,
                    onDone = { navController.popBackStack() },
                )
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

            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute,
                    navController = navController,
                )
            }
            if (showQuickAddFab) {
                QuickAddFab(
                    onClick = { navController.navigate(DripinDestination.Save.route) },
                )
            }
        }
    }
}

@Composable
private fun BoxScope.BottomBar(
    currentRoute: String?,
    navController: NavHostController,
) {
    Surface(
        modifier = Modifier
            .align(androidx.compose.ui.Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 22.dp,
    ) {
        NavigationBar(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0.dp),
        ) {
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    ),
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                        )
                    },
                    label = { Text(text = destination.label) },
                    alwaysShowLabel = true,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.QuickAddFab(
    onClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = Modifier
            .align(androidx.compose.ui.Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(end = 22.dp, bottom = 114.dp)
            .size(60.dp),
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 10.dp,
        ),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "添加内容",
        )
    }
}

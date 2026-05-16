package com.example.studs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studs.di.AppContainer
import com.example.studs.ui.screens.DriveListScreen
import com.example.studs.ui.screens.HomeScreen
import com.example.studs.ui.screens.PdfViewerScreen
import com.example.studs.ui.screens.SettingsScreen
import com.example.studs.ui.viewmodels.DriveViewModel
import com.example.studs.ui.viewmodels.HomeViewModel
import com.example.studs.ui.viewmodels.SettingsViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String, val title: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Store : Screen("store", "Store", Icons.Filled.Store, Icons.Outlined.Store)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val items = listOf(
    Screen.Home,
    Screen.Store,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    val windowSizeClass = calculateWindowSizeClass(LocalContext.current as ComponentActivity)
    val useNavigationRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    
    Row(modifier = Modifier.fillMaxSize()) {
        if (useNavigationRail) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            val currentRoute = currentDestination?.route
            if (currentRoute == Screen.Home.route || currentRoute == Screen.Store.route || currentRoute == Screen.Settings.route || currentRoute?.startsWith("folder/") == true) {
                NavigationRail(
                    header = {
                        Icon(
                            painter = painterResource(id = com.example.studs.R.drawable.ic_book),
                            contentDescription = "App Logo",
                            modifier = Modifier.padding(vertical = 16.dp).size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items.forEach { screen ->
                            NavigationRailItem(
                                icon = {
                                    Icon(
                                        imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                            screen.selectedIcon
                                        } else {
                                            screen.unselectedIcon
                                        },
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            bottomBar = {
                if (!useNavigationRail) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    val currentRoute = currentDestination?.route
            if (currentRoute == Screen.Home.route || currentRoute == Screen.Store.route || currentRoute == Screen.Settings.route || currentRoute?.startsWith("folder/") == true) {
                        NavigationBar(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                                screen.selectedIcon
                                            } else {
                                                screen.unselectedIcon
                                            },
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
                
                composable(Screen.Home.route) {
                    val viewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.provideFactory(appContainer.driveRepository)
                    )
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPdf = { downloadUrl, name, uiId ->
                            val encodedUrl = URLEncoder.encode(downloadUrl, StandardCharsets.UTF_8.toString())
                            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                            val encodedUiId = URLEncoder.encode(uiId, StandardCharsets.UTF_8.toString())
                            navController.navigate("pdf/$encodedUrl/$encodedName/$encodedUiId")
                        }
                    )
                }

                composable(Screen.Store.route) {
                    val viewModel: DriveViewModel = viewModel(
                        factory = DriveViewModel.provideFactory(appContainer.driveRepository)
                    )
                    DriveListScreen(
                        folderId = "ROOT",
                        folderName = "Store",
                        viewModel = viewModel,
                        onNavigateToFolder = { id, name ->
                            val encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString())
                            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                            navController.navigate("folder/$encodedId/$encodedName")
                        },
                        onNavigateToPdf = { downloadUrl, name, uiId ->
                            val encodedUrl = URLEncoder.encode(downloadUrl, StandardCharsets.UTF_8.toString())
                            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                            val encodedUiId = URLEncoder.encode(uiId, StandardCharsets.UTF_8.toString())
                            navController.navigate("pdf/$encodedUrl/$encodedName/$encodedUiId")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    val viewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.provideFactory(appContainer.settingsRepository)
                    )
                    SettingsScreen(viewModel = viewModel)
                }

                composable(
                    route = "folder/{folderId}/{folderName}",
                    arguments = listOf(
                        navArgument("folderId") { type = NavType.StringType },
                        navArgument("folderName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val folderIdEncoded = backStackEntry.arguments?.getString("folderId") ?: "ROOT"
                    val folderNameEncoded = backStackEntry.arguments?.getString("folderName") ?: "Folder"
                    
                    val folderId = URLDecoder.decode(folderIdEncoded, StandardCharsets.UTF_8.toString())
                    val folderName = URLDecoder.decode(folderNameEncoded, StandardCharsets.UTF_8.toString())
                    
                    val viewModel: DriveViewModel = viewModel(
                        factory = DriveViewModel.provideFactory(appContainer.driveRepository)
                    )
                    DriveListScreen(
                        folderId = folderId,
                        folderName = folderName,
                        viewModel = viewModel,
                        onNavigateToFolder = { id, name ->
                            val encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString())
                            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                            navController.navigate("folder/$encodedId/$encodedName")
                        },
                        onNavigateToPdf = { downloadUrl, name, uiId ->
                            val encodedUrl = URLEncoder.encode(downloadUrl, StandardCharsets.UTF_8.toString())
                            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                            val encodedUiId = URLEncoder.encode(uiId, StandardCharsets.UTF_8.toString())
                            navController.navigate("pdf/$encodedUrl/$encodedName/$encodedUiId")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "pdf/{downloadUrl}/{fileName}/{uiId}",
                    arguments = listOf(
                        navArgument("downloadUrl") { type = NavType.StringType },
                        navArgument("fileName") { type = NavType.StringType },
                        navArgument("uiId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val downloadUrlEncoded = backStackEntry.arguments?.getString("downloadUrl") ?: ""
                    val fileNameEncoded = backStackEntry.arguments?.getString("fileName") ?: ""
                    val uiIdEncoded = backStackEntry.arguments?.getString("uiId") ?: ""
                    
                    val downloadUrl = URLDecoder.decode(downloadUrlEncoded, StandardCharsets.UTF_8.toString())
                    val fileName = URLDecoder.decode(fileNameEncoded, StandardCharsets.UTF_8.toString())
                    val uiId = URLDecoder.decode(uiIdEncoded, StandardCharsets.UTF_8.toString())
                    
                    val viewModel: DriveViewModel = viewModel(
                        factory = DriveViewModel.provideFactory(appContainer.driveRepository)
                    )
                    PdfViewerScreen(
                        downloadUrl = downloadUrl,
                        fileName = fileName,
                        uiId = uiId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

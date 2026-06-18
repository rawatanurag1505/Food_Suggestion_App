package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object DailyCheckInRoute

@Serializable
object PantryRoute

@Serializable
object ProfileRoute

@Serializable
object SuggestionRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    if (currentUser == null) {
        var showSignup by remember { mutableStateOf(false) }
        if (showSignup) {
            SignupScreen(
                viewModel = viewModel,
                onNavigateToLogin = { showSignup = false },
                onSignupSuccess = { /* Automatically logged in via ViewModel state */ }
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignup = { showSignup = true },
                onLoginSuccess = { /* Automatically logged in via ViewModel state */ }
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Hide bottom bar on deep sub-screens like Suggestion response screen
                if (currentRoute?.contains("SuggestionRoute") != true) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRoute?.contains("HomeRoute") == true || currentRoute == null,
                                onClick = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentRoute?.contains("DailyCheckInRoute") == true,
                                onClick = {
                                    navController.navigate(DailyCheckInRoute) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Suggest") },
                                label = { Text("Suggest") }
                            )
                            NavigationBarItem(
                                selected = currentRoute?.contains("PantryRoute") == true,
                                onClick = {
                                    navController.navigate(PantryRoute) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Kitchen, contentDescription = "Pantry") },
                                label = { Text("Pantry") }
                            )
                            NavigationBarItem(
                                selected = currentRoute?.contains("ProfileRoute") == true,
                                onClick = {
                                    navController.navigate(ProfileRoute) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile") }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                composable<HomeRoute> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSuggest = { navController.navigate(DailyCheckInRoute) },
                        onNavigateToPantry = { navController.navigate(PantryRoute) },
                        onNavigateToProfile = { navController.navigate(ProfileRoute) },
                        onLogout = {
                            // Handled Reactively by AppNavigation checking currentUser == null
                        }
                    )
                }
                composable<DailyCheckInRoute> {
                    DailyCheckInScreen(viewModel, onNavigateToSuggestions = {
                        navController.navigate(SuggestionRoute)
                    })
                }
                composable<PantryRoute> { PantryScreen(viewModel) }
                composable<ProfileRoute> { ProfileScreen(viewModel) }
                composable<SuggestionRoute> {
                    SuggestionScreen(viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

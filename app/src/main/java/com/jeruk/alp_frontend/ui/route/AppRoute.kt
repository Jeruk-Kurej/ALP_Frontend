package com.jeruk.alp_frontend.ui.route

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeruk.alp_frontend.ui.view.*
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel

// Enum diperbarui dengan Icon untuk Bottom Bar
enum class AppView(
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    Welcoming("Welcoming"),
    Login("Login"),
    Register("Register"),
    Home("Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    Setting("Setting", Icons.Filled.Settings, Icons.Outlined.Settings)
}

data class BottomNavItem(val view: AppView, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentView = AppView.entries.find { it.name == currentRoute } ?: AppView.Welcoming

    val bottomNavItems = listOf(
        BottomNavItem(AppView.Home, "Toko"),
        BottomNavItem(AppView.Setting, "Setting")
    )

    Scaffold(
        topBar = {
            // Sembunyikan TopBar di halaman awal
            if (currentView != AppView.Welcoming &&
                currentView != AppView.Login &&
                currentView != AppView.Register &&
                currentView != AppView.Home
            ) {
                MyTopAppBar(currentView = currentView, authViewModel = authViewModel)
            }
        },
        bottomBar = {
            MyBottomNavigationBar(
                navController = navController,
                currentDestination = currentDestination,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Welcoming.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppView.Welcoming.name) {
                WelcomingView(onNavigateToLogin = { navController.navigate(AppView.Login.name) })
            }
            composable(route = AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Welcoming.name) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(AppView.Register.name) }
                )
            }
            composable(route = AppView.Register.name) {
                RegisterView(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Welcoming.name) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(route = AppView.Home.name) {
                val userState by authViewModel.userState.collectAsState()
                TokoView(token = userState.token, navController = navController)
            }
        }
    }
}

@Composable
fun MyBottomNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<BottomNavItem>
) {
    // Bottom Bar hanya muncul di Home/Setting
    if (items.any { it.view.name == currentDestination?.route }) {
        NavigationBar(containerColor = Color.White) {
            items.forEach { item ->
                val isSelected =
                    currentDestination?.hierarchy?.any { it.route == item.view.name } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (isSelected) item.view.selectedIcon!! else item.view.unselectedIcon!!,
                            null
                        )
                    },
                    label = { Text(item.label) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.view.name) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(
    currentView: AppView,
    authViewModel: AuthViewModel
) {
    // Kita observe loading state buat ganti judul jadi "Loading..."
    val isLoading by authViewModel.isLoading.collectAsState()

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = if (isLoading) "Loading..." else currentView.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF6200EE), // Ganti warna sesukamu (misal Ungu/Biru)
            titleContentColor = Color.White
        )
    )
}
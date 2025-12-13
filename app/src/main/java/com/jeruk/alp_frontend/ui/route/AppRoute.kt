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

// 1. Data Class ini harus ada supaya List di bawah tidak merah
data class BottomNavItem(
    val view: AppView,
    val label: String
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentView = AppView.entries.find { it.name == currentRoute } ?: AppView.Welcoming

    Scaffold(
        topBar = {
            // TopBar hanya muncul di halaman tertentu (bukan Welcoming, Login, Register, Home)
            if (currentView != AppView.Welcoming &&
                currentView != AppView.Home &&
                currentView != AppView.Login &&
                currentView != AppView.Register) {
                MyTopAppBar(currentView = currentView, authViewModel = authViewModel)
            }
        },
        bottomBar = {
            MyBottomNavigationBar(
                navController = navController,
                currentDestination = currentDestination,
                items = listOf(
                    BottomNavItem(AppView.Home, "Toko"),
                    BottomNavItem(AppView.Setting, "Setting")
                )
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Welcoming.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- HALAMAN WELCOMING ---
            composable(AppView.Welcoming.name) {
                WelcomingView { navController.navigate(AppView.Login.name) }
            }

            // --- HALAMAN LOGIN ---
            composable(AppView.Login.name) {
                LoginView(
                    onLoginSuccess = {
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Welcoming.name) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(AppView.Register.name) }
                )
            }

            // --- HALAMAN REGISTER (Tadi ini hilang, makanya mungkin error) ---
            composable(AppView.Register.name) {
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

            // --- HALAMAN HOME (TOKO) ---
            composable(AppView.Home.name) {
                val userState by authViewModel.userState.collectAsState()
                TokoView(token = userState.token, navController = navController)
            }

            // --- HALAMAN SETTING ---
            composable(AppView.Setting.name) {
                // Kamu bisa buat SettingView.kt nanti, sementara pakai Text dulu
                Text("Halaman Setting", modifier = Modifier.padding(innerPadding))
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
    if (items.any { it.view.name == currentDestination?.route }) {
        NavigationBar(containerColor = Color.White) {
            items.forEach { item ->
                val isSelected =
                    currentDestination?.hierarchy?.any { it.route == item.view.name } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.view.selectedIcon!! else item.view.unselectedIcon!!,
                            contentDescription = item.label
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
    val isLoading by authViewModel.isLoading.collectAsState()

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = if (isLoading) "Loading..." else currentView.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF6200EE),
            titleContentColor = Color.White
        )
    )
}
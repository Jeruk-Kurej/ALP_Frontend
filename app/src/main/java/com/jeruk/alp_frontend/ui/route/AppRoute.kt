package com.jeruk.alp_frontend.ui.route

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.jeruk.alp_frontend.ui.view.Auth.LoginView
import com.jeruk.alp_frontend.ui.view.Auth.RegisterView
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

    // Waiter Mode
    Home("Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),

    // Admin Mode
    Analysis("Home", Icons.Filled.GridView, Icons.Outlined.GridView),
    AdminProduk("Produk", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    AdminToko("Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    Setting("Setting", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppRoute() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentView = AppView.entries.find { it.name == currentRoute } ?: AppView.Welcoming

    // Logic: Cek apakah user sedang di mode Admin
    val adminRoutes = listOf(AppView.Analysis.name, AppView.AdminProduk.name, AppView.AdminToko.name)

    // Perbaikan: Gunakan safe call agar tidak crash saat backstack kosong
    val prevRoute = try { navController.previousBackStackEntry?.destination?.route } catch (e: Exception) { null }
    val isAdminMode = adminRoutes.contains(currentRoute) || (currentRoute == AppView.Setting.name && prevRoute in adminRoutes)

    Scaffold(
        bottomBar = {
            // Bottom bar hanya muncul jika route tidak null dan sesuai kondisi
            if (currentRoute != null && (currentView == AppView.Home || currentView == AppView.Setting || isAdminMode)) {
                NavigationBar(containerColor = Color.White) {
                    val items = if (isAdminMode) {
                        listOf(
                            BottomNavItem(AppView.Analysis, "Home"),
                            BottomNavItem(AppView.AdminToko, "Toko"),
                            BottomNavItem(AppView.AdminProduk, "Produk"),
                            BottomNavItem(AppView.Setting, "Setting")
                        )
                    } else {
                        listOf(
                            BottomNavItem(AppView.Home, "Toko"),
                            BottomNavItem(AppView.Setting, "Setting")
                        )
                    }

                    items.forEach { item ->
                        val isSelected = currentRoute == item.view.name
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.view.name) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(item.label, fontSize = 10.sp) },
                            icon = {
                                // Gunakan Icon dari enum, pastikan tidak null
                                val icon = if (isSelected) item.view.selectedIcon else item.view.unselectedIcon
                                if (icon != null) {
                                    Icon(icon, contentDescription = null)
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF9333EA),
                                indicatorColor = Color(0xFFF3E8FF)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Welcoming.name, // Pastikan ini ada di bawah!
            modifier = Modifier.padding(innerPadding)
        ) {
            // WAJIB ADA: Rute-rute awal agar tidak Force Close
            composable(AppView.Welcoming.name) {
                WelcomingView { navController.navigate(AppView.Login.name) }
            }

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

            composable(AppView.Home.name) {
                val userState by authViewModel.userState.collectAsState()
                TokoView(token = userState.token, navController = navController)
            }

            // Rute Admin Mode
            composable(AppView.Analysis.name) { AnalysisPageView(navController) }

            composable(AppView.Setting.name) {
                SettingView(
                    navController = navController,
                    onLogout = {
                        navController.navigate(AppView.Welcoming.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
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
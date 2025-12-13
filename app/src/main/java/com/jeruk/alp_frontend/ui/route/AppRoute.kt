package com.jeruk.alp_frontend.ui.route

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    val currentRoute = navBackStackEntry?.destination?.route
    val currentView = AppView.entries.find { it.name == currentRoute } ?: AppView.Welcoming

    Scaffold(
        topBar = {
            // TopBar muncul di Toko (image_167a80) dan Register (image_3054c6)
            if (currentView == AppView.Home || currentView == AppView.Register) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if(currentView == AppView.Home) "Pilih Toko" else "Daftar Akun",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    },
                    navigationIcon = {
                        if (currentView == AppView.Register) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            // BottomBar hanya muncul di Home/Setting
            if (currentView == AppView.Home || currentView == AppView.Setting) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        BottomNavItem(AppView.Home, "Toko"),
                        BottomNavItem(AppView.Setting, "Setting")
                    )
                    items.forEach { item ->
                        val isSelected = currentRoute == item.view.name
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { navController.navigate(item.view.name) },
                            label = { Text(item.label, fontSize = 12.sp) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.view.selectedIcon!! else item.view.unselectedIcon!!,
                                    contentDescription = null
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF9333EA),
                                indicatorColor = Color(0xFFF3E8FF),
                                unselectedIconColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Welcoming.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppView.Welcoming.name) { WelcomingView { navController.navigate(AppView.Login.name) } }
            composable(AppView.Login.name) {
                LoginView(
                    onLoginSuccess = { navController.navigate(AppView.Home.name) },
                    onNavigateToRegister = { navController.navigate(AppView.Register.name) }
                )
            }
            composable(AppView.Register.name) {
                RegisterView(
                    onRegisterSuccess = { navController.navigate(AppView.Home.name) },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(AppView.Home.name) {
                TokoView(token = "TOKEN_HERE", navController = navController)
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
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
import androidx.compose.runtime.*
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
import androidx.navigation.compose.*
import com.jeruk.alp_frontend.ui.view.*
import com.jeruk.alp_frontend.ui.view.Auth.LoginView
import com.jeruk.alp_frontend.ui.view.Auth.RegisterView
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel

// 1. Model untuk Item di Bottom Bar
data class BottomNavItem(
    val view: AppView,
    val label: String
)

// 2. Enum Route dengan metadata Icon
enum class AppView(
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    Welcoming("Welcome"),
    Login("Login"),
    Register("Daftar Akun"),

    // --- Waiter Mode ---
    Home("Pilih Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),

    // --- Admin Mode ---
    Analysis("Dashboard", Icons.Filled.GridView, Icons.Outlined.GridView),
    AdminToko("Kelola Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    AdminProduk("Kelola Produk", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),

    // --- Shared ---
    Setting("Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings),

    // --- Sub-Pages ---
    AnalysisDetail("Detail Analisis"),
    CreateToko("Tambah Toko")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute() {
    val navController = rememberNavController()
    // 1. Inisialisasi AuthViewModel di sini agar state token terjaga selama app jalan
    val authViewModel: AuthViewModel = viewModel()
    val userState by authViewModel.userState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentView = AppView.entries.find { it.name == currentRoute } ?: AppView.Welcoming

    // State Mode Admin
    val adminRoutes = listOf(AppView.Analysis.name, AppView.AdminToko.name, AppView.AdminProduk.name, AppView.AnalysisDetail.name, AppView.CreateToko.name)
    var isUserInAdminMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute in adminRoutes) isUserInAdminMode = true
        else if (currentRoute == AppView.Home.name) isUserInAdminMode = false
    }

    val rootPages = listOf(AppView.Home.name, AppView.Analysis.name, AppView.AdminToko.name, AppView.AdminProduk.name, AppView.Setting.name)
    val canNavigateBack = currentRoute !in rootPages && navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            val noHeaderPages = listOf(AppView.Welcoming.name, AppView.Login.name, AppView.Register.name)
            if (currentRoute !in noHeaderPages && currentRoute != null) {
                MyTopAppBar(currentView, navController, canNavigateBack)
            }
        },
        bottomBar = {
            if (currentRoute in rootPages) {
                val items = if (isUserInAdminMode) {
                    listOf(BottomNavItem(AppView.Analysis, "Home"), BottomNavItem(AppView.AdminToko, "Toko"), BottomNavItem(AppView.AdminProduk, "Produk"), BottomNavItem(AppView.Setting, "Setting"))
                } else {
                    listOf(BottomNavItem(AppView.Home, "Toko"), BottomNavItem(AppView.Setting, "Setting"))
                }
                MyBottomNavigationBar(navController, navBackStackEntry?.destination, items)
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = AppView.Welcoming.name, modifier = Modifier.padding(innerPadding)) {
            composable(AppView.Welcoming.name) { WelcomingView { navController.navigate(AppView.Login.name) } }

            composable(AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel, // Kirim instance yang sama!
                    onLoginSuccess = {
                        navController.navigate(AppView.Home.name)
                    },
                    onNavigateToRegister = { navController.navigate(AppView.Register.name) }
                )
            }

            composable(AppView.Home.name) {
                // Gunakan token dari userState level atas
                TokoView(token = userState.token, navController = navController)
            }

            composable(AppView.AdminToko.name) {
                TokoAdminView(
                    navController = navController,
                    authViewModel = authViewModel // Kirim instance yang sudah punya token!
                )
            }
            composable(AppView.CreateToko.name) {
                // KIRIM TOKEN DARI userState KE CreateTokoView
                CreateTokoView(
                    token = userState.token,
                    onSuccess = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(AppView.Analysis.name) { AnalysisPageView(navController) }
            composable(AppView.Setting.name) {
                if (isUserInAdminMode) {
                    SettingAdminView(navController, onLogout = { navController.navigate(AppView.Welcoming.name) { popUpTo(0) { inclusive = true } } }, onExitAdminMode = { isUserInAdminMode = false; navController.navigate(AppView.Home.name) })
                } else {
                    SettingView(navController, onLogout = { navController.navigate(AppView.Welcoming.name) { popUpTo(0) { inclusive = true } } })
                }
            }
        }
    }
}

// --- HELPER COMPONENTS (HIG STYLE) ---

@Composable
fun MyBottomNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<BottomNavItem>
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.view.name } == true
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
                    val icon = if (isSelected) item.view.selectedIcon else item.view.unselectedIcon
                    if (icon != null) Icon(icon, contentDescription = null)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(
    currentView: AppView,
    navController: NavHostController,
    canNavigateBack: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = currentView.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}
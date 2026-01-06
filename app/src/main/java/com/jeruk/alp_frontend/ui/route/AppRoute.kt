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
import com.jeruk.alp_frontend.ui.view.Analysis.AnalysisPageView
import com.jeruk.alp_frontend.ui.view.Auth.LoginView
import com.jeruk.alp_frontend.ui.view.Auth.RegisterView
import com.jeruk.alp_frontend.ui.view.Setting.SettingAdminView
import com.jeruk.alp_frontend.ui.view.Setting.SettingView
import com.jeruk.alp_frontend.ui.view.Product.AddProductView
import com.jeruk.alp_frontend.ui.view.Product.ProductAdminView
import com.jeruk.alp_frontend.ui.view.Product.UpdateProductView
import com.jeruk.alp_frontend.ui.view.Category.AddCategoryView
import com.jeruk.alp_frontend.ui.view.Category.UpdateCategoryView
import com.jeruk.alp_frontend.ui.view.Toko.CreateTokoView
import com.jeruk.alp_frontend.ui.view.Toko.TokoAdminView
import com.jeruk.alp_frontend.ui.view.Toko.TokoView
import com.jeruk.alp_frontend.ui.view.Toko.UpdateTokoView
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
    CreateToko("Tambah Toko"),
    UpdateToko("Edit Toko"),
    AddProduct("Tambah Produk"),
    UpdateProduct("Edit Produk"),
    AddCategory("Tambah Kategori"),
    UpdateCategory("Edit Kategori")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute() {
    val navController = rememberNavController()
    // Inisialisasi AuthViewModel di level Root agar token awet
    val authViewModel: AuthViewModel = viewModel()
    val userState by authViewModel.userState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentView =
        AppView.entries.find { it.name?.split("/")?.first() == it.name } ?: AppView.Welcoming

    // State Mode Admin
    val adminRoutes = listOf(
        AppView.Analysis.name,
        AppView.AdminToko.name,
        AppView.AdminProduk.name,
        AppView.AnalysisDetail.name,
        AppView.CreateToko.name,
        AppView.UpdateToko.name,
        AppView.AddProduct.name,
        AppView.UpdateProduct.name,
        AppView.AddCategory.name,
        AppView.UpdateCategory.name
    )
    val waiterRoutes = listOf(
        AppView.Home.name
    )
    var isUserInAdminMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        // Cek rute admin (termasuk rute dengan parameter seperti UpdateToko/1)
        val cleanRoute = currentRoute?.split("/")?.first()
        if (cleanRoute in adminRoutes) {
            isUserInAdminMode = true
        } else if (cleanRoute in waiterRoutes) {
            isUserInAdminMode = false
        }
        // Setting page keeps current mode
    }

    val rootPages = listOf(
        AppView.Home.name,
        AppView.Analysis.name,
        AppView.AdminToko.name,
        AppView.AdminProduk.name,
        AppView.Setting.name
    )

    val canNavigateBack = currentRoute !in rootPages && navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            val noHeaderPages =
                listOf(AppView.Welcoming.name, AppView.Login.name, AppView.Register.name)
            val currentBaseRoute = currentRoute?.split("/")?.first()

            if (currentBaseRoute !in noHeaderPages && currentRoute != null) {
                // Cari title berdasarkan enum yang cocok
                val displayView =
                    AppView.entries.find { it.name == currentBaseRoute } ?: currentView
                MyTopAppBar(displayView, navController, canNavigateBack)
            }
        },
        bottomBar = {
            if (currentRoute in rootPages) {
                val items = if (isUserInAdminMode) {
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
                MyBottomNavigationBar(navController, navBackStackEntry?.destination, items)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Welcoming.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- AUTHENTICATION ---
            composable(AppView.Welcoming.name) {
                WelcomingView { navController.navigate(AppView.Login.name) }
            }

            composable(AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel,
                    onLoginSuccess = { navController.navigate(AppView.Home.name) },
                    onNavigateToRegister = { navController.navigate(AppView.Register.name) }
                )
            }

            composable(AppView.Register.name) {
                RegisterView(
                    authViewModel = authViewModel,
                    onRegisterSuccess = { navController.navigate(AppView.Home.name) },
                    onNavigateToLogin = { navController.navigate(AppView.Login.name) }
                )
            }

            // --- WAITER MODE ---
            composable(AppView.Home.name) {
                TokoView(token = userState.token, navController = navController)
            }

            // --- ADMIN MODE: DASHBOARD & TOKO ---
            composable(AppView.Analysis.name) {
                AnalysisPageView(navController)
            }

            composable(AppView.AdminToko.name) {
                TokoAdminView(navController = navController, authViewModel = authViewModel)
            }

            // Di dalam NavHost AppRoute.kt
            composable(AppView.CreateToko.name) {
                CreateTokoView(
                    token = userState.token,
                    onSuccess = {
                        // INI HARUS ADA DAN BENAR!
                        navController.popBackStack()
                    },
                    navController = navController
                )
            }

            composable("${AppView.UpdateToko.name}/{tokoId}") { backStackEntry ->
                val tokoId = backStackEntry.arguments?.getString("tokoId")?.toInt() ?: 0
                UpdateTokoView(
                    token = userState.token,
                    tokoId = tokoId,
                    onSuccess = {
                        // INI JUGA HARUS ADA!
                        navController.popBackStack()
                    },
                    navController = navController
                )
            }

            // --- ADMIN MODE: PRODUCT & CATEGORY ---
            composable(AppView.AdminProduk.name) {
                ProductAdminView(navController, token = userState.token)
            }

            composable(AppView.AddProduct.name) {
                AddProductView(navController = navController, token = userState.token)
            }

            composable("${AppView.UpdateProduct.name}/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
                UpdateProductView(
                    token = userState.token,
                    productId = productId,
                    onSuccess = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(AppView.AddCategory.name) {
                AddCategoryView(navController = navController, token = userState.token)
            }

            composable("${AppView.UpdateCategory.name}/{categoryId}") { backStackEntry ->
                val categoryId =
                    backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
                UpdateCategoryView(
                    token = userState.token,
                    categoryId = categoryId,
                    onSuccess = { navController.popBackStack() },
                    navController = navController
                )
            }

            // --- SETTING ---
            composable(AppView.Setting.name) {
                if (isUserInAdminMode) {
                    SettingAdminView(
                        navController = navController,
                        onLogout = {
                            navController.navigate(AppView.Welcoming.name) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onExitAdminMode = {
                            // 1. Matikan Mode Admin
                            isUserInAdminMode = false

                            // 2. Navigasi Pindah Tab yang BENAR
                            navController.navigate(AppView.Home.name) {
                                // Pop up ke Root (Welcoming) agar behavior sama seperti klik BottomBar
                                popUpTo(navController.graph.findStartDestination().id) {
                                    // PENTING: saveState = false
                                    // Kita TIDAK mau menyimpan state halaman Admin ini.
                                    // Biar nanti kalau balik ke Setting, halamannya fresh/reset.
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState =
                                    true // Mengembalikan state Home yang lama (yang di-stash)
                            }
                        }
                    )
                } else {
                    SettingView(
                        navController = navController,
                        onLogout = {
                            navController.navigate(AppView.Welcoming.name) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun MyBottomNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<BottomNavItem>
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        items.forEach { item ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.route == item.view.name } == true
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
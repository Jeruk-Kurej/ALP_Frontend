package com.jeruk.alp_frontend.ui.route

import androidx.activity.compose.BackHandler
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
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.jeruk.alp_frontend.data.container.AppContainer

// Import Views
import com.jeruk.alp_frontend.ui.view.Analysis.AnalysisPageView
import com.jeruk.alp_frontend.ui.view.Analysis.AnalysisDetailView
import com.jeruk.alp_frontend.ui.view.Auth.LoginView
import com.jeruk.alp_frontend.ui.view.Auth.RegisterView
import com.jeruk.alp_frontend.ui.view.Setting.SettingAdminView
import com.jeruk.alp_frontend.ui.view.Setting.SettingView
import com.jeruk.alp_frontend.ui.view.Product.AddProductView
import com.jeruk.alp_frontend.ui.view.Product.ProductAdminView
import com.jeruk.alp_frontend.ui.view.Product.UpdateProductView
import com.jeruk.alp_frontend.ui.view.Category.AddCategoryView
import com.jeruk.alp_frontend.ui.view.Category.UpdateCategoryView
import com.jeruk.alp_frontend.ui.view.Order.CashPageView
import com.jeruk.alp_frontend.ui.view.Order.OrderPageView
import com.jeruk.alp_frontend.ui.view.Order.PaymentPageView
import com.jeruk.alp_frontend.ui.view.Order.QRISPageView
import com.jeruk.alp_frontend.ui.view.Order.SuccessPageView
import com.jeruk.alp_frontend.ui.view.Product.ProductMenuView
import com.jeruk.alp_frontend.ui.view.Toko.CreateTokoView
import com.jeruk.alp_frontend.ui.view.Toko.TokoAdminView
import com.jeruk.alp_frontend.ui.view.Toko.TokoView
import com.jeruk.alp_frontend.ui.view.Toko.UpdateTokoView
import com.jeruk.alp_frontend.ui.view.WelcomingView
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel
import com.jeruk.alp_frontend.ui.viewmodel.OrderViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel

data class BottomNavItem(
    val view: AppView,
    val label: String
)

enum class AppView(
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    Welcoming("Welcome"),
    Login("Login"),
    Register("Daftar Akun"),
    Home("Pilih Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    ProductMenu("Menu Toko"),
    OrderPage("Keranjang Belanja"),
    PaymentPage("Pembayaran"),
    QRISPage("Scan QRIS"),
    CashPage("Pembayaran Tunai"),
    SuccessPage("Pembayaran Selesai"),
    Analysis("Dashboard", Icons.Filled.GridView, Icons.Outlined.GridView),
    AdminToko("Kelola Toko", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    AdminProduk("Kelola Produk", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    Setting("Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings),
    AnalysisDetail("Detail Analisis"),
    CreateToko("Tambah Toko"),
    UpdateToko("Edit Toko"),
    AddProduct("Tambah Produk"),
    UpdateProduct("Edit Produk"),
    AddCategory("Tambah Kategori"),
    UpdateCategory("Edit Kategori"),
    AdminHome("Dashboard Admin"),
    WaiterHome("Home Waiter")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute(
    startDestination: String = AppView.Welcoming.name
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    // --- FIX 1: MONITORING TOKEN DARI DATASTORE SECARA REALTIME ---
    val userToken by AppContainer.userPreferencesRepository.userToken.collectAsState(initial = "loading")

    LaunchedEffect(userToken) {
        val authPages = listOf(AppView.Welcoming.name, AppView.Login.name, AppView.Register.name)
        val currentBaseRoute =
            navController.currentBackStackEntry?.destination?.route?.split("/")?.first()

        // Jika token dihapus Interceptor (karena 401) dan user sedang di halaman aplikasi utama
        if ((userToken == null || userToken == "") && currentBaseRoute !in authPages && userToken != "loading") {
            navController.navigate(AppView.Welcoming.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val userState by authViewModel.userState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logika Mode Admin/Waiter
    val adminRoutes = listOf(
        AppView.Analysis.name, AppView.AdminToko.name, AppView.AdminProduk.name,
        AppView.AnalysisDetail.name, AppView.CreateToko.name, AppView.UpdateToko.name,
        AppView.AddProduct.name, AppView.UpdateProduct.name, AppView.AddCategory.name,
        AppView.UpdateCategory.name, AppView.AdminHome.name
    )
    val waiterRoutes = listOf(
        AppView.Home.name, AppView.ProductMenu.name, AppView.OrderPage.name,
        AppView.PaymentPage.name, AppView.QRISPage.name, AppView.CashPage.name,
        AppView.SuccessPage.name, AppView.WaiterHome.name
    )

    var isUserInAdminMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        val cleanRoute = currentRoute?.split("/")?.first()
        if (cleanRoute in adminRoutes) {
            isUserInAdminMode = true
        } else if (cleanRoute in waiterRoutes) {
            isUserInAdminMode = false
        }
    }

    val rootPages = listOf(
        AppView.Home.name, AppView.Analysis.name, AppView.AdminToko.name,
        AppView.AdminProduk.name, AppView.Setting.name,
        AppView.AdminHome.name, AppView.WaiterHome.name
    )

    val canNavigateBack = currentRoute !in rootPages && navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            val noHeaderPages = listOf(
                AppView.Welcoming.name,
                AppView.Login.name,
                AppView.Register.name,
                AppView.SuccessPage.name
            )
            val currentBaseRoute = currentRoute?.split("/")?.first()

            if (currentBaseRoute !in noHeaderPages && currentRoute != null) {
                val currentView =
                    AppView.entries.find { it.name == currentBaseRoute } ?: AppView.Welcoming
                val titleText = if (currentBaseRoute == AppView.ProductMenu.name) {
                    val name = navBackStackEntry?.arguments?.getString("tokoName") ?: ""
                    val address = navBackStackEntry?.arguments?.getString("tokoAddress") ?: ""
                    if (address.isNotEmpty()) "$name - $address" else name
                } else {
                    currentView.title
                }
                MyTopAppBar(titleText, navController, canNavigateBack)
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppView.Welcoming.name) {
                WelcomingView { navController.navigate(AppView.Login.name) }
            }

            composable(AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        // --- FIX 2: MEMBERSIHKAN BACKSTACK SETELAH LOGIN ---
                        val target =
                            if (authViewModel.userState.value.role == "admin") AppView.Analysis.name else AppView.Home.name
                        navController.navigate(target) {
                            popUpTo(AppView.Login.name) { inclusive = true }
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
                            popUpTo(AppView.Register.name) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(AppView.Login.name) }
                )
            }

            // --- WAITER MODE ---
            composable(AppView.WaiterHome.name) {
                TokoView(
                    token = userState.token,
                    navController = navController
                )
            }
            composable(AppView.Home.name) {
                TokoView(
                    token = userState.token,
                    navController = navController
                )
            }

            composable(
                route = "${AppView.ProductMenu.name}/{tokoId}/{tokoName}/{tokoAddress}",
                arguments = listOf(
                    navArgument("tokoId") { type = NavType.IntType },
                    navArgument("tokoName") { type = NavType.StringType },
                    navArgument("tokoAddress") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ProductMenuView(
                    navController = navController,
                    token = userState.token,
                    tokoId = backStackEntry.arguments?.getInt("tokoId") ?: 0,
                    tokoName = backStackEntry.arguments?.getString("tokoName") ?: "Toko",
                    productViewModel = productViewModel
                )
            }

            composable(
                "${AppView.OrderPage.name}/{token}/{tokoId}",
                listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType })
            ) {
                OrderPageView(
                    navController,
                    productViewModel,
                    it.arguments?.getString("token") ?: "",
                    it.arguments?.getInt("tokoId") ?: 0
                )
            }

            composable(
                "${AppView.PaymentPage.name}/{token}/{tokoId}",
                listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType })
            ) {
                PaymentPageView(
                    navController,
                    productViewModel,
                    it.arguments?.getString("token") ?: "",
                    it.arguments?.getInt("tokoId") ?: 0
                )
            }

            composable(
                "QRISPage/{token}/{tokoId}",
                listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType })
            ) {
                QRISPageView(
                    navController,
                    productViewModel,
                    orderViewModel,
                    it.arguments?.getString("token") ?: "",
                    it.arguments?.getInt("tokoId") ?: 0
                )
            }

            composable(
                "CashPage/{token}/{tokoId}",
                listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType })
            ) {
                CashPageView(
                    navController,
                    productViewModel,
                    orderViewModel,
                    it.arguments?.getString("token") ?: "",
                    it.arguments?.getInt("tokoId") ?: 0
                )
            }

            composable(AppView.SuccessPage.name) {
                BackHandler(enabled = true) {
                    navController.navigate(AppView.Home.name) {
                        popUpTo(AppView.Home.name) {
                            inclusive = true
                        }
                    }
                }
                SuccessPageView(navController, productViewModel)
            }

            // --- ADMIN MODE ---
            composable(AppView.AdminHome.name) {
                AnalysisPageView(
                    navController,
                    token = userState.token
                )
            }
            composable(AppView.Analysis.name) {
                AnalysisPageView(
                    navController,
                    token = userState.token
                )
            }
            composable(AppView.AnalysisDetail.name) {
                AnalysisDetailView(
                    navController,
                    userState.token
                )
            }
            composable(AppView.AdminToko.name) { TokoAdminView(navController, authViewModel) }
            composable(AppView.CreateToko.name) {
                CreateTokoView(
                    userState.token,
                    { navController.popBackStack() },
                    navController
                )
            }
            composable("${AppView.UpdateToko.name}/{tokoId}") {
                UpdateTokoView(
                    userState.token,
                    it.arguments?.getString("tokoId")?.toInt() ?: 0,
                    { navController.popBackStack() },
                    navController
                )
            }
            composable(AppView.AdminProduk.name) {
                ProductAdminView(
                    navController,
                    userState.token
                )
            }
            composable(AppView.AddProduct.name) { AddProductView(navController, userState.token) }
            composable("${AppView.UpdateProduct.name}/{productId}") { backStackEntry ->
                // Pastikan kita ambil productId dengan aman
                val productIdString = backStackEntry.arguments?.getString("productId")
                val productIdInt = productIdString?.toIntOrNull() ?: 0

                UpdateProductView(
                    token = userState.token,
                    productId = productIdInt, // Pastikan di View-nya butuh Int
                    onSuccess = { navController.popBackStack() },
                    navController = navController
                )
            }
            composable(AppView.AddCategory.name) { AddCategoryView(navController, userState.token) }
            composable("${AppView.UpdateCategory.name}/{categoryId}") {
                UpdateCategoryView(
                    userState.token,
                    it.arguments?.getString("categoryId")?.toIntOrNull() ?: 0,
                    { navController.popBackStack() },
                    navController
                )
            }

            // --- SETTING & LOGOUT ---
            composable(AppView.Setting.name) {
                if (isUserInAdminMode) {
                    SettingAdminView(
                        navController = navController,
                        onLogout = {
                            authViewModel.logout()
                            // Navigasi logout sudah di-handle oleh LaunchedEffect(userToken) secara global
                        },
                        onExitAdminMode = {
                            isUserInAdminMode = false
                            navController.navigate(AppView.Home.name) {
                                popToStartDestination(navController)
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                } else {
                    SettingView(
                        navController = navController,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}

// --- HELPER UNTUK NAVIGASI ---
fun NavOptionsBuilder.popToStartDestination(navController: NavHostController) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
}

// ... MyBottomNavigationBar dan MyTopAppBar tetap sama seperti kode sebelumnya

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
    title: String,
    navController: NavHostController,
    canNavigateBack: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
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
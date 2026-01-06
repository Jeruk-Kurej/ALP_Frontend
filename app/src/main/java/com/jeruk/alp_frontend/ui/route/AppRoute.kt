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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// Import semua View kamu
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
    ProductMenu("Menu Toko"),
    OrderPage("Keranjang Belanja"),
    PaymentPage("Pembayaran"),
    QRISPage("Scan QRIS"),
    CashPage("Pembayaran Tunai"),
    SuccessPage("Pembayaran Selesai"),

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
    UpdateCategory("Edit Kategori"),

    // Tambahan untuk mapping dari MainActivity (Opsional, jika kamu pakai nama ini di MainActivity)
    AdminHome("Dashboard Admin"),
    WaiterHome("Home Waiter")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute(
    // 🔥 PERBAIKAN UTAMA: Tambah parameter ini
    startDestination: String = AppView.Welcoming.name
) {
    val navController = rememberNavController()

    // --- VIEWMODELS ---
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    val userState by authViewModel.userState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logic menentukan mode Admin/Waiter
    val adminRoutes = listOf(
        AppView.Analysis.name, AppView.AdminToko.name, AppView.AdminProduk.name,
        AppView.AnalysisDetail.name, AppView.CreateToko.name, AppView.UpdateToko.name,
        AppView.AddProduct.name, AppView.UpdateProduct.name, AppView.AddCategory.name,
        AppView.UpdateCategory.name, AppView.AdminHome.name
    )
    val waiterRoutes = listOf(
        AppView.Home.name,
        AppView.ProductMenu.name,
        AppView.OrderPage.name,
        AppView.PaymentPage.name,
        AppView.QRISPage.name,
        AppView.CashPage.name,
        AppView.SuccessPage.name,
        AppView.WaiterHome.name
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

    // List halaman utama yang menampilkan Bottom Bar
    val rootPages = listOf(
        AppView.Home.name, AppView.Analysis.name, AppView.AdminToko.name,
        AppView.AdminProduk.name, AppView.Setting.name,
        // Mapping tambahan jika diperlukan
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
            // 🔥 PERBAIKAN UTAMA: Gunakan parameter startDestination disini
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- AUTHENTICATION ---
            composable(AppView.Welcoming.name) {
                WelcomingView { navController.navigate(AppView.Login.name) }
            }

            composable(AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel,
                    // Redirect Login sesuai role
                    onLoginSuccess = {
                        // Cek role manual di sini jika login manual
                        val target = if(authViewModel.userState.value.role == "admin") AppView.Analysis.name else AppView.Home.name
                        navController.navigate(target)
                    },
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
            // Mapping WaiterHome ke tampilan Home (TokoView)
            composable(AppView.WaiterHome.name) {
                TokoView(token = userState.token, navController = navController)
            }

            composable(AppView.Home.name) {
                TokoView(token = userState.token, navController = navController)
            }

            composable(
                route = "${AppView.ProductMenu.name}/{tokoId}/{tokoName}/{tokoAddress}",
                arguments = listOf(
                    navArgument("tokoId") { type = NavType.IntType },
                    navArgument("tokoName") { type = NavType.StringType },
                    navArgument("tokoAddress") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tokoId = backStackEntry.arguments?.getInt("tokoId") ?: 0
                val tokoName = backStackEntry.arguments?.getString("tokoName") ?: "Toko"

                ProductMenuView(
                    navController = navController,
                    token = userState.token,
                    tokoId = tokoId,
                    tokoName = tokoName,
                    productViewModel = productViewModel
                )
            }

            composable(
                route = "${AppView.OrderPage.name}/{token}/{tokoId}",
                arguments = listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val tokoId = backStackEntry.arguments?.getInt("tokoId") ?: 0
                OrderPageView(
                    navController = navController,
                    productViewModel = productViewModel,
                    token = token,
                    tokoId = tokoId
                )
            }

            composable(
                route = "${AppView.PaymentPage.name}/{token}/{tokoId}",
                arguments = listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val tokoId = backStackEntry.arguments?.getInt("tokoId") ?: 0
                PaymentPageView(
                    navController = navController,
                    productViewModel = productViewModel,
                    token = token,
                    tokoId = tokoId
                )
            }

            composable(
                route = "QRISPage/{token}/{tokoId}",
                arguments = listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val tokoId = backStackEntry.arguments?.getInt("tokoId") ?: 0
                QRISPageView(
                    navController = navController,
                    productViewModel = productViewModel,
                    orderViewModel = orderViewModel,
                    token = token,
                    tokoId = tokoId
                )
            }

            composable(
                route = "CashPage/{token}/{tokoId}",
                arguments = listOf(
                    navArgument("token") { type = NavType.StringType },
                    navArgument("tokoId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val tokoId = backStackEntry.arguments?.getInt("tokoId") ?: 0
                CashPageView(
                    navController = navController,
                    productViewModel = productViewModel,
                    orderViewModel = orderViewModel,
                    token = token,
                    tokoId = tokoId
                )
            }

            composable(AppView.SuccessPage.name) {
                BackHandler(enabled = true) {
                    navController.navigate(AppView.Home.name) {
                        popUpTo(AppView.Home.name) { inclusive = true }
                    }
                }
                SuccessPageView(navController, productViewModel)
            }

            // --- ADMIN MODE ---
            // Mapping AdminHome ke AnalysisView
            composable(AppView.AdminHome.name) {
                AnalysisPageView(navController, token = userState.token)
            }

            composable(AppView.Analysis.name) {
                AnalysisPageView(
                    navController,
                    token = userState.token
                )
            }

            composable(AppView.AnalysisDetail.name) {
                AnalysisDetailView(
                    navController = navController,
                    token = userState.token
                )
            }

            composable(AppView.AdminToko.name) {
                TokoAdminView(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(AppView.CreateToko.name) {
                CreateTokoView(
                    token = userState.token,
                    onSuccess = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable("${AppView.UpdateToko.name}/{tokoId}") { backStackEntry ->
                val tokoId = backStackEntry.arguments?.getString("tokoId")?.toInt() ?: 0
                UpdateTokoView(
                    token = userState.token,
                    tokoId = tokoId,
                    onSuccess = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(AppView.AdminProduk.name) {
                ProductAdminView(
                    navController,
                    token = userState.token
                )
            }
            composable(AppView.AddProduct.name) {
                AddProductView(
                    navController = navController,
                    token = userState.token
                )
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
                AddCategoryView(
                    navController = navController,
                    token = userState.token
                )
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
                            authViewModel.logout() // Pastikan ViewModel juga logout
                            navController.navigate(AppView.Welcoming.name) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
                        },
                        onExitAdminMode = {
                            isUserInAdminMode = false
                            navController.navigate(AppView.Home.name) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                } else {
                    SettingView(
                        navController = navController,
                        onLogout = {
                            authViewModel.logout()
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
package com.jeruk.alp_frontend.ui.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeruk.alp_frontend.ui.view.LoginView
import com.jeruk.alp_frontend.ui.view.RegisterView
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel

// 1. Enum untuk Daftar Halaman
enum class AppView(val title: String) {
    Login("Login"),
    Register("Register"),
    Home("Home")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoute() {
    val navController = rememberNavController()

    // Inisialisasi ViewModel di level Route (Parent)
    // Supaya bisa di-share ke Login dan Register
    val authViewModel: AuthViewModel = viewModel()

    // Cek halaman aktif sekarang buat judul TopBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentView = AppView.entries.find { it.name == currentRoute } ?: AppView.Login

    Scaffold(
        topBar = {
            // Kita pakai TopAppBar custom mirip punya kamu
            MyTopAppBar(
                currentView = currentView,
                authViewModel = authViewModel
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Login.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- HALAMAN LOGIN ---
            composable(route = AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        // Kalau sukses login, pindah ke Home & Hapus history login (biar di-back g balik login)
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Login.name) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(AppView.Register.name)
                    }
                )
            }

            // --- HALAMAN REGISTER ---
            composable(route = AppView.Register.name) {
                RegisterView(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        // Kalau sukses register, langsung masuk ke Home juga
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Login.name) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack() // Balik ke halaman sebelumnya (Login)
                    }
                )
            }

            // --- HALAMAN HOME (Placeholder Dulu) ---
            composable(route = AppView.Home.name) {
                // Nanti ini diganti HomePage beneran
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Selamat Datang di Home Page!")
                }
            }
        }
    }
}

// TopAppBar ala ArtistExplorerApp
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
package com.jeruk.alp_frontend.ui.route

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeruk.alp_frontend.ui.view.LoginView
import com.jeruk.alp_frontend.ui.view.RegisterView
import com.jeruk.alp_frontend.ui.view.WelcomingView
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel

enum class AppView(val title: String) {
    Welcoming("Welcoming"),
    Login("Login"),
    Register("Register"),
    Home("Home")
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
            if (currentView != AppView.Welcoming &&
                currentView != AppView.Home &&
                currentView != AppView.Login &&
                currentView != AppView.Register) {
                MyTopAppBar(currentView = currentView, authViewModel = authViewModel)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppView.Welcoming.name, // START DARI WELCOMING
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- HALAMAN WELCOMING ---
            composable(route = AppView.Welcoming.name) {
                WelcomingView(
                    onNavigateToLogin = {
                        navController.navigate(AppView.Login.name)
                    }
                )
            }

            // --- HALAMAN LOGIN ---
            composable(route = AppView.Login.name) {
                LoginView(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Welcoming.name) { inclusive = true }
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
                        navController.navigate(AppView.Home.name) {
                            popUpTo(AppView.Welcoming.name) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
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
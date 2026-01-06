package com.jeruk.alp_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.ui.route.AppRoute
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.theme.ALP_FrontendTheme
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Agar tampilan full screen modern
        setContent {
            ALP_FrontendTheme {
                // 1. Panggil ViewModel Auth
                val authViewModel: AuthViewModel = viewModel()

                // 2. Pantau data user (Token & Role) secara real-time
                // Data ini otomatis terisi dari DataStore karena fungsi init di AuthViewModel
                val userState by authViewModel.userState.collectAsState()

                // 3. LOGIKA PENENTUAN HALAMAN AWAL (Start Destination)
                val startDestination = if (userState.token.isNotEmpty()) {
                    // Jika Token Ada (Sudah Login) -> Cek Role
                    if (userState.role == "admin") {
                        AppView.AdminHome.name
                    } else {
                        AppView.WaiterHome.name
                    }
                } else {
                    // Jika Token Kosong (Belum Login) -> Ke Halaman Login
                    AppView.Login.name
                }

                // 4. Kirim startDestination ke AppRoute
                // (Pastikan file AppRoute.kt kamu menerima parameter ini, lihat langkah di bawah)
                AppRoute(startDestination = startDestination)
            }
        }
    }
}
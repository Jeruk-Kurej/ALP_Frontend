package com.jeruk.alp_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.ui.route.AppRoute
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.theme.ALP_FrontendTheme
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ALP_FrontendTheme {
                val authViewModel: AuthViewModel = viewModel()
                val userState by authViewModel.userState.collectAsState()

                // State Loading (Default True = Muncul Spinner dulu)
                var isCheckingToken by remember { mutableStateOf(true) }
                // Default tujuan awal adalah Login
                var startDestination by remember { mutableStateOf(AppView.Login.name) }

                // --- LOGIKA UTAMA: VALIDASI TOKEN ---
                LaunchedEffect(Unit) {
                    // Beri jeda sedikit (100ms) untuk memastikan DataStore selesai membaca data dari HP
                    // Ini penting agar userState.token tidak terbaca kosong padahal ada datanya
                    delay(100)

                    val token = authViewModel.userState.value.token

                    if (token.isNotEmpty()) {
                        // Jika ada token, JANGAN LANGSUNG MASUK.
                        // Tanya server dulu: "Valid gak?"
                        authViewModel.validateToken(
                            token = token,
                            onSuccess = {
                                // Server bilang OK -> Cek Role untuk tentukan arah
                                val role = authViewModel.userState.value.role
                                startDestination = if (role == "admin") {
                                    AppView.AdminHome.name
                                } else {
                                    AppView.WaiterHome.name
                                }
                                // Matikan loading, tampilkan halaman
                                isCheckingToken = false
                            },
                            onError = {
                                // Server bilang Token Basi (Error 401) -> Lempar ke Login
                                startDestination = AppView.Login.name
                                isCheckingToken = false
                            }
                        )
                    } else {
                        // Tidak ada token di HP -> Langsung ke Login
                        startDestination = AppView.Login.name
                        isCheckingToken = false
                    }
                }

                // --- UI ---
                if (isCheckingToken) {
                    // 1. Tampilkan Loading Putih + Spinner di tengah layar
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // 2. Setelah selesai cek, baru panggil AppRoute
                    AppRoute(startDestination = startDestination)
                }
            }
        }
    }
}
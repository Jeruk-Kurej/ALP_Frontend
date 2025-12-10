package com.jeruk.alp_frontend.ui.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel

@Composable
fun LoginView(
    // Nanti kita pakai ini buat navigasi ke Register atau Home
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,

    // Inisialisasi ViewModel langsung di sini (Tanpa Factory ribet)
    authViewModel: AuthViewModel = viewModel()
) {
    // 1. Observe State dari ViewModel
    //    Kita 'mengintip' isi userState dan isLoading secara realtime
    val userState by authViewModel.userState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Context buat nampilin Toast
    val context = LocalContext.current

    // Variable buat nyimpen text inputan user
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2. Side Effect: Bereaksi kalau state berubah
    //    Kode di dalam sini akan jalan setiap kali 'userState' berubah
    LaunchedEffect(userState) {
        // A. Kalau Token ada isinya -> Berarti Sukses Login!
        if (userState.token.isNotEmpty()) {
            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
            onLoginSuccess() // Pindah halaman
        }

        // B. Kalau isError true -> Munculin pesan error
        if (userState.isError) {
            Toast.makeText(context, userState.errorMessage, Toast.LENGTH_LONG).show()
            // (Opsional) Reset error biar gak muncul terus
            // authViewModel.resetError()
        }
    }

    // 3. Tampilan UI (Layout)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Page", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Login
        Button(
            onClick = {
                // Panggil fungsi login di ViewModel
                authViewModel.login(email, password)
            },
            enabled = !isLoading, // Matikan tombol kalau lagi loading
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol ke Register
        TextButton(onClick = { onNavigateToRegister() }) {
            Text("Belum punya akun? Register di sini")
        }
    }
}
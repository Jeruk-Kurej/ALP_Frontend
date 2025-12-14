package com.jeruk.alp_frontend.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.route.AppView

@Composable
fun SettingAdminView(
    navController: NavController,
    onLogout: () -> Unit,
    onExitAdminMode: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Konten Placeholder
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Bahasa & Mata Uang (Gunakan komponen yang sudah kita buat sebelumnya)
            Text("Pengaturan Admin", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Menu Khusus Admin: Keluar dari Mode Admin
            ActionItem(
                title = "Keluar dari Mode Admin",
                sub = "Kembali ke tampilan Waiter",
                icon = Icons.Outlined.Storefront,
                iconBg = Color(0xFFF3E8FF),
                iconTint = Color(0xFF9333EA)
            ) {
                onExitAdminMode()
            }

            // Menu Logout
            ActionItem(
                title = "Keluar",
                sub = "Logout dari akun Anda",
                icon = Icons.Outlined.Logout,
                iconBg = Color(0xFFFFF1F0),
                iconTint = Color(0xFFF44336)
            ) {
                showLogoutConfirm = true
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar?") },
            confirmButton = {
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal") }
            }
        )
    }
}
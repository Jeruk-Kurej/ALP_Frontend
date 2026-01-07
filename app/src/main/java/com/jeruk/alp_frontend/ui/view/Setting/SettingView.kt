package com.jeruk.alp_frontend.ui.view.Setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.data.container.AppContainer
import kotlinx.coroutines.launch

@Composable
fun SettingView(
    navController: NavController,
    onLogout: () -> Unit
) {
    // Get preferences repository
    val userPreferences = AppContainer.userPreferencesRepository
    val scope = rememberCoroutineScope()
    
    // State untuk Toggles (Lokal)
    var selectedLanguage by remember { mutableStateOf("Indonesia") }
    
    // ✅ Read currency from DataStore (reactive)
    val selectedCurrency by userPreferences.selectedCurrency.collectAsState(initial = "IDR")

    // State untuk Popups
    var showAdminDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- KARTU MATA UANG ---
            SettingCard(
                title = "Mata Uang",
                sub = "Pilih mata uang transaksi",
                icon = Icons.Outlined.Payments,
                iconBg = Color(0xFFFCE4EC),
                iconTint = Color(0xFFE91E63)
            ) {
                CurrencyOption(
                    "Indonesian Rupiah",
                    "Rp",
                    selectedCurrency == "IDR"
                ) { 
                    // ✅ Save to DataStore
                    scope.launch { userPreferences.setCurrency("IDR") }
                }
                CurrencyOption("US Dollar", "$", selectedCurrency == "USD") {
                    // ✅ Save to DataStore
                    scope.launch { userPreferences.setCurrency("USD") }
                }
                CurrencyOption("Euro", "€", selectedCurrency == "EUR") {
                    scope.launch { userPreferences.setCurrency("EUR") }
                }
                CurrencyOption("Japanese Yen", "¥", selectedCurrency == "JPY") {
                    scope.launch { userPreferences.setCurrency("JPY") }
                }
                CurrencyOption("British Pound", "£", selectedCurrency == "GBP") {
                    scope.launch { userPreferences.setCurrency("GBP") }
                }
            }

            // --- ADMIN MODE ---
            ActionItem(
                title = "Admin Mode",
                sub = "Akses panel admin",
                icon = Icons.Outlined.Shield,
                iconBg = Color(0xFFE3F2FD),
                iconTint = Color(0xFF2196F3)
            ) {
                showAdminDialog = true
            }

            // --- KELUAR / LOGOUT ---
            ActionItem(
                title = "Keluar",
                sub = "Logout dari aplikasi",
                icon = Icons.AutoMirrored.Outlined.Logout,
                iconBg = Color(0xFFFFF1F0),
                iconTint = Color(0xFFF44336)
            ) {
                showLogoutConfirm = true
            }
        }
    }

    // --- DIALOGS ---
    if (showAdminDialog) {
        Dialog(onDismissRequest = { showAdminDialog = false }) {
            AdminFormView(
                onDismiss = { showAdminDialog = false },
                onAdminAuthenticated = {
                    showAdminDialog = false
                    navController.navigate(AppView.Analysis.name) {
                        popUpTo(AppView.Setting.name) { inclusive = true }
                    }
                }
            )
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Konfirmasi Keluar") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                Button(
                    onClick = { onLogout(); showLogoutConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal") }
            }
        )
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun SettingCard(
    title: String,
    sub: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg), contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconTint)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(sub, color = Color.Gray, fontSize = 12.sp)
                }
            }
            content()
        }
    }
}

@Composable
fun ActionItem(
    title: String,
    sub: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(sub, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFF9FAFB) else Color.Transparent)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9333EA))
        )
        Text(label, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun CurrencyOption(label: String, symbol: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFF9FAFB) else Color.Transparent)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9333EA))
        )
        Text(label, fontSize = 14.sp, modifier = Modifier
            .padding(start = 8.dp)
            .weight(1f))
        Text(symbol, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}
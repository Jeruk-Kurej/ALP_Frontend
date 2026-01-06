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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingView(
    navController: NavController,
    onLogout: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    token: String = ""
) {
    // Get preferences repository
    val userPreferences = AppContainer.userPreferencesRepository
    val scope = rememberCoroutineScope()
    
    // Collect currency from DataStore
    val selectedCurrency by userPreferences.selectedCurrency.collectAsState(initial = "IDR")

    // State untuk Popups
    var showAdminDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    
    // Check if admin password is set
    val hasAdminPassword by userPreferences.hasAdminPassword.collectAsState(initial = false)
    val adminPasswordState by userViewModel.adminPasswordState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Background konsisten dengan TokoView
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
                sub = "Pilih mata uang untuk transaksi",
                icon = Icons.Outlined.Payments,
                iconBg = Color(0xFFFCE4EC),
                iconTint = Color(0xFFE91E63)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CurrencySymbol("Rp", selectedCurrency == "IDR") { 
                        scope.launch { userPreferences.setCurrency("IDR") }
                    }
                    CurrencySymbol("$", selectedCurrency == "USD") { 
                        scope.launch { userPreferences.setCurrency("USD") }
                    }
                    CurrencySymbol("€", selectedCurrency == "EUR") { 
                        scope.launch { userPreferences.setCurrency("EUR") }
                    }
                    CurrencySymbol("¥", selectedCurrency == "JPY") { 
                        scope.launch { userPreferences.setCurrency("JPY") }
                    }
                    CurrencySymbol("£", selectedCurrency == "GBP") { 
                        scope.launch { userPreferences.setCurrency("GBP") }
                    }
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
    // Di dalam SettingView.kt
    if (showAdminDialog) {
        Dialog(onDismissRequest = { 
            if (!adminPasswordState.isLoading) {
                showAdminDialog = false
                userViewModel.resetAdminPasswordState()
            }
        }) {
            AdminFormView(
                hasAdminPassword = hasAdminPassword,
                isLoading = adminPasswordState.isLoading,
                errorMessage = if (adminPasswordState.isError) adminPasswordState.message else null,
                onDismiss = { 
                    showAdminDialog = false
                    userViewModel.resetAdminPasswordState()
                },
                onSetPassword = { password ->
                    // First time: Set the admin password
                    android.util.Log.d("SettingView", "Setting admin password, token: ${token.take(20)}...")
                    userViewModel.updateAdminPassword(token, password)
                },
                onVerifyPassword = { password, onError ->
                    // Verify existing password
                    android.util.Log.d("SettingView", "Verifying admin password, token: ${token.take(20)}...")
                    userViewModel.verifyAdminPassword(
                        token = token,
                        password = password,
                        onSuccess = {
                            showAdminDialog = false
                            navController.navigate(AppView.Analysis.name) {
                                popUpTo(AppView.Setting.name) { inclusive = true }
                            }
                        },
                        onError = onError
                    )
                },
                onAdminAuthenticated = {
                    showAdminDialog = false
                    navController.navigate(AppView.Analysis.name) {
                        popUpTo(AppView.Setting.name) { inclusive = true }
                    }
                }
            )
        }
    }
    
    // Handle password set success
    LaunchedEffect(adminPasswordState.isSuccess) {
        if (adminPasswordState.isSuccess) {
            // Save the flag that admin password is set
            scope.launch {
                userPreferences.setAdminPasswordStatus(true)
                userViewModel.resetAdminPasswordState()
                showAdminDialog = false
                // Navigate to admin mode
                navController.navigate(AppView.Analysis.name) {
                    popUpTo(AppView.Setting.name) { inclusive = true }
                }
            }
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

// --- KOMPONEN PENDUKUNG (HIG STYLE) ---

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
fun CurrencySymbol(symbol: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (selected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF60A5FA), Color(0xFFA855F7)))
                    )
                } else {
                    Modifier.background(color = Color(0xFFF3F4F6))
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Color(0xFF6B7280)
        )
    }
}
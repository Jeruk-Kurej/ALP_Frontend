package com.jeruk.alp_frontend.ui.view.Setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.ui.viewmodel.AdminAuthViewModel
import kotlinx.coroutines.delay

// --- COLORS (Disamakan dengan Form Kategori) ---
private val GradientStart = Color(0xFF6B9FFF) // Biru Muda
private val GradientEnd = Color(0xFFBA68C8)   // Ungu
private val LightPurpleBg = Color(0xFFF3E8FF) // Background Icon pudar
private val TextDark = Color(0xFF111827)

@Composable
fun AdminFormView(
    onDismiss: () -> Unit,
    onAdminAuthenticated: () -> Unit,
    viewModel: AdminAuthViewModel = viewModel()
) {
    // Collect Data
    val storedPin by viewModel.storedPin.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) return

    // Jika storedPin kosong/null, berarti mode SETUP. Jika ada isi, mode LOGIN.
    val isSetupMode = storedPin.isNullOrBlank()

    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // State untuk Dialog Konfirmasi Reset
    var showResetDialog by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // --- LOGIC SUBMIT ---
    val submitAction = {
        keyboardController?.hide()
        if (isSetupMode) {
            // MODE BUAT PIN BARU
            if (password.length >= 4) {
                viewModel.setAdminPin(password)
                onAdminAuthenticated()
            } else {
                isError = true
            }
        } else {
            // MODE LOGIN
            if (viewModel.verifyPin(password)) {
                onAdminAuthenticated()
            } else {
                isError = true
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    // --- DIALOG KONFIRMASI RESET PIN ---
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset PIN Admin?", fontWeight = FontWeight.Bold) },
            text = { Text("PIN lama akan dihapus. Anda perlu membuat PIN baru untuk mengakses halaman ini.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetPin() // Panggil fungsi reset di ViewModel
                        showResetDialog = false
                        password = "" // Kosongkan input
                        isError = false
                        // Otomatis UI akan berubah jadi mode "Buat PIN Baru" karena storedPin jadi null/kosong
                    }
                ) {
                    Text("Ya, Reset", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Close Button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.LightGray)
                }
            }

            // --- HEADER ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFF3E8FF), // Light Purple
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSetupMode) Icons.Default.VpnKey else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFBA68C8) // Gradient End Purple
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isSetupMode) "Buat PIN Admin" else "Admin Access",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = if (isSetupMode) "Atur PIN keamanan baru" else "Masukkan PIN Admin",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // --- INPUT FIELD ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp) // Jarak diperkecil biar "Lupa PIN" nempel
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            password = it
                            isError = false
                        }
                    },
                    placeholder = { Text(if (isSetupMode) "Contoh: 123456" else "****") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            null,
                            tint = if (isError) Color.Red else Color(0xFF6B9FFF)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submitAction() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFBA68C8),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        cursorColor = Color(0xFFBA68C8)
                    )
                )

                if (isError) {
                    Text(
                        text = if (isSetupMode) "Minimal 4 digit angka" else "PIN Salah!",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // --- TOMBOL LUPA PIN (Hanya muncul kalau bukan mode setup) ---
                if (!isSetupMode) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = { showResetDialog = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                "Lupa PIN?",
                                fontSize = 12.sp,
                                color = Color(0xFFBA68C8), // Warna Ungu
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // --- BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))
                ) {
                    Text("Batal", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { submitAction() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF6B9FFF), Color(0xFFBA68C8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isSetupMode) "Simpan" else "Masuk",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
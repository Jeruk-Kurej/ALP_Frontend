package com.jeruk.alp_frontend.ui.view.Setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import kotlinx.coroutines.delay

@Composable
fun AdminFormView(
    onDismiss: () -> Unit,
    onAdminAuthenticated: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // 1. Controller Keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    // 2. Requester Focus (Untuk memicu keyboard otomatis)
    val focusRequester = remember { FocusRequester() }

    // 3. Fungsi Logika Submit (Biar bisa dipanggil Button maupun Keyboard)
    val submitAction = {
        keyboardController?.hide()
        if (password == "123") {
            onAdminAuthenticated()
        } else {
            isError = true
        }
    }

    // 4. Efek saat Dialog muncul: Tunggu sebentar lalu fokus ke textfield
    LaunchedEffect(Unit) {
        delay(100) // Delay sedikit agar animasi dialog selesai dulu
        focusRequester.requestFocus() // Panggil keyboard
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

            // --- HEADER TITLE ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF2196F3)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Admin Access", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Masukkan PIN/Password Admin",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // --- INPUT FIELD ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Password Admin", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isError = false
                    },
                    placeholder = { Text("Masukkan password", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester), // Tempel FocusRequester di sini
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = isError,

                    // CONFIG KEYBOARD & ACTIONS
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done // Tombol Enter jadi Checkmark/Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitAction() // Otomatis Submit saat Enter ditekan
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC084FC),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                // Pesan Error (Opsional tampil jika salah)
                if (isError) {
                    Text(
                        "Password salah!",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // --- BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))
                ) {
                    Text("Batal", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { submitAction() }, // Gunakan logika yang sama
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
                                    colors = listOf(
                                        Color(0xFF60A5FA),
                                        Color(0xFFA855F7)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Masuk", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
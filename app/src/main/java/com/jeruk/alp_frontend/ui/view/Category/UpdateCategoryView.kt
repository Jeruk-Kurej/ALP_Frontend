package com.jeruk.alp_frontend.ui.view.Category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel

@Composable
fun UpdateCategoryView(
    token: String,
    categoryId: Int,
    onSuccess: () -> Unit,
    navController: NavController,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    // 1. State dari ViewModel (Sama seperti Product)
    val selectedCategory by categoryViewModel.selectedCategory.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val errorMessage by categoryViewModel.errorMessage.collectAsState()

    // Pastikan kamu sudah menambahkan 'isSuccess' di CategoryViewModel ya!
    val isSuccess by categoryViewModel.isSuccess.collectAsState()

    // Form State
    var categoryName by remember { mutableStateOf("") }

    // 2. Load Data Awal
    LaunchedEffect(Unit) {
        categoryViewModel.clearMessages() // Reset state dulu
        categoryViewModel.getCategoryById(token, categoryId)
    }

    // 3. Pre-fill Form (Sama persis logic Product)
    LaunchedEffect(selectedCategory) {
        selectedCategory?.let { category ->
            categoryName = category.name
        }
    }

    // 4. Handle Success (Trigger navigasi hanya kalau API sukses)
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onSuccess()
            categoryViewModel.clearMessages()
        }
    }

    // Cleanup saat keluar layar
    DisposableEffect(Unit) {
        onDispose {
            categoryViewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- FORM INPUT ---
        Text(
            text = "Nama Kategori",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Masukkan nama kategori") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- ERROR CARD ---
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = errorMessage ?: "Terjadi kesalahan",
                    color = Color(0xFFDC2626),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- BUTTON UPDATE ---
        Button(
            onClick = {
                // Logic disini bersih, cuma panggil ViewModel
                categoryViewModel.updateCategory(token, categoryId, categoryName)
            },
            enabled = !isLoading && categoryName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color(0xFFE2E8F0)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (!isLoading && categoryName.isNotBlank()) {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF059669)) // Warna Hijau Khas Category
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Simpan Perubahan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (categoryName.isNotBlank()) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- BUTTON BATAL ---
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF64748B)
            )
        ) {
            Text(
                text = "Batal",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
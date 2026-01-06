package com.jeruk.alp_frontend.ui.view.Category

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel

// --- COLORS ---
private val BrandPrimary = Color(0xFF4F46E5)
private val BrandBackground = Color(0xFFF8FAFC)

@Composable
fun AddCategoryView(
    navController: NavController,
    token: String,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    // State Form
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") } // UI Only (sesuai kode lama)

    // ViewModel State
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val successMessage by categoryViewModel.successMessage.collectAsState()
    val errorMessage by categoryViewModel.errorMessage.collectAsState()

    // Handle Success
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, "Kategori berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            categoryViewModel.clearMessages()
            navController.popBackStack()
        }
    }

    // Handle Error
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // --- MAIN LAYOUT (Tanpa Header/Scaffold) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        // CONTENT (Scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Card Input
            CategorySectionCard(title = "Detail Kategori") {
                CategoryCustomTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = "Nama Kategori",
                    placeholder = "Contoh: Makanan Berat",
                    icon = Icons.Default.Label
                )

                CategoryCustomTextField(
                    value = categoryDescription,
                    onValueChange = { categoryDescription = it },
                    label = "Deskripsi (Opsional)",
                    placeholder = "Jelaskan kategori ini...",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    modifier = Modifier.height(120.dp)
                )
            }
        }

        // BOTTOM ACTION (Sticky di bawah)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (categoryName.isBlank()) {
                            Toast.makeText(context, "Nama kategori wajib diisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Create Action
                        categoryViewModel.createCategory(token, categoryName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Simpan Kategori", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- HELPER COMPONENTS (Copy bagian ini juga agar tidak error) ---

@Composable
fun CategorySectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            content()
        }
    }
}

@Composable
fun CategoryCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF94A3B8), fontSize = 14.sp) },
            leadingIcon = if (icon != null) {
                { Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF4F46E5),
                unfocusedBorderColor = Color(0xFFE2E8F0),
            ),
            singleLine = singleLine
        )
    }
}
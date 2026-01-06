package com.jeruk.alp_frontend.ui.view.Category

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel

// --- COLORS ---
private val BrandPrimary = Color(0xFF4F46E5)
private val BrandBackground = Color(0xFFF8FAFC)

@Composable
fun UpdateCategoryView(
    token: String,
    categoryId: Int,
    onSuccess: () -> Unit,
    navController: NavController,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    // State Data
    val selectedCategory by categoryViewModel.selectedCategory.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val isSuccess by categoryViewModel.isSuccess.collectAsState()

    // Form State
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") } // Sudah ditambahkan

    // Load Data Awal
    LaunchedEffect(Unit) {
        categoryViewModel.clearMessages()
        categoryViewModel.getCategoryById(token, categoryId)
    }

    // Isi Form saat data didapat
    LaunchedEffect(selectedCategory) {
        selectedCategory?.let {
            categoryName = it.name
            // Jika nanti backend sudah support deskripsi, bisa di-uncomment baris bawah ini:
            // categoryDescription = it.description ?: ""
        }
    }

    // Handle Sukses Update
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Kategori berhasil diupdate!", Toast.LENGTH_SHORT).show()
            onSuccess()
            categoryViewModel.clearMessages()
        }
    }

    // --- MAIN LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        // CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            UpdateCategorySectionCard(title = "Edit Kategori") {
                // 1. Nama Kategori
                UpdateCategoryCustomTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = "Nama Kategori",
                    placeholder = "Nama kategori...",
                    icon = Icons.Default.Label
                )

                // 2. Deskripsi (Sudah ditambahkan)
                UpdateCategoryCustomTextField(
                    value = categoryDescription,
                    onValueChange = { categoryDescription = it },
                    label = "Deskripsi",
                    placeholder = "Deskripsi kategori...",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    modifier = Modifier.height(120.dp)
                )
            }
        }

        // BOTTOM ACTION
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (categoryName.isBlank()) {
                            Toast.makeText(context, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Update Action (Description dikirim jika backend support, jika tidak biarkan seperti ini)
                        categoryViewModel.updateCategory(token, categoryId, categoryName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun UpdateCategorySectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
fun UpdateCategoryCustomTextField(
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
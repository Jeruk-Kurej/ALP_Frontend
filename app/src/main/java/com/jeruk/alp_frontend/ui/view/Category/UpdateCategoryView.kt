package com.jeruk.alp_frontend.ui.view.Category

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel

// --- COLORS (Saya rename agar tidak bentrok jika satu file) ---
private val EditPageBackground = Color(0xFFF3F4F6)
private val EditTextDark = Color(0xFF111827)
private val EditTextGray = Color(0xFF6B7280)
private val EditGradientStart = Color(0xFF6B9FFF)
private val EditGradientEnd = Color(0xFFBA68C8)

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
    var categoryDescription by remember { mutableStateOf("") }

    // Validasi sederhana
    val isFormValid = categoryName.isNotBlank()

    // Load Data Awal
    LaunchedEffect(Unit) {
        categoryViewModel.clearMessages()
        categoryViewModel.getCategoryById(token, categoryId)
    }

    // Isi Form saat data didapat
    LaunchedEffect(selectedCategory) {
        selectedCategory?.let {
            categoryName = it.name
            // Jika backend ada deskripsi: categoryDescription = it.description ?: ""
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
            .background(EditPageBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. SCROLLABLE FORM
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Edit Kategori",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = EditTextDark
                )
                Text(
                    text = "Perbarui informasi kategorimu", fontSize = 15.sp, color = EditTextGray
                )
            }

            // Form Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pakai Helper Component yang sudah direname (EditCategoryInput)
                    EditCategoryInput(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = "Nama Kategori",
                        placeholder = "Nama kategori...",
                        icon = Icons.Default.Label
                    )

                    EditCategoryInput(
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
        }

        // 2. GRADIENT BUTTON
        Button(
            onClick = {
                // Pastikan fungsi ini ada di ViewModel kamu
                categoryViewModel.updateCategory(token, categoryId, categoryName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            enabled = !isLoading && isFormValid,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (!isLoading && isFormValid) {
                            Brush.horizontalGradient(listOf(EditGradientStart, EditGradientEnd))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)))
                        }
                    ), contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = Color.White, modifier = Modifier.size(24.dp)
                )
                else Text(
                    "Simpan Perubahan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// --- REUSABLE COMPONENT (RENAME) ---
// Saya ganti namanya jadi "EditCategoryInput" supaya tidak bentrok dengan "CategoryCustomTextField"
// yang ada di file AddCategoryView.kt
@Composable
private fun EditCategoryInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Text(
            text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = EditTextDark
        )
        // Pastikan import androidx.compose.material3.* ada
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = EditGradientStart,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            // Jika error di 'colors', coba hapus focusedTextColor/unfocusedTextColor
            // karena di beberapa versi Compose M3 parameternya berbeda.
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = EditGradientStart,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = EditGradientStart,
                focusedTextColor = EditTextDark,
                unfocusedTextColor = EditTextDark
            ),
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp))
    }
}
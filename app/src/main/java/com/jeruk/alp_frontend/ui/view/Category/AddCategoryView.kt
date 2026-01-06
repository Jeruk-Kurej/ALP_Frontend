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

// --- COLORS ---
private val PageBackground = Color(0xFFF3F4F6)
private val TextDark = Color(0xFF111827)
private val TextGray = Color(0xFF6B7280)
private val GradientStart = Color(0xFF6B9FFF)
private val GradientEnd = Color(0xFFBA68C8)

@Composable
fun AddCategoryView(
    navController: NavController,
    token: String,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    // State Form
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") }

    // ViewModel State
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val successMessage by categoryViewModel.successMessage.collectAsState()
    val errorMessage by categoryViewModel.errorMessage.collectAsState()

    // Validasi: Tombol hanya nyala jika nama terisi
    val isFormValid = categoryName.isNotBlank()

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

    // --- MAIN LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
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
                    text = "Buat Kategori",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Kelompokkan produkmu agar lebih rapi",
                    fontSize = 15.sp,
                    color = TextGray
                )
            }

            // Card Input
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
        }

        // 2. GRADIENT BUTTON
        Button(
            onClick = {
                categoryViewModel.createCategory(token, categoryName)
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
                            Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                else Text(
                    "Simpan Kategori",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// --- REUSABLE COMPONENT ---
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
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = GradientStart,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = GradientStart,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            ),
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}
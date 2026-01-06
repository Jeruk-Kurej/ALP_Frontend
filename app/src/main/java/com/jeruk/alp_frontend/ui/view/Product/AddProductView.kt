package com.jeruk.alp_frontend.ui.view.Product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import java.io.File

// --- COLORS ---
private val BrandBackground = Color(0xFFF8FAFC)
private val TextMain = Color(0xFF1E293B)
private val TextLabel = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductView(
    navController: NavController,
    token: String,
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    // State Form
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }

    // State Kategori
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    // State Gambar
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    // Collect Data
    val categories by categoryViewModel.categories.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val isSuccess by productViewModel.isSuccess.collectAsState()
    val productErrorMessage by productViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        productViewModel.clearMessages()
        if (token.isNotEmpty()) categoryViewModel.getAllCategories(token)
    }

    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = categories.first().id
            selectedCategoryName = categories.first().name
        }
    }

    LaunchedEffect(isSuccess, productErrorMessage) {
        if (isSuccess) {
            Toast.makeText(context, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            productViewModel.clearMessages()
            navController.popBackStack()
        }
        if (productErrorMessage != null) {
            Toast.makeText(context, productErrorMessage, Toast.LENGTH_LONG).show()
        }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val file = File(context.cacheDir, "product_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { out -> inputStream?.copyTo(out) }
                    imageFile = file
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    // --- MAIN LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .padding(top = 8.dp)
    ) {

        // 1. SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Upload Gambar
            AddSectionCard(title = "Media Produk") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedImageUri == null) Color(0xFFF1F5F9) else Color.White)
                        .addDashedBorder(
                            if (selectedImageUri == null) 2.dp else 0.dp,
                            Color(0xFFCBD5E1),
                            12.dp
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Upload Foto",
                                color = TextMain,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Info Dasar
            AddSectionCard(title = "Informasi Dasar") {
                AddCustomTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = "Nama Produk",
                    placeholder = "Contoh: Sepatu Nike",
                    icon = Icons.Default.Label
                )
                AddCustomTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    label = "Deskripsi",
                    placeholder = "Jelaskan produkmu...",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    modifier = Modifier.height(100.dp)
                )
            }

            // Detail
            AddSectionCard(title = "Detail Penjualan") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            AddCustomTextField(
                                value = selectedCategoryName,
                                onValueChange = {},
                                label = "Kategori",
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            selectedCategoryName = cat.name
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AddCustomTextField(
                            value = productPrice,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) productPrice = it
                            },
                            label = "Harga",
                            placeholder = "0",
                            prefixText = "Rp ", // <--- GANTI ICON JADI PREFIX TEKS RP
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }

        // 2. BOTTOM BUTTON
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 10.dp,
            color = Color.White
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val priceVal = productPrice.toIntOrNull()
                        if (productName.isBlank() || productDescription.isBlank() || priceVal == null || selectedCategoryId == null || imageFile == null) {
                            Toast.makeText(context, "Lengkapi semua data", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }
                        productViewModel.createProduct(
                            token,
                            productName,
                            productDescription,
                            priceVal,
                            selectedCategoryId!!,
                            "",
                            imageFile
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    else Text("Simpan Produk", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---
@Composable
fun AddSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            content()
        }
    }
}

@Composable
fun AddCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: ImageVector? = null,
    prefixText: String? = null, // Tambahan parameter untuk Prefix Text (Rp)
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF94A3B8), fontSize = 14.sp) },
            // Logic: Kalau ada icon pakai icon, kalau ada prefixText pakai prefixText
            leadingIcon = if (icon != null) {
                { Icon(icon, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) }
            } else null,
            prefix = if (prefixText != null) {
                { Text(prefixText, fontWeight = FontWeight.SemiBold, color = TextMain) }
            } else null,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF4F46E5),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF1E293B)
            ),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            readOnly = readOnly,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}

fun Modifier.addDashedBorder(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    cornerRadiusDp: androidx.compose.ui.unit.Dp
) = drawBehind {
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(cornerRadiusDp.toPx()))
}
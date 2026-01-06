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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductView(
    navController: NavController,
    token: String,
    productId: Int,
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    onSuccess: () -> Unit
) {
    val context = LocalContext.current

    val selectedProduct by productViewModel.selectedProduct.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    val isSuccess by productViewModel.isSuccess.collectAsState()

    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var currentTokoIds by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        productViewModel.clearMessages()
        productViewModel.getProductById(token, productId)
        categoryViewModel.getAllCategories(token)
    }

    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { product ->
            productName = product.name
            productDescription = product.description
            productPrice = product.price.toString()
            selectedCategoryId = product.categoryId
            selectedCategoryName = product.categoryName ?: ""
            currentTokoIds = product.tokoIds.joinToString(",")
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Produk berhasil diupdate!", Toast.LENGTH_SHORT).show()
            onSuccess()
            productViewModel.clearMessages()
        }
    }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val file =
                        File(context.cacheDir, "update_img_${System.currentTimeMillis()}.jpg")
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
        // 1. CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gambar
            UpdateSectionCard(title = "Foto Produk") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (selectedProduct?.imageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = selectedProduct?.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, null, tint = Color.Gray)
                            Text("Tidak ada gambar", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    // Tombol Edit Kecil di Pojok
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    modifier = Modifier.size(12.dp),
                                    tint = BrandPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Ubah",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Info
            UpdateSectionCard(title = "Informasi Produk") {
                UpdateCustomTextField(
                    value = productName, onValueChange = { productName = it },
                    label = "Nama Produk", icon = Icons.Default.Label
                )
                UpdateCustomTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    label = "Deskripsi",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    modifier = Modifier.height(100.dp)
                )
            }

            // Detail
            UpdateSectionCard(title = "Detail Penjualan") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                            UpdateCustomTextField(
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
                                            selectedCategoryId = cat.id; selectedCategoryName =
                                            cat.name; categoryExpanded = false
                                        })
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        UpdateCustomTextField(
                            value = productPrice,
                            onValueChange = { if (it.all { c -> c.isDigit() }) productPrice = it },
                            label = "Harga",
                            prefixText = "Rp ", // <--- GANTI JADI RP
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }

        // 2. BOTTOM BUTTON
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 10.dp, color = Color.White) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val priceValue = productPrice.toIntOrNull() ?: 0
                        selectedCategoryId?.let { catId ->
                            productViewModel.updateProduct(
                                token,
                                productId,
                                productName,
                                productDescription,
                                priceValue,
                                catId,
                                currentTokoIds,
                                imageFile
                            )
                        }
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
                    else Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- HELPER COMPONENTS (Sama persis dengan AddProductView) ---
@Composable
fun UpdateSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
fun UpdateCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: ImageVector? = null,
    prefixText: String? = null,
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
                focusedTextColor = Color(0xFF1E293B), // FIXED
                unfocusedTextColor = Color(0xFF1E293B) // FIXED
            ),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            readOnly = readOnly,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}
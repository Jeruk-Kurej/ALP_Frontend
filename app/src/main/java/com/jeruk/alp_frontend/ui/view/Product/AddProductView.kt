package com.jeruk.alp_frontend.ui.view.Product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel
import com.jeruk.alp_frontend.utils.TokenUtils
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductView(
    navController: NavController,
    token: String,
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    val categories by categoryViewModel.categories.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val productState by productViewModel.productState.collectAsState()
    val categoryErrorMessage by categoryViewModel.errorMessage.collectAsState()

    // Fetch categories when view loads
    LaunchedEffect(Unit) {
        android.util.Log.d("AddProductView", "=== TOKEN DEBUG INFO ===")
        android.util.Log.d("AddProductView", "Token received: ${if (token.isNotEmpty()) "Present (length: ${token.length})" else "EMPTY"}")

        if (token.isNotEmpty()) {
            // Decode and inspect the JWT token
            val payload = TokenUtils.decodeJwt(token)
            if (payload != null) {
                android.util.Log.d("AddProductView", "Token Payload:")
                android.util.Log.d("AddProductView", "  - User ID: ${payload.userId}")
                android.util.Log.d("AddProductView", "  - Username: ${payload.username}")
                android.util.Log.d("AddProductView", "  - Role: ${payload.role}")
                android.util.Log.d("AddProductView", "  - Issued At: ${payload.iat}")
                android.util.Log.d("AddProductView", "  - Expires At: ${payload.exp}")

                // Check if token is expired
                val isExpired = TokenUtils.isTokenExpired(token)
                android.util.Log.d("AddProductView", "  - Is Expired: $isExpired")

                // Check if user is admin
                val isAdmin = TokenUtils.isAdmin(token)
                android.util.Log.d("AddProductView", "  - Is Admin: $isAdmin")

                if (isExpired) {
                    android.util.Log.e("AddProductView", "TOKEN IS EXPIRED! User needs to re-login.")
                    Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                } else if (!isAdmin) {
                    android.util.Log.w("AddProductView", "USER IS NOT ADMIN! This might cause 403 errors.")
                    Toast.makeText(context, "Warning: You may not have admin access", Toast.LENGTH_LONG).show()
                }
            } else {
                android.util.Log.e("AddProductView", "Failed to decode JWT token!")
            }

            android.util.Log.d("AddProductView", "Token value (first 50 chars): ${token.take(50)}...")
            android.util.Log.d("AddProductView", "========================")

            // Fetch categories
            categoryViewModel.getAllCategories(token)
        } else {
            android.util.Log.e("AddProductView", "Cannot fetch categories: Token is empty!")
            Toast.makeText(context, "Authentication required. Please login.", Toast.LENGTH_LONG).show()
        }
    }

    // Show category error if any
    LaunchedEffect(categoryErrorMessage) {
        categoryErrorMessage?.let { error ->
            Toast.makeText(context, "Error loading categories: $error", Toast.LENGTH_LONG).show()
        }
    }

    // Set default category when categories are loaded
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = categories.first().id
            selectedCategoryName = categories.first().name
        }
    }


    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Convert URI to File
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.cacheDir, "product_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                imageFile = file
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle success/error states
    LaunchedEffect(productState) {
        if (productState.isSuccess) {
            Toast.makeText(context, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        if (productState.isError) {
            Toast.makeText(context, productState.errorMessage ?: "Gagal menambahkan produk", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Foto Produk Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Foto Produk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image placeholder/preview - clickable to select image
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 2.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(Color(0xFFF5F5F7))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected product image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Select image",
                                tint = Color(0xFFBDBDBD),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Instructions
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pilih gambar produk",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Ketuk kotak di samping untuk memilih gambar dari galeri",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                        if (selectedImageUri != null) {
                            Text(
                                text = "✓ Gambar dipilih",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Nama Produk
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row {
                    Text(
                        text = "Nama Produk ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "*",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Red
                    )
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Contoh: Espresso", fontSize = 14.sp, color = Color.Gray)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F7),
                        unfocusedContainerColor = Color(0xFFF5F5F7),
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Deskripsi Produk
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row {
                    Text(
                        text = "Deskripsi Produk ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "*",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Red
                    )
                }

                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text("Deskripsi produk...", fontSize = 14.sp, color = Color.Gray)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F7),
                        unfocusedContainerColor = Color(0xFFF5F5F7),
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 5
                )
            }
        }

        // Harga dan Kategori Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Harga
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row {
                        Text(
                            text = "Harga ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "*",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Red
                        )
                    }

                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Rp    25000", fontSize = 14.sp, color = Color.Gray)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F7),
                            unfocusedContainerColor = Color(0xFFF5F5F7),
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Kategori
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Kategori",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "*",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Red
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F5F7),
                                unfocusedContainerColor = Color(0xFFF5F5F7),
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = {
                                if (selectedCategoryName.isEmpty()) {
                                    Text("Pilih kategori", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        selectedCategoryName = category.name
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Batal Button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Batal",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            // Simpan Produk Button
            Button(
                onClick = {
                    // Validation
                    if (selectedCategoryId == null) {
                        Toast.makeText(context, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (productName.isBlank()) {
                        Toast.makeText(context, "Nama produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (productDescription.isBlank()) {
                        Toast.makeText(context, "Deskripsi produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (productPrice.isBlank()) {
                        Toast.makeText(context, "Harga produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (imageFile == null) {
                        Toast.makeText(context, "Pilih gambar produk terlebih dahulu", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Parse price - remove "Rp" and spaces if present
                    val priceValue = productPrice.replace("Rp", "").replace(".", "").replace(",", "").trim().toIntOrNull()
                    if (priceValue == null || priceValue <= 0) {
                        Toast.makeText(context, "Harga produk harus berupa angka positif", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Log what we're sending
                    android.util.Log.d("AddProductView", "Creating product with:")
                    android.util.Log.d("AddProductView", "- Name: $productName")
                    android.util.Log.d("AddProductView", "- Price: $priceValue")
                    android.util.Log.d("AddProductView", "- CategoryId: $selectedCategoryId")
                    android.util.Log.d("AddProductView", "- CategoryName: $selectedCategoryName")
                    android.util.Log.d("AddProductView", "- Has Image: ${imageFile != null}")

                    // Create product - toko will be assigned later
                    productViewModel.createProduct(
                        token = token,
                        name = productName,
                        description = productDescription,
                        price = priceValue,
                        categoryId = selectedCategoryId!!,
                        tokoIds = "", // Empty - toko will be assigned later
                        imageFile = imageFile
                    )
                },
                enabled = !isLoading &&
                         productName.isNotBlank() &&
                         productDescription.isNotBlank() &&
                         productPrice.isNotBlank() &&
                         selectedCategoryId != null &&
                         imageFile != null,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B9FFF), Color(0xFFBA68C8))
                            )
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
                            text = "Simpan Produk",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


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

    // Fetch Categories
    LaunchedEffect(Unit) {
        productViewModel.clearMessages()
        if (token.isNotEmpty()) {
            categoryViewModel.getAllCategories(token)
        } else {
            Toast.makeText(context, "Authentication required", Toast.LENGTH_LONG).show()
        }
    }

    // Set default category if available
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = categories.first().id
            selectedCategoryName = categories.first().name
        }
    }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
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

    // Handle Success/Error
    LaunchedEffect(isSuccess, productErrorMessage) {
        if (isSuccess) {
            Toast.makeText(context, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            productViewModel.clearMessages()
            navController.popBackStack()
        }
        if (productErrorMessage != null) {
            // Tampilkan error 500 disini jika terjadi
            Toast.makeText(context, productErrorMessage, Toast.LENGTH_LONG).show()
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
        // --- Foto Produk ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Foto Produk", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        else Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                    }
                    Text("Pilih gambar produk", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // --- Nama Produk ---
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelRequired("Nama Produk")
                OutlinedTextField(
                    value = productName, onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors()
                )
            }
        }

        // --- Deskripsi ---
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelRequired("Deskripsi")
                OutlinedTextField(
                    value = productDescription, onValueChange = { productDescription = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = inputColors(), maxLines = 5
                )
            }
        }

        // --- Harga & Kategori ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelRequired("Harga")
                    OutlinedTextField(
                        value = productPrice, onValueChange = { if(it.all { c -> c.isDigit() }) productPrice = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = inputColors(), placeholder = { Text("10000") }
                    )
                }
            }
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelRequired("Kategori")
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                        OutlinedTextField(
                            value = selectedCategoryName, onValueChange = {}, readOnly = true,
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            colors = inputColors()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; selectedCategoryName = cat.name; categoryExpanded = false })
                            }
                        }
                    }
                }
            }
        }

        // --- Tombol Action ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Batal") }

            Button(
                onClick = {
                    val priceVal = productPrice.toIntOrNull()
                    if (productName.isBlank() || productDescription.isBlank() || priceVal == null || selectedCategoryId == null) {
                        Toast.makeText(context, "Lengkapi data (Nama, Deskripsi, Harga, Kategori)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (imageFile == null) {
                        Toast.makeText(context, "Pilih gambar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // --- PENTING ---
                    // tokoIds dikirim string kosong "" karena user tidak mau assign toko sekarang.
                    // Jika ini menyebabkan Error 500, Backend harus diperbaiki.
                    productViewModel.createProduct(
                        token = token,
                        name = productName,
                        description = productDescription,
                        price = priceVal,
                        categoryId = selectedCategoryId!!,
                        tokoIds = "", // <--- KOSONG
                        imageFile = imageFile
                    )
                },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B9FFF)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan")
            }
        }
    }
}

@Composable
fun LabelRequired(text: String) {
    Row {
        Text(text, fontWeight = FontWeight.SemiBold)
        Text("*", color = Color.Red, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFF5F5F7),
    unfocusedContainerColor = Color(0xFFF5F5F7),
    focusedBorderColor = Color(0xFFE0E0E0),
    unfocusedBorderColor = Color(0xFFE0E0E0)
)
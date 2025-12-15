package com.jeruk.alp_frontend.ui.view.Product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
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
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import java.io.File
import java.io.FileOutputStream

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

    // State from ViewModels
    val selectedProduct by productViewModel.selectedProduct.collectAsState()
    val productState by productViewModel.productState.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Form State
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    // Load product data
    LaunchedEffect(Unit) {
        android.util.Log.d("UpdateProductView", "Loading product ID: $productId with token")
        productViewModel.getProductById(token, productId)
        categoryViewModel.getAllCategories(token)
    }

    // Pre-fill form when product data is loaded
    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { product ->
            productName = product.name
            productDescription = product.description
            productPrice = product.price.toString()
            selectedCategoryId = product.categoryId
            selectedCategoryName = product.categoryName
        }
    }

    // Image Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Convert Uri to File
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            imageFile = file
            inputStream?.close()
            outputStream.close()
        }
    }

    // Navigate back on success
    LaunchedEffect(productState.isSuccess) {
        if (productState.isSuccess) {
            onSuccess()
        }
    }

    // Clear ViewModel state on dispose
    DisposableEffect(Unit) {
        onDispose {
            productViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Produk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F7))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Product Name
            Text(
                text = "Nama Produk",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan nama produk") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Deskripsi",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = productDescription,
                onValueChange = { productDescription = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Masukkan deskripsi produk") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Price
            Text(
                text = "Harga",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = productPrice,
                onValueChange = { if (it.all { char -> char.isDigit() }) productPrice = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan harga produk") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            Text(
                text = "Kategori",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                    )
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
            Spacer(modifier = Modifier.height(16.dp))

            // Image Upload
            Text(
                text = "Foto Produk",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Klik untuk ubah foto",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            val currentError = productState.errorMessage
            if (currentError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = currentError,
                        color = Color(0xFFDC2626),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Update Button
            Button(
                onClick = {
                    val priceValue = productPrice.toIntOrNull() ?: 0
                    selectedCategoryId?.let { catId ->
                        android.util.Log.d("UpdateProductView", "Updating product: id=$productId")
                        android.util.Log.d("UpdateProductView", "Name: $productName, Price: $priceValue, CategoryId: $catId")
                        android.util.Log.d("UpdateProductView", "New image selected: ${imageFile != null}")

                        productViewModel.updateProduct(
                            token = token,
                            productId = productId,
                            name = productName,
                            description = productDescription,
                            price = priceValue,
                            categoryId = catId,
                            tokoIds = "", // Empty for now, toko assignment handled elsewhere
                            imageFile = imageFile // Only send if user selected a new image
                        )
                    }
                },
                enabled = !isLoading && productName.isNotBlank() && productDescription.isNotBlank() && productPrice.isNotBlank() && selectedCategoryId != null,
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
                            brush = if (!isLoading && productName.isNotBlank()) {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
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
                            color = if (productName.isNotBlank()) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

package com.jeruk.alp_frontend.ui.view.Product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
private val PageBackground = Color(0xFFF3F4F6)

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

    // Logic Validasi: Tidak boleh kosong
    val isFormValid = productName.isNotBlank() &&
            productDescription.isNotBlank() &&
            productPrice.isNotBlank()

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
                    text = "Edit Produk",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Perbarui informasi produkmu",
                    fontSize = 15.sp,
                    color = TextGray
                )
            }

            // Card Form
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB))
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
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
                                Text("Upload Gambar", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        // Badge Edit
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ketuk untuk mengubah foto", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    // Input Fields
                    ProductCustomTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Nama Produk",
                        icon = Icons.Default.Label
                    )

                    ProductCustomTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        label = "Deskripsi",
                        icon = Icons.Default.Description,
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                                ProductCustomTextField(
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
                                            })
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCustomTextField(
                                value = productPrice,
                                onValueChange = {
                                    if (it.all { c -> c.isDigit() }) productPrice = it
                                },
                                label = "Harga",
                                prefixText = "Rp ",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }
        }

        // 2. BUTTON (GRADIENT)
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
                    "Simpan Perubahan",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
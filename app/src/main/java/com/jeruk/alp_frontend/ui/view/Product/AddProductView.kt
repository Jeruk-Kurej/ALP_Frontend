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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
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

// --- COLORS (Sama dengan Toko) ---
private val PageBackground = Color(0xFFF3F4F6)
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

    // Validasi Form (Agar tombol nyala/mati)
    val isFormValid = productName.isNotBlank() &&
            productDescription.isNotBlank() &&
            productPrice.isNotBlank() &&
            selectedCategoryId != null &&
            imageFile != null

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
                    // Konversi Uri ke File
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
                    text = "Tambah Produk",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Lengkapi detail produkmu",
                    fontSize = 15.sp,
                    color = TextGray
                )
            }

            // Card Form Utama
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Upload Gambar Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedImageUri == null) Color(0xFFF9FAFB) else Color.White)
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
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
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = GradientStart,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Pilih Foto Produk",
                                    color = TextGray,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Input Fields
                    ProductCustomTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Nama Produk",
                        placeholder = "Contoh: Sepatu Nike",
                        icon = Icons.Default.Label
                    )

                    ProductCustomTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        label = "Deskripsi",
                        placeholder = "Jelaskan produkmu...",
                        icon = Icons.Default.Description,
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Dropdown Kategori
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }
                            ) {
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
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Input Harga
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCustomTextField(
                                value = productPrice,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) productPrice = it
                                },
                                label = "Harga",
                                placeholder = "0",
                                prefixText = "Rp ",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
            }
        }

        // 2. GRADIENT BUTTON
        Button(
            onClick = {
                val priceVal = productPrice.toIntOrNull()
                if (priceVal != null && selectedCategoryId != null) {
                    productViewModel.createProduct(
                        token,
                        productName,
                        productDescription,
                        priceVal,
                        selectedCategoryId!!,
                        "",
                        imageFile
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            enabled = !isLoading && isFormValid, // Logic Validasi di sini
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
                    "Simpan Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// --- REUSABLE COMPONENT (Style Toko) ---
@Composable
fun ProductCustomTextField(
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
                { Icon(icon, null, tint = GradientStart, modifier = Modifier.size(20.dp)) }
            } else null,
            prefix = if (prefixText != null) {
                { Text(prefixText, fontWeight = FontWeight.SemiBold, color = TextDark) }
            } else null,
            trailingIcon = trailingIcon,
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            readOnly = readOnly,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}
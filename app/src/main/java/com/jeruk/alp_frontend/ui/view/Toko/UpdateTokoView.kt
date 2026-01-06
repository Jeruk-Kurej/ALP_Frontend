package com.jeruk.alp_frontend.ui.view.Toko

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
// import com.jeruk.alp_frontend.utils.uriToFile
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import com.jeruk.alp_frontend.utils.CurrencyFormatter
import com.jeruk.alp_frontend.data.container.AppContainer

@Composable
fun UpdateTokoView(
    token: String,
    tokoId: Int,
    onSuccess: () -> Unit,
    navController: NavController,
    tokoViewModel: TokoViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current

    // States
    val selectedToko by tokoViewModel.currentToko.collectAsState()
    val isSuccess by tokoViewModel.isSuccess.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()
    val products by productViewModel.products.collectAsState()

    // Form Vars
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val selectedProductIds = remember { mutableStateListOf<Int>() }

    // Init Logic
    LaunchedEffect(Unit) {
        tokoViewModel.getTokoById(token, tokoId)
        productViewModel.getAllProducts(token)
    }

    LaunchedEffect(selectedToko) {
        selectedToko?.let {
            name = it.name
            location = it.address
            description = it.description
        }
    }

    LaunchedEffect(products, tokoId) {
        if (products.isNotEmpty()) {
            val alreadyAssignedIds = products.filter { it.tokoIds.contains(tokoId) }.map { it.id }
            selectedProductIds.clear()
            selectedProductIds.addAll(alreadyAssignedIds)
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) onSuccess()
    }

    DisposableEffect(Unit) {
        onDispose { tokoViewModel.clearState() }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        uri?.let { selectedImageFile = uriToFile(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)) // Match Theme
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER ---
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Edit Informasi Toko",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Perbarui detail dan kelola menu",
                fontSize = 15.sp,
                color = TextGray
            )
        }

        // --- FORM UTAMA ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Uploader
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!selectedToko?.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = selectedToko?.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                null,
                                tint = GradientStart,
                                modifier = Modifier.size(40.dp)
                            )
                            Text("Ubah Foto", color = TextGray, fontSize = 14.sp)
                        }
                    }
                }

                CustomTextField(name, { name = it }, "Nama Toko", "Nama toko")
                CustomTextField(location, { location = it }, "Lokasi", "Alamat lengkap")
                CustomTextField(
                    description,
                    { description = it },
                    "Deskripsi",
                    "Tentang toko...",
                    3
                )
            }
        }

        // --- PILIH MENU (DESIGN BARU) ---
        Text(
            text = "Ketersediaan Menu",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextDark,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada produk terdaftar.", color = TextGray)
                    }
                } else {
                    products.forEachIndexed { index, product ->
                        val isSelected = selectedProductIds.contains(product.id)

                        // Row Item yang lebih cantik
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isSelected) selectedProductIds.remove(product.id)
                                    else selectedProductIds.add(product.id)
                                }
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent) // Highlight jika dipilih
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Check
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) GradientStart else Color(0xFFD1D5DB),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Thumbnail Kecil
                            if (product.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE5E7EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        product.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            // Info Text
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (isSelected) TextDark else Color.Gray
                                )
                                Text(
                                    text = formatRupiah(product.price),
                                    fontSize = 12.sp,
                                    color = if (isSelected) GradientStart else Color.Gray
                                )
                            }
                        }
                        if (index < products.size - 1) {
                            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- SUBMIT BUTTON (GRADIENT) ---
        Button(
            onClick = {
                tokoViewModel.updateToko(
                    token,
                    tokoId,
                    name,
                    description,
                    location,
                    selectedImageFile,
                    selectedProductIds
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            enabled = !isLoading && name.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(GradientStart, GradientEnd)
                        )
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

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// Helper Format Rupiah (Bisa ditaruh di utils kalau mau rapi)
@Composable
fun formatRupiah(number: Int): String {
    val selectedCurrency by AppContainer.userPreferencesRepository.selectedCurrency.collectAsState(initial = "IDR")
    return CurrencyFormatter.formatPrice(number, selectedCurrency)
}
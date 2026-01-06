package com.jeruk.alp_frontend.ui.view.Toko

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    // State Toko
    val selectedToko by tokoViewModel.currentToko.collectAsState()
    val isSuccess by tokoViewModel.isSuccess.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()

    // State Product (Real Data)
    val products by productViewModel.products.collectAsState()

    // Form State
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // List ID Produk yang dipilih
    val selectedProductIds = remember { mutableStateListOf<Int>() }

    // 1. Fetch Data Awal (Toko & Semua Produk)
    LaunchedEffect(Unit) {
        tokoViewModel.getTokoById(token, tokoId)
        productViewModel.getAllProducts(token) // Tarik semua produk real
    }

    // 2. Pre-fill Form Data Toko
    LaunchedEffect(selectedToko) {
        selectedToko?.let {
            name = it.name
            location = it.address
            description = it.description
        }
    }

    // 3. Pre-fill Checkbox Produk (LOGIC DIPERBAIKI)
    LaunchedEffect(products, tokoId) {
        if (products.isNotEmpty()) {
            val alreadyAssignedIds = products.filter { product ->
                // ✅ FIX: Cek berdasarkan ID (Angka), bukan Nama
                product.tokoIds.contains(tokoId)
            }.map { it.id }

            selectedProductIds.clear()
            selectedProductIds.addAll(alreadyAssignedIds)
        }
    }

    // 4. Monitor Sukses
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onSuccess()
        }
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
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER ---
        Column {
            Text(
                text = "Edit Detail Toko",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
            Text(
                text = "Perbarui informasi toko dan atur ketersediaan menu",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // --- INPUT FIELDS ---
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nama Toko",
            placeholder = "Nama Toko"
        )
        CustomTextField(
            value = location,
            onValueChange = { location = it },
            label = "Lokasi",
            placeholder = "Alamat Lengkap"
        )
        CustomTextField(
            value = description,
            onValueChange = { description = it },
            label = "Deskripsi",
            placeholder = "Tentang Toko...",
            minLines = 3
        )

        // --- IMAGE SECTION ---
        Text(text = "Foto Toko", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF374151))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (!selectedToko?.imageUrl.isNullOrEmpty()) {
                AsyncImage(model = selectedToko?.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color(0xFF9333EA), modifier = Modifier.size(40.dp))
                    Text("Ganti Foto Toko", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        // --- SECTION PILIH PRODUK ---
        Text(
            text = "Kelola Menu di Toko Ini",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF374151)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (products.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Memuat produk atau belum ada produk...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    products.forEachIndexed { index, product ->
                        val isSelected = selectedProductIds.contains(product.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedProductIds.remove(product.id)
                                    else selectedProductIds.add(product.id)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null, // Handled by row click
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF9333EA))
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Tampilkan Gambar Produk Kecil
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
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = formatRupiah(product.price),
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (index < products.size - 1) HorizontalDivider(color = Color(0xFFF3F4F6))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SUBMIT BUTTON ---
        Button(
            onClick = {
                tokoViewModel.updateToko(
                    token,
                    tokoId,
                    name,
                    description,
                    location,
                    selectedImageFile,
                    selectedProductIds // Kirim ID produk yang dicentang
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(18.dp)),
            enabled = !isLoading && name.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF9333EA), Color(0xFFBA68C8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Helper Format Rupiah (Bisa ditaruh di utils kalau mau rapi)
fun formatRupiah(number: Int): String {
    val localeID = Locale("in", "ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    return formatRupiah.format(number).replace("Rp", "Rp ")
}
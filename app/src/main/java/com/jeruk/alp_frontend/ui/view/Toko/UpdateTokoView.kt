package com.jeruk.alp_frontend.ui.view.Toko

import android.net.Uri
import android.util.Log
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
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel
import java.io.File

// Dummy Model untuk Produk
data class DummyProduct(val id: Int, val name: String, val price: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTokoView(
    token: String,
    tokoId: Int,
    onSuccess: () -> Unit,
    navController: NavController,
    tokoViewModel: TokoViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentToko by tokoViewModel.selectedToko.collectAsState()
    val isSuccess by tokoViewModel.isSuccess.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()

    // Form State
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- DUMMY PRODUCT STATE ---
    val dummyProducts = remember {
        listOf(
            DummyProduct(1, "Espresso Blend", "Rp 25.000"),
            DummyProduct(2, "Caramel Macchiato", "Rp 35.000"),
            DummyProduct(3, "Croissant Almond", "Rp 28.000"),
            DummyProduct(4, "Matcha Latte", "Rp 30.000")
        )
    }
    val selectedProductIds = remember { mutableStateListOf<Int>() }

    // 1. Ambil data awal saat masuk (Pastikan repository.getTokoById sudah fix)
    LaunchedEffect(Unit) {
        tokoViewModel.getTokoById(token, tokoId)
    }

    // 2. Pre-fill data ke form
    LaunchedEffect(currentToko) {
        currentToko?.let {
            name = it.name
            location = it.address
            description = it.description
        }
    }

    // 3. Monitor Sukses untuk Navigasi Balik
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Log.d("TOKO_DEBUG", "Update Berhasil, Navigasi Balik Dipicu!")
            onSuccess()
        }
    }

    // 4. Cleanup saat meninggalkan page
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
        // --- HEADER SECTION ---
        Column {
            Text(
                text = "Edit Detail Toko",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
            Text(
                text = "Perbarui informasi toko dan daftar produkmu",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // --- INPUT FIELDS ---
//        CustomTextField(value = name, onValueChange = { name = it }, label = "Nama Toko", placeholder = "Nama Toko")
//        CustomTextField(value = location, onValueChange = { location = it }, label = "Lokasi", placeholder = "Alamat Lengkap")
//        CustomTextField(value = description, onValueChange = { description = it }, label = "Deskripsi", placeholder = "Tentang Toko...", minLines = 3)

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
            } else if (!currentToko?.imageUrl.isNullOrEmpty()) {
                AsyncImage(model = currentToko?.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color(0xFF9333EA), modifier = Modifier.size(40.dp))
                    Text("Ganti Foto Toko", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        // --- SECTION PILIH PRODUK (DUMMY) ---
        Text(text = "Pilih Produk Toko", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF374151))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                dummyProducts.forEach { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedProductIds.contains(product.id)) selectedProductIds.remove(product.id)
                                else selectedProductIds.add(product.id)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedProductIds.contains(product.id),
                            onCheckedChange = null, // Handled by row click
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF9333EA))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(text = product.price, color = Color.Gray, fontSize = 13.sp)
                        }
                        if (selectedProductIds.contains(product.id)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        }
                    }
                    if (product != dummyProducts.last()) HorizontalDivider(color = Color(0xFFF3F4F6))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SUBMIT BUTTON (Gradient) ---
        Button(
            onClick = { tokoViewModel.updateToko(token, tokoId, name, description, location, selectedImageFile) },
            modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(18.dp)),
            enabled = !isLoading && name.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFFBA68C8)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(40.dp)) // Extra space for scroll
    }
}
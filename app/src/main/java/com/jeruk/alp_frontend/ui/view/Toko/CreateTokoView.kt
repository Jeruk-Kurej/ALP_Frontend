package com.jeruk.alp_frontend.ui.view.Toko

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Info
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
// Pastikan import ini sesuai dengan lokasi helper uriToFile kamu
// import com.jeruk.alp_frontend.utils.uriToFile
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel
import java.io.File

// --- WARNA TEMA (Sama dengan Product View) ---
val BrandPrimary = Color(0xFF4F46E5)
val TextDark = Color(0xFF111827)
val TextGray = Color(0xFF6B7280)
val GradientStart = Color(0xFF6B9FFF)
val GradientEnd = Color(0xFFBA68C8)

@Composable
fun CreateTokoView(
    token: String,
    onSuccess: () -> Unit,
    navController: NavController,
    tokoViewModel: TokoViewModel = viewModel()
) {
    val context = LocalContext.current
    val isSuccess by tokoViewModel.isSuccess.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()
    val errorMessage by tokoViewModel.errorMessage.collectAsState()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            // Pastikan fungsi uriToFile sudah kamu import/buat
            selectedImageFile = uriToFile(context, it)
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onSuccess()
        }
    }

    DisposableEffect(Unit) {
        onDispose { tokoViewModel.clearState() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)) // Background abu muda clean
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER SECTION ---
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Buat Toko Baru",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Lengkapi data untuk mulai berjualan",
                fontSize = 15.sp,
                color = TextGray
            )
        }

        // --- FORM CARD ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp),
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
                        // Overlay ganti foto
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ketuk untuk mengganti", color = Color.White, fontSize = 12.sp)
                        }
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
                                text = "Upload Foto Toko",
                                color = TextGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama Toko",
                    placeholder = "Contoh: Kopi Senja"
                )

                CustomTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Alamat",
                    placeholder = "Lokasi lengkap toko"
                )

                CustomTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Deskripsi",
                    placeholder = "Ceritakan keunikan tokomu...",
                    minLines = 3
                )
            }
        }

        // --- ERROR MESSAGE ---
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorMessage!!, color = Color(0xFFB91C1C), fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- SUBMIT BUTTON (GRADIENT STYLE) ---
        Button(
            onClick = {
                tokoViewModel.createToko(token, name, description, location, selectedImageFile)
            },
            enabled = !isLoading && name.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (!isLoading && name.isNotEmpty()) {
                            // 🔥 WARNA GRADIENT (Sama seperti Product)
                            Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Simpan Toko",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- REUSABLE INPUT COMPONENT ---
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextDark
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = GradientStart, // Focus warna tema
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = GradientStart
            ),
            minLines = minLines,
            singleLine = minLines == 1
        )
    }
}
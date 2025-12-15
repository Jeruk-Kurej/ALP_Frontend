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
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel
import java.io.File

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
        uri?.let { selectedImageFile = uriToFile(context, it) }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Log.d("TOKO_DEBUG", "Navigasi Balik Dipicu!")
            onSuccess()
        }
    }

    DisposableEffect(Unit) {
        onDispose { tokoViewModel.clearState() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Background abu-abu sangat muda khas Apple
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- HEADER SECTION ---
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Informasi Toko",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
            Text(
                text = "Lengkapi detail toko untuk mulai berjualan",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // --- INPUT FIELDS ---
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nama Toko",
            placeholder = "Contoh: Kedai Kopi Enak"
        )

        CustomTextField(
            value = location,
            onValueChange = { location = it },
            label = "Lokasi / Alamat",
            placeholder = "Masukkan alamat lengkap"
        )

        CustomTextField(
            value = description,
            onValueChange = { description = it },
            label = "Deskripsi Toko",
            placeholder = "Ceritakan sedikit tentang tokomu...",
            minLines = 3
        )

        // --- IMAGE PICKER SECTION ---
        Text(
            text = "Foto Profil Toko",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF374151)
        )

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
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pilih Gambar Terbaik",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // --- ERROR MESSAGE (Tonal Style) ---
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorMessage!!, color = Color(0xFFE11D48), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SUBMIT BUTTON (Gradient Primary Button) ---
        Button(
            onClick = { tokoViewModel.createToko(token, name, description, location, selectedImageFile) },
            enabled = !isLoading && name.isNotEmpty() && token.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(18.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (!isLoading && name.isNotEmpty()) {
                            Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFFBA68C8)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFFD1D5DB)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Daftarkan Toko Sekarang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- REUSABLE CUSTOM TEXT FIELD ---
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF374151)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF9333EA),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = Color(0xFF9333EA)
            ),
            minLines = minLines,
            singleLine = minLines == 1
        )
    }
}
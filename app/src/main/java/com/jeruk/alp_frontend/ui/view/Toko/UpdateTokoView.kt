package com.jeruk.alp_frontend.ui.view.Toko

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel
import java.io.File

@Composable
fun UpdateTokoView(
    token: String,
    tokoId: Int,
    onSuccess: () -> Unit,
    navController: NavController,
    tokoViewModel: TokoViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentToko by tokoViewModel.currentToko.collectAsState()
    val isSuccess by tokoViewModel.isSuccess.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) { tokoViewModel.getTokoById(token, tokoId) }

    LaunchedEffect(currentToko) {
        currentToko?.let {
            name = it.name
            location = it.address
            description = it.description
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) { onSuccess() }
    }

    DisposableEffect(Unit) {
        onDispose { tokoViewModel.clearState() }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        uri?.let { selectedImageFile = uriToFile(context, it) }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        Text("Preview Foto", fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3F4F6)).clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (!currentToko?.imageUrl.isNullOrEmpty()) {
                AsyncImage(model = currentToko?.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.Gray)
                    Text("Klik Ganti Foto", color = Color.Gray)
                }
            }
        }

        Button(
            onClick = { tokoViewModel.updateToko(token, tokoId, name, description, location, selectedImageFile) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading && name.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White)
            else Text("Simpan Perubahan")
        }
    }
}
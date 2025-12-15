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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Toko") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        Text("Foto Toko", fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3F4F6)).clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Text("Pilih Foto Toko", color = Color.Gray)
                }
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
        }

        Button(
            onClick = { tokoViewModel.createToko(token, name, description, location, selectedImageFile) },
            enabled = !isLoading && name.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Simpan Toko", fontWeight = FontWeight.Bold)
        }
    }
}
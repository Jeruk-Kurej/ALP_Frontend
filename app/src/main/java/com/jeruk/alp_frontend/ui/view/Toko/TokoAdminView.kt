package com.jeruk.alp_frontend.ui.view.Toko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jeruk.alp_frontend.ui.model.Toko
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.AuthViewModel
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel

@Composable
fun TokoAdminView(
    navController: NavController,
    authViewModel: AuthViewModel,
    tokoViewModel: TokoViewModel = viewModel()
) {
    val userState by authViewModel.userState.collectAsState()
    val tokos by tokoViewModel.tokos.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Toko?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (userState.token.isNotEmpty()) {
            tokoViewModel.getMyTokos(userState.token)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7)) // Background abu-abu muda bersih
    ) {
        // --- HEADER SECTION (With Add Button Inside) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Toko",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${tokos.size} toko terdaftar",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // TOMBOL TAMBAH (Sejajar Header)
            Button(
                onClick = { navController.navigate(AppView.CreateToko.name) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B9FFF), Color(0xFFBA68C8))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                color = Color(0xFFBA68C8)
            )
        }

        // --- LIST TOKO ---
        if (tokos.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada toko yang terdaftar", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tokos) { item ->
                    AdminTokoCard(
                        toko = item,
                        onEdit = {
                            navController.navigate("${AppView.UpdateToko.name}/${item.id}")
                        },
                        onDelete = { showDeleteDialog = item }
                    )
                }
            }
        }
    }

    // Modal Konfirmasi Hapus (Sama seperti sebelumnya)
    showDeleteDialog?.let { toko ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Toko?") },
            text = { Text("Apakah Anda yakin ingin menghapus '${toko.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        tokoViewModel.deleteToko(userState.token, toko.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Ya, Hapus", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
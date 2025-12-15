package com.jeruk.alp_frontend.ui.view.Toko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    authViewModel: AuthViewModel = viewModel(),
    tokoViewModel: TokoViewModel = viewModel()
) {
    val userState by authViewModel.userState.collectAsState()
    val tokos by tokoViewModel.tokos.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Toko?>(null) }

    // Logic Refresh: Menarik data ulang setiap kali masuk ke page ini
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (userState.token.isNotEmpty()) {
            tokoViewModel.getMyTokos(userState.token)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AppView.CreateToko.name) },
                containerColor = Color(0xFF10B981)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF9FAFB)) // Warna background kalem HIG
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF10B981))
            }

            if (tokos.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada toko yang terdaftar", color = Color.Gray)
                }
            } else {
                // --- LIST TOKO KHUSUS ADMIN ---
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tokos) { item ->
                        AdminTokoCard(
                            toko = item,
                            onEdit = { /* TODO: Navigasi Edit */ },
                            onDelete = { showDeleteDialog = item }
                        )
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Hapus
    showDeleteDialog?.let { toko ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Toko?") },
            text = { Text("Apakah Anda yakin ingin menghapus '${toko.name}'? Data tidak dapat dikembalikan.") },
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

// --- KOMPONEN KARTU ADMIN (DENGAN EDIT & DELETE) ---



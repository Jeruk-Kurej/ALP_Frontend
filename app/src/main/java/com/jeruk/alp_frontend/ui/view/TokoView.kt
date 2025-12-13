package com.jeruk.alp_frontend.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jeruk.alp_frontend.ui.model.Toko
import com.jeruk.alp_frontend.ui.viewmodel.TokoViewModel

@Composable
fun TokoView(
    token: String,
    navController: NavController,
    tokoViewModel: TokoViewModel = viewModel()
) {
    // Pakai collectAsState sesuai style kamu
    val tokos by tokoViewModel.tokos.collectAsState()
    val isLoading by tokoViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        tokoViewModel.getMyTokos(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Background abu-abu muda HIG
    ) {
        // --- CYAN HEADER BANNER ---
        TokoBannerHeader()

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4FACFE))
            }
        } else if (tokos.isEmpty()) {
            // Style Empty Bryan
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Belum ada toko yang tersedia", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        } else {
            // --- LIST TOKO ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Spasi lega ala HIG
            ) {
                items(tokos) { item ->
                    // Panggil file yang baru kita pisah tadi!
                    TokoCardView(toko = item) {
                        // Navigasi ke produk toko ini
                        navController.navigate("ProductMenu/${item.id}")
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun TokoBannerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF00BFA5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Storefront, null, tint = Color.White, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selamat Datang!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Pilih toko untuk memulai sesi kasir", color = Color.White.copy(0.85f), fontSize = 14.sp)
        }
    }
}
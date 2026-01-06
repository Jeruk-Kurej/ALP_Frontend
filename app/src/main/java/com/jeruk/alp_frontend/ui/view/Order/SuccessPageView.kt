package com.jeruk.alp_frontend.ui.view.Order

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.delay

@Composable
fun SuccessPageView(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    var isLoading by remember { mutableStateOf(true) }

    // Efek Gimmick Loading & Auto-Navigate
    LaunchedEffect(Unit) {
        delay(2000) // Gimmick loading selama 2 detik
        isLoading = false
        delay(3000) // Tampilkan success selama 3 detik baru balik ke menu

        // Reset keranjang belanja setelah sukses
        productViewModel.clearCart()

        // Kembali ke ProductMenuView (mencari route yang sesuai di stack)
        navController.navigate(AppView.Home.name) {
            popUpTo(AppView.Home.name) { inclusive = false }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            // Tampilan Loading Gimmick
            CircularProgressIndicator(
                color = Color(0xFFC084FC),
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Memproses Pembayaran...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        } else {
            // Tampilan Success Berdasarkan Gambar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)), // Hijau muda
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF22C55E), // Hijau sukses
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pembayaran Berhasil!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Terima kasih atas pembelian Anda",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}
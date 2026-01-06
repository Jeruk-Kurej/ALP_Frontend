package com.jeruk.alp_frontend.ui.view.Order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentPageView(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    // --- DATA AMBIL DARI VIEWMODEL ---
    val cartItems by productViewModel.cartItems.collectAsState()
    val products by productViewModel.products.collectAsState()

    // Hitung total akhir (sama dengan kalkulasi di OrderPage)
    val grandTotal = remember(cartItems, products) {
        val subTotal = products.filter { cartItems.containsKey(it.id) }.sumOf {
            (it.price * (cartItems[it.id] ?: 0)).toDouble()
        }
        subTotal + (subTotal * 0.1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Background abu-abu sangat muda
            .padding(20.dp)
    ) {
        // 1. CARD TOTAL PEMBAYARAN (GRADIENT)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF60A5FA), // Biru terang
                            Color(0xFFC084FC)  // Ungu terang
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "Total Pembayaran",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatRupiah(grandTotal),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. LABEL PILIH METODE
        Text(
            text = "Pilih Metode Pembayaran",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2937)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. DAFTAR METODE PEMBAYARAN
        PaymentMethodItem(
            title = "QRIS",
            description = "Scan QR code untuk pembayaran",
            icon = Icons.Default.QrCodeScanner,
            iconBgColor = Color(0xFFF5F3FF), // Background ungu muda
            iconTintColor = Color(0xFF8B5CF6), // Ikon ungu
            onClick = {
                navController.navigate("QRISPage")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PaymentMethodItem(
            title = "Tunai",
            description = "Bayar dengan uang tunai",
            icon = Icons.Default.Payments,
            iconBgColor = Color(0xFFF0FDF4), // Background hijau muda
            iconTintColor = Color(0xFF22C55E), // Ikon hijau
            onClick = {
                navController.navigate("CashPage")
            }
        )
    }
}

@Composable
fun PaymentMethodItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box Ikon dengan background bulat kotak (soft color)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTintColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
package com.jeruk.alp_frontend.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomingView(
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- LOGO & JUDUL ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF64B5F6), Color(0xFFBA68C8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(45.dp))
            }

            Text(
                text = "Sum-O",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFFEC4899))
                    )
                )
            )
            Text(
                text = "Sum it up. Own your business",
                fontSize = 14.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )
        }

        // --- AREA TENGAH ---
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Deskripsi Utama
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Text(
                    text = "Sistem POS dan manajemen bisnis yang modern, mudah, dan powerful untuk membantu Anda mengelola bisnis dengan lebih baik.",
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kotak Fitur Overview
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureOverviewItem(
                    title = "POS Cepat & Responsif",
                    desc = "Proses transaksi dalam hitungan detik",
                    icon = Icons.Default.ShoppingCart,
                    iconBg = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1976D2)
                )
                FeatureOverviewItem(
                    title = "Manajemen Inventaris",
                    desc = "Kontrol stok produk dengan mudah",
                    icon = Icons.Default.AutoGraph,
                    iconBg = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF7B1FA2)
                )
                FeatureOverviewItem(
                    title = "Analisis Bisnis Real-time",
                    desc = "Laporan penjualan lengkap & akurat",
                    icon = Icons.Default.InsertChartOutlined,
                    iconBg = Color(0xFFFCE4EC),
                    iconColor = Color(0xFFC2185B)
                )
            }
        }

        // --- Button & FOOTER ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2979FF), Color(0xFFBA68C8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mulai Sekarang", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Text(
                text = "© 2025 Sum-O - All rights reserved",
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun FeatureOverviewItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = desc, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
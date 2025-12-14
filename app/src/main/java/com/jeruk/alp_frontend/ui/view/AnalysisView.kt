package com.jeruk.alp_frontend.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.route.AppView

@Composable
fun AnalysisPageView(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // --- GRID RINGKASAN (4 Kotak) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMiniCard(Modifier.weight(1f), "Total Produk", "6", Icons.Default.Inventory2, Color(0xFFE3F2FD), Color(0xFF2196F3))
                SummaryMiniCard(Modifier.weight(1f), "Kategori", "3", Icons.Default.LocalOffer, Color(0xFFF3E5F5), Color(0xFF9C27B0))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMiniCard(Modifier.weight(1f), "Total Toko", "2", Icons.Default.Storefront, Color(0xFFFCE4EC), Color(0xFFE91E63))
                SummaryMiniCard(Modifier.weight(1f), "Pendapatan", "Rp 0", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFE8F5E9), Color(0xFF4CAF50))
            }

            // --- ANALISIS KESELURUHAN CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Analisis Bisnis Keseluruhan", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    AnalysisItem("Total Transaksi Hari Ini", "Semua pembayaran", "0", Icons.Default.BarChart, Color(0xFFE3F2FD), Color(0xFF2196F3))
                    AnalysisItem("Pendapatan Hari Ini", "Total pemasukan", "Rp 0", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFE8F5E9), Color(0xFF4CAF50))

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            navController.navigate(AppView.AnalysisDetail.name)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF60A5FA), Color(0xFFA855F7)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lihat Analisis Lengkap", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(modifier: Modifier, title: String, value: String, icon: ImageVector, bgColor: Color, iconColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(bgColor), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun AnalysisItem(title: String, sub: String, value: String, icon: ImageVector, bgColor: Color, iconColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(bgColor), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(sub, color = Color.Gray, fontSize = 11.sp)
        }
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if(value.contains("Rp")) Color(0xFF10B981) else Color.Black)
    }
}
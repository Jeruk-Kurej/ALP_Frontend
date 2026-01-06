package com.jeruk.alp_frontend.ui.view.Analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController

@Composable
fun AnalysisDetailView(navController: NavController) {
    var selectedPeriod by remember { mutableStateOf("Minggu") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Keseluruhan Section
            Text(
                "Total Keseluruhan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TotalCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Pendapatan",
                    value = "Rp 1.550.000",
                    icon = Icons.Default.TrendingUp,
                    iconBgColor = Color(0xFFFFF4ED),
                    iconColor = Color(0xFFFF6B35)
                )
                TotalCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Pesanan",
                    value = "3",
                    icon = Icons.Default.ShoppingBag,
                    iconBgColor = Color(0xFFE0F7F4),
                    iconColor = Color(0xFF14B8A6)
                )
            }

            // Produk Terlaris Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Produk Terlaris",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Belum ada transaksi",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Performa Per Cabang Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Performa Per Cabang",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Branch Items
                    BranchPerformanceItem(
                        name = "Kopi Nusantara - Cabang Sudirman",
                        orders = "0 pesanan",
                        revenue = "Rp 0"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BranchPerformanceItem(
                        name = "Kopi Nusantara - Cabang Kemang",
                        orders = "0 pesanan",
                        revenue = "Rp 0"
                    )
                }
            }

            // Pendapatan 7 Hari Terakhir Section with Tabs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pendapatan 7 Hari Terakhir",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Period Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PeriodTab("Hari", selectedPeriod == "Hari") { selectedPeriod = "Hari" }
                        PeriodTab("Minggu", selectedPeriod == "Minggu") { selectedPeriod = "Minggu" }
                        PeriodTab("Bulan", selectedPeriod == "Bulan") { selectedPeriod = "Bulan" }
                        PeriodTab("Tahun", selectedPeriod == "Tahun") { selectedPeriod = "Tahun" }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chart Placeholder - showing sample data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Grafik Pendapatan",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pendapatan : Rp 3.600.000",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Pendapatan Per Kategori Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pendapatan Per Kategori",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Grafik Kategori",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Filter Toko & Hari Ini/Bulan Ini Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Filter Toko Dropdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD1FAE5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Filter Toko",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    "Semua Toko",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hari Ini Section
                    Text(
                        "Hari Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DaySummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Pendapatan",
                            value = "Rp 0",
                            icon = Icons.Default.AttachMoney,
                            bgColor = Color(0xFFD1FAE5),
                            iconColor = Color(0xFF10B981)
                        )
                        DaySummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Pesanan",
                            value = "0",
                            icon = Icons.Default.ShoppingBag,
                            bgColor = Color(0xFFDBEAFE),
                            iconColor = Color(0xFF3B82F6)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bulan Ini Section
                    Text(
                        "Bulan Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DaySummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Pendapatan",
                            value = "Rp 0",
                            icon = Icons.Default.AttachMoney,
                            bgColor = Color(0xFFFCE7F3),
                            iconColor = Color(0xFFEC4899)
                        )
                        DaySummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Pesanan",
                            value = "0",
                            icon = Icons.Default.ShoppingBag,
                            bgColor = Color(0xFFFEE2E2),
                            iconColor = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TotalCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(title, fontSize = 12.sp, color = Color(0xFF6B7280))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun BranchPerformanceItem(name: String, orders: String, revenue: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFD1FAE5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(orders, fontSize = 12.sp, color = Color(0xFF6B7280))
            }
        }
        Text(
            revenue,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF10B981)
        )
    }
}

@Composable
fun PeriodTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF9333EA) else Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            label,
            color = if (isSelected) Color.White else Color(0xFF6B7280),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun DaySummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(title, fontSize = 12.sp, color = Color(0xFF6B7280))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

    }
}
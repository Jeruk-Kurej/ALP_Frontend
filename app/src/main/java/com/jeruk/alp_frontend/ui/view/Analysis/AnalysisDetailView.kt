package com.jeruk.alp_frontend.ui.view.Analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.ui.viewmodel.AnalysisViewModel
import com.jeruk.alp_frontend.ui.viewmodel.TopProductResult
import com.jeruk.alp_frontend.ui.model.Toko
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalysisDetailView(
    navController: NavController,
    token: String,
    viewModel: AnalysisViewModel = viewModel()
) {
    // --- STATE UI ---
    var selectedPeriod by remember { mutableStateOf("Minggu") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // --- DATA STATE FROM VIEWMODEL ---
    val dashboardState by viewModel.dashboardState.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val categorySales by viewModel.categorySales.collectAsState() // Data Grafik Kategori
    val isLoading by viewModel.isLoading.collectAsState()

    val tokoList by viewModel.tokoList.collectAsState()
    val selectedToko by viewModel.selectedToko.collectAsState()

    // --- LOAD DATA ---
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData(token)
        viewModel.calculateTopProducts(token)
        viewModel.calculateCategorySales(token)
    }

    // --- HELPER FUNCTIONS ---
    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount).replace("Rp", "Rp ").substringBefore(",00")
    }

    fun formatSimplePrice(amount: Double): String {
        return if (amount >= 1_000_000) {
            val millions = amount / 1_000_000
            String.format("%.1fjt", millions)
        } else if (amount >= 1_000) {
            val thousands = amount / 1_000
            String.format("%.0frb", thousands)
        } else {
            String.format("%.0f", amount)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ================================================================
            // 1. CARD INTERAKTIF: FILTER TOKO + DATA HARI INI
            // ================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // HEADER: DROPDOWN
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .clickable { isDropdownExpanded = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFD1FAE5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Filter Toko", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = selectedToko?.name ?: "Semua Toko",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Icon(if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
                        }

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.background(Color.White).width(280.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Toko", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.selectToko(null)
                                    isDropdownExpanded = false
                                }
                            )
                            Divider()
                            tokoList.forEach { toko ->
                                DropdownMenuItem(
                                    text = { Text(toko.name) },
                                    onClick = {
                                        viewModel.selectToko(toko)
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))

                    SectionTitle("Ringkasan Hari Ini", Icons.Default.CalendarToday)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            Modifier.weight(1f), "Pendapatan", formatRupiah(dashboardState.todayRevenue),
                            Icons.Default.AttachMoney, Color(0xFFD1FAE5), Color(0xFF10B981)
                        )
                        SummaryCard(
                            Modifier.weight(1f), "Pesanan", dashboardState.todayOrders.toString(),
                            Icons.Default.ShoppingBag, Color(0xFFDBEAFE), Color(0xFF3B82F6)
                        )
                    }
                }
            }

            // =========================================
            // 2. TOTAL KESELURUHAN (GLOBAL)
            // =========================================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Total Keseluruhan", Icons.Default.Summarize)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        Modifier.weight(1f), "Total Pendapatan", formatRupiah(dashboardState.totalRevenue),
                        Icons.Default.TrendingUp, Color(0xFFFFF7ED), Color(0xFFF97316)
                    )
                    SummaryCard(
                        Modifier.weight(1f), "Total Pesanan", dashboardState.totalOrders.toString(),
                        Icons.Default.Inventory, Color(0xFFE0F2FE), Color(0xFF0EA5E9)
                    )
                }
            }

            // =========================================
            // 3. PRODUK TERLARIS (Moved Here)
            // =========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Produk Terlaris", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (topProducts.isEmpty()) {
                        Text("Belum ada data penjualan", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        topProducts.forEachIndexed { index, item ->
                            TopProductItem(index + 1, item)
                            if (index < topProducts.size - 1) Divider(color = Color(0xFFF3F4F6), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }

            // =========================================
            // 4. CHART PENDAPATAN (Placeholder)
            // =========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grafik Pendapatan (Line)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PeriodTab("Hari", selectedPeriod == "Hari") { selectedPeriod = "Hari" }
                        PeriodTab("Minggu", selectedPeriod == "Minggu") { selectedPeriod = "Minggu" }
                        PeriodTab("Bulan", selectedPeriod == "Bulan") { selectedPeriod = "Bulan" }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShowChart, null, tint = Color(0xFFA855F7), modifier = Modifier.size(60.dp))
                        Text("Grafik akan muncul di sini", Modifier.padding(top = 80.dp), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // =========================================
            // 5. PENDAPATAN PER KATEGORI (Moved Here)
            // =========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pendapatan Per Kategori", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (categorySales.isEmpty()) {
                        Text("Belum ada data kategori", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val maxRevenue = categorySales.maxOfOrNull { it.totalRevenue } ?: 1.0

                        Row(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            categorySales.forEach { cat ->
                                val barHeightFraction = (cat.totalRevenue / maxRevenue).toFloat()
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = formatSimplePrice(cat.totalRevenue),
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6366F1), modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .fillMaxHeight(barHeightFraction)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(Brush.verticalGradient(listOf(Color(0xFF818CF8), Color(0xFFC7D2FE))))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = cat.name, fontSize = 11.sp, color = Color.Gray,
                                        maxLines = 1, fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun SectionTitle(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF374151))
    }
}

@Composable
fun SummaryCard(modifier: Modifier, title: String, value: String, icon: ImageVector, bgCol: Color, iconCol: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(bgCol), Alignment.Center) {
                Icon(icon, null, tint = iconCol, modifier = Modifier.size(20.dp))
            }
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun TopProductItem(rank: Int, data: TopProductResult) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3E8FF)), Alignment.Center) {
                Text("#$rank", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9333EA))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(data.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                Text("${data.totalSold} terjual", fontSize = 11.sp, color = Color.Gray)
            }
        }
        Text(
            text = formatRp.format(data.totalRevenue).substringBefore(",00"),
            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF9333EA)
        )
    }
}

@Composable
fun PeriodTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White else Color.Transparent),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(1.dp) else ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, color = if (isSelected) Color(0xFF9333EA) else Color.Gray, fontSize = 12.sp)
    }
}
package com.jeruk.alp_frontend.ui.view.Analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeruk.alp_frontend.ui.viewmodel.AnalysisViewModel
import com.jeruk.alp_frontend.ui.viewmodel.TopProductResult
import com.jeruk.alp_frontend.ui.viewmodel.TimePeriod
import com.jeruk.alp_frontend.ui.model.Toko
import java.text.NumberFormat
import java.util.Locale
import com.jeruk.alp_frontend.utils.CurrencyFormatter
import com.jeruk.alp_frontend.data.container.AppContainer

@Composable
fun AnalysisDetailView(
    navController: NavController,
    token: String,
    viewModel: AnalysisViewModel = viewModel()
) {
    // --- STATE UI ---
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // --- DATA STATE FROM VIEWMODEL ---
    val dashboardState by viewModel.dashboardState.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val categorySales by viewModel.categorySales.collectAsState() // Data Grafik Kategori
    val dailyRevenue by viewModel.dailyRevenue.collectAsState() // Data Line Chart
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    val tokoList by viewModel.tokoList.collectAsState()
    val selectedToko by viewModel.selectedToko.collectAsState()

    // --- LOAD DATA ---
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData(token)
        viewModel.calculateTopProducts(token)
        viewModel.calculateCategorySales(token)
        viewModel.calculateDailyRevenue(token)
    }

    // --- RE-CALCULATE when period changes ---
    LaunchedEffect(selectedPeriod) {
        viewModel.calculateCategorySales(token)
        viewModel.calculateDailyRevenue(token)
    }

    // --- HELPER FUNCTIONS ---
    @Composable
    fun formatRupiah(amount: Double): String {
        val selectedCurrency by AppContainer.userPreferencesRepository.selectedCurrency.collectAsState(initial = "IDR")
        return CurrencyFormatter.formatPrice(amount, selectedCurrency)
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
            // 4. CHART PENDAPATAN (Line Chart)
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
                        Text("Grafik Pendapatan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PeriodTab("Hari", selectedPeriod == TimePeriod.DAY) { 
                            viewModel.setTimePeriod(TimePeriod.DAY)
                        }
                        PeriodTab("Minggu", selectedPeriod == TimePeriod.WEEK) { 
                            viewModel.setTimePeriod(TimePeriod.WEEK)
                        }
                        PeriodTab("Bulan", selectedPeriod == TimePeriod.MONTH) { 
                            viewModel.setTimePeriod(TimePeriod.MONTH)
                        }
                        PeriodTab("Tahun", selectedPeriod == TimePeriod.YEAR) { 
                            viewModel.setTimePeriod(TimePeriod.YEAR)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Line Chart
                    if (dailyRevenue.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada data", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LineChart(
                            data = dailyRevenue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            // =========================================
            // 5. PENDAPATAN PER KATEGORI (Real Category Data)
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
                        Column {
                            Text("Pendapatan Per Kategori", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                text = when (selectedPeriod) {
                                    TimePeriod.DAY -> "1 Hari Terakhir"
                                    TimePeriod.WEEK -> "7 Hari Terakhir"
                                    TimePeriod.MONTH -> "30 Hari Terakhir"
                                    TimePeriod.YEAR -> "12 Bulan Terakhir"
                                },
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (categorySales.isEmpty()) {
                        Text("Belum ada data kategori", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val maxRevenue = categorySales.maxOfOrNull { it.totalRevenue } ?: 1.0

                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                                .width(32.dp)
                                                .fillMaxHeight(barHeightFraction)
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(Brush.verticalGradient(listOf(Color(0xFF818CF8), Color(0xFFC7D2FE))))
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                categorySales.forEach { cat ->
                                    Text(
                                        text = cat.name,
                                        fontSize = 11.sp,
                                        color = Color(0xFF374151),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2
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
    val selectedCurrency by AppContainer.userPreferencesRepository.selectedCurrency.collectAsState(initial = "IDR")
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
            text = CurrencyFormatter.formatPrice(data.totalRevenue, selectedCurrency),
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

@Composable
fun LineChart(
    data: List<com.jeruk.alp_frontend.ui.viewmodel.DailyRevenue>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxRevenue = data.maxOfOrNull { it.revenue } ?: 1.0
    val minRevenue = 0.0
    
    // Helper function to format currency
    fun formatCurrency(value: Double): String {
        return when {
            value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format("%.0fK", value / 1_000)
            else -> String.format("%.0f", value)
        }
    }
    
    // Calculate Y-axis labels (5 levels)
    val yLabels = (0..4).map { index ->
        maxRevenue * (4 - index) / 4
    }
    
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-axis labels
            Column(
                modifier = Modifier.width(50.dp).padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yLabels.forEach { value ->
                    Text(
                        text = formatCurrency(value),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Chart
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .padding(vertical = 8.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 20f
                
                val usableWidth = width - padding * 2
                val usableHeight = height - padding * 2
                
                // Draw horizontal grid lines
                yLabels.forEachIndexed { index, _ ->
                    val y = padding + (index.toFloat() / 4) * usableHeight
                    drawLine(
                        color = Color(0xFFE5E7EB),
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }
                
                // Calculate points
                val points = data.mapIndexed { index, dailyRevenue ->
                    val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * usableWidth
                    val normalizedValue = if (maxRevenue > minRevenue) {
                        ((dailyRevenue.revenue - minRevenue) / (maxRevenue - minRevenue)).toFloat()
                    } else {
                        0.5f
                    }
                    val y = height - padding - (normalizedValue * usableHeight)
                    Offset(x, y)
                }
                
                // Draw gradient area under the line
                val gradientPath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, height - padding)
                        lineTo(points.first().x, points.first().y)
                        
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                        
                        lineTo(points.last().x, height - padding)
                        close()
                    }
                }
                
                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB5A3FF).copy(alpha = 0.3f),
                            Color(0xFFB5A3FF).copy(alpha = 0.05f)
                        )
                    )
                )
                
                // Draw line
                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = Color(0xFF9F7AEA),
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                
                // Draw points (dots)
                points.forEach { point ->
                    drawCircle(
                        color = Color(0xFF9F7AEA),
                        radius = 6f,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = point
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // X-axis labels (day names or month numbers)
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { dailyRevenue ->
                Text(
                    text = dailyRevenue.dayLabel,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
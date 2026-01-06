package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Toko
import com.jeruk.alp_frontend.ui.model.Order // Pastikan import Order benar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- ENUMS ---

enum class TimePeriod {
    DAY, WEEK, MONTH, YEAR
}

// --- DATA CLASSES ---

data class TopProductResult(
    val name: String,
    val totalSold: Int,
    val totalRevenue: Double
)

data class CategoryResult(
    val name: String,
    val totalRevenue: Double
)

data class DailyRevenue(
    val date: String,
    val revenue: Double,
    val dayLabel: String // e.g., "Sen", "Sel", "Rab"
)

data class DashboardState(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayOrders: Int = 0,
    // Time-period specific data
    val dayRevenue: Double = 0.0,
    val weekRevenue: Double = 0.0,
    val monthRevenue: Double = 0.0,
    val yearRevenue: Double = 0.0,
    // Field tambahan agar AnalysisPageView tidak error
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalTokos: Int = 0
)

// --- VIEWMODEL ---

class AnalysisViewModel : ViewModel() {

    private val orderRepository = AppContainer.orderRepository
    private val tokoRepository = AppContainer.tokoRepository
    private val productRepository = AppContainer.productRepository
    private val categoryRepository = AppContainer.categoryRepository

    // State Dashboard
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    // State List Toko
    private val _tokoList = MutableStateFlow<List<Toko>>(emptyList())
    val tokoList: StateFlow<List<Toko>> = _tokoList.asStateFlow()

    // State Toko Terpilih
    private val _selectedToko = MutableStateFlow<Toko?>(null)
    val selectedToko: StateFlow<Toko?> = _selectedToko.asStateFlow()

    // Top Products
    private val _topProducts = MutableStateFlow<List<TopProductResult>>(emptyList())
    val topProducts: StateFlow<List<TopProductResult>> = _topProducts.asStateFlow()

    // Category Sales (Untuk Grafik Batang)
    private val _categorySales = MutableStateFlow<List<CategoryResult>>(emptyList())
    val categorySales: StateFlow<List<CategoryResult>> = _categorySales.asStateFlow()

    // Daily Revenue Data (Untuk Line Chart)
    private val _dailyRevenue = MutableStateFlow<List<DailyRevenue>>(emptyList())
    val dailyRevenue: StateFlow<List<DailyRevenue>> = _dailyRevenue.asStateFlow()

    // Selected Time Period
    private val _selectedPeriod = MutableStateFlow(TimePeriod.WEEK)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allOrdersCache: List<Order> = emptyList()
    private var allProductsCache = emptyList<com.jeruk.alp_frontend.ui.model.Product>()

    fun loadDashboardData(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Ambil Semua Data
                val orders = orderRepository.getAllOrders(token)
                val tokos = tokoRepository.getMyTokos(token)
                val products = productRepository.getAllProducts(token)

                // Ambil kategori (handle error jika belum ada backendnya)
                val categories = try {
                    categoryRepository.getAllCategories(token)
                } catch (e: Exception) {
                    emptyList()
                }

                // Simpan Cache
                allOrdersCache = orders
                allProductsCache = products
                _tokoList.value = tokos

                // 2. Hitung Data Global
                val globalRevenue = orders.sumOf { order ->
                    order.orderItems.sumOf { item -> item.orderAmount * item.productPrice }
                }.toDouble()

                val globalOrderCount = orders.size

                // 3. Hitung Revenue by Time Period
                val dayRevenue = calculateRevenueByPeriod(orders, TimePeriod.DAY)
                val weekRevenue = calculateRevenueByPeriod(orders, TimePeriod.WEEK)
                val monthRevenue = calculateRevenueByPeriod(orders, TimePeriod.MONTH)
                val yearRevenue = calculateRevenueByPeriod(orders, TimePeriod.YEAR)

                // 4. Update State Awal
                updateTodayMetrics(null)

                // 5. Update TOTAL KESELURUHAN with Time Period Data
                _dashboardState.value = _dashboardState.value.copy(
                    totalRevenue = globalRevenue,
                    totalOrders = globalOrderCount,
                    dayRevenue = dayRevenue,
                    weekRevenue = weekRevenue,
                    monthRevenue = monthRevenue,
                    yearRevenue = yearRevenue,
                    totalProducts = products.size,
                    totalCategories = categories.size,
                    totalTokos = tokos.size
                )

            } catch (e: Exception) {
                println("Error loading dashboard: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectToko(toko: Toko?) {
        _selectedToko.value = toko
        updateTodayMetrics(toko)
    }

    private fun updateTodayMetrics(toko: Toko?) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        // Filter Toko
        val filteredByToko = if (toko == null) {
            allOrdersCache
        } else {
            // Pastikan Order punya field 'tokoId'
            allOrdersCache.filter { it.tokoId == toko.id }
        }

        // Filter Hari Ini
        val ordersToday = filteredByToko.filter { order ->
            try {
                // Extract date from createdAt (format: "2024-01-07T10:30:00Z" or "2024-01-07")
                val orderDate = if (order.createDate.contains("T")) {
                    order.createDate.substring(0, 10) // Ambil YYYY-MM-DD
                } else {
                    order.createDate.take(10)
                }
                orderDate == todayStr
            } catch (e: Exception) {
                android.util.Log.e(
                    "AnalysisViewModel",
                    "Error parsing date: ${order.createDate}",
                    e
                )
                false
            }
        }

        val revenueToday = ordersToday.sumOf { order ->
            order.orderItems.sumOf { item -> item.orderAmount * item.productPrice }
        }.toDouble()

        val countToday = ordersToday.size

        _dashboardState.value = _dashboardState.value.copy(
            todayRevenue = revenueToday,
            todayOrders = countToday
        )
    }

    fun calculateTopProducts(token: String) {
        viewModelScope.launch {
            try {
                val orders =
                    if (allOrdersCache.isNotEmpty()) allOrdersCache else orderRepository.getAllOrders(
                        token
                    )
                val productMap = mutableMapOf<Int, TopProductResult>()

                orders.forEach { order ->
                    order.orderItems.forEach { item ->
                        val id = item.productId
                        val name = item.productName
                        val qty = item.orderAmount
                        val price = item.productPrice.toString().toDoubleOrNull() ?: 0.0
                        val revenue = qty * price

                        if (productMap.containsKey(id)) {
                            val current = productMap[id]!!
                            productMap[id] = current.copy(
                                totalSold = current.totalSold + qty,
                                totalRevenue = current.totalRevenue + revenue
                            )
                        } else {
                            productMap[id] = TopProductResult(name, qty, revenue)
                        }
                    }
                }

                val sortedList = productMap.values.toList()
                    .sortedByDescending { it.totalSold }
                    .take(5)

                _topProducts.value = sortedList
            } catch (e: Exception) {
                println("Error calculating top products: ${e.message}")
            }
        }
    }

    // --- HELPER: Calculate revenue by time period ---
    private fun calculateRevenueByPeriod(orders: List<Order>, period: TimePeriod): Double {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // Calculate cutoff date based on period
        calendar.time = currentDate
        when (period) {
            TimePeriod.DAY -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            TimePeriod.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            TimePeriod.MONTH -> calendar.add(Calendar.DAY_OF_YEAR, -30)
            TimePeriod.YEAR -> calendar.add(Calendar.YEAR, -1)
        }
        val cutoffDate = calendar.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return orders.filter { order ->
            try {
                val orderDate = dateFormat.parse(order.createDate.substring(0, 10))
                orderDate?.after(cutoffDate) ?: false
            } catch (e: Exception) {
                false
            }
        }.sumOf { order ->
            order.orderItems.sumOf { item -> item.orderAmount * item.productPrice }
        }.toDouble()
    }

    // --- FUNCTION: Change selected period ---
    fun setTimePeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    // --- FUNCTION: Get revenue for current selected period ---
    fun getCurrentPeriodRevenue(): Double {
        return when (_selectedPeriod.value) {
            TimePeriod.DAY -> _dashboardState.value.dayRevenue
            TimePeriod.WEEK -> _dashboardState.value.weekRevenue
            TimePeriod.MONTH -> _dashboardState.value.monthRevenue
            TimePeriod.YEAR -> _dashboardState.value.yearRevenue
        }
    }

    // --- FUNCTION: Calculate daily revenue for line chart ---
    fun calculateDailyRevenue(token: String) {
        viewModelScope.launch {
            try {
                val orders =
                    if (allOrdersCache.isNotEmpty()) allOrdersCache else orderRepository.getAllOrders(
                        token
                    )
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
                val monthFormat = SimpleDateFormat("MMM", Locale("id", "ID"))

                val calendar = Calendar.getInstance()
                val currentDate = calendar.time

                // For MONTH and YEAR periods, group by month instead of day
                if (_selectedPeriod.value == TimePeriod.MONTH || _selectedPeriod.value == TimePeriod.YEAR) {
                    val numMonths = if (_selectedPeriod.value == TimePeriod.YEAR) 12 else 12
                    val revenueByMonth = mutableMapOf<String, Double>()

                    // Initialize all months with 0
                    for (i in 0 until numMonths) {
                        calendar.time = currentDate
                        calendar.add(Calendar.MONTH, -(numMonths - 1 - i))
                        val monthKey =
                            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
                        revenueByMonth[monthKey] = 0.0
                    }

                    // Calculate actual revenue per month
                    orders.forEach { order ->
                        try {
                            val orderDateStr = order.createDate.substring(0, 7) // yyyy-MM
                            if (revenueByMonth.containsKey(orderDateStr)) {
                                val revenue = order.orderItems.sumOf { item ->
                                    item.orderAmount * item.productPrice
                                }.toDouble()
                                revenueByMonth[orderDateStr] =
                                    revenueByMonth[orderDateStr]!! + revenue
                            }
                        } catch (e: Exception) {
                            // Skip invalid dates
                        }
                    }

                    // Convert to list with month labels
                    val result =
                        revenueByMonth.entries.sortedBy { it.key }.mapIndexed { index, entry ->
                            val monthLabel = (index + 1).toString() // "1", "2", "3", ..., "12"

                            DailyRevenue(
                                date = entry.key,
                                revenue = entry.value,
                                dayLabel = monthLabel
                            )
                        }

                    _dailyRevenue.value = result
                } else {
                    // For DAY and WEEK periods, show daily data
                    val numDays = if (_selectedPeriod.value == TimePeriod.DAY) 7 else 7
                    val revenueByDate = mutableMapOf<String, Double>()

                    // Initialize all dates with 0
                    for (i in 0 until numDays) {
                        calendar.time = currentDate
                        calendar.add(Calendar.DAY_OF_YEAR, -(numDays - 1 - i))
                        val dateStr = dateFormat.format(calendar.time)
                        revenueByDate[dateStr] = 0.0
                    }

                    // Calculate actual revenue per date
                    orders.forEach { order ->
                        try {
                            val orderDateStr = order.createDate.substring(0, 10)
                            if (revenueByDate.containsKey(orderDateStr)) {
                                val revenue = order.orderItems.sumOf { item ->
                                    item.orderAmount * item.productPrice
                                }.toDouble()
                                revenueByDate[orderDateStr] =
                                    revenueByDate[orderDateStr]!! + revenue
                            }
                        } catch (e: Exception) {
                            // Skip invalid dates
                        }
                    }

                    // Convert to list with day labels
                    val result =
                        revenueByDate.entries.sortedBy { it.key }.mapIndexed { index, entry ->
                            val date = dateFormat.parse(entry.key)
                            val dayLabel = if (date != null) {
                                dayFormat.format(date).take(3).capitalize()
                            } else {
                                "Day $index"
                            }

                            DailyRevenue(
                                date = entry.key,
                                revenue = entry.value,
                                dayLabel = dayLabel
                            )
                        }

                    _dailyRevenue.value = result
                }

            } catch (e: Exception) {
                println("Error calculating daily revenue: ${e.message}")
            }
        }
    }

    // --- LOGIC: HITUNG KATEGORI UNTUK GRAFIK (Using Real Category Data) ---
    fun calculateCategorySales(token: String) {
        viewModelScope.launch {
            try {
                val orders =
                    if (allOrdersCache.isNotEmpty()) allOrdersCache else orderRepository.getAllOrders(
                        token
                    )
                val products =
                    if (allProductsCache.isNotEmpty()) allProductsCache else productRepository.getAllProducts(
                        token
                    )

                // Create product ID to category name map
                val productCategoryMap = products.associate { it.id to it.categoryName }

                // Filter orders based on selected time period
                val filteredOrders = filterOrdersByPeriod(orders, _selectedPeriod.value)

                val catMap = mutableMapOf<String, Double>()

                filteredOrders.forEach { order ->
                    order.orderItems.forEach { item ->
                        // Get actual category name from product
                        val catName = productCategoryMap[item.productId] ?: "Lainnya"

                        val revenue = item.orderAmount * item.productPrice.toDouble()
                        val currentTotal = catMap.getOrDefault(catName, 0.0)
                        catMap[catName] = currentTotal + revenue
                    }
                }

                val resultList = catMap.map {
                    CategoryResult(it.key, it.value)
                }.sortedByDescending { it.totalRevenue }

                _categorySales.value = resultList

            } catch (e: Exception) {
                println("Gagal hitung kategori: ${e.message}")
            }
        }
    }

    private fun filterOrdersByPeriod(orders: List<Order>, period: TimePeriod): List<Order> {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        calendar.time = currentDate
        when (period) {
            TimePeriod.DAY -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            TimePeriod.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            TimePeriod.MONTH -> calendar.add(Calendar.DAY_OF_YEAR, -30)
            TimePeriod.YEAR -> calendar.add(Calendar.YEAR, -1)
        }
        val cutoffDate = calendar.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return orders.filter { order ->
            try {
                val orderDate = dateFormat.parse(order.createDate.substring(0, 10))
                orderDate?.after(cutoffDate) ?: false
            } catch (e: Exception) {
                false
            }
        }
    }
}
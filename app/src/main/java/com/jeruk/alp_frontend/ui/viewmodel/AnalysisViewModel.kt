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
import java.util.Date
import java.util.Locale

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

data class DashboardState(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayOrders: Int = 0,
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var allOrdersCache: List<Order> = emptyList()

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
                _tokoList.value = tokos

                // 2. Hitung Data Global
                val globalRevenue = orders.sumOf { order ->
                    order.orderItems.sumOf { item -> item.orderAmount * item.productPrice }
                }.toDouble()

                val globalOrderCount = orders.size

                // 3. Update State Awal
                updateTodayMetrics(null)

                // 4. Update TOTAL KESELURUHAN
                _dashboardState.value = _dashboardState.value.copy(
                    totalRevenue = globalRevenue,
                    totalOrders = globalOrderCount,
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
            // Logic tanggal (bypass true sementara)
            // order.createdAt.contains(todayStr)
            true
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
                val orders = if (allOrdersCache.isNotEmpty()) allOrdersCache else orderRepository.getAllOrders(token)
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

    // --- LOGIC BARU: HITUNG KATEGORI UNTUK GRAFIK ---
    fun calculateCategorySales(token: String) {
        viewModelScope.launch {
            try {
                val orders = if (allOrdersCache.isNotEmpty()) allOrdersCache else orderRepository.getAllOrders(token)
                val catMap = mutableMapOf<String, Double>()

                orders.forEach { order ->
                    order.orderItems.forEach { item ->
                        // Logic sederhana pengelompokan nama (karena belum tentu ada field category)
                        val nameLower = item.productName.lowercase()
                        val catName = when {
                            nameLower.contains("kopi") || nameLower.contains("coffee") -> "Kopi"
                            nameLower.contains("tea") || nameLower.contains("teh") -> "Minuman"
                            nameLower.contains("roti") || nameLower.contains("bread") -> "Makanan"
                            nameLower.contains("cake") || nameLower.contains("kue") -> "Snack"
                            else -> "Lainnya"
                        }

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
}
package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// State khusus untuk Dashboard
data class DashboardState(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalTokos: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayOrders: Int = 0
)

class AnalysisViewModel : ViewModel() {

    // Akses ke semua Repository
    private val orderRepository = AppContainer.orderRepository
    private val productRepository = AppContainer.productRepository
    private val categoryRepository = AppContainer.categoryRepository
    private val tokoRepository = AppContainer.tokoRepository

    // State untuk Dashboard
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    // State untuk Top 5 (yang sudah ada sebelumnya)
    private val _topProducts = MutableStateFlow<List<TopProductResult>>(emptyList())
    val topProducts: StateFlow<List<TopProductResult>> = _topProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- FUNGSI BARU UNTUK DASHBOARD ---
    fun loadDashboardData(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Ambil Data secara Paralel/Berurutan
                val allOrders = orderRepository.getAllOrders(token)
                val allProducts = productRepository.getAllProducts(token)
                val allCategories = categoryRepository.getAllCategories(token)
                val allTokos = tokoRepository.getMyTokos(token)

                // 2. Hitung Total Keseluruhan
                val revenue = allOrders.sumOf {
                    // Asumsi: hitung total dari item, atau field totalPrice jika ada
                    it.orderItems.sumOf { item -> item.orderAmount * item.productPrice }
                }.toDouble()

                val orderCount = allOrders.size
                val productCount = allProducts.size
                val categoryCount = allCategories.size
                val tokoCount = allTokos.size

                // 3. Hitung Data "Hari Ini" (Menggunakan SimpleDateFormat pengganti LocalDate)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date()) // Menghasilkan string contoh: "2024-05-28"

                // Filter order yang tanggalnya mengandung string hari ini
                val ordersToday = allOrders.filter { order ->
                    // TODO: Sesuaikan logika ini dengan format tanggal dari backend kamu
                    // Jika order.createdAt adalah string ISO (contoh: "2024-05-28T14:30:00"), gunakan:
                    // order.createdAt.contains(today)

                    // Untuk sementara kita set TRUE agar data muncul dulu (menganggap semua order valid)
                    true
                }

                val revenueToday = ordersToday.sumOf {
                    it.orderItems.sumOf { item -> item.orderAmount * item.productPrice }
                }.toDouble()

                val txToday = ordersToday.size

                // 4. Update State
                _dashboardState.value = DashboardState(
                    totalRevenue = revenue,
                    totalOrders = orderCount,
                    totalProducts = productCount,
                    totalCategories = categoryCount,
                    totalTokos = tokoCount,
                    todayRevenue = revenueToday,
                    todayOrders = txToday
                )

            } catch (e: Exception) {
                println("Error loading dashboard: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- FUNGSI TOP PRODUCT (YANG LAMA TETAP ADA) ---
    fun calculateTopProducts(token: String) {
        viewModelScope.launch {
            try {
                val allOrders = orderRepository.getAllOrders(token)
                val productMap = mutableMapOf<Int, TopProductResult>()

                allOrders.forEach { order ->
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
                println("Error calculating analysis: ${e.message}")
            }
        }
    }
}

// Data Class Top Product (Tetap)
data class TopProductResult(
    val name: String,
    val totalSold: Int,
    val totalRevenue: Double
)
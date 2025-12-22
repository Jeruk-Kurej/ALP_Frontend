package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() { // <-- Constructor kosong (No Factory)

    // Inisialisasi repository langsung dari Container sesuai style Bryan
    private val repository = AppContainer.orderRepository

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun getAllOrders(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllOrders(token)
                _orders.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat daftar pesanan"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createOrder(
        token: String,
        customerName: String,
        paymentId: Int,
        tokoId: Int,
        orderItems: List<Map<String, Int>>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.createOrder(
                    token, customerName, paymentId, tokoId, orderItems
                )
                _selectedOrder.value = result
                _successMessage.value = "Order created successfully"
                getAllOrders(token) // Refresh list otomatis setelah buat order
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(token: String, orderId: Int, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.updateOrderStatus(token, orderId, status)
                _selectedOrder.value = result
                _successMessage.value = "Order status updated successfully"
                getAllOrders(token) // Refresh list otomatis setelah update
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
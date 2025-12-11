package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.repository.OrderRepository
import com.jeruk.alp_frontend.ui.model.Order
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _selectedOrder = MutableLiveData<Order>()
    val selectedOrder: LiveData<Order> = _selectedOrder

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun getAllOrders(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllOrders(token)
                _orders.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
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
                getAllOrders(token) // Refresh the list
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
                getAllOrders(token) // Refresh the list
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
package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() { // <-- Constructor kosong sesuai style Bryan

    // Inisialisasi repository langsung dari Container
    private val repository = AppContainer.paymentRepository

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _selectedPayment = MutableStateFlow<Payment?>(null)
    val selectedPayment: StateFlow<Payment?> = _selectedPayment

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun getAllPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllPayments()
                _payments.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat metode pembayaran"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPayment(token: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.createPayment(token, name)
                _selectedPayment.value = result
                _successMessage.value = "Payment created successfully"
                getAllPayments() // Refresh list setelah create
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePayment(token: String, paymentId: Int, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.updatePayment(token, paymentId, name)
                _selectedPayment.value = result
                _successMessage.value = "Payment updated successfully"
                getAllPayments() // Refresh list setelah update
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePayment(token: String, paymentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val message = repository.deletePayment(token, paymentId)
                _successMessage.value = message
                getAllPayments() // Refresh list setelah delete
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
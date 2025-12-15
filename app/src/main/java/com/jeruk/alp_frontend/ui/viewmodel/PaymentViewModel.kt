package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.repository.PaymentRepository
import com.jeruk.alp_frontend.ui.model.Payment
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repository: PaymentRepository
) : ViewModel() {

    private val _payments = MutableLiveData<List<Payment>>()
    val payments: LiveData<List<Payment>> = _payments

    private val _selectedPayment = MutableLiveData<Payment>()
    val selectedPayment: LiveData<Payment> = _selectedPayment

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun getAllPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllPayments()
                _payments.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
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
                getAllPayments() // Refresh the list
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
                getAllPayments() // Refresh the list
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
                getAllPayments() // Refresh the list
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
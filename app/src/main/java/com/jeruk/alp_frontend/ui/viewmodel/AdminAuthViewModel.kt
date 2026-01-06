package com.jeruk.alp_frontend.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.utils.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(application)

    // State PIN yang tersimpan
    private val _storedPin = MutableStateFlow<String?>(null) // null = belum setting
    val storedPin: StateFlow<String?> = _storedPin

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Pantau terus nilai PIN di storage
        viewModelScope.launch {
            repository.adminPinFlow.collectLatest { pin ->
                _storedPin.value = pin
                _isLoading.value = false
            }
        }
    }

    fun setAdminPin(newPin: String) {
        viewModelScope.launch {
            repository.savePin(newPin)
        }
    }

    fun verifyPin(inputPin: String): Boolean {
        return _storedPin.value == inputPin
    }

    fun resetPin() {
        viewModelScope.launch {
            repository.savePin("") // Atau hapus key-nya. Di sini kita timpa jadi kosong biar kembali ke mode Setup
            // Jika kamu punya fungsi logout user, panggil di sini juga.
        }
    }
}
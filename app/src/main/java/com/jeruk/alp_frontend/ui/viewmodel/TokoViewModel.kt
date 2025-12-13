package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Toko
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class TokoViewModel : ViewModel() {

    // 1. Inisialisasi repository langsung di dalam (Style Bryan)
    private val repository = AppContainer().tokoRepository

    // 2. Deklarasi State (Pastikan semua yang dipanggil di bawah ada di sini)
    private val _tokos = MutableStateFlow<List<Toko>>(emptyList())
    val tokos: StateFlow<List<Toko>> = _tokos

    private val _selectedToko = MutableStateFlow<Toko?>(null) // Harus ada ini biar gak merah
    val selectedToko: StateFlow<Toko?> = _selectedToko

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null) // Harus ada ini biar gak merah
    val successMessage: StateFlow<String?> = _successMessage

    // --- FUNGSI-FUNGSI (Semua harus di dalam kurung kurawal Class ini) ---

    fun getMyTokos(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getMyTokos(token)
                _tokos.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTokoById(tokoId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getTokoById(tokoId)
                _selectedToko.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createToko(
        token: String,
        name: String,
        description: String,
        location: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.createToko(
                    token, name, description, location, imageFile
                )
                _selectedToko.value = result
                _successMessage.value = "Toko created successfully"
                getMyTokos(token) // Refresh list setelah buat
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
} // <--- KURUNG TUTUP CLASS HARUS DI PALING BAWAH
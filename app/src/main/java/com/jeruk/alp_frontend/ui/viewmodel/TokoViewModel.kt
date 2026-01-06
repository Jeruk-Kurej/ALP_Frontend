package com.jeruk.alp_frontend.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Toko
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class TokoViewModel : ViewModel() {

    private val repository = AppContainer.tokoRepository

    // States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _tokos = MutableStateFlow<List<Toko>>(emptyList())
    val tokos: StateFlow<List<Toko>> = _tokos.asStateFlow()

    private val _currentToko = MutableStateFlow<Toko?>(null)
    val currentToko: StateFlow<Toko?> = _currentToko.asStateFlow()

    fun getMyTokos(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getMyTokos(token)
                _tokos.value = result
            } catch (e: Exception) {
                Log.e("TOKO_VM", "Gagal ambil list toko: ${e.message}")
                _errorMessage.value = "Gagal memuat toko: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTokoById(token: String, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _currentToko.value = repository.getTokoById(token, id)
            } catch (e: Exception) {
                Log.e("TOKO_VM", "Error Detail: ${e.message}")
                _errorMessage.value = "Gagal memuat detail toko"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createToko(token: String, name: String, description: String, location: String, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                repository.createToko(token, name, description, location, imageFile)
                _isSuccess.value = true
                getMyTokos(token) // Refresh list otomatis
            } catch (e: Exception) {
                _errorMessage.value = "Gagal membuat toko: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateToko(
        token: String,
        id: Int,
        name: String,
        desc: String,
        loc: String,
        file: File?,
        selectedProductIds: List<Int>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                repository.updateToko(token, id, name, desc, loc, file, selectedProductIds)
                _isSuccess.value = true

                // Refresh data
                getMyTokos(token)
                getTokoById(token, id)

            } catch (e: Exception) {
                Log.e("TOKO_VM", "Gagal update: ${e.message}")
                _errorMessage.value = "Gagal update: ${e.message}"
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteToko(token: String, tokoId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.deleteToko(token, tokoId)
                // Hapus dari list lokal supaya UI langsung update
                val currentList = _tokos.value.toMutableList()
                currentList.removeAll { it.id == tokoId }
                _tokos.value = currentList

            } catch (e: Exception) {
                Log.e("TOKO_VM", "Gagal hapus toko: ${e.message}")
                _errorMessage.value = "Gagal menghapus toko: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearState() {
        _isSuccess.value = false
        _errorMessage.value = null
        _currentToko.value = null
    }
}
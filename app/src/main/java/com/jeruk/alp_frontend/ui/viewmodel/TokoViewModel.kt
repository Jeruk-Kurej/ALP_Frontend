package com.jeruk.alp_frontend.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Toko
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class TokoViewModel : ViewModel() {
    private val repository = AppContainer.tokoRepository

    private val _tokos = MutableStateFlow<List<Toko>>(emptyList())
    val tokos: StateFlow<List<Toko>> = _tokos

    private val _selectedToko = MutableStateFlow<Toko?>(null)
    val selectedToko: StateFlow<Toko?> = _selectedToko

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun getMyTokos(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getMyTokos(token)
                _tokos.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("TokoViewModel", "Error getting tokos: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTokoById(token: String, tokoId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getTokoById(token, tokoId)
                _selectedToko.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("TokoViewModel", "Error getting toko by id: ${e.message}", e)
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
            _isSuccess.value = false
            try {
                val result = repository.createToko(
                    token, name, description, location, imageFile
                )
                _selectedToko.value = result
                _successMessage.value = "Toko berhasil dibuat"
                _isSuccess.value = true
                Log.d("TokoViewModel", "Toko created successfully: ${result.name}")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSuccess.value = false
                Log.e("TokoViewModel", "Error creating toko: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun clearState() {
        _isSuccess.value = false
        _errorMessage.value = null
        _successMessage.value = null
        _isLoading.value = false
    }

    fun updateToko(
        token: String,
        tokoId: Int,
        name: String,
        description: String,
        location: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                val result = repository.updateToko(
                    token, tokoId, name, description, location, imageFile
                )
                _selectedToko.value = result
                _successMessage.value = "Toko berhasil diupdate"
                _isSuccess.value = true
                Log.d("TokoViewModel", "Toko updated successfully: ${result.name}")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSuccess.value = false
                Log.e("TokoViewModel", "Error updating toko: ${e.message}", e)
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
                _successMessage.value = "Toko berhasil dihapus"
                Log.d("TokoViewModel", "Toko deleted successfully: $tokoId")
                // Refresh the list
                getMyTokos(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("TokoViewModel", "Error deleting toko: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearData() {
        Log.d("TokoViewModel", "Clearing all toko data")
        _tokos.value = emptyList()
        _selectedToko.value = null
        _isLoading.value = false
        _isSuccess.value = false
        _errorMessage.value = null
        _successMessage.value = null
    }
}

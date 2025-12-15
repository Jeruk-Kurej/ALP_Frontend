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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _tokos = MutableStateFlow<List<Toko>>(emptyList())
    val tokos: StateFlow<List<Toko>> = _tokos

    private val _currentToko = MutableStateFlow<Toko?>(null)
    val currentToko: StateFlow<Toko?> = _currentToko

    fun getTokoById(token: String, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentToko.value = repository.getTokoById(token, id)
            } catch (e: Exception) {
                Log.e("TOKO_VM", "Error: ${e.message}")
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
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateToko(token: String, id: Int, name: String, desc: String, loc: String, file: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                repository.updateToko(token, id, name, desc, loc, file)
                _isSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSuccess.value = false
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

    fun getMyTokos(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _tokos.value = repository.getMyTokos(token) }
            catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun deleteToko(token: String, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteToko(token, id)
                getMyTokos(token)
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }
}
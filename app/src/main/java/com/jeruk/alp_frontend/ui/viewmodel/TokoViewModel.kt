package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.repository.TokoRepository
import com.jeruk.alp_frontend.ui.model.Toko
import kotlinx.coroutines.launch
import java.io.File

class TokoViewModel(
    private val repository: TokoRepository
) : ViewModel() {

    private val _tokos = MutableLiveData<List<Toko>>()
    val tokos: LiveData<List<Toko>> = _tokos

    private val _selectedToko = MutableLiveData<Toko>()
    val selectedToko: LiveData<Toko> = _selectedToko

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

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
                getMyTokos(token) // Refresh the list
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
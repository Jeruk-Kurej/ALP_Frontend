package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val repository = AppContainer.categoryRepository

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun getAllCategories(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllCategories(token)
                _categories.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat kategori"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 👇 UPDATED: Meneruskan token ke repository
    fun getCategoryById(token: String, categoryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Sekarang pass token ke repository!
                val result = repository.getCategoryById(token, categoryId)
                _selectedCategory.value = result
                android.util.Log.d("CategoryVM", "Success load: ${result.name}")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                android.util.Log.e("CategoryVM", "Error load: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCategory(token: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                val result = repository.createCategory(token, name)
                _selectedCategory.value = result
                _successMessage.value = "Kategori berhasil dibuat"
                _isSuccess.value = true
                getAllCategories(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(token: String, categoryId: Int, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                val result = repository.updateCategory(token, categoryId, name)
                _selectedCategory.value = result
                _successMessage.value = "Kategori berhasil diperbarui"
                _isSuccess.value = true
                getAllCategories(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(token: String, categoryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                val message = repository.deleteCategory(token, categoryId)
                _successMessage.value = message
                _isSuccess.value = true
                getAllCategories(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        _isSuccess.value = false
    }
}
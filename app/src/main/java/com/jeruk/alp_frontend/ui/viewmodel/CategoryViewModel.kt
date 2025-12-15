package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() { // <-- Constructor kosong sesuai style Bryan

    // Inisialisasi repository langsung dari Container
    private val repository = AppContainer.categoryRepository

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun getAllCategories(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                android.util.Log.d("CategoryViewModel", "Fetching categories...")
                val result = repository.getAllCategories(token)
                android.util.Log.d("CategoryViewModel", "Fetched ${result.size} categories")
                _categories.value = result
            } catch (e: Exception) {
                android.util.Log.e("CategoryViewModel", "Error fetching categories: ${e.message}", e)
                _errorMessage.value = e.message ?: "Gagal memuat kategori"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCategoryById(categoryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getCategoryById(categoryId)
                _selectedCategory.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCategory(token: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.createCategory(token, name)
                _selectedCategory.value = result
                _successMessage.value = "Category created successfully"
                getAllCategories(token) // Refresh list otomatis with token
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(token: String, categoryId: Int, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.updateCategory(token, categoryId, name)
                _selectedCategory.value = result
                _successMessage.value = "Category updated successfully"
                getAllCategories(token) // Refresh list otomatis with token
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(token: String, categoryId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val message = repository.deleteCategory(token, categoryId)
                _successMessage.value = message
                getAllCategories(token) // Refresh list otomatis with token
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
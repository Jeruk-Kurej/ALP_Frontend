package com.jeruk.alp_frontend.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProductViewModel : ViewModel() {

    // 1. Repository
    private val repository = AppContainer.productRepository

    // 2. Data List & Detail
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // 3. State UI (Loading, Success, Error)
    // Kita pakai variabel biasa saja (Normal Way), tidak perlu ProductState class lagi.

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // -----------------------------------------------------------
    // FUNCTION: GET ALL PRODUCTS
    // -----------------------------------------------------------
    fun getAllProducts(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Reset error
            try {
                Log.d("ProductViewModel", "Fetching all products...")
                val result = repository.getAllProducts(token)
                _products.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat produk: ${e.message}"
                Log.e("ProductViewModel", "Error fetch products", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -----------------------------------------------------------
    // FUNCTION: GET BY ID
    // -----------------------------------------------------------
    fun getProductById(token: String, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedProduct.value = null
            try {
                val result = repository.getProductById(token, productId)
                _selectedProduct.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat detail: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -----------------------------------------------------------
    // FUNCTION: CREATE PRODUCT
    // -----------------------------------------------------------
    fun createProduct(
        token: String,
        name: String,
        description: String,
        price: Int,
        categoryId: Int,
        tokoIds: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = false
            _errorMessage.value = null

            try {
                Log.d("ProductVM", "Mengirim data: Name=$name, Price=$price, Cat=$categoryId, Toko=$tokoIds")

                repository.createProduct(
                    token, name, description, price, categoryId, tokoIds, imageFile
                )

                _successMessage.value = "Produk berhasil dibuat"
                _isSuccess.value = true
                getAllProducts(token)

            } catch (e: retrofit2.HttpException) {
                // Tangkap error dari Server (400, 401, 500)
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ProductVM", "Server Error Body: $errorBody")
                _errorMessage.value = "Server menolak: $errorBody"
                _isSuccess.value = false
            } catch (e: Exception) {
                // Error koneksi atau kodingan
                Log.e("ProductVM", "Error Local: ${e.message}", e)
                _errorMessage.value = "Gagal: ${e.message}"
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -----------------------------------------------------------
    // FUNCTION: UPDATE PRODUCT
    // -----------------------------------------------------------
    fun updateProduct(
        token: String,
        productId: Int,
        name: String,
        description: String,
        price: Int,
        categoryId: Int,
        tokoIds: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = false
            _errorMessage.value = null

            try {
                val result = repository.updateProduct(
                    token, productId, name, description, price, categoryId, tokoIds, imageFile
                )
                _selectedProduct.value = result
                _successMessage.value = "Produk berhasil diperbarui"
                _isSuccess.value = true

                getAllProducts(token)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal update produk: ${e.message}"
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -----------------------------------------------------------
    // FUNCTION: DELETE PRODUCT
    // -----------------------------------------------------------
    fun deleteProduct(token: String, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val message = repository.deleteProduct(token, productId)
                _successMessage.value = message

                // Hapus manual dari list lokal biar cepat update UI
                val currentList = _products.value.toMutableList()
                currentList.removeAll { it.id == productId }
                _products.value = currentList

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -----------------------------------------------------------
    // CLEANUP / RESET STATE
    // -----------------------------------------------------------
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        _isSuccess.value = false
        _selectedProduct.value = null
    }
}
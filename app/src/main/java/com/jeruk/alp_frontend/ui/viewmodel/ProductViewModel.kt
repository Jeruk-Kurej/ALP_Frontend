package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ProductState(
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class ProductViewModel : ViewModel() {

    // Inisialisasi repository langsung dari singleton AppContainer
    private val repository = AppContainer.productRepository

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _productState = MutableStateFlow(ProductState())
    val productState: StateFlow<ProductState> = _productState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun getAllProducts(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                android.util.Log.d("ProductViewModel", "Fetching products with token")
                val result = repository.getAllProducts(token)
                _products.value = result
                android.util.Log.d("ProductViewModel", "Products loaded: ${result.size} items")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                android.util.Log.e("ProductViewModel", "Error loading products: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProductById(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getProductById(productId)
                _selectedProduct.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

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
            _productState.value = ProductState()
            try {
                android.util.Log.d("ProductViewModel", "Creating product: name=$name, price=$price, categoryId=$categoryId, tokoIds=$tokoIds, hasImage=${imageFile != null}")

                val result = repository.createProduct(
                    token, name, description, price, categoryId, tokoIds, imageFile
                )

                android.util.Log.d("ProductViewModel", "Product created successfully: ${result.name}")

                _selectedProduct.value = result
                _successMessage.value = "Product created successfully"
                _productState.value = ProductState(isSuccess = true)
                getAllProducts(token) // Refresh the list with token
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = "HTTP ${e.code()}: ${errorBody ?: e.message()}"
                android.util.Log.e("ProductViewModel", "HTTP Error creating product: $errorMsg", e)
                _errorMessage.value = errorMsg
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } catch (e: java.io.IOException) {
                val errorMsg = "Network error: ${e.message}"
                android.util.Log.e("ProductViewModel", "Network error creating product", e)
                _errorMessage.value = errorMsg
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                android.util.Log.e("ProductViewModel", "Error creating product", e)
                _errorMessage.value = errorMsg
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

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
            _errorMessage.value = null
            try {
                val result = repository.updateProduct(
                    token, productId, name, description, price, categoryId, tokoIds, imageFile
                )
                _selectedProduct.value = result
                _successMessage.value = "Product updated successfully"
                getAllProducts(token) // Refresh the list with token
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(token: String, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val message = repository.deleteProduct(token, productId)
                _successMessage.value = message
                getAllProducts(token) // Refresh the list with token
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

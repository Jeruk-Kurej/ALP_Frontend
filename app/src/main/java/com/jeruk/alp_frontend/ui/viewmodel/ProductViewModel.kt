package com.jeruk.alp_frontend.ui.viewmodel

import android.util.Log
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

    // successMessage is legacy, prefer productState. We keep it for now to avoid breaking other parts.
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun getAllProducts(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d("ProductViewModel", "Fetching products with token")
                val result = repository.getAllProducts(token)
                _products.value = result
                Log.d("ProductViewModel", "Products loaded: ${result.size} items")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("ProductViewModel", "Error loading products: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProductById(token: String, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedProduct.value = null // Clear previous before loading
            try {
                Log.d("ProductViewModel", "Getting product by ID: $productId")
                val result = repository.getProductById(token, productId)
                _selectedProduct.value = result
                Log.d("ProductViewModel", "Product loaded: ${result.name}")
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("ProductViewModel", "Error getting product: ${e.message}", e)
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
                Log.d(
                    "ProductViewModel",
                    "Creating product: name=$name, hasImage=${imageFile != null}"
                )

                val result = repository.createProduct(
                    token,
                    name,
                    description,
                    price,
                    categoryId,
                    tokoIds,
                    imageFile
                )

                Log.d("ProductViewModel", "Product created successfully: ${result.name}")

                _successMessage.value = "Product created successfully"
                _productState.value = ProductState(isSuccess = true)
                getAllProducts(token) // Refresh the list
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = "HTTP ${e.code()}: ${errorBody ?: e.message()}"
                Log.e("ProductViewModel", "HTTP Error creating product: $errorMsg", e)
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } catch (e: java.io.IOException) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("ProductViewModel", "Network error creating product", e)
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("ProductViewModel", "Error creating product", e)
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
            _productState.value = ProductState() // Reset state
            try {
                Log.d("ProductViewModel", "Updating product id=$productId with name=$name")
                val result = repository.updateProduct(
                    token,
                    productId,
                    name,
                    description,
                    price,
                    categoryId,
                    tokoIds,
                    imageFile
                )
                _selectedProduct.value = result
                _productState.value = ProductState(isSuccess = true) // Set success state
                getAllProducts(token) // Refresh list
                Log.d("ProductViewModel", "Product updated successfully: ${result.name}")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = "HTTP ${e.code()}: ${errorBody ?: e.message()}"
                Log.e("ProductViewModel", "HTTP error updating product: $errorMsg", e)
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } catch (e: java.io.IOException) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("ProductViewModel", "Network error updating product: $errorMsg", e)
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
            } catch (e: Exception) {
                val errorMsg = "General error: ${e.message}"
                Log.e("ProductViewModel", "Error updating product: $errorMsg", e)
                _productState.value = ProductState(isError = true, errorMessage = errorMsg)
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
        _productState.value = ProductState() // Also reset the product state
    }
}

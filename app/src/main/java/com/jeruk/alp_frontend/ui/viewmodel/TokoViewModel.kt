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
    // 1. Repository
    private val repository = AppContainer.tokoRepository
    private val productRepository = AppContainer.productRepository

    // 2. States
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

    // -------------------------------------------------------
    // 1. GET MY TOKOS (Perbaikan Nama Fungsi)
    // -------------------------------------------------------
    // Nama fungsi disamakan dengan yang dipanggil di TokoAdminView
    fun getMyTokos(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Memanggil fungsi di Repository
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

    // -------------------------------------------------------
    // 2. DELETE TOKO (Penambahan Fungsi Baru)
    // -------------------------------------------------------
    fun deleteToko(token: String, tokoId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.deleteToko(token, tokoId)

                // Setelah delete sukses, refresh list toko
                getMyTokos(token)

            } catch (e: Exception) {
                Log.e("TOKO_VM", "Gagal hapus toko: ${e.message}")
                _errorMessage.value = "Gagal menghapus toko: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -------------------------------------------------------
    // FUNGSI LAINNYA (TETAP SAMA)
    // -------------------------------------------------------

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
                // Refresh list setelah create (opsional, kalau user langsung balik ke list)
                getMyTokos(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateToko(token: String, id: Int, name: String, desc: String, loc: String, file: File?, selectedProductIds: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false
            try {
                // Update Detail Toko
                repository.updateToko(token, id, name, desc, loc, file)

                // Update Relasi Produk
                val allProducts = productRepository.getAllProducts(token)
                val currentTokoName = _currentToko.value?.name ?: name

                allProducts.forEach { product ->
                    val shouldBeInToko = selectedProductIds.contains(product.id)
                    val isCurrentlyInToko = product.tokos.contains(currentTokoName)

                    if (shouldBeInToko && !isCurrentlyInToko) {
                        try { productRepository.updateProductTokoRelation(token, product.id, id, true) }
                        catch (e: Exception) { Log.e("TOKO_VM", "Fail Add: ${e.message}") }
                    } else if (!shouldBeInToko && isCurrentlyInToko) {
                        try { productRepository.updateProductTokoRelation(token, product.id, id, false) }
                        catch (e: Exception) { Log.e("TOKO_VM", "Fail Remove: ${e.message}") }
                    }
                }

                _isSuccess.value = true
                // Refresh list
                getMyTokos(token)
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
}
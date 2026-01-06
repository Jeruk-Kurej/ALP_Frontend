package com.jeruk.alp_frontend.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel : ViewModel() {

    // 1. Ambil Repository API & DataStore
    private val authRepository = AppContainer.authRepository
    private val userPreferences = AppContainer.userPreferencesRepository

    // State untuk data User (Token, Role, dll)
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    // State untuk Loading Spinner
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 🔥 INIT: Jalan otomatis saat aplikasi dibuka
    // Fungsinya: Mengecek apakah ada data user yang tersimpan di HP
    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Menggabungkan data Token, Email, dan Role dari DataStore secara real-time
            combine(
                userPreferences.userToken,
                userPreferences.userEmail,
                userPreferences.userRole
            ) { token, email, role ->
                if (!token.isNullOrEmpty()) {
                    // Jika token ada, berarti User sedang login
                    User(
                        token = token,
                        email = email ?: "",
                        role = role ?: "user",
                        // Pastikan model User kamu punya field ini atau logika sejenis
                        // Jika model User kamu belum punya field 'isAuthenticated',
                        // logika di MainActivity cukup cek token.isNotEmpty()
                    )
                } else {
                    // Jika token kosong, return User kosong (Belum Login)
                    User()
                }
            }.collect { savedUser ->
                // Update state aplikasi sesuai data di DataStore
                _userState.value = savedUser
            }
        }
    }

    // Fungsi Login
    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            resetError() // Hapus error lama sebelum mencoba login baru

            try {
                Log.d("AuthViewModel", "Attempting login for: $username")

                // 1. Panggil API Login
                val result = authRepository.loginUser(username, pass)

                // 2. Jika Sukses, SIMPAN KE DATASTORE (HP)
                // Pastikan result.token, result.email, dll tidak null
                userPreferences.saveUser(
                    token = result.token,
                    email = result.email,
                    role = result.role,
                    id = result.id.toString() // Sesuaikan jika ID di model kamu Int/String
                )

                // Update UI State manual (supaya responsif)
                _userState.value = result
                Log.d("AuthViewModel", "Login successful & Saved to DataStore")

            } catch (e: SocketTimeoutException) {
                Log.e("AuthViewModel", "Login timeout", e)
                _userState.value = _userState.value.copy(
                    isError = true,
                    errorMessage = "Koneksi timeout. Periksa koneksi internet Anda."
                )
            } catch (e: UnknownHostException) {
                Log.e("AuthViewModel", "Unknown host", e)
                _userState.value = _userState.value.copy(
                    isError = true,
                    errorMessage = "Tidak dapat terhubung ke server. Periksa koneksi internet."
                )
            } catch (e: IOException) {
                Log.e("AuthViewModel", "IO error", e)
                _userState.value = _userState.value.copy(
                    isError = true,
                    errorMessage = "Masalah jaringan atau server tidak dapat dijangkau."
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _userState.value = _userState.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Login gagal. Cek username/password."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi Register
    fun register(username: String, email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            resetError()

            try {
                Log.d("AuthViewModel", "Attempting registration for: $username")
                val result = authRepository.registerUser(username, email, pass)

                // Opsional: Jika register langsung mengembalikan Token, simpan juga ke DataStore
                if (result.token.isNotEmpty()) {
                    userPreferences.saveUser(
                        token = result.token,
                        email = result.email,
                        role = result.role,
                        id = result.id.toString()
                    )
                }

                _userState.value = result
                Log.d("AuthViewModel", "Registration successful")

            } catch (e: SocketTimeoutException) {
                Log.e("AuthViewModel", "Registration timeout", e)
                _userState.value = _userState.value.copy(
                    isError = true,
                    errorMessage = "Koneksi timeout. Coba lagi nanti."
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error", e)
                _userState.value = _userState.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Registrasi gagal."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Fungsi Logout (Penting untuk menghapus sesi)
    fun logout() {
        viewModelScope.launch {
            try {
                userPreferences.clearUser() // Hapus data dari HP
                _userState.value = User()   // Reset state UI ke kosong
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout error", e)
            }
        }
    }

    // Reset status error
    fun resetError() {
        // Kita pakai copy supaya data user lain (email/token) tidak hilang, cuma reset errornya
        _userState.value = _userState.value.copy(isError = false, errorMessage = null)
    }
}
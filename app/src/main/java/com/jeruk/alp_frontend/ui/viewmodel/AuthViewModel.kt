package com.jeruk.alp_frontend.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel : ViewModel() {

    // State untuk data User (Token, dll)
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState

    // State untuk Loading Spinner
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val repository = AppContainer.authRepository

    // Fungsi Login menggunakan USERNAME
    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("AuthViewModel", "Attempting login for: $username")
                val result = repository.loginUser(username, pass)
                _userState.value = result
                Log.d("AuthViewModel", "Login successful")
            } catch (e: SocketTimeoutException) {
                Log.e("AuthViewModel", "Login timeout", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = "Koneksi timeout. Periksa koneksi internet Anda."
                )
            } catch (e: UnknownHostException) {
                Log.e("AuthViewModel", "Unknown host", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = "Tidak dapat terhubung ke server. Periksa koneksi internet."
                )
            } catch (e: IOException) {
                Log.e("AuthViewModel", "IO error", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = "Tidak ada koneksi internet atau server tidak dapat dijangkau."
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _userState.value = User(
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
            try {
                Log.d("AuthViewModel", "Attempting registration for: $username, email: $email")
                val result = repository.registerUser(username, email, pass)
                _userState.value = result
                Log.d("AuthViewModel", "Registration successful")
            } catch (e: SocketTimeoutException) {
                Log.e("AuthViewModel", "Registration timeout", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = "Koneksi timeout. Periksa koneksi internet Anda."
                )
            } catch (e: UnknownHostException) {
                Log.e("AuthViewModel", "Unknown host", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = "Tidak dapat terhubung ke server. Periksa koneksi internet."
                )
            } catch (e: IOException) {
                Log.e("AuthViewModel", "IO error", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = "Tidak ada koneksi internet atau server tidak dapat dijangkau."
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error", e)
                _userState.value = User(
                    isError = true,
                    errorMessage = e.message ?: "Registrasi gagal. Silakan coba lagi."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Reset status error agar pesan tidak muncul terus menerus
    fun resetError() {
        _userState.value = _userState.value.copy(isError = false, errorMessage = null)
    }
}
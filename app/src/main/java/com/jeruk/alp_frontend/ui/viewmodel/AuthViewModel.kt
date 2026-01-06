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
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel : ViewModel() {

    // 1. Ambil Repository API & DataStore
    private val authRepository = AppContainer.authRepository

    // Kita butuh TokoRepository (atau repository lain yang butuh token)
    // Fungsinya hanya untuk "Ping" ke server guna mengetes validitas token
    private val tokoRepository = AppContainer.tokoRepository

    private val userPreferences = AppContainer.userPreferencesRepository

    // State untuk data User (Token, Role, dll)
    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState.asStateFlow()

    // State untuk Loading Spinner
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 🔥 INIT: Jalan otomatis saat aplikasi dibuka
    // Fungsinya: Mengecek apakah ada data user yang tersimpan di HP (Lokal)
    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            combine(
                userPreferences.userToken,
                userPreferences.userEmail,
                userPreferences.userRole
            ) { token, email, role ->
                if (!token.isNullOrEmpty()) {
                    User(
                        token = token,
                        email = email ?: "",
                        role = role ?: "user"
                    )
                } else {
                    User()
                }
            }.collect { savedUser ->
                _userState.value = savedUser
            }
        }
    }

    // --- FUNGSI BARU: VALIDASI TOKEN KE SERVER ---
    // Dipanggil di MainActivity sebelum masuk ke Home
    fun validateToken(token: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                // Kita coba panggil API ringan yang butuh token (misal: Get My Tokos)
                // Kalau token BASI, ini akan melempar error 401
                tokoRepository.getMyTokos(token)

                // Kalau sampai sini, berarti Token Valid
                onSuccess()
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // Token Ditolak Server (Expired/Unauthorized)
                    Log.e("AuthViewModel", "Token Expired (401). Auto Logout.")
                    logout() // Hapus sesi lokal
                    onError() // Suruh UI balik ke Login
                } else {
                    // Error lain (misal Server Error 500), tapi Token mungkin masih oke.
                    // Tetap izinkan masuk (Offline mode / Server trouble)
                    onSuccess()
                }
            } catch (e: Exception) {
                // Error koneksi internet biasa, izinkan masuk (bisa handle error di view nanti)
                onSuccess()
            }
        }
    }

    // Fungsi Login
    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            resetError()

            try {
                Log.d("AuthViewModel", "Attempting login for: $username")
                val result = authRepository.loginUser(username, pass)

                userPreferences.saveUser(
                    token = result.token,
                    email = result.email,
                    role = result.role,
                    id = result.id.toString()
                )

                _userState.value = result
                Log.d("AuthViewModel", "Login successful & Saved to DataStore")

            } catch (e: SocketTimeoutException) {
                handleError("Koneksi timeout. Periksa koneksi internet Anda.")
            } catch (e: UnknownHostException) {
                handleError("Tidak dapat terhubung ke server. Periksa koneksi internet.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                handleError(e.message ?: "Login gagal. Cek username/password.")
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

                if (result.token.isNotEmpty()) {
                    userPreferences.saveUser(
                        token = result.token,
                        email = result.email,
                        role = result.role,
                        id = result.id.toString()
                    )
                }
                _userState.value = result
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error", e)
                handleError(e.message ?: "Registrasi gagal.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Fungsi Logout
    fun logout() {
        viewModelScope.launch {
            try {
                userPreferences.clearUser()
                _userState.value = User()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout error", e)
            }
        }
    }

    fun resetError() {
        _userState.value = _userState.value.copy(isError = false, errorMessage = null)
    }

    private fun handleError(msg: String) {
        _userState.value = _userState.value.copy(isError = true, errorMessage = msg)
    }
}
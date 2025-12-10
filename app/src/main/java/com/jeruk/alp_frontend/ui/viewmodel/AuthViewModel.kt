package com.jeruk.alp_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeruk.alp_frontend.data.container.AppContainer
import com.jeruk.alp_frontend.ui.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel : ViewModel() {

    private val _userState = MutableStateFlow(User())
    val userState: StateFlow<User> = _userState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val repository = AppContainer().authRepository

    fun register(username: String, email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.registerUser(username, email, pass)

                _userState.value = result

            } catch (e: IOException) {
                _userState.value = User(
                    isError = true,
                    errorMessage = "Tidak ada koneksi internet."
                )
            } catch (e: Exception) {
                _userState.value = User(
                    isError = true,
                    errorMessage = e.message ?: "Registrasi gagal."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.loginUser(email, pass)
                _userState.value = result

            } catch (e: IOException) {
                _userState.value = User(
                    isError = true,
                    errorMessage = "Tidak ada koneksi internet."
                )
            } catch (e: Exception) {
                _userState.value = User(
                    isError = true,
                    errorMessage = e.message ?: "Login gagal."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetError() {
        _userState.value = _userState.value.copy(isError = false, errorMessage = null)
    }
}
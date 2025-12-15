package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.dto.Auth.LoginRequest
import com.jeruk.alp_frontend.data.dto.Auth.RegisterRequest
import com.jeruk.alp_frontend.data.service.AuthService
import com.jeruk.alp_frontend.ui.model.User

class AuthRepository(private val service: AuthService) {

    suspend fun registerUser(username: String, email: String, pass: String): User {
        val request = RegisterRequest(username = username, email = email, password = pass)
        val response = service.registerUser(request)

        // Style Bryan: Langsung unwrap body jika sukses
        val body = response.body()!!

        return User(
            token = body.data.token ?: "", // Elvis operator agar token tidak null
            username = username ?: "",
            email = email ?: ""
        )
    }

    suspend fun loginUser(username: String, pass: String): User {
        android.util.Log.d("AuthRepository", "=== LOGIN REQUEST ===")
        android.util.Log.d("AuthRepository", "Username: $username")

        val request = LoginRequest(username = username, password = pass)
        val response = service.loginUser(request)

        android.util.Log.d("AuthRepository", "Login Response Code: ${response.code()}")
        android.util.Log.d("AuthRepository", "Login Successful: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("AuthRepository", "Login Failed: ${response.code()} - $errorBody")
            throw Exception("Login failed: ${response.code()}")
        }

        val body = response.body()!!
        val token = body.data.token ?: ""

        android.util.Log.d("AuthRepository", "Token received: ${if (token.isNotEmpty()) "YES (length: ${token.length}, first 20 chars: ${token.take(20)}...)" else "NO/EMPTY"}")
        android.util.Log.d("AuthRepository", "Full token: $token")

        return User(
            token = token,
            username = username ?: ""
        )
    }
}
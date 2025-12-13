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
        val request = LoginRequest(username = username, password = pass)
        val response = service.loginUser(request)

        val body = response.body()!!

        return User(
            token = body.data.token ?: "",
            username = username ?: ""
        )
    }
}
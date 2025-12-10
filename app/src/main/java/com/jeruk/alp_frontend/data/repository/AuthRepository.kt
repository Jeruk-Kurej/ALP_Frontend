package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.dto.Auth.LoginRequest
import com.jeruk.alp_frontend.data.dto.Auth.RegisterRequest
import com.jeruk.alp_frontend.data.service.AuthService
import com.jeruk.alp_frontend.ui.model.User
import retrofit2.HttpException

class AuthRepository(private val service: AuthService) {

    suspend fun registerUser(username: String, email: String, pass: String): User {
        // 1. Siapkan Request DTO
        val request = RegisterRequest(username = username, email = email, password = pass)

        // 2. Panggil Service
        val response = service.registerUser(request)

        // 3. Cek sukses
        if (response.isSuccessful) {
            val body = response.body()!!

            // 4. Mapping DTO ke UI Model "User"
            // Karena backend cuma kasih token, username & email ambil dari inputan aja sementara
            return User(
                token = body.data.token,
                username = username,
                email = email
            )
        } else {
            // Error handling simpel (lempar error biar UI tau)
            throw Exception("Register Failed: ${response.code()}")
        }
    }

    suspend fun loginUser(email: String, pass: String): User {
        // 1. Siapkan Request DTO
        val request = LoginRequest(email = email, password = pass)

        // 2. Panggil Service
        val response = service.loginUser(request)

        if (response.isSuccessful) {
            val body = response.body()!!

            // 3. Mapping ke UI Model "User"
            return User(
                token = body.data.token,
                email = email
            )
        } else {
            throw Exception("Login Failed: ${response.code()}")
        }
    }
}
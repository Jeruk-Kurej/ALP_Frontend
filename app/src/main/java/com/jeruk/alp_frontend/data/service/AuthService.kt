package com.jeruk.alp_frontend.data.service

import com.jeruk.alp_frontend.data.dto.Auth.LoginRequest
import com.jeruk.alp_frontend.data.dto.Auth.LoginResponse
import com.jeruk.alp_frontend.data.dto.Auth.RegisterRequest
import com.jeruk.alp_frontend.data.dto.Auth.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("register")
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}
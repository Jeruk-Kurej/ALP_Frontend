package com.jeruk.alp_frontend.data.container

import com.google.gson.GsonBuilder
import com.jeruk.alp_frontend.data.repository.AuthRepository
import com.jeruk.alp_frontend.data.service.AuthService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer {

    companion object {
        // Jangan pakai "localhost", tapi pakai "10.0.2.2"
        private const val BASE_URL = "http://10.152.62.164:3000/api/"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    // Membuat Service dari Retrofit
    private val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    // Membuat Repository dengan memasukkan Service ke dalamnya
    val authRepository: AuthRepository by lazy {
        AuthRepository(authService)
    }
}
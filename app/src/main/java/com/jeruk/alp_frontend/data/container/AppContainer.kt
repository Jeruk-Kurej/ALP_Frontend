package com.jeruk.alp_frontend.data.container

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.jeruk.alp_frontend.data.interceptor.AuthInterceptor
import com.jeruk.alp_frontend.data.repository.*
import com.jeruk.alp_frontend.data.service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppContainer {

    private const val ROOT_URL = "https://alpbackend-production.up.railway.app"
    private const val BASE_URL = "$ROOT_URL/api/"

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context
    }

    val userPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    // 1. Buat OkHttpClient dengan Interceptor
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor {
                Log.d("AUTH_DEBUG", "401 Unauthorized detected! Clearing session...")

                // Menjalankan clearUser() dari repository kamu
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        userPreferencesRepository.clearUser()
                        Log.d("AUTH_DEBUG", "DataStore cleared successfully")
                    } catch (e: Exception) {
                        Log.e("AUTH_DEBUG", "Error clearing DataStore: ${e.message}")
                    }
                }
            })
            .build()
    }

    // 2. Hubungkan OkHttpClient ke Retrofit
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Memasang satpam (Interceptor)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    // 3. Service & Repository (Semua otomatis pakai retrofit yang ada interceptornya)
    val authRepository: AuthRepository by lazy { AuthRepository(retrofit.create(AuthService::class.java)) }
    val tokoRepository: TokoRepository by lazy {
        TokoRepository(
            retrofit.create(TokoService::class.java),
            ROOT_URL
        )
    }
    val productRepository: ProductRepository by lazy {
        ProductRepository(
            retrofit.create(
                ProductService::class.java
            ), ROOT_URL
        )
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(
            retrofit.create(
                CategoryService::class.java
            )
        )
    }
    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(
            retrofit.create(
                PaymentService::class.java
            )
        )
    }
    val orderRepository: OrderRepository by lazy {
        OrderRepository(
            retrofit.create(OrderService::class.java),
            ROOT_URL
        )
    }
}
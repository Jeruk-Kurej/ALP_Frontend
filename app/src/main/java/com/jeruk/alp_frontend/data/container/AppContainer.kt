package com.jeruk.alp_frontend.data.container

import android.content.Context
import com.google.gson.GsonBuilder
import com.jeruk.alp_frontend.data.repository.*
import com.jeruk.alp_frontend.data.service.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppContainer {

    // Gunakan 10.0.2.2 untuk Emulator
    private const val ROOT_URL = "https://alpbackend-production.up.railway.app"
    private const val BASE_URL = "$ROOT_URL/api/"

    // 1. Siapkan variabel context
    private lateinit var appContext: Context

    // 2. Fungsi untuk memasukkan context (Dipanggil di Application class)
    fun initialize(context: Context) {
        appContext = context
    }

    // 3. UserPreferencesRepository sekarang bisa pakai appContext
    val userPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    val authRepository: AuthRepository by lazy { AuthRepository(retrofit.create(AuthService::class.java)) }

    val tokoRepository: TokoRepository by lazy {
        TokoRepository(
            retrofit.create(TokoService::class.java),
            ROOT_URL
        )
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(
            retrofit.create(ProductService::class.java),
            ROOT_URL
        )
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(retrofit.create(CategoryService::class.java))
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(retrofit.create(PaymentService::class.java))
    }

    val orderRepository: OrderRepository by lazy {
        OrderRepository(
            retrofit.create(OrderService::class.java),
            ROOT_URL
        )
    }
}
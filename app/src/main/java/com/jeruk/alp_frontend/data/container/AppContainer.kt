package com.jeruk.alp_frontend.data.container

import com.google.gson.GsonBuilder
import com.jeruk.alp_frontend.data.repository.*
import com.jeruk.alp_frontend.data.service.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer {
    companion object {
        // Use 10.0.2.2 for Android Emulator (points to host machine's localhost)
        // Use 10.0.187.183 for Physical Device (your Mac's actual IP on network)
        private const val ROOT_URL = "http://10.152.62.164:3000"
        private const val BASE_URL = "$ROOT_URL/api/"
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
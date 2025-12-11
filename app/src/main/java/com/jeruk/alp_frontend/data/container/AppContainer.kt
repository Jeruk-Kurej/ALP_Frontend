package com.jeruk.alp_frontend.data.container

import com.google.gson.GsonBuilder
import com.jeruk.alp_frontend.data.repository.AuthRepository
import com.jeruk.alp_frontend.data.repository.TokoRepository
import com.jeruk.alp_frontend.data.repository.ProductRepository
import com.jeruk.alp_frontend.data.repository.CategoryRepository
import com.jeruk.alp_frontend.data.service.AuthService
import com.jeruk.alp_frontend.data.service.TokoService
import com.jeruk.alp_frontend.data.service.ProductService
import com.jeruk.alp_frontend.data.service.CategoryService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer {

    companion object {
        // IP 10.0.2.2 digunakan untuk koneksi dari Emulator Android ke server di komputer host
        // ROOT_URL buat Gambar (tanpa /api/)
        private const val ROOT_URL = "http://10.0.2.2:3000"

        // BASE_URL buat Retrofit (pakai /api/)
        private const val BASE_URL = "$ROOT_URL/api/"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    // --- AUTH ---
    private val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository(authService)
    }

    // --- TOKO ---
    private val tokoService: TokoService by lazy {
        retrofit.create(TokoService::class.java)
    }
    val tokoRepository: TokoRepository by lazy {
        TokoRepository(tokoService, ROOT_URL)
    }

    // --- PRODUCT ---
    private val productService: ProductService by lazy {
        retrofit.create(ProductService::class.java)
    }
    val productRepository: ProductRepository by lazy {
        ProductRepository(productService, ROOT_URL)
    }

    // --- CATEGORY ---
    private val categoryService: CategoryService by lazy {
        retrofit.create(CategoryService::class.java)
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(categoryService)
    }
}

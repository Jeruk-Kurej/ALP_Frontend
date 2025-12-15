package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.CategoryService
import com.jeruk.alp_frontend.ui.model.Category

class CategoryRepository(
    private val service: CategoryService
) {

    // Fetch all categories - requires authentication token (fixes 401 error)
    suspend fun getAllCategories(token: String): List<Category> {
        android.util.Log.d("CategoryRepository", "=== GET ALL CATEGORIES ===")
        android.util.Log.d("CategoryRepository", "Token: ${if (token.isNotEmpty()) "Present (${token.take(20)}...)" else "EMPTY"}")

        if (token.isEmpty()) {
            android.util.Log.e("CategoryRepository", "Token is empty!")
            throw Exception("Authentication required: Token is empty")
        }

        android.util.Log.d("CategoryRepository", "Calling API to get all categories")
        val response = service.getAllCategories("Bearer $token")
        android.util.Log.d("CategoryRepository", "API Response: ${response.code()}, Success: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("CategoryRepository", "=== API ERROR ===")
            android.util.Log.e("CategoryRepository", "Status Code: ${response.code()}")
            android.util.Log.e("CategoryRepository", "Error Body: $errorBody")
            android.util.Log.e("CategoryRepository", "Response Headers: ${response.headers()}")

            val errorMessage = when (response.code()) {
                401 -> "Unauthorized: Token tidak valid atau sudah expired. Silakan login kembali."
                403 -> "Forbidden: Akses ditolak. Kemungkinan:\n" +
                       "1. Token Anda sudah expired - login ulang\n" +
                       "2. Anda tidak memiliki role admin\n" +
                       "3. Token tidak valid"
                404 -> "Categories endpoint not found"
                500 -> "Server error: Backend sedang bermasalah"
                else -> "Failed to fetch categories: ${response.code()}"
            }
            throw Exception(errorMessage)
        }

        val body = response.body()!! // Style Bryan: Force Unwrap !!
        android.util.Log.d("CategoryRepository", "Categories data: ${body.data.size} items")

        return body.data.map { item ->
            Category(
                id = item.id,
                name = item.name ?: "" // Elvis operator agar aman
            )
        }
    }

    suspend fun getCategoryById(categoryId: Int): Category {
        val response = service.getCategoryById(categoryId)
        val item = response.body()!!.data

        return Category(
            id = item.id,
            name = item.name ?: ""
        )
    }

    suspend fun createCategory(token: String, name: String): Category {
        val bodyMap = mapOf("name" to name)
        val response = service.createCategory("Bearer $token", bodyMap)
        val item = response.body()!!.data

        return Category(
            id = item.id,
            name = item.name ?: ""
        )
    }

    suspend fun updateCategory(token: String, categoryId: Int, name: String): Category {
        val bodyMap = mapOf("name" to name)
        val response = service.updateCategory("Bearer $token", categoryId, bodyMap)
        val item = response.body()!!.data

        return Category(
            id = item.id,
            name = item.name ?: ""
        )
    }

    suspend fun deleteCategory(token: String, categoryId: Int): String {
        val response = service.deleteCategory("Bearer $token", categoryId)
        val body = response.body()!! // Konsisten pakai force unwrap

        return body.message ?: "Category deleted successfully"
    }
}

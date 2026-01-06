package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.CategoryService
import com.jeruk.alp_frontend.ui.model.Category

class CategoryRepository(
    private val service: CategoryService
) {

    suspend fun getAllCategories(token: String): List<Category> {
        val response = service.getAllCategories("Bearer $token")

        if (!response.isSuccessful) {
            throw Exception("Failed to fetch categories: ${response.code()}")
        }

        val body = response.body()!!
        return body.data.map { item ->
            Category(id = item.id, name = item.name ?: "")
        }
    }

    // 👇 UPDATED: Tambah parameter token
    suspend fun getCategoryById(token: String, categoryId: Int): Category {
        // Panggil service dengan Token
        val response = service.getCategoryById("Bearer $token", categoryId)

        // Cek error biar ga crash kalau token expired atau ID salah
        if (!response.isSuccessful) {
            throw Exception("Gagal ambil detail kategori: ${response.code()}")
        }

        val item = response.body()!!.data

        return Category(
            id = item.id,
            name = item.name ?: ""
        )
    }

    suspend fun createCategory(token: String, name: String): Category {
        val bodyMap = mapOf("name" to name)
        val response = service.createCategory("Bearer $token", bodyMap)

        if (!response.isSuccessful) throw Exception("Gagal create category")

        val item = response.body()!!.data
        return Category(id = item.id, name = item.name ?: "")
    }

    suspend fun updateCategory(token: String, categoryId: Int, name: String): Category {
        val bodyMap = mapOf("name" to name)
        val response = service.updateCategory("Bearer $token", categoryId, bodyMap)

        if (!response.isSuccessful) throw Exception("Gagal update category")

        val item = response.body()!!.data
        return Category(id = item.id, name = item.name ?: "")
    }

    suspend fun deleteCategory(token: String, categoryId: Int): String {
        val response = service.deleteCategory("Bearer $token", categoryId)

        if (!response.isSuccessful) throw Exception("Gagal delete category")

        val body = response.body()!!
        return body.message ?: "Category deleted successfully"
    }
}
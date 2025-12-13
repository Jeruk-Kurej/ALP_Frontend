package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.CategoryService
import com.jeruk.alp_frontend.ui.model.Category

class CategoryRepository(
    private val service: CategoryService
) {

    suspend fun getAllCategories(): List<Category> {
        val response = service.getAllCategories()
        val body = response.body()!! // Style Bryan: Force Unwrap !!

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
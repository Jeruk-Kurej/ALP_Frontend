package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.CategoryService
import com.jeruk.alp_frontend.ui.model.Category

class CategoryRepository(
    private val service: CategoryService
) {

    suspend fun getAllCategories(): List<Category> {
        val response = service.getAllCategories()

        if (response.isSuccessful) {
            val body = response.body()!!
            return body.data.map { item ->
                Category(
                    id = item.id,
                    name = item.name
                )
            }
        } else {
            throw Exception("Failed to fetch categories: ${response.code()}")
        }
    }

    suspend fun getCategoryById(categoryId: Int): Category {
        val response = service.getCategoryById(categoryId)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Category(
                id = item.id,
                name = item.name
            )
        } else {
            throw Exception("Failed to fetch category: ${response.code()}")
        }
    }

    suspend fun createCategory(token: String, name: String): Category {
        val body = mapOf("name" to name)
        val response = service.createCategory("Bearer $token", body)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Category(
                id = item.id,
                name = item.name
            )
        } else {
            throw Exception("Failed to create category: ${response.code()}")
        }
    }

    suspend fun updateCategory(token: String, categoryId: Int, name: String): Category {
        val body = mapOf("name" to name)
        val response = service.updateCategory("Bearer $token", categoryId, body)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Category(
                id = item.id,
                name = item.name
            )
        } else {
            throw Exception("Failed to update category: ${response.code()}")
        }
    }

    suspend fun deleteCategory(token: String, categoryId: Int): String {
        val response = service.deleteCategory("Bearer $token", categoryId)

        if (response.isSuccessful) {
            val deletedCategory = response.body()?.data
            return "Category '${deletedCategory?.name}' deleted successfully"
        } else {
            throw Exception("Failed to delete category: ${response.code()}")
        }
    }
}
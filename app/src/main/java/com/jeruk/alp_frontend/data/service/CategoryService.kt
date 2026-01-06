package com.jeruk.alp_frontend.data.service

import com.jeruk.alp_frontend.data.dto.Category.CreateCategory
import com.jeruk.alp_frontend.data.dto.Category.DeleteCategory
import com.jeruk.alp_frontend.data.dto.Category.GetAllCategories
import com.jeruk.alp_frontend.data.dto.Category.GetCategoryById
import com.jeruk.alp_frontend.data.dto.Category.UpdateCategoryById
import retrofit2.Response
import retrofit2.http.*

interface CategoryService {

    @GET("categories")
    suspend fun getAllCategories(
        @Header("Authorization") token: String
    ): Response<GetAllCategories>

    // 👇 UPDATED: Sekarang butuh token biar ga 401 Unauthorized
    @GET("categories/{categoryId}")
    suspend fun getCategoryById(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Int
    ): Response<GetCategoryById>

    @POST("categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<CreateCategory>

    @PUT("categories/{categoryId}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Int,
        @Body body: Map<String, String>
    ): Response<UpdateCategoryById>

    @DELETE("categories/{categoryId}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Int
    ): Response<DeleteCategory>
}
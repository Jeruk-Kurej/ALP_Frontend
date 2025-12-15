package com.jeruk.alp_frontend.data.service

import com.jeruk.alp_frontend.data.dto.Product.CreateProduct
import com.jeruk.alp_frontend.data.dto.Product.DeleteProductById
import com.jeruk.alp_frontend.data.dto.Product.GetAllProducts
import com.jeruk.alp_frontend.data.dto.Product.GetProductById
import com.jeruk.alp_frontend.data.dto.Product.UpdateProductById
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProductService {

    @GET("products")
    suspend fun getAllProducts(
        @Header("Authorization") token: String
    ): Response<GetAllProducts>

    @GET("products/{productId}")
    suspend fun getProductById(
        @Header("Authorization") token: String,
        @Path("productId") productId: Int
    ): Response<GetProductById>

    @Multipart
    @POST("products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("categoryId") categoryId: RequestBody,  // Changed from category_id to categoryId
        @Part image: MultipartBody.Part,
        @Part("toko_ids") tokoIds: RequestBody? = null  // Optional - omit if null
    ): Response<CreateProduct>

    @Multipart
    @PUT("products/{productId}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("productId") productId: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("toko_ids") tokoIds: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<UpdateProductById>

    @DELETE("products/{productId}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("productId") productId: Int
    ): Response<DeleteProductById>
}
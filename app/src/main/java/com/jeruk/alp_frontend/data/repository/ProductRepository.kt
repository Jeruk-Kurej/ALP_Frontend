package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.ProductService
import com.jeruk.alp_frontend.ui.model.Product
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductRepository(
    private val service: ProductService,
    private val baseUrl: String
) {

    suspend fun getAllProducts(): List<Product> {
        val response = service.getAllProducts()

        if (response.isSuccessful) {
            val body = response.body()!!
            return body.data.map { item ->
                Product(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    price = item.price,
                    imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                    categoryId = item.category.id,
                    categoryName = item.category.name,
                    tokos = item.tokos.map { it.name }
                )
            }
        } else {
            throw Exception("Failed to fetch products: ${response.code()}")
        }
    }

    suspend fun getProductById(productId: Int): Product {
        val response = service.getProductById(productId)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Product(
                id = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                categoryId = item.category.id,
                categoryName = item.category.name,
                tokos = item.tokos.map { it.name }
            )
        } else {
            throw Exception("Failed to fetch product: ${response.code()}")
        }
    }

    suspend fun createProduct(
        token: String,
        name: String,
        description: String,
        price: Int,
        categoryId: Int,
        tokoIds: String,
        imageFile: File?
    ): Product {
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val tokoIdsBody = tokoIds.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        val response = service.createProduct(
            "Bearer $token",
            nameBody,
            descBody,
            priceBody,
            categoryBody,
            tokoIdsBody,
            imagePart
        )

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Product(
                id = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                categoryId = item.category.id,
                categoryName = item.category.name,
                tokos = item.tokos.map { it.name }
            )
        } else {
            throw Exception("Failed to create product: ${response.code()}")
        }
    }

    suspend fun updateProduct(
        token: String,
        productId: Int,
        name: String,
        description: String,
        price: Int,
        categoryId: Int,
        tokoIds: String,
        imageFile: File?
    ): Product {
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val tokoIdsBody = tokoIds.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        val response = service.updateProduct(
            "Bearer $token",
            productId,
            nameBody,
            descBody,
            priceBody,
            categoryBody,
            tokoIdsBody,
            imagePart
        )

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Product(
                id = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                categoryId = item.category.id,
                categoryName = item.category.name,
                tokos = item.tokos.map { it.name }
            )
        } else {
            throw Exception("Failed to update product: ${response.code()}")
        }
    }

    suspend fun deleteProduct(token: String, productId: Int): String {
        val response = service.deleteProduct("Bearer $token", productId)

        if (response.isSuccessful) {
            return response.body()?.message ?: "Product deleted successfully"
        } else {
            throw Exception("Failed to delete product: ${response.code()}")
        }
    }
}
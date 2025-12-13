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
        val body = response.body()!! // Style Bryan: Force Unwrap !!

        return body.data.map { item ->
            Product(
                id = item.id,
                name = item.name,
                description = item.description ?: "", // Pakai Elvis agar aman
                price = item.price,
                // Logic gambar agar URL-nya utuh
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                categoryId = item.category.id,
                categoryName = item.category.name ?: "",
                tokos = item.tokos.map { it.name }
            )
        }
    }

    suspend fun getProductById(productId: Int): Product {
        val response = service.getProductById(productId)
        val item = response.body()!!.data

        return Product(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            price = item.price,
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            categoryId = item.category.id,
            categoryName = item.category.name ?: "",
            tokos = item.tokos.map { it.name }
        )
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
            token = "Bearer $token",
            name = nameBody,
            description = descBody,
            price = priceBody,
            categoryId = categoryBody,
            tokoIds = tokoIdsBody,
            image = imagePart
        )

        val item = response.body()!!.data

        return Product(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            price = item.price,
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            categoryId = item.category.id,
            categoryName = item.category.name ?: "",
            tokos = item.tokos.map { it.name }
        )
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
            token = "Bearer $token",
            productId = productId,
            name = nameBody,
            description = descBody,
            price = priceBody,
            categoryId = categoryBody,
            tokoIds = tokoIdsBody,
            image = imagePart
        )

        val item = response.body()!!.data

        return Product(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            price = item.price,
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            categoryId = item.category.id,
            categoryName = item.category.name ?: "",
            tokos = item.tokos.map { it.name }
        )
    }

    suspend fun deleteProduct(token: String, productId: Int): String {
        val response = service.deleteProduct("Bearer $token", productId)
        val body = response.body()!! // Konsisten pakai force unwrap style kamu

        return body.message ?: "Product deleted successfully"
    }
}
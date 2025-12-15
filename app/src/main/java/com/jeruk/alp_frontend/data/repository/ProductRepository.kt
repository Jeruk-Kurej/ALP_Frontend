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
        android.util.Log.d("ProductRepository", "Preparing product creation request")
        android.util.Log.d("ProductRepository", "Token: ${if (token.isNotEmpty()) "Present" else "Empty"}")
        android.util.Log.d("ProductRepository", "Product details: name=$name, price=$price")
        android.util.Log.d("ProductRepository", "CategoryId: $categoryId (type: ${categoryId::class.simpleName})")
        android.util.Log.d("ProductRepository", "TokoIds: ${if (tokoIds.isEmpty()) "Empty (will not send)" else tokoIds}")
        android.util.Log.d("ProductRepository", "Image file: ${imageFile?.absolutePath}, exists: ${imageFile?.exists()}, size: ${imageFile?.length()}")

        // Validate categoryId
        if (categoryId <= 0) {
            android.util.Log.e("ProductRepository", "Invalid categoryId: $categoryId")
            throw Exception("Invalid category ID: $categoryId")
        }

        // Convert all values to RequestBody with proper content type
        // IMPORTANT: Use "multipart/form-data" as content type for all form fields
        val nameBody = name.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val descBody = description.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        // For numeric values, ensure they're properly formatted as plain strings
        val priceBody = price.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())

        // Critical: CategoryId must be sent as a clean integer string
        val categoryIdString = categoryId.toString()
        android.util.Log.d("ProductRepository", "CategoryId value: $categoryId")
        android.util.Log.d("ProductRepository", "CategoryId as string: '$categoryIdString'")
        android.util.Log.d("ProductRepository", "CategoryId string length: ${categoryIdString.length}")

        val categoryBody = categoryIdString.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        // DON'T send toko_ids at all - completely omit the field
        // Backend will handle this as nullable/optional
        // Only send it when we have actual toko IDs in the future
        val tokoIdsBody: okhttp3.RequestBody? = null

        // Make sure image exists - throw error if not provided
        if (imageFile == null || !imageFile.exists()) {
            android.util.Log.e("ProductRepository", "Image file is required but not provided or doesn't exist")
            throw Exception("Image file is required")
        }

        // Create image part with proper mime type
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        android.util.Log.d("ProductRepository", "Sending request to API")
        android.util.Log.d("ProductRepository", "=== REQUEST PARAMETERS ===")
        android.util.Log.d("ProductRepository", "- Name: $name")
        android.util.Log.d("ProductRepository", "- Description: $description")
        android.util.Log.d("ProductRepository", "- Price: $price")
        android.util.Log.d("ProductRepository", "- CategoryId: $categoryId")
        android.util.Log.d("ProductRepository", "- Image: ${imageFile.name} (${imageFile.length()} bytes)")
        android.util.Log.d("ProductRepository", "- TokoIds: NOT SENT (null)")
        android.util.Log.d("ProductRepository", "=========================")

        val response = try {
            service.createProduct(
                token = "Bearer $token",
                name = nameBody,
                description = descBody,
                price = priceBody,
                categoryId = categoryBody,
                image = imagePart,  // Image is required
                tokoIds = tokoIdsBody  // Optional - will be null if not provided
            )
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Exception during API call", e)
            throw e
        }

        android.util.Log.d("ProductRepository", "Response code: ${response.code()}")
        android.util.Log.d("ProductRepository", "Response successful: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("ProductRepository", "=== API ERROR DETAILS ===")
            android.util.Log.e("ProductRepository", "Status Code: ${response.code()}")

            // Truncate error if too long to see the key parts
            if (errorBody != null && errorBody.length > 500) {
                android.util.Log.e("ProductRepository", "Error Body (first 500 chars): ${errorBody.take(500)}")
                android.util.Log.e("ProductRepository", "Error Body (last 200 chars): ${errorBody.takeLast(200)}")
            } else {
                android.util.Log.e("ProductRepository", "Error Body: $errorBody")
            }

            android.util.Log.e("ProductRepository", "Response Headers: ${response.headers()}")

            // Create user-friendly error message
            val shortError = if (errorBody != null && errorBody.length > 100) {
                errorBody.take(100) + "..."
            } else {
                errorBody ?: "Unknown error"
            }

            throw Exception("Failed to create product: ${response.code()} - $shortError")
        }

        val item = response.body()?.data ?: throw Exception("Empty response body")

        android.util.Log.d("ProductRepository", "Product created successfully: ${item.name}, image: ${item.image}")

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
        val nameBody = name.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val descBody = description.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val priceBody = price.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val categoryBody = categoryId.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())

        // Only send tokoIds if it's not empty
        val tokoIdsBody = if (tokoIds.isNotEmpty()) {
            tokoIds.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        } else {
            null
        }

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
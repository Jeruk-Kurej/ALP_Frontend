package com.jeruk.alp_frontend.data.repository

import android.webkit.MimeTypeMap
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

    // Helper 1: Format Token
    private fun formatToken(token: String): String {
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    // Helper 2: Siapkan Gambar
    private fun prepareImagePart(file: File?): MultipartBody.Part? {
        return file?.let {
            val extension = it.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
            val requestBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestBody)
        }
    }

    suspend fun getAllProducts(token: String): List<Product> {
        val response = service.getAllProducts(formatToken(token))

        if (!response.isSuccessful) {
            throw Exception("Failed to load products: ${response.code()}")
        }

        val body = response.body()!!

        return body.data.map { item ->
            Product(
                id = item.id,
                name = item.name,
                description = item.description ?: "",
                price = item.price,
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                categoryId = item.category.id,
                categoryName = item.category.name ?: "",
                tokos = item.tokos.map { it.name },
                tokoIds = item.tokos.map { it.id }
            )
        }
    }

    suspend fun getProductById(token: String, productId: Int): Product {
        val response = service.getProductById(formatToken(token), productId)

        if (!response.isSuccessful) {
            throw Exception("Failed to load product: ${response.code()}")
        }

        val item = response.body()?.data ?: throw Exception("Product not found")

        return Product(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            price = item.price,
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            categoryId = item.category.id,
            categoryName = item.category.name ?: "",
            tokos = item.tokos.map { it.name },
            tokoIds = item.tokos.map { it.id }
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
        val textType = "text/plain".toMediaTypeOrNull()

        // 1. Siapkan data text
        val namePart = name.toRequestBody(textType)
        val descriptionPart = description.toRequestBody(textType)

        // Backend melakukan parsing Number(), jadi aman kirim string angka
        val pricePart = price.toString().toRequestBody(textType)
        val categoryIdPart = categoryId.toString().toRequestBody(textType)

        // 2. Siapkan Toko IDs (Optional)
        val tokoIdsPart = if (tokoIds.isBlank()) null else tokoIds.toRequestBody(textType)

        // 3. Siapkan Image
        val imagePart = prepareImagePart(imageFile)

        // 4. Kirim
        val response = service.createProduct(
            formatToken(token),
            namePart,
            descriptionPart,
            pricePart,
            categoryIdPart, // <--- Kirim ini (sekarang labelnya sudah 'categoryId')
            tokoIdsPart,
            imagePart
        )

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Product(
                id = item.id,
                name = item.name,
                description = item.description ?: "",
                price = item.price,
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                categoryId = item.category.id,
                categoryName = item.category.name ?: "",
                tokos = item.tokos.map { it.name },
                tokoIds = item.tokos.map { it.id }
            )
        } else {
            val errorMsg = response.errorBody()?.string()
            android.util.Log.e("REPO", "Create Error: $errorMsg")
            throw Exception("Gagal: $errorMsg")
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
        val textType = "text/plain".toMediaTypeOrNull()

        val nameBody = name.toRequestBody(textType)
        val descBody = description.toRequestBody(textType)
        val priceBody = price.toString().toRequestBody(textType)
        val categoryBody = categoryId.toString().toRequestBody(textType)

        val tokoIdsBody = if (tokoIds.isNotEmpty()) {
            tokoIds.toRequestBody(textType)
        } else {
            null
        }

        val imagePart = prepareImagePart(imageFile)

        val response = service.updateProduct(
            token = formatToken(token),
            productId = productId,
            name = nameBody,
            description = descBody,
            price = priceBody,
            categoryId = categoryBody,
            tokoIds = tokoIdsBody,
            image = imagePart
        )

        if (!response.isSuccessful) {
            val errorStr = response.errorBody()?.string()
            throw Exception("Failed to update product: $errorStr")
        }

        val item = response.body()?.data ?: throw Exception("Empty response after update")

        return Product(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            price = item.price,
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            categoryId = item.category.id,
            categoryName = item.category.name ?: "",
            tokos = item.tokos.map { it.name },
            tokoIds = item.tokos.map { it.id }
        )
    }

    suspend fun deleteProduct(token: String, productId: Int): String {
        val response = service.deleteProduct(formatToken(token), productId)
        if (!response.isSuccessful) throw Exception("Failed delete")
        val body = response.body()!!
        return body.message ?: "Product deleted successfully"
    }

    suspend fun updateProductTokoRelation(token: String, productId: Int, tokoId: Int, isAdding: Boolean) {
        val product = getProductById(token, productId)

        val currentTokoIds = product.tokoIds.toMutableList()

        if (isAdding) {
            if (!currentTokoIds.contains(tokoId)) currentTokoIds.add(tokoId)
        } else {
            currentTokoIds.remove(tokoId)
        }

        val tokoIdsString = currentTokoIds.joinToString(",")

        updateProduct(
            token = token,
            productId = productId,
            name = product.name,
            description = product.description,
            price = product.price,
            categoryId = product.categoryId,
            tokoIds = tokoIdsString,
            imageFile = null
        )
    }
}
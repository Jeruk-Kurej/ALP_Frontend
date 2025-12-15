package com.jeruk.alp_frontend.data.repository

import android.util.Log
import com.jeruk.alp_frontend.data.service.TokoService
import com.jeruk.alp_frontend.ui.model.Toko
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class TokoRepository(private val service: TokoService, private val baseUrl: String) {

    // Helper agar format token selalu benar: "Bearer <token>"
    private fun formatToken(token: String): String {
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    suspend fun createToko(token: String, name: String, desc: String, loc: String, imageFile: File?): Toko {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val locPart = loc.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            // AMBIL MIME TYPE SECARA AKURAT
            val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(it.absolutePath)
            val mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"

            Log.d("DEBUG_MULTIPART", "Mengirim file: ${it.name} dengan tipe: $mimeType")

            val requestBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
            // "image" di sini harus sama dengan upload.single("image") di backend
            MultipartBody.Part.createFormData("image", it.name, requestBody)
        }

        val response = service.createToko("Bearer $token", namePart, descPart, locPart, imagePart)
        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Toko(item.id, item.name, item.description ?: "", item.location ?: "", "", false)
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("API_ERROR", "Error 401/400: $errorBody")
            throw Exception("Server Error: $errorBody")
        }
    }

    suspend fun getMyTokos(token: String): List<Toko> {
        val response = service.getAllMyTokos("Bearer $token")
        if (!response.isSuccessful) return emptyList()
        return response.body()!!.data.map { item ->
            Toko(
                id = item.id,
                name = item.name ?: "",
                description = item.description ?: "",
                address = item.location ?: "",
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                isOpen = item.is_open
            )
        }
    }

    suspend fun deleteToko(token: String, id: Int) {
        service.deleteToko("Bearer $token", id)
    }
}
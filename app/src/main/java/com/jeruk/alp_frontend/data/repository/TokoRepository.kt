package com.jeruk.alp_frontend.data.repository

import android.util.Log
import android.webkit.MimeTypeMap
import com.jeruk.alp_frontend.data.service.TokoService
import com.jeruk.alp_frontend.ui.model.Toko
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class TokoRepository(private val service: TokoService, private val baseUrl: String) {

    private fun formatToken(token: String): String = if (token.startsWith("Bearer ")) token else "Bearer $token"

    suspend fun getTokoById(tokoId: Int): Toko {
        val response = service.getTokoById(tokoId)
        val item = response.body()!!.data
        return Toko(
            id = item.id,
            name = item.name ?: "",
            description = item.description ?: "",
            address = item.location ?: "",
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            isOpen = item.is_open
        )
    }

    suspend fun createToko(token: String, name: String, desc: String, loc: String, imageFile: File?): Toko {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val locPart = loc.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = prepareImagePart(imageFile)

        val response = service.createToko(formatToken(token), namePart, descPart, locPart, imagePart)
        if (response.isSuccessful) return getTokoById(response.body()!!.data.id)
        else throw Exception("Gagal Create: ${response.errorBody()?.string()}")
    }

    suspend fun updateToko(token: String, id: Int, name: String, desc: String, loc: String, imageFile: File?): Toko {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val locPart = loc.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = prepareImagePart(imageFile)

        val response = service.updateToko(formatToken(token), id, namePart, descPart, locPart, imagePart)
        if (response.isSuccessful) return getTokoById(id)
        else throw Exception("Gagal Update: ${response.errorBody()?.string()}")
    }

    suspend fun getMyTokos(token: String): List<Toko> {
        val response = service.getAllMyTokos(formatToken(token))
        if (!response.isSuccessful) return emptyList()
        return response.body()!!.data.map { item ->
            Toko(item.id, item.name ?: "", item.description ?: "", item.location ?: "", if (item.image != null) "$baseUrl${item.image}" else "", item.is_open)
        }
    }

    suspend fun deleteToko(token: String, id: Int) {
        service.deleteToko(formatToken(token), id)
    }

    private fun prepareImagePart(file: File?): MultipartBody.Part? {
        return file?.let {
            val extension = MimeTypeMap.getFileExtensionFromUrl(it.absolutePath)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
            val requestBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestBody)
        }
    }
}
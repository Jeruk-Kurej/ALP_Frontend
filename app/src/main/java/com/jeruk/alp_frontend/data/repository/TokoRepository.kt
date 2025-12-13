package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.TokoService
import com.jeruk.alp_frontend.ui.model.Toko
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class TokoRepository(
    private val service: TokoService,
    private val baseUrl: String
) {

    suspend fun getMyTokos(token: String): List<Toko> {
        val response = service.getAllMyTokos("Bearer $token")
        val body = response.body()!! // Style Bryan: Force Unwrap !!

        return body.data.map { item ->
            Toko(
                id = item.id,
                name = item.name,
                // Tambahkan ?: "" agar aman seperti di ArtistArtistRepository
                description = item.description ?: "",
                address = item.location ?: "",
                // Perbaiki logic URL gambar agar tidak jadi "http://...null"
                imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
                isOpen = item.is_open
            )
        }
    }

    suspend fun getTokoById(tokoId: Int): Toko {
        val response = service.getTokoById(tokoId)
        val item = response.body()!!.data // Langsung ambil datanya

        return Toko(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            address = item.location ?: "",
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            isOpen = item.is_open
        )
    }

    suspend fun createToko(
        token: String,
        name: String,
        description: String,
        location: String,
        imageFile: File?
    ): Toko {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val locationPart = location.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestBody)
        }

        val response = service.createToko(
            token = "Bearer $token",
            name = namePart,
            description = descriptionPart,
            location = locationPart,
            image = imagePart
        )

        val item = response.body()!!.data

        return Toko(
            id = item.id,
            name = item.name,
            description = item.description ?: "",
            address = item.location ?: "",
            imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",
            isOpen = item.is_open
        )
    }
}
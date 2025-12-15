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
    private val baseUrl: String // <--- KITA MINTA URL DARI LUAR (Container)
) {

    suspend fun getMyTokos(token: String): List<Toko> {
        val response = service.getAllMyTokos("Bearer $token")

        if (response.isSuccessful) {
            val body = response.body()!!
            return body.data.map { item ->
                Toko(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    address = item.location,
                    imageUrl = "$baseUrl${item.image}",
                    isOpen = item.is_open
                )
            }
        } else {
            throw Exception("Gagal mengambil data toko: ${response.code()}")
        }
    }

    suspend fun getTokoById(token: String, tokoId: Int): Toko {
        val response = service.getTokoById("Bearer $token", tokoId)

        if (response.isSuccessful) {
            val body = response.body()!!
            val item = body.data
            return Toko(
                id = item.id,
                name = item.name,
                description = item.description,
                address = item.location,
                imageUrl = "$baseUrl${item.image}",
                isOpen = item.is_open,
                ownerId = item.owner.id  // Map owner.id from backend
            )
        } else {
            throw Exception("Gagal mengambil data toko: ${response.code()}")
        }
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

        if (response.isSuccessful) {
            val body = response.body()!!
            val item = body.data
            return Toko(
                id = item.id,
                name = item.name,
                description = item.description,
                address = item.location,
                imageUrl = "$baseUrl${item.image}",
                isOpen = item.is_open,
                ownerId = item.owner.id  // Map owner.id from backend
            )
        } else {
            throw Exception("Gagal membuat toko: ${response.code()}")
        }
    }

    suspend fun updateToko(
        token: String,
        tokoId: Int,
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

        val response = service.updateToko(
            token = "Bearer $token",
            tokoId = tokoId,
            name = namePart,
            description = descriptionPart,
            location = locationPart,
            image = imagePart
        )

        if (response.isSuccessful) {
            val body = response.body()!!
            val item = body.data
            return Toko(
                id = item.id,
                name = item.name,
                description = item.description,
                address = item.location,
                imageUrl = "$baseUrl${item.image}",
                isOpen = item.is_open,
                ownerId = item.owner.id
            )
        } else {
            throw Exception("Gagal mengupdate toko: ${response.code()}")
        }
    }

    suspend fun deleteToko(token: String, tokoId: Int) {
        val response = service.deleteToko("Bearer $token", tokoId)
        if (!response.isSuccessful) {
            throw Exception("Gagal menghapus toko: ${response.code()}")
        }
    }
}
package com.jeruk.alp_frontend.data.repository

import android.webkit.MimeTypeMap
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

    private fun formatToken(token: String): String =
        if (token.startsWith("Bearer ")) token else "Bearer $token"

    // --- Helper Image ---
    private fun prepareImagePart(file: File?): MultipartBody.Part? {
        return file?.let {
            val extension = it.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
            val requestBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestBody)
        }
    }

    suspend fun getTokoById(token: String, tokoId: Int): Toko {
        val response = service.getTokoById(formatToken(token), tokoId)
        if (!response.isSuccessful) throw Exception("Gagal ambil data: ${response.code()}")
        val item = response.body()!!.data
        return Toko(
            item.id,
            item.name ?: "",
            item.description ?: "",
            item.location ?: "",
            if (item.image != null) "$baseUrl${item.image}" else "",
            item.is_open
        )
    }

    suspend fun createToko(
        token: String,
        name: String,
        desc: String,
        loc: String,
        imageFile: File?
    ): Toko {
        val textType = "text/plain".toMediaTypeOrNull()
        val namePart = name.toRequestBody(textType)
        val descPart = desc.toRequestBody(textType)
        val locPart = loc.toRequestBody(textType)
        val imagePart = prepareImagePart(imageFile)

        val response = service.createToko(formatToken(token), namePart, descPart, locPart, imagePart)

        if (response.isSuccessful) return getTokoById(token, response.body()!!.data.id)
        else throw Exception("Gagal Simpan: ${response.errorBody()?.string()}")
    }

    // 👇 BAGIAN INI SUDAH DIPERBAIKI
    suspend fun updateToko(
        token: String,
        id: Int,
        name: String,
        desc: String,
        loc: String,
        imageFile: File?,
        productIds: List<Int> // Menerima List ID Produk
    ): Toko {
        val textType = "text/plain".toMediaTypeOrNull()

        // 1. Siapkan data text
        val namePart = name.toRequestBody(textType)
        val descPart = desc.toRequestBody(textType)
        val locPart = loc.toRequestBody(textType) // Pastikan Service pakai @Part("address") atau "location" sesuai backend
        val imagePart = prepareImagePart(imageFile)

        // 2. Siapkan List ID Produk untuk Multipart
        // Ini mengubah [1, 2, 3] menjadi Multipart Part berulang
        val productIdsParts = ArrayList<MultipartBody.Part>()
        for (productId in productIds) {
            productIdsParts.add(
                MultipartBody.Part.createFormData("productIds", productId.toString())
            )
        }

        // 3. Panggil Service
        val response = service.updateToko(
            formatToken(token),
            id,
            namePart,
            descPart,
            locPart,
            productIdsParts, // Kirim list part
            imagePart
        )

        if (response.isSuccessful) return getTokoById(token, id)
        else throw Exception("Gagal Update: ${response.errorBody()?.string()}")
    }

    suspend fun getMyTokos(token: String): List<Toko> {
        val response = service.getAllMyTokos(formatToken(token))
        if (!response.isSuccessful) return emptyList()
        return response.body()!!.data.map { item ->
            Toko(
                item.id,
                item.name ?: "",
                item.description ?: "",
                item.location ?: "",
                if (item.image != null) "$baseUrl${item.image}" else "",
                item.is_open
            )
        }
    }

    suspend fun deleteToko(token: String, id: Int) {
        service.deleteToko(formatToken(token), id)
    }
}
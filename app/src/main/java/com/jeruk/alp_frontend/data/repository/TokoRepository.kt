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
    private val baseUrl: String // Masih disimpan tapi tidak dipakai untuk Image
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
            id = item.id,
            name = item.name ?: "",
            description = item.description ?: "",
            address = item.location ?: "",
            // 🔥 PERBAIKAN UTAMA DI SINI:
            // 1. Hapus "$baseUrl"
            // 2. Tambah .toString() agar aman dari error Type Mismatch
            imageUrl = item.image?.toString() ?: "",
            isOpen = item.is_open // Pastikan nama variabel di Model Toko sesuai (isOpen atau is_open)
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

    suspend fun updateToko(
        token: String,
        id: Int,
        name: String,
        desc: String,
        loc: String,
        imageFile: File?,
        productIds: List<Int>
    ): Toko {
        val textType = "text/plain".toMediaTypeOrNull()

        val namePart = name.toRequestBody(textType)
        val descPart = desc.toRequestBody(textType)
        val locPart = loc.toRequestBody(textType)
        val imagePart = prepareImagePart(imageFile)

        // Kirim List ID Produk sebagai Multipart berulang
        val productIdsParts = ArrayList<MultipartBody.Part>()
        for (productId in productIds) {
            productIdsParts.add(
                MultipartBody.Part.createFormData("productIds", productId.toString())
            )
        }

        val response = service.updateToko(
            formatToken(token),
            id,
            namePart,
            descPart,
            locPart,
            productIdsParts,
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
                id = item.id,
                name = item.name ?: "",
                description = item.description ?: "",
                address = item.location ?: "",
                // 🔥 PERBAIKAN DISINI JUGA:
                imageUrl = item.image?.toString() ?: "",
                isOpen = item.is_open
            )
        }
    }

    suspend fun deleteToko(token: String, id: Int) {
        val response = service.deleteToko(formatToken(token), id)
        if (!response.isSuccessful) throw Exception("Gagal hapus toko")
    }
}
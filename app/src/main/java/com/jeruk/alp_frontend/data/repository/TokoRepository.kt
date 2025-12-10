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
                    description = item.description ?: "",
                    address = item.location ?: "",

                    // Logic gabungin URL jadi dinamis sesuai inputan Container
                    imageUrl = if (item.image != null) "$baseUrl${item.image}" else "",

                    isOpen = item.is_open
                )
            }
        } else {
            throw Exception("Gagal mengambil data toko: ${response.code()}")
        }
    }

    // ... Function getTokoById dan createToko sama logicnya ...
    // ... Tinggal hapus hardcode URL saja ...
}
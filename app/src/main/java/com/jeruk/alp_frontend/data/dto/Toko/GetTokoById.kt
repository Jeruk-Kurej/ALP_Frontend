package com.jeruk.alp_frontend.data.dto.Toko

data class GetTokoById(
    val code: Int,
    val `data`: TokoData,
    val status: String
)
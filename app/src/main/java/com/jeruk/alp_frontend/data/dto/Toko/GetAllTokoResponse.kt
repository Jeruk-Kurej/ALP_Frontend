package com.jeruk.alp_frontend.data.dto.Toko

data class GetAllTokoResponse(
    val code: Int,
    val `data`: List<TokoData>,
    val status: String
)
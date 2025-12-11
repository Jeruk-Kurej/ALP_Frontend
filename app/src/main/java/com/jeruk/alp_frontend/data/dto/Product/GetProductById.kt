package com.jeruk.alp_frontend.data.dto.Product

data class GetProductById(
    val code: Int,
    val `data`: DataX,
    val status: String
)
package com.jeruk.alp_frontend.data.dto.Product

data class CreateProduct(
    val code: Int,
    val `data`: ProductData,
    val status: String
)
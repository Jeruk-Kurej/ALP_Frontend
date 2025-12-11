package com.jeruk.alp_frontend.data.dto.Product

data class GetAllProducts(
    val code: Int,
    val `data`: List<ProductData>,
    val status: String
)
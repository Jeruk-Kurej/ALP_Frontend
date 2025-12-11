package com.jeruk.alp_frontend.data.dto.Order

data class CreateOrder(
    val code: Int,
    val `data`: OrderData,
    val status: String
)
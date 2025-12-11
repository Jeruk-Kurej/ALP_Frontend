package com.jeruk.alp_frontend.data.dto.Order

data class UpdateOrderStatus(
    val code: Int,
    val `data`: OrderData,
    val status: String
)
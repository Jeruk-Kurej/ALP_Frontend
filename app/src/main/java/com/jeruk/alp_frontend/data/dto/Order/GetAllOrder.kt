package com.jeruk.alp_frontend.data.dto.Order

data class GetAllOrder(
    val code: Int,
    val `data`: List<OrderData>,
    val status: String
)
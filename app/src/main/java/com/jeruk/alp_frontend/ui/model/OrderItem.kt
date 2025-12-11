package com.jeruk.alp_frontend.ui.model

data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val productPrice: Int,
    val productImageUrl: String,
    val orderAmount: Int
)


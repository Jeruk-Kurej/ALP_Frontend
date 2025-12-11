package com.jeruk.alp_frontend.ui.model

data class Order(
    val id: Int,
    val customerName: String,
    val createDate: String,
    val status: String,
    val totalPrice: Int,
    val paymentId: Int,
    val paymentName: String,
    val tokoId: Int,
    val tokoName: String,
    val orderItems: List<OrderItem>
)


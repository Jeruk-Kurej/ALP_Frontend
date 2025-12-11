package com.jeruk.alp_frontend.data.dto.Order

data class OrderData(
    val create_date: String,
    val customer_name: String,
    val id: Int,
    val orderItems: List<OrderItemData>,
    val payment: PaymentData,
    val payment_id: Int,
    val status: String,
    val toko: TokoData,
    val toko_id: Int,
    val total_price: Int
)


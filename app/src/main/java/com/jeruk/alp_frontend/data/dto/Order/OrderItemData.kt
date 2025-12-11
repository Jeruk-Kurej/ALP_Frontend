package com.jeruk.alp_frontend.data.dto.Order

data class OrderItemData(
    val id: Int,
    val order_amount: Int,
    val product: ProductData,
    val product_id: Int
)


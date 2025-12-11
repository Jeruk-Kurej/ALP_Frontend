package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.OrderService
import com.jeruk.alp_frontend.ui.model.Order
import com.jeruk.alp_frontend.ui.model.OrderItem

class OrderRepository(
    private val service: OrderService,
    private val baseUrl: String
) {

    suspend fun getAllOrders(token: String): List<Order> {
        val response = service.getAllOrders("Bearer $token")

        if (response.isSuccessful) {
            val body = response.body()!!
            return body.data.map { item ->
                Order(
                    id = item.id,
                    customerName = item.customer_name,
                    createDate = item.create_date,
                    status = item.status,
                    totalPrice = item.total_price,
                    paymentId = item.payment_id,
                    paymentName = item.payment.name,
                    tokoId = item.toko_id,
                    tokoName = item.toko.name,
                    orderItems = item.orderItems.map { orderItem ->
                        OrderItem(
                            id = orderItem.id,
                            productId = orderItem.product_id,
                            productName = orderItem.product.name,
                            productPrice = orderItem.product.price,
                            productImageUrl = if (orderItem.product.image != null)
                                "$baseUrl${orderItem.product.image}" else "",
                            orderAmount = orderItem.order_amount
                        )
                    }
                )
            }
        } else {
            throw Exception("Failed to fetch orders: ${response.code()}")
        }
    }

    suspend fun createOrder(
        token: String,
        customerName: String,
        paymentId: Int,
        tokoId: Int,
        orderItems: List<Map<String, Int>>
    ): Order {
        val body = mapOf(
            "customer_name" to customerName,
            "payment_id" to paymentId,
            "toko_id" to tokoId,
            "order_items" to orderItems
        )
        val response = service.createOrder("Bearer $token", body)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Order(
                id = item.id,
                customerName = item.customer_name,
                createDate = item.create_date,
                status = item.status,
                totalPrice = item.total_price,
                paymentId = item.payment_id,
                paymentName = item.payment.name,
                tokoId = item.toko_id,
                tokoName = item.toko.name,
                orderItems = item.orderItems.map { orderItem ->
                    OrderItem(
                        id = orderItem.id,
                        productId = orderItem.product_id,
                        productName = orderItem.product.name,
                        productPrice = orderItem.product.price,
                        productImageUrl = if (orderItem.product.image != null)
                            "$baseUrl${orderItem.product.image}" else "",
                        orderAmount = orderItem.order_amount
                    )
                }
            )
        } else {
            throw Exception("Failed to create order: ${response.code()}")
        }
    }

    suspend fun updateOrderStatus(token: String, orderId: Int, status: String): Order {
        val body = mapOf("status" to status)
        val response = service.updateOrderStatus("Bearer $token", orderId, body)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Order(
                id = item.id,
                customerName = item.customer_name,
                createDate = item.create_date,
                status = item.status,
                totalPrice = item.total_price,
                paymentId = item.payment_id,
                paymentName = item.payment.name,
                tokoId = item.toko_id,
                tokoName = item.toko.name,
                orderItems = item.orderItems.map { orderItem ->
                    OrderItem(
                        id = orderItem.id,
                        productId = orderItem.product_id,
                        productName = orderItem.product.name,
                        productPrice = orderItem.product.price,
                        productImageUrl = if (orderItem.product.image != null)
                            "$baseUrl${orderItem.product.image}" else "",
                        orderAmount = orderItem.order_amount
                    )
                }
            )
        } else {
            throw Exception("Failed to update order status: ${response.code()}")
        }
    }
}
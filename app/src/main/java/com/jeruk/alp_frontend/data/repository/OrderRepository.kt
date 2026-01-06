package com.jeruk.alp_frontend.data.repository

import android.util.Log
import com.jeruk.alp_frontend.data.service.OrderService
import com.jeruk.alp_frontend.ui.model.Order
import com.jeruk.alp_frontend.ui.model.OrderItem
import com.jeruk.alp_frontend.data.service.OrderItemRequest
import com.jeruk.alp_frontend.data.service.OrderRequest

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
        orderItems: List<OrderItemRequest>
    ): Order {

        val requestBody = OrderRequest(
            customerName = customerName,
            paymentId = paymentId,
            tokoId = tokoId,
            items = orderItems
        )

        val response = service.createOrder("Bearer $token", requestBody)
        if (response.isSuccessful) {
            Log.d("OrderRepository", "Order created successfully!")
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
            val errorBody = response.errorBody()?.string()
            Log.e("OrderRepository", "Failed to create order: ${response.code()}")
            Log.e("OrderRepository", "Error body: $errorBody")
            throw Exception("Failed to create order: ${response.code()} - $errorBody")
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
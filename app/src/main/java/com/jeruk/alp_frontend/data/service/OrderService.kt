package com.jeruk.alp_frontend.data.service

import com.google.gson.annotations.SerializedName
import com.jeruk.alp_frontend.data.dto.Order.CreateOrder
import com.jeruk.alp_frontend.data.dto.Order.GetAllOrder
import com.jeruk.alp_frontend.data.dto.Order.UpdateOrderStatus
import retrofit2.Response
import retrofit2.http.*

// --- PERBAIKAN UTAMA DI SINI ---

data class OrderRequest(
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("payment_id") val paymentId: Int,
    @SerializedName("toko_id") val tokoId: Int,

    // Backend Controller baris 17 minta "items", BUKAN "order_items"
    @SerializedName("items") val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("product_id") val productId: Int,

    // Backend Controller baris 19 minta "amount", BUKAN "order_amount"
    @SerializedName("amount") val amount: Int
)

interface OrderService {
    @GET("orders")
    suspend fun getAllOrders(
        @Header("Authorization") token: String
    ): Response<GetAllOrder>

    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body body: OrderRequest
    ): Response<CreateOrder>

    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Int,
        @Body body: Map<String, String>
    ): Response<UpdateOrderStatus>
}
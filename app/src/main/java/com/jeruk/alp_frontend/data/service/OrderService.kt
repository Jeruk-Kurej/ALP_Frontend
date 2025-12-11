package com.jeruk.alp_frontend.data.service

import com.jeruk.alp_frontend.data.dto.Order.CreateOrder
import com.jeruk.alp_frontend.data.dto.Order.GetAllOrder
import com.jeruk.alp_frontend.data.dto.Order.UpdateOrderStatus
import retrofit2.Response
import retrofit2.http.*

interface OrderService {

    @GET("orders")
    suspend fun getAllOrders(
        @Header("Authorization") token: String
    ): Response<GetAllOrder>

    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): Response<CreateOrder>

    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Int,
        @Body body: Map<String, String>
    ): Response<UpdateOrderStatus>
}
package com.jeruk.alp_frontend.data.service

import com.jeruk.alp_frontend.data.dto.Payment.CreatePayment
import com.jeruk.alp_frontend.data.dto.Payment.DeletePayment
import com.jeruk.alp_frontend.data.dto.Payment.GetAllPayment
import com.jeruk.alp_frontend.data.dto.Payment.UpdatePayment
import retrofit2.Response
import retrofit2.http.*

interface PaymentService {

    @GET("payments")
    suspend fun getAllPayments(): Response<GetAllPayment>

    @POST("payments")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<CreatePayment>

    @PUT("payments/{paymentId}")
    suspend fun updatePayment(
        @Header("Authorization") token: String,
        @Path("paymentId") paymentId: Int,
        @Body body: Map<String, String>
    ): Response<UpdatePayment>

    @DELETE("payments/{paymentId}")
    suspend fun deletePayment(
        @Header("Authorization") token: String,
        @Path("paymentId") paymentId: Int
    ): Response<DeletePayment>
}
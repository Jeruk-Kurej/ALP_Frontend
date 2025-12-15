package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.PaymentService
import com.jeruk.alp_frontend.ui.model.Payment

class PaymentRepository(
    private val service: PaymentService
) {

    suspend fun getAllPayments(): List<Payment> {
        val response = service.getAllPayments()

        if (response.isSuccessful) {
            val body = response.body()!!
            return body.data.map { item ->
                Payment(
                    id = item.id,
                    name = item.name
                )
            }
        } else {
            throw Exception("Failed to fetch payments: ${response.code()}")
        }
    }

    suspend fun createPayment(token: String, name: String): Payment {
        val body = mapOf("name" to name)
        val response = service.createPayment("Bearer $token", body)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Payment(
                id = item.id,
                name = item.name
            )
        } else {
            throw Exception("Failed to create payment: ${response.code()}")
        }
    }

    suspend fun updatePayment(token: String, paymentId: Int, name: String): Payment {
        val body = mapOf("name" to name)
        val response = service.updatePayment("Bearer $token", paymentId, body)

        if (response.isSuccessful) {
            val item = response.body()!!.data
            return Payment(
                id = item.id,
                name = item.name
            )
        } else {
            throw Exception("Failed to update payment: ${response.code()}")
        }
    }

    suspend fun deletePayment(token: String, paymentId: Int): String {
        val response = service.deletePayment("Bearer $token", paymentId)

        if (response.isSuccessful) {
            return "Payment deleted successfully"
        } else {
            throw Exception("Failed to delete payment: ${response.code()}")
        }
    }
}
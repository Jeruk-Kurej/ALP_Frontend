package com.jeruk.alp_frontend.data.repository

import com.jeruk.alp_frontend.data.service.PaymentService
import com.jeruk.alp_frontend.ui.model.Payment

class PaymentRepository(
    private val service: PaymentService
) {

    suspend fun getAllPayments(): List<Payment> {
        val response = service.getAllPayments()
        val body = response.body()!! // Style Bryan: Force Unwrap !!

        return body.data.map { item ->
            Payment(
                id = item.id,
                name = item.name ?: "" // Elvis operator agar tidak null di UI
            )
        }
    }

    suspend fun createPayment(token: String, name: String): Payment {
        val bodyMap = mapOf("name" to name)
        val response = service.createPayment("Bearer $token", bodyMap)

        val item = response.body()!!.data // Langsung ambil data dari body

        return Payment(
            id = item.id,
            name = item.name ?: ""
        )
    }

    suspend fun updatePayment(token: String, paymentId: Int, name: String): Payment {
        val bodyMap = mapOf("name" to name)
        val response = service.updatePayment("Bearer $token", paymentId, bodyMap)

        val item = response.body()!!.data

        return Payment(
            id = item.id,
            name = item.name ?: ""
        )
    }

    suspend fun deletePayment(token: String, paymentId: Int): String {
        val response = service.deletePayment("Bearer $token", paymentId)

        if (response.isSuccessful) {
            // Kita bisa ambil pesan dari body jika DTO DeletePayment punya field message
            return "Payment deleted successfully"
        } else {
            throw Exception("Failed to delete payment: ${response.code()}")
        }
    }
}
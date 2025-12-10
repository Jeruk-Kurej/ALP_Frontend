package com.jeruk.alp_frontend.data.dto.Auth

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)
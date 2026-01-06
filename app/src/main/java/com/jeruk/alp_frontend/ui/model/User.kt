package com.jeruk.alp_frontend.ui.model

data class User(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val token: String = "",

    val isAuthenticated: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
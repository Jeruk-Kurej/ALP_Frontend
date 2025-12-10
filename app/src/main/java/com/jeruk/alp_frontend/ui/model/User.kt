package com.jeruk.alp_frontend.ui.model

data class User(
    val token: String = "",
    val username: String = "",
    val email: String = "",

    val isError: Boolean = false,
    val errorMessage: String? = null
)
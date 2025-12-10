package com.jeruk.alp_frontend.ui.model

data class Toko(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val isOpen: Boolean = false
)
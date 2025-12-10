package com.jeruk.alp_frontend.data.dto.Toko

data class Data(
    val description: String,
    val id: Int,
    val image: Any,
    val is_open: Boolean,
    val location: String,
    val name: String,
    val owner: Owner
)
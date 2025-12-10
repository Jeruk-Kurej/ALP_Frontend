package com.jeruk.alp_frontend.data.dto.Toko

data class DataX(
    val id: Int,
    val name: String,
    val is_open: Boolean,

    val description: String?,
    val location: String?,

    val image: String?,

    val owner: OwnerX
)
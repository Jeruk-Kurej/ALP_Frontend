package com.jeruk.alp_frontend.data.dto.Product

data class Data(
    val category: Category,
    val description: String,
    val id: Int,
    val image: Any,
    val name: String,
    val price: Int,
    val tokos: List<Toko>
)
package com.jeruk.alp_frontend.data.dto.Product

data class DataX(
    val category: CategoryX,
    val description: String,
    val id: Int,
    val image: Any,
    val name: String,
    val price: Int,
    val tokos: List<TokoX>
)
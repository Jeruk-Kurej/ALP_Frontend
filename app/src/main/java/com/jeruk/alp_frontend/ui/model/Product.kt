package com.jeruk.alp_frontend.ui.model

data class Product(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val categoryId: Int = 0,
    val categoryName: String = "",
    val tokos: List<String> = emptyList(),
    val tokoIds: List<Int> = emptyList(),
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
package com.jeruk.alp_frontend.data.dto.upload

data class UploadData(
    val filename: String,
    val mimetype: String,
    val size: Int,
    val url: String
)
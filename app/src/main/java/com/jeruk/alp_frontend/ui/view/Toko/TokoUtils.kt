package com.jeruk.alp_frontend.ui.view.Toko

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun uriToFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
    val extension = when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }

    val inputStream = contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "IMG_${System.currentTimeMillis()}.$extension")
    val outputStream = FileOutputStream(tempFile)

    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}
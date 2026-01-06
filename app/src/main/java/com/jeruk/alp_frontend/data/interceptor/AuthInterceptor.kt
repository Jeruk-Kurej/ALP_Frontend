package com.jeruk.alp_frontend.data.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val onUnauthorized: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Jika server merespon 401 (Unauthorized/Token Expired)
        if (response.code == 401) {
            onUnauthorized()
        }

        return response
    }
}
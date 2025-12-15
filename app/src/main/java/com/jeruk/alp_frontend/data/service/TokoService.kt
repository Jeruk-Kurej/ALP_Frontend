package com.jeruk.alp_frontend.data.service

import com.jeruk.alp_frontend.data.dto.Toko.GetAllTokoResponse
import com.jeruk.alp_frontend.data.dto.Toko.GetTokoById
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface TokoService {
    @GET("tokos/my/stores")
    suspend fun getAllMyTokos(@Header("Authorization") token: String): Response<GetAllTokoResponse>

    @Multipart
    @POST("tokos")
    suspend fun createToko(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<GetTokoById>

    @Multipart
    @PUT("tokos/{tokoId}")
    suspend fun updateToko(
        @Header("Authorization") token: String,
        @Path("tokoId") tokoId: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("location") location: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<GetTokoById>

    @DELETE("tokos/{tokoId}")
    suspend fun deleteToko(
        @Header("Authorization") token: String,
        @Path("tokoId") tokoId: Int
    ): Response<GetTokoById>
}
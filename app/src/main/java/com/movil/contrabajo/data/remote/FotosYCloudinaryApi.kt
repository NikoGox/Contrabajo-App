package com.movil.contrabajo.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// ==========================================
// 1. API DE FOTOS (Tu Búnker en Azure - Puerto 8084)
// ==========================================
interface FotosApiService {
    @GET("api/fotos/firma")
    fun obtenerFirmaCloudinary(
        @Header("Authorization") authorization: String
    ): Call<FirmaCloudinaryResponseDto>
}

data class FirmaCloudinaryResponseDto(
    @SerializedName("apiKey") val apiKey: String,
    @SerializedName("signature") val signature: String,
    @SerializedName("timestamp") val timestamp: String
)

// ==========================================
// 2. API DE CLOUDINARY (Internet)
// ==========================================
interface CloudinaryApiService {
    // Tiro directo a Cloudinary (IMPORTANTE: Fíjate que no lleva el @Header Authorization)
    @Multipart
    @POST("v1_1/dl1jjb7lx/image/upload") // Tu Cloud Name fijo aquí
    fun subirImagen(
        @Part file: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("signature") signature: RequestBody
    ): Call<CloudinaryUploadResponseDto>
}

data class CloudinaryUploadResponseDto(
    @SerializedName("secure_url") val secureUrl: String
)
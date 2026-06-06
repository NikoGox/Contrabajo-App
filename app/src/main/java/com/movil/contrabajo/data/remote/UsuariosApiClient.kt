package com.movil.contrabajo.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.movil.contrabajo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object UsuariosApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401 &&
                chain.request().header("Authorization")?.startsWith("Bearer ") == true
            ) {
                SesionEventos.emitirSesionInvalida()
            }
            response
        }
        .build()

    val api: UsuariosApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.USUARIOS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UsuariosApiService::class.java)
}

object FotosApiClient {
    // 1. Le agregamos el interceptor para que imprima en el Logcat todo lo que entra y sale
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: FotosApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.FOTOS_BASE_URL)
        .client(client) // 2. Se lo enchufamos aquí
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FotosApiService::class.java)
}

object CloudinaryApiClient {
    // 1. Igual aquí, le ponemos el interceptor a Cloudinary
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: CloudinaryApiService = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")
        .client(client) // 2. Se lo enchufamos aquí
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CloudinaryApiService::class.java)
}
internal fun bearer(token: String): String = "Bearer $token"

internal fun <T> ejecutarApi(call: Call<T>): Result<T> {
    return runCatching { call.execute() }
        .fold(
            onSuccess = { response -> response.aResultado() },
            onFailure = { Result.failure(IllegalStateException(mensajeConexion(it), it)) }
        )
}

private fun <T> Response<T>.aResultado(): Result<T> {
    if (isSuccessful) {
        val body = body()
        return if (body != null) {
            Result.success(body)
        } else {
            Result.failure(IllegalStateException("Respuesta vacia del servidor"))
        }
    }
    return Result.failure(IllegalArgumentException(errorBody()?.string().mensajeErrorServidor(code())))
}

private fun String?.mensajeErrorServidor(codigo: Int): String {
    if (isNullOrBlank()) return "Error del servidor ($codigo)"
    return runCatching {
        val json = Gson().fromJson(this, JsonObject::class.java)
        json?.get("error")?.asString
            ?: json?.get("mensaje")?.asString
            ?: "Error del servidor ($codigo)"
    }.getOrElse { "Error del servidor ($codigo)" }
}

private fun mensajeConexion(error: Throwable): String {
    return when (error) {
        is java.net.ConnectException -> "No se pudo conectar con el backend de usuarios. Verifica que este corriendo en el puerto 8081."
        is java.net.SocketTimeoutException -> "El backend de usuarios tardo demasiado en responder."
        else -> error.message ?: "No se pudo completar la solicitud al backend"
    }
}

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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

interface ServiciosApiService {
    @GET("api/catalogos/categorias")
    fun listarCategorias(@Header("Authorization") authorization: String): Call<List<CategoriaServicioDto>>

    @GET("api/catalogos/tipos-precio")
    fun listarTiposPrecio(@Header("Authorization") authorization: String): Call<List<TipoPrecioDto>>

    @GET("api/ofertas")
    fun listarOfertas(@Header("Authorization") authorization: String): Call<List<OfertaServicioDto>>

    @GET("api/ofertas/{id}")
    fun buscarOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<OfertaServicioDto>

    @GET("api/ofertas/trabajador/{idTrabajador}")
    fun listarOfertasTrabajador(
        @Header("Authorization") authorization: String,
        @Path("idTrabajador") idTrabajador: Int
    ): Call<List<OfertaServicioDto>>

    @POST("api/ofertas")
    fun crearOferta(
        @Header("Authorization") authorization: String,
        @Body request: OfertaServicioRequestDto
    ): Call<OfertaServicioDto>

    @PATCH("api/ofertas/{id}")
    fun actualizarOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: OfertaServicioUpdateRequestDto
    ): Call<OfertaServicioDto>

    @DELETE("api/ofertas/{id}")
    fun eliminarOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<Map<String, String>>
}

data class CategoriaServicioDto(
    val id: Int?,
    val nombre: String?
)

data class TipoPrecioDto(
    val id: Int?,
    val nombre: String?
)

data class OfertaServicioDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String?,
    val precio: BigDecimal?,
    val disponible: Boolean?,
    val fechaPublicacion: String?,
    val idTrabajador: Int?,
    val idCategoria: Int?,
    val idTipoPrecio: Int?,
    val categoria: String?,
    val tipoPrecio: String?,
    val ubicacionReferencia: String? = null,
    val latitudReferencia: Double? = null,
    val longitudReferencia: Double? = null
)

data class OfertaServicioRequestDto(
    val titulo: String,
    val descripcion: String,
    val precio: BigDecimal?,
    val idCategoria: Int,
    val idTipoPrecio: Int?
)

data class OfertaServicioUpdateRequestDto(
    val titulo: String? = null,
    val descripcion: String? = null,
    val precio: BigDecimal? = null,
    val disponible: Boolean? = null,
    val idCategoria: Int? = null,
    val idTipoPrecio: Int? = null
)

object ServiciosApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ServiciosApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVICIOS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ServiciosApiService::class.java)
}

internal fun <T> ejecutarApiServicios(call: Call<T>): Result<T> {
    return runCatching { call.execute() }
        .fold(
            onSuccess = { response -> response.aResultadoServicios() },
            onFailure = { Result.failure(IllegalStateException(mensajeConexionServicios(it), it)) }
        )
}

private fun <T> Response<T>.aResultadoServicios(): Result<T> {
    if (isSuccessful) {
        val body = body()
        return if (body != null) Result.success(body) else Result.failure(IllegalStateException("Respuesta vacia del servidor"))
    }
    return Result.failure(IllegalArgumentException(errorBody()?.string().mensajeErrorServidorServicios(code())))
}

private fun String?.mensajeErrorServidorServicios(codigo: Int): String {
    if (isNullOrBlank()) return "Error del servidor de servicios ($codigo)"
    return runCatching {
        val json = Gson().fromJson(this, JsonObject::class.java)
        json?.get("error")?.asString
            ?: json?.get("mensaje")?.asString
            ?: "Error del servidor de servicios ($codigo)"
    }.getOrElse { "Error del servidor de servicios ($codigo)" }
}

private fun mensajeConexionServicios(error: Throwable): String {
    return when (error) {
        is java.net.ConnectException -> "No se pudo conectar con el backend de servicios. Verifica que este corriendo en el puerto 8082."
        is java.net.SocketTimeoutException -> "El backend de servicios tardo demasiado en responder."
        else -> error.message ?: "No se pudo completar la solicitud al backend de servicios"
    }
}

package com.movil.contrabajo.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.movil.contrabajo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
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

    @GET("api/ofertas/{id}/disponibilidad")
    fun obtenerDisponibilidadOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<Boolean>

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

    @PUT("api/ofertas/{id}")
    fun actualizarOfertaPut(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: OfertaServicioUpdateRequestDto
    ): Call<OfertaServicioDto>

    @PATCH("api/ofertas/{id}/disponibilidad/activar")
    fun activarDisponibilidadOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<OfertaServicioDto>

    @PATCH("api/ofertas/{id}/disponibilidad/desactivar")
    fun desactivarDisponibilidadOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<OfertaServicioDto>

    @DELETE("api/ofertas/{id}")
    fun eliminarOferta(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<Map<String, String>>

    // ── Fotos de oferta ──────────────────────────────────────────────────────
    @Multipart
    @POST("api/fotos/{idOferta}")
    fun subirFotoOferta(
        @Header("Authorization") authorization: String,
        @Path("idOferta") idOferta: Int,
        @Part imagen: MultipartBody.Part
    ): Call<FotoOfertaResponseDto>

    @GET("api/fotos/oferta/{idOferta}")
    fun listarFotosOferta(
        @Header("Authorization") authorization: String,
        @Path("idOferta") idOferta: Int
    ): Call<List<FotoOfertaResponseDto>>

    @DELETE("api/fotos/{idFoto}")
    fun eliminarFotoOferta(
        @Header("Authorization") authorization: String,
        @Path("idFoto") idFoto: Int
    ): Call<Map<String, String>>

    // ── Citas de servicio ─────────────────────────────────────────────────────
    @POST("api/citas/solicitar")
    fun solicitarCita(
        @Header("Authorization") authorization: String,
        @Body dto: SolicitarCitaRequestDto
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/aceptar")
    fun aceptarCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/rechazar")
    fun rechazarCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/reenviar")
    fun reenviarCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/comenzar")
    fun comenzarCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/confirmar-inicio")
    fun confirmarInicioCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/finalizar")
    fun finalizarCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/confirmar-finalizacion")
    fun confirmarFinalizacionCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @PATCH("api/citas/{id}/cancelar")
    fun cancelarCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @GET("api/citas/mis-citas")
    fun misCitas(
        @Header("Authorization") authorization: String
    ): Call<List<CitaServicioDto>>

    @GET("api/citas/{id}")
    fun obtenerCita(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<CitaServicioDto>

    @POST("api/valoraciones")
    fun crearValoracion(
        @Header("Authorization") authorization: String,
        @Body dto: ValoracionRequestDto
    ): Call<Map<String, String>>

    @GET("api/valoraciones/trabajador/{idTrabajador}")
    fun obtenerValoracionesTrabajador(
        @Header("Authorization") authorization: String,
        @Path("idTrabajador") idTrabajador: Int
    ): Call<List<ValoracionServicioDto>>
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
    val borrado: Boolean? = null,
    val fechaPublicacion: String?,
    val idTrabajador: Int?,
    val idCategoria: Int?,
    val idTipoPrecio: Int?,
    val categoria: String?,
    val tipoPrecio: String?,
    val rangoDisponibilidadM: Int? = null,
    val ubicacionReferencia: String? = null,
    val latitudReferencia: Double? = null,
    val longitudReferencia: Double? = null,
    val nombreTrabajador: String? = null,
    val usernameTrabajador: String? = null
)

data class OfertaServicioRequestDto(
    val titulo: String,
    val descripcion: String,
    val precio: BigDecimal?,
    val idCategoria: Int,
    val idTipoPrecio: Int?
)

data class FotoOfertaResponseDto(
    @SerializedName("id_foto")           val idFoto: Int?,
    @SerializedName("enlace")            val enlace: String?,
    @SerializedName("nombre_original")   val nombreOriginal: String?,
    @SerializedName("tipo_mime")         val tipoMime: String?,
    @SerializedName("tamano_bytes")      val tamanoBytes: Long?,
    @SerializedName("ancho_px")          val anchoPx: Int?,
    @SerializedName("alto_px")           val altoPx: Int?,
    @SerializedName("fecha_subida")      val fechaSubida: String?,
    @SerializedName("id_oferta_servicio") val idOfertaServicio: Int?,
    @SerializedName("id_usuario")        val idUsuario: Int?
)

data class OfertaServicioUpdateRequestDto(
    val titulo: String? = null,
    val descripcion: String? = null,
    val precio: BigDecimal? = null,
    val disponible: Boolean? = null,
    val idCategoria: Int? = null,
    val idTipoPrecio: Int? = null
)

data class SolicitarCitaRequestDto(
    @SerializedName("idOfertaServicio") val idOfertaServicio: Int,
    @SerializedName("comentario")       val comentario: String,
    @SerializedName("idChatOferta")     val idChatOferta: Long? = null
)

data class CitaServicioDto(
    @SerializedName("id")                val id: Int?,
    @SerializedName("comentario")        val comentario: String?,
    @SerializedName("fechaSolicitud")    val fechaSolicitud: String?,
    @SerializedName("fechaInicioTrabajo") val fechaInicioTrabajo: String?,
    @SerializedName("fechaFinTrabajo")   val fechaFinTrabajo: String?,
    @SerializedName("idOfertaServicio")  val idOfertaServicio: Int?,
    @SerializedName("tituloOferta")      val tituloOferta: String?,
    @SerializedName("idCliente")         val idCliente: Int?,
    @SerializedName("idTrabajador")      val idTrabajador: Int?,
    @SerializedName("idEstado")          val idEstado: Int?,
    @SerializedName("codigoEstado")      val codigoEstado: String?,
    @SerializedName("estado")            val estado: String?
)

data class ValoracionRequestDto(
    @SerializedName("idCita") val idCita: Int,
    @SerializedName("voto") val voto: Int,
    @SerializedName("comentario") val comentario: String
)

data class ValoracionServicioDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("idCita") val idCita: Int?,
    @SerializedName("idOfertaServicio") val idOfertaServicio: Int?,
    @SerializedName("idCliente") val idCliente: Int?,
    @SerializedName("idTrabajador") val idTrabajador: Int?,
    @SerializedName("voto") val voto: Int?,
    @SerializedName("comentario") val comentario: String?,
    @SerializedName("fechaVoto") val fechaVoto: String?
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

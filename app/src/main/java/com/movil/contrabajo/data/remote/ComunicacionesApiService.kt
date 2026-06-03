package com.movil.contrabajo.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.movil.contrabajo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ──────────────────────────────────────────────────────────────────────────────
// Retrofit interface — puerto 8083 (comunicaciones_api)
// ──────────────────────────────────────────────────────────────────────────────
interface ComunicacionesApiService {

    /** Crea o recupera un chat para la combinacion trabajador+cliente+oferta */
    @POST("api/chats/iniciar")
    fun iniciarChat(
        @Header("Authorization") auth: String,
        @Body dto: ChatIniciarRequestDto
    ): Call<ChatDto>

    /** Envia un mensaje de texto al chat */
    @POST("api/chats/mensaje")
    fun enviarMensaje(
        @Header("Authorization") auth: String,
        @Body dto: MensajeChatEnviarDto
    ): Call<MensajeChatDto>

    /** Lista todos los chats del usuario autenticado */
    @GET("api/chats")
    fun listarChats(
        @Header("Authorization") auth: String
    ): Call<List<ChatDto>>

    /** Historial de mensajes de un chat */
    @GET("api/chats/{idChat}/historial")
    fun obtenerHistorial(
        @Header("Authorization") auth: String,
        @Path("idChat") idChat: Long
    ): Call<List<MensajeChatDto>>

    /** Vincula una cita existente al chat */
    @PATCH("api/chats/{idChat}/vincular-cita")
    fun vincularCita(
        @Header("Authorization") auth: String,
        @Path("idChat") idChat: Long,
        @Body dto: VincularCitaRequestDto
    ): Call<ChatDto>

    /** Marca mensajes como recibidos */
    @PATCH("api/chats/{idChat}/recibidos")
    fun marcarRecibidos(
        @Header("Authorization") auth: String,
        @Path("idChat") idChat: Long
    ): Call<Void>

    /** Marca mensajes como leidos */
    @PATCH("api/chats/{idChat}/leidos")
    fun marcarLeidos(
        @Header("Authorization") auth: String,
        @Path("idChat") idChat: Long
    ): Call<Void>

    /** Desactiva (cierra) un chat */
    @PATCH("api/chats/desactivar")
    fun desactivarChat(
        @Header("Authorization") auth: String,
        @Body dto: ChatIniciarRequestDto
    ): Call<Void>

    @PATCH("api/chats/{idChat}/desactivar")
    fun desactivarChatPorId(
        @Header("Authorization") auth: String,
        @Path("idChat") idChat: Long
    ): Call<Void>

    @GET("api/reportes/tipos")
    fun obtenerTiposReporte(
        @Header("Authorization") auth: String
    ): Call<List<TipoReporteDto>>

    @POST("api/reportes")
    fun crearReporte(
        @Header("Authorization") auth: String,
        @Body dto: CrearReporteRequestDto
    ): Call<ReporteResponseDto>

    @GET("api/reportes")
    fun listarReportesModeracion(
        @Header("Authorization") auth: String,
        @Query("busqueda") busqueda: String? = null,
        @Query("estadoRevision") estadoRevision: String? = null,
        @Query("idTipoReporte") idTipoReporte: Int? = null,
        @Query("ordenarRecientes") ordenarRecientes: Boolean = true
    ): Call<List<ReporteResponseDto>>

    @GET("api/reportes/{idReporte}")
    fun obtenerDetalleReporte(
        @Header("Authorization") auth: String,
        @Path("idReporte") idReporte: Long
    ): Call<ReporteResponseDto>

    @PATCH("api/reportes/{idReporte}/revision")
    fun revisarReporte(
        @Header("Authorization") auth: String,
        @Path("idReporte") idReporte: Long,
        @Body dto: RevisarReporteRequestDto
    ): Call<ReporteResponseDto>
}

// ──────────────────────────────────────────────────────────────────────────────
// DTOs de request
// ──────────────────────────────────────────────────────────────────────────────
data class ChatIniciarRequestDto(
    @SerializedName("idTrabajador")       val idTrabajador: Int,
    @SerializedName("idOfertaServicio")   val idOfertaServicio: Int,
    // Datos de visualizacion — se almacenan en chat_oferta para evitar joins posteriores
    @SerializedName("usernameTrabajador") val usernameTrabajador: String? = null,
    @SerializedName("usernameCliente")    val usernameCliente: String? = null,
    @SerializedName("tituloServicio")     val tituloServicio: String? = null
)

data class MensajeChatEnviarDto(
    @SerializedName("idChatOferta")  val idChatOferta: Long,
    @SerializedName("contenido")     val contenido: String,
    @SerializedName("tipo")          val tipo: Int = 0
)

data class VincularCitaRequestDto(
    @SerializedName("idCita")  val idCita: Int
)

data class CrearReporteRequestDto(
    @SerializedName("idTipoReporte") val idTipoReporte: Int,
    @SerializedName("idOfertaServicio") val idOfertaServicio: Long? = null,
    @SerializedName("idUsuarioReportado") val idUsuarioReportado: Int? = null,
    @SerializedName("idChatCita") val idChatCita: Long? = null,
    @SerializedName("comentario") val comentario: String
)

data class RevisarReporteRequestDto(
    @SerializedName("medidaAplicada") val medidaAplicada: String
)

// ──────────────────────────────────────────────────────────────────────────────
// DTOs de respuesta
// ──────────────────────────────────────────────────────────────────────────────
data class ChatDto(
    @SerializedName("id")                 val id: Long?,
    @SerializedName("idTrabajador")       val idTrabajador: Int?,
    @SerializedName("idCliente")          val idCliente: Int?,
    @SerializedName("idOfertaServicio")   val idOfertaServicio: Int?,
    @SerializedName("idCita")             val idCita: Int?,
    @SerializedName("activo")             val activo: Boolean?,
    @SerializedName("fechaCreacion")      val fechaCreacion: String?,
    @SerializedName("ultimoMensaje")      val ultimoMensaje: String?,
    @SerializedName("fechaUltimoMensaje") val fechaUltimoMensaje: String?,
    @SerializedName("mensajesNoLeidos")   val mensajesNoLeidos: Long?,
    // Datos de visualizacion desnormalizados
    @SerializedName("usernameTrabajador") val usernameTrabajador: String?,
    @SerializedName("usernameCliente")    val usernameCliente: String?,
    @SerializedName("tituloServicio")     val tituloServicio: String?
)

data class MensajeChatDto(
    @SerializedName("id")            val id: Long?,
    @SerializedName("idChatOferta")  val idChatOferta: Long?,
    @SerializedName("idEmisor")      val idEmisor: Int?,
    @SerializedName("idReceptor")    val idReceptor: Int?,
    @SerializedName("contenido")     val contenido: String?,
    @SerializedName("fechaEnvio")    val fechaEnvio: String?,
    @SerializedName("fechaRecibido") val fechaRecibido: String?,
    @SerializedName("fechaLeido")    val fechaLeido: String?,
    /** 0 = normal, 1 = sistema (generado automaticamente por el backend) */
    @SerializedName("tipo")          val tipo: Int? = null
)

data class TipoReporteDto(
    @SerializedName(value = "id", alternate = ["idTipoReporte", "id_tipo_reporte"]) val id: Int?,
    @SerializedName(value = "nombre", alternate = ["tipoReporteNombre", "nombre_tipo"]) val nombre: String?
)

data class ReporteResponseDto(
    @SerializedName("idReporte") val idReporte: Long?,
    @SerializedName("idEmisor") val idEmisor: Int?,
    @SerializedName("idUsuarioReportado") val idUsuarioReportado: Int?,
    @SerializedName("idOfertaServicio") val idOfertaServicio: Long?,
    @SerializedName("idChatCita") val idChatCita: Long?,
    @SerializedName("idTipoReporte") val idTipoReporte: Int?,
    @SerializedName("comentario") val comentario: String?,
    @SerializedName("fechaCreacion") val fechaCreacion: String?,
    @SerializedName("estadoRevision") val estadoRevision: String?,
    @SerializedName("idModeradorRevisor") val idModeradorRevisor: Int?,
    @SerializedName("fechaRevision") val fechaRevision: String?,
    @SerializedName("medidaAplicada") val medidaAplicada: String?,
    @SerializedName("tipoReporteNombre") val tipoReporteNombre: String?,
    @SerializedName("emisorUsername") val emisorUsername: String?,
    @SerializedName("usuarioReportadoUsername") val usuarioReportadoUsername: String?,
    @SerializedName("usuarioReportadoNombre") val usuarioReportadoNombre: String?,
    @SerializedName("servicioTitulo") val servicioTitulo: String?,
    @SerializedName("servicioFotoUrl") val servicioFotoUrl: String?
)

// ──────────────────────────────────────────────────────────────────────────────
// Singleton Retrofit client
// ──────────────────────────────────────────────────────────────────────────────
object ComunicacionesApiClient {
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

    val api: ComunicacionesApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.COMUNICACIONES_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ComunicacionesApiService::class.java)
}

// ──────────────────────────────────────────────────────────────────────────────
// Helpers de ejecucion (mismo patron que ServiciosApiService)
// ──────────────────────────────────────────────────────────────────────────────
internal fun <T> ejecutarApiComunicaciones(call: Call<T>): Result<T> {
    return runCatching { call.execute() }
        .fold(
            onSuccess = { response -> response.aResultadoComunicaciones() },
            onFailure = { Result.failure(IllegalStateException(mensajeConexionComunicaciones(it), it)) }
        )
}

private fun <T> Response<T>.aResultadoComunicaciones(): Result<T> {
    if (isSuccessful) {
        val body = body()
        return if (body != null) Result.success(body)
        else {
            // Endpoints que retornan Void (204/200 sin cuerpo)
            @Suppress("UNCHECKED_CAST")
            Result.success(null as T)
        }
    }
    return Result.failure(IllegalArgumentException(errorBody()?.string().mensajeErrorComunicaciones(code())))
}

private fun String?.mensajeErrorComunicaciones(codigo: Int): String {
    if (isNullOrBlank()) return "Error del servidor de comunicaciones ($codigo)"
    return runCatching {
        val json = Gson().fromJson(this, JsonObject::class.java)
        json?.get("error")?.asString
            ?: json?.get("mensaje")?.asString
            ?: "Error del servidor de comunicaciones ($codigo)"
    }.getOrElse { "Error del servidor de comunicaciones ($codigo)" }
}

private fun mensajeConexionComunicaciones(error: Throwable): String {
    return when (error) {
        is java.net.ConnectException -> "No se pudo conectar con el backend de comunicaciones. Verifica que este corriendo en el puerto 8083."
        is java.net.SocketTimeoutException -> "El backend de comunicaciones tardo demasiado en responder."
        else -> error.message ?: "No se pudo completar la solicitud al backend de comunicaciones"
    }
}

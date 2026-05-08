package com.movil.contrabajo.data.remote

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface UsuariosApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequestDto): Call<LoginResponseDto>

    @POST("api/auth/logout")
    fun logout(@Header("Authorization") authorization: String): Call<Map<String, String>>

    @GET("api/auth/validar-bd")
    fun validarSesion(@Query("token") token: String): Call<Boolean>

    @POST("api/usuarios/registrar")
    fun registrar(@Body request: UsuarioRegistroRequestDto): Call<UsuarioResponseDto>

    @GET("api/usuarios/comunas")
    fun listarComunas(): Call<List<ComunaDto>>

    @GET("api/usuarios/disponibilidad/run")
    fun runDisponible(@Query("run") run: Int): Call<Boolean>

    @GET("api/usuarios/disponibilidad/username")
    fun usernameDisponible(@Query("username") username: String): Call<Boolean>

    @GET("api/usuarios/disponibilidad/correo")
    fun correoDisponible(@Query("correo") correo: String): Call<Boolean>

    @GET("api/usuarios/{id}")
    fun buscarUsuario(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<UsuarioResponseDto>

    @PATCH("api/usuarios/{id}")
    fun actualizarUsuario(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: UsuarioUpdateRequestDto
    ): Call<UsuarioResponseDto>

    @PATCH("api/usuarios/{id}/verificar-ocr")
    fun verificarOcr(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: OcrSimuladoRequestDto
    ): Call<UsuarioResponseDto>

    @POST("api/usuarios/recuperar/verificar-respuestas")
    fun verificarRespuestas(@Body request: RecuperacionPasswordRequestDto): Call<Map<String, String>>

    @GET("api/usuarios/recuperar/preguntas")
    fun obtenerPreguntasRecuperacion(@Query("username") username: String): Call<PreguntasSeguridadDto>

    @GET("api/usuarios/{id}/preguntas-seguridad")
    fun obtenerPreguntasSeguridadPerfil(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Call<PreguntasSeguridadDto>

    @GET("api/usuarios/perfil/preguntas-seguridad")
    fun obtenerPreguntasSeguridadPerfilActual(
        @Header("Authorization") authorization: String
    ): Call<PreguntasSeguridadDto>

    @POST("api/usuarios/recuperar/cambiar-password")
    fun cambiarPassword(@Body request: RecuperacionPasswordRequestDto): Call<Map<String, String>>

    @PATCH("api/usuarios/{id}/preguntas-seguridad")
    fun actualizarPreguntasSeguridad(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: RecuperacionRegistroRequestDto
    ): Call<PreguntasSeguridadDto>

    @PATCH("api/usuarios/{id}/preguntas-seguridad/{indice}")
    fun actualizarPreguntaSeguridadParcial(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Path("indice") indice: Int,
        @Body request: PreguntaSeguridadUpdateRequestDto
    ): Call<PreguntasSeguridadDto>

    @PATCH("api/usuarios/perfil/preguntas-seguridad/{indice}")
    fun actualizarPreguntaSeguridadParcialPerfilActual(
        @Header("Authorization") authorization: String,
        @Path("indice") indice: Int,
        @Body request: PreguntaSeguridadUpdateRequestDto
    ): Call<PreguntasSeguridadDto>

    // ── Foto de perfil ───────────────────────────────────────────────────────
    @Multipart
    @POST("api/usuarios/foto-perfil")
    fun subirFotoPerfil(
        @Header("Authorization") authorization: String,
        @Part imagen: MultipartBody.Part
    ): Call<FotoPerfilResponseDto>

    @GET("api/usuarios/foto-perfil/{idUsuario}")
    fun obtenerFotoPerfil(
        @Path("idUsuario") idUsuario: Int
    ): Call<FotoPerfilResponseDto>

    @DELETE("api/usuarios/foto-perfil")
    fun eliminarFotoPerfil(
        @Header("Authorization") authorization: String
    ): Call<Map<String, String>>
}

data class LoginRequestDto(
    val username: String,
    val password: String
)

data class LoginResponseDto(
    val token: String?,
    val usuario: UsuarioResponseDto?
)

data class UsuarioResponseDto(
    val id: Int?,
    val run: Int?,
    val dv: String?,
    val username: String?,
    val nombre: String?,
    val apellidos: String?,
    val correo: String?,
    val telefono: String?,
    @SerializedName("rango_disponibilidad_m")
    val rangoDisponibilidadM: Int? = null,
    @SerializedName("rango_busqueda_m")
    val rangoBusquedaM: Int? = null,
    val fechaNacimiento: String?,
    val perfil: String?,
    val verificado: Boolean?,
    val direccion: DireccionResponseDto? = null
)

data class ComunaDto(
    val id: Int?,
    val nombre: String?,
    val idRegion: Int?,
    val region: String?
)

data class DireccionResponseDto(
    val id: Int?,
    val calle: String?,
    val numero: String?,
    val comuna: ComunaDto?,
    val latitud: Double?,
    val longitud: Double?
)

data class UsuarioRegistroRequestDto(
    val run: Int,
    val dv: String,
    val username: String,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val telefono: String,
    val correo: String,
    val password: String,
    val fechaNacimiento: String,
    val recuperacion: RecuperacionRegistroRequestDto,
    val direccion: DireccionRegistroRequestDto
)

data class RecuperacionRegistroRequestDto(
    val pregunta1: String,
    val respuesta1: String,
    val pregunta2: String,
    val respuesta2: String
)

data class DireccionRegistroRequestDto(
    val calle: String,
    val numero: String,
    val idComuna: Int?,
    val idCiudad: Int = 1,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class UsuarioUpdateRequestDto(
    val telefono: String? = null,
    val correo: String? = null,
    @SerializedName("rango_disponibilidad_m")
    val rangoDisponibilidadM: Int? = null,
    @SerializedName("rango_busqueda_m")
    val rangoBusquedaM: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val calle: String? = null,
    val numero: String? = null,
    val idComuna: Int? = null
)

data class OcrSimuladoRequestDto(
    val rutOcr: Int,
    val dvOcr: String,
    val numeroDocumento: String
)

data class PreguntasSeguridadDto(
    val pregunta1: String?,
    val pregunta2: String?
)

data class RecuperacionPasswordRequestDto(
    val username: String,
    val respuesta1: String,
    val respuesta2: String,
    val nuevaPassword: String? = null
)

data class PreguntaSeguridadUpdateRequestDto(
    val pregunta: String,
    val respuesta: String
)

data class FotoPerfilResponseDto(
    @SerializedName("id_foto_perfil") val idFotoPerfil: Int?,
    @SerializedName("enlace")         val enlace: String?,
    @SerializedName("nombre_original") val nombreOriginal: String?,
    @SerializedName("tipo_mime")      val tipoMime: String?,
    @SerializedName("tamano_bytes")   val tamanoBytes: Long?,
    @SerializedName("ancho_px")       val anchoPx: Int?,
    @SerializedName("alto_px")        val altoPx: Int?,
    @SerializedName("fecha_subida")   val fechaSubida: String?,
    @SerializedName("id_usuario")     val idUsuario: Int?
)

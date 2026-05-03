package com.movil.contrabajo.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuariosApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequestDto): Call<LoginResponseDto>

    @POST("api/auth/logout")
    fun logout(@Header("Authorization") authorization: String): Call<Map<String, String>>

    @GET("api/auth/validar-bd")
    fun validarSesion(@Query("token") token: String): Call<Boolean>

    @POST("api/usuarios/registrar")
    fun registrar(@Body request: UsuarioRegistroRequestDto): Call<UsuarioResponseDto>

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

    @POST("api/usuarios/recuperar/verificar-respuestas")
    fun verificarRespuestas(@Body request: RecuperacionPasswordRequestDto): Call<Map<String, String>>

    @POST("api/usuarios/recuperar/cambiar-password")
    fun cambiarPassword(@Body request: RecuperacionPasswordRequestDto): Call<Map<String, String>>
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
    val username: String?,
    val nombre: String?,
    val apellidos: String?,
    val correo: String?,
    val telefono: String?,
    val fechaNacimiento: String?,
    val perfil: String?,
    val verificado: Boolean?
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
    val idComuna: Int,
    val idCiudad: Int = 1
)

data class UsuarioUpdateRequestDto(
    val telefono: String? = null,
    val correo: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class RecuperacionPasswordRequestDto(
    val username: String,
    val respuesta1: String,
    val respuesta2: String,
    val nuevaPassword: String? = null
)

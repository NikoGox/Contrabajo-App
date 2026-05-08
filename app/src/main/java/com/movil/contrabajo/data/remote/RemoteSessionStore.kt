package com.movil.contrabajo.data.remote

import android.content.Context
import com.google.gson.Gson
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.Usuario
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RemoteSessionStore(context: Context) {
    private val preferencias = context.applicationContext.getSharedPreferences(
        NOMBRE_PREFERENCIAS,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun guardarSesion(token: String, usuario: Usuario) {
        preferencias.edit()
            .putString(CLAVE_TOKEN, token)
            .putString(CLAVE_USUARIO, gson.toJson(usuario))
            .apply()
    }

    fun obtenerToken(): String? = preferencias.getString(CLAVE_TOKEN, null)
        ?.takeIf { it.isNotBlank() }

    fun obtenerUsuario(): Usuario? {
        val json = preferencias.getString(CLAVE_USUARIO, null) ?: return null
        return runCatching { gson.fromJson(json, Usuario::class.java) }.getOrNull()
    }

    fun actualizarUsuario(usuario: Usuario) {
        val token = obtenerToken() ?: return
        guardarSesion(token, usuario)
    }

    fun guardarFotoPerfil(uriLocal: String): Usuario? {
        val usuario = obtenerUsuario()?.copy(fotoPerfilUrl = uriLocal) ?: return null
        actualizarUsuario(usuario)
        return usuario
    }

    fun limpiarSesion() {
        preferencias.edit().clear().apply()
    }

    companion object {
        private const val NOMBRE_PREFERENCIAS = "contrabajo_remote_session"
        private const val CLAVE_TOKEN = "jwt_token"
        private const val CLAVE_USUARIO = "usuario"

        fun usuarioDesdeDto(dto: UsuarioResponseDto, passwordTemporal: String = ""): Usuario {
            val apellidos = dto.apellidos.orEmpty().trim()
            val partesApellido = apellidos.split(" ").filter { it.isNotBlank() }
            val apellidoPaterno = partesApellido.firstOrNull().orEmpty()
            val apellidoMaterno = partesApellido.drop(1).joinToString(" ")
            val perfil = when (dto.perfil.orEmpty().uppercase()) {
                "MODERADOR" -> TipoPerfil.MODERADOR
                "TRABAJADOR" -> TipoPerfil.TRABAJADOR
                "PREMIUM" -> TipoPerfil.PREMIUM
                else -> TipoPerfil.USUARIO_BASE
            }

            return Usuario(
                idUsuario = dto.id?.toLong() ?: 0L,
                run = dto.run?.toString().orEmpty(),
                dv = dto.dv.orEmpty(),
                username = dto.username.orEmpty(),
                nombre = dto.nombre.orEmpty(),
                apellidoPaterno = apellidoPaterno,
                apellidoMaterno = apellidoMaterno,
                telefono = dto.telefono.orEmpty(),
                correo = dto.correo.orEmpty(),
                contrasenaHash = passwordTemporal,
                fechaRegistro = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                fechaNacimiento = dto.fechaNacimiento.orEmpty(),
                verificado = dto.verificado ?: false,
                tipoPerfil = perfil,
                rangoDisponibilidadM = dto.rangoDisponibilidadM ?: 20_000,
                rangoBusquedaM = dto.rangoBusquedaM ?: 20_000,
                direccionCalle = dto.direccion?.calle.orEmpty(),
                direccionNumero = dto.direccion?.numero.orEmpty(),
                direccionComuna = dto.direccion?.comuna?.nombre.orEmpty(),
                direccionRegion = dto.direccion?.comuna?.region.orEmpty().ifBlank { "Region Metropolitana" },
                direccionLatitud = dto.direccion?.latitud,
                direccionLongitud = dto.direccion?.longitud
            )
        }
    }
}

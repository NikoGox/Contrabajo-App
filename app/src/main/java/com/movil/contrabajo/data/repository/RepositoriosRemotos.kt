package com.movil.contrabajo.data.repository

import com.movil.contrabajo.data.remote.DireccionRegistroRequestDto
import com.movil.contrabajo.data.remote.LoginRequestDto
import com.movil.contrabajo.data.remote.RecuperacionPasswordRequestDto
import com.movil.contrabajo.data.remote.RecuperacionRegistroRequestDto
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.UsuarioRegistroRequestDto
import com.movil.contrabajo.data.remote.UsuarioUpdateRequestDto
import com.movil.contrabajo.data.remote.UsuariosApiService
import com.movil.contrabajo.data.remote.bearer
import com.movil.contrabajo.data.remote.ejecutarApi
import com.movil.contrabajo.domain.model.FiltroMarketplaceConfig
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.PreguntasSeguridadCatalogo
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario

class RepositorioAutenticacionRemoto(
    private val api: UsuariosApiService,
    private val sessionStore: RemoteSessionStore
) : RepositorioAutenticacion {

    override fun obtenerSesionActiva(): Usuario? {
        val token = sessionStore.obtenerToken() ?: return null
        val usuario = sessionStore.obtenerUsuario() ?: return null
        val tokenValido = ejecutarApi(api.validarSesion(token)).getOrDefault(false)
        return if (tokenValido) usuario else {
            sessionStore.limpiarSesion()
            null
        }
    }

    override fun iniciarSesion(
        identificador: String,
        contrasena: String,
        recordarme: Boolean
    ): Result<Usuario> {
        val username = identificador.trim()
        val password = contrasena.trim()
        if (username.isBlank()) return Result.failure(IllegalArgumentException("Ingresa tu nombre de usuario"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("Ingresa tu contrasena"))
        if (username.contains("@")) {
            return Result.failure(IllegalArgumentException("El login online usa nombre de usuario, no correo"))
        }

        return ejecutarApi(api.login(LoginRequestDto(username = username, password = password)))
            .mapCatching { response ->
                val token = response.token?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("El backend no devolvio token de sesion")
                val usuarioDto = response.usuario
                    ?: throw IllegalStateException("El backend no devolvio datos de usuario")
                val usuario = RemoteSessionStore.usuarioDesdeDto(usuarioDto, passwordTemporal = password)
                sessionStore.guardarSesion(token, usuario)
                usuario
            }
    }

    override fun registrarUsuario(registro: RegistroPendiente): Result<Usuario> {
        validarRegistroRemoto(registro)?.let { return Result.failure(IllegalArgumentException(it)) }
        val request = registro.toRegistroRequest()
            ?: return Result.failure(IllegalArgumentException("La comuna seleccionada aun no esta soportada por el backend"))

        return ejecutarApi(api.registrar(request)).fold(
            onSuccess = {
                iniciarSesion(
                    identificador = registro.username,
                    contrasena = registro.contrasena,
                    recordarme = true
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    override fun obtenerPreguntasRecuperacion(identificador: String): Result<List<PreguntaSeguridadConfig>> {
        val username = identificador.trim()
        if (username.isBlank()) return Result.failure(IllegalArgumentException("Ingresa tu nombre de usuario"))
        return Result.success(
            listOf(
                PreguntaSeguridadConfig(indice = 1, pregunta = "Pregunta 1", respuesta = ""),
                PreguntaSeguridadConfig(indice = 2, pregunta = "Pregunta 2", respuesta = "")
            )
        )
    }

    override fun validarRespuestasRecuperacion(
        identificador: String,
        respuesta1: String,
        respuesta2: String
    ): Result<Unit> {
        val request = RecuperacionPasswordRequestDto(
            username = identificador.trim(),
            respuesta1 = respuesta1.trim(),
            respuesta2 = respuesta2.trim()
        )
        return ejecutarApi(api.verificarRespuestas(request)).map { Unit }
    }

    override fun restablecerContrasenaRecuperacion(
        identificador: String,
        respuesta1: String,
        respuesta2: String,
        nuevaContrasena: String,
        confirmarContrasena: String
    ): Result<Unit> {
        val nueva = nuevaContrasena.trim()
        if (nueva.length < 6) return Result.failure(IllegalArgumentException("La contrasena debe tener al menos 6 caracteres"))
        if (nueva != confirmarContrasena.trim()) return Result.failure(IllegalArgumentException("Las contrasenas no coinciden"))
        val request = RecuperacionPasswordRequestDto(
            username = identificador.trim(),
            respuesta1 = respuesta1.trim(),
            respuesta2 = respuesta2.trim(),
            nuevaPassword = nueva
        )
        return ejecutarApi(api.cambiarPassword(request)).map {
            sessionStore.limpiarSesion()
            Unit
        }
    }

    override fun cerrarSesion() {
        val token = sessionStore.obtenerToken()
        if (token != null) {
            ejecutarApi(api.logout(bearer(token)))
        }
        sessionStore.limpiarSesion()
    }

    private fun validarRegistroRemoto(registro: RegistroPendiente): String? {
        return when {
            registro.nombre.trim().isBlank() -> "Ingresa tu nombre"
            registro.apellidoPaterno.trim().isBlank() -> "Ingresa tu apellido paterno"
            registro.run.filter { it.isDigit() }.length !in 7..8 -> "El RUN debe tener 7 u 8 digitos"
            registro.dv.trim().isBlank() -> "Ingresa el DV"
            registro.username.trim().isBlank() -> "Ingresa un nombre de usuario"
            registro.username.trim().length > 20 -> "El nombre de usuario debe tener maximo 20 caracteres"
            registro.username.contains("@") -> "El nombre de usuario online no debe ser un correo"
            registro.correo.trim().isBlank() || !registro.correo.contains("@") -> "Ingresa un correo valido"
            registro.telefono.normalizarTelefonoBackend().length != 8 ->
                "Ingresa los 8 digitos restantes del celular"
            registro.fechaNacimiento.trim().isBlank() -> "Ingresa tu fecha de nacimiento"
            registro.contrasena.length < 8 -> "La contrasena debe tener al menos 8 caracteres"
            registro.contrasena.none { it.isUpperCase() } -> "La contrasena debe incluir al menos 1 mayuscula"
            registro.contrasena.none { it.isDigit() } -> "La contrasena debe incluir al menos 1 numero"
            registro.contrasena.none { !it.isLetterOrDigit() } -> "La contrasena debe incluir al menos 1 simbolo"
            registro.contrasena != registro.confirmarContrasena -> "Las contrasenas no coinciden"
            !PreguntasSeguridadCatalogo.esValida(registro.preguntaSeguridad1) -> "Selecciona una pregunta de seguridad valida (1)"
            !PreguntasSeguridadCatalogo.esValida(registro.preguntaSeguridad2) -> "Selecciona una pregunta de seguridad valida (2)"
            registro.preguntaSeguridad1.trim().equals(registro.preguntaSeguridad2.trim(), ignoreCase = true) ->
                "Debes seleccionar dos preguntas de seguridad diferentes"
            registro.respuestaSeguridad1.trim().isBlank() || registro.respuestaSeguridad2.trim().isBlank() ->
                "Debes responder ambas preguntas de seguridad"
            else -> null
        }
    }
}

class RepositorioPerfilRemoto(
    private val api: UsuariosApiService,
    private val sessionStore: RemoteSessionStore,
    private val localFallback: RepositorioPerfil
) : RepositorioPerfil {

    override fun obtenerPerfilActual(): Usuario? {
        return sessionStore.obtenerUsuario()
    }

    override fun solicitarVerificacionTrabajador(run: String, dv: String, numeroDocumento: String): Result<Usuario> =
        localFallback.solicitarVerificacionTrabajador(run, dv, numeroDocumento)

    override fun obtenerPreguntasSeguridad(): List<PreguntaSeguridadConfig> = localFallback.obtenerPreguntasSeguridad()

    override fun guardarPreguntaSeguridad(
        indice: Int,
        pregunta: String,
        respuesta: String
    ): Result<List<PreguntaSeguridadConfig>> = localFallback.guardarPreguntaSeguridad(indice, pregunta, respuesta)

    override fun obtenerUbicacionAjustes(): UbicacionAjustesConfig = localFallback.obtenerUbicacionAjustes()

    override fun guardarUbicacionAjustes(config: UbicacionAjustesConfig): Result<UbicacionAjustesConfig> {
        val token = sessionStore.obtenerToken()
        val usuario = sessionStore.obtenerUsuario()
        if (token != null && usuario != null && (config.latitud != null || config.longitud != null)) {
            ejecutarApi(
                api.actualizarUsuario(
                    authorization = bearer(token),
                    id = usuario.idUsuario.toInt(),
                    request = UsuarioUpdateRequestDto(latitud = config.latitud, longitud = config.longitud)
                )
            ).onSuccess {
                refrescarUsuarioDesdeBackend(token, usuario).onFailure { return Result.failure(it) }
            }.onFailure {
                return Result.failure(it)
            }
        }
        return localFallback.guardarUbicacionAjustes(config)
    }

    override fun obtenerFiltrosMarketplace(): FiltroMarketplaceConfig = localFallback.obtenerFiltrosMarketplace()

    override fun guardarFiltrosMarketplace(config: FiltroMarketplaceConfig): Result<FiltroMarketplaceConfig> =
        localFallback.guardarFiltrosMarketplace(config)

    override fun limpiarFiltrosMarketplace(): Result<FiltroMarketplaceConfig> = localFallback.limpiarFiltrosMarketplace()

    override fun actualizarFotoPerfil(uriLocal: String): Result<Usuario> {
        if (uriLocal.isBlank()) return Result.failure(IllegalArgumentException("Selecciona una foto valida"))
        val usuario = sessionStore.guardarFotoPerfil(uriLocal)
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return Result.success(usuario)
    }

    override fun actualizarContactoPerfil(correo: String, telefono: String): Result<Usuario> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay token de sesion activo"))
        val usuario = sessionStore.obtenerUsuario()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val correoNormalizado = correo.trim().lowercase()
        val telefonoNormalizado = telefono.normalizarTelefonoBackend()
        if (correoNormalizado.isBlank() || !correoNormalizado.contains("@")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo valido"))
        }
        if (telefonoNormalizado.length != 8) {
            return Result.failure(IllegalArgumentException("Ingresa los 8 digitos restantes del celular"))
        }

        return ejecutarApi(
            api.actualizarUsuario(
                authorization = bearer(token),
                id = usuario.idUsuario.toInt(),
                request = UsuarioUpdateRequestDto(
                    telefono = telefonoNormalizado,
                    correo = correoNormalizado
                )
            )
        ).fold(
            onSuccess = {
                refrescarUsuarioDesdeBackend(token, usuario)
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    private fun refrescarUsuarioDesdeBackend(token: String, usuarioActual: Usuario): Result<Usuario> {
        return ejecutarApi(api.buscarUsuario(bearer(token), usuarioActual.idUsuario.toInt()))
            .mapCatching {
                val actualizado = RemoteSessionStore.usuarioDesdeDto(it, passwordTemporal = usuarioActual.contrasenaHash)
                    .copy(fotoPerfilUrl = usuarioActual.fotoPerfilUrl)
                sessionStore.actualizarUsuario(actualizado)
                actualizado
            }
    }
}

private fun RegistroPendiente.toRegistroRequest(): UsuarioRegistroRequestDto? {
    val idComuna = idComunaBackend(comuna) ?: return null
    val telefonoNormalizado = telefono.normalizarTelefonoBackend()
    return UsuarioRegistroRequestDto(
        run = run.filter { it.isDigit() }.toInt(),
        dv = dv.trim().uppercase(),
        username = username.trim(),
        nombre = nombre.trim(),
        apellidoPaterno = apellidoPaterno.trim(),
        apellidoMaterno = apellidoMaterno.trim(),
        telefono = telefonoNormalizado,
        correo = correo.trim().lowercase(),
        password = contrasena,
        fechaNacimiento = fechaNacimiento.trim(),
        recuperacion = RecuperacionRegistroRequestDto(
            pregunta1 = preguntaSeguridad1.trim(),
            respuesta1 = respuestaSeguridad1.trim(),
            pregunta2 = preguntaSeguridad2.trim(),
            respuesta2 = respuestaSeguridad2.trim()
        ),
        direccion = DireccionRegistroRequestDto(
            calle = calle.trim().ifBlank { "Sin calle" },
            numero = numeroDireccion.trim().ifBlank { "Sin numero" },
            idComuna = idComuna
        )
    )
}

private fun idComunaBackend(comuna: String): Int? {
    return when (normalizarComuna(comuna)) {
        "lampa" -> 1
        "batuco" -> 2
        "colina" -> 3
        "quilicura" -> 4
        "santiago" -> 5
        else -> null
    }
}

private fun normalizarComuna(comuna: String): String {
    return java.text.Normalizer.normalize(comuna.trim(), java.text.Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase()
}

private fun String.normalizarTelefonoBackend(): String {
    return filter { it.isDigit() }
        .let { if (it.startsWith("56")) it.drop(2) else it }
        .let { if (it.length == 9 && it.startsWith("9")) it.drop(1) else it }
        .take(8)
}

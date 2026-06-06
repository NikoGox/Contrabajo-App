package com.movil.contrabajo.data.repository

import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import com.movil.contrabajo.data.remote.FotosApiClient
import com.movil.contrabajo.data.remote.CloudinaryApiClient
import com.movil.contrabajo.data.remote.FotoPerfilRequestDto
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.movil.contrabajo.data.remote.DireccionRegistroRequestDto
import com.movil.contrabajo.data.remote.LoginRequestDto
import com.movil.contrabajo.data.remote.OcrSimuladoRequestDto
import com.movil.contrabajo.data.remote.PreguntaSeguridadUpdateRequestDto
import com.movil.contrabajo.data.remote.PreguntasSeguridadDto
import com.movil.contrabajo.data.remote.RecuperacionPasswordRequestDto
import com.movil.contrabajo.data.remote.RecuperacionRegistroRequestDto
import com.movil.contrabajo.data.remote.ChatDto
import com.movil.contrabajo.data.remote.ChatIniciarRequestDto
import com.movil.contrabajo.data.remote.CitaServicioDto
import com.movil.contrabajo.data.remote.ComunicacionesApiService
import com.movil.contrabajo.data.remote.CrearReporteRequestDto
import com.movil.contrabajo.data.remote.MensajeChatDto
import com.movil.contrabajo.data.remote.MensajeChatEnviarDto
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.FotoOfertaResponseDto
import com.movil.contrabajo.data.remote.FotoPerfilResponseDto
import com.movil.contrabajo.data.remote.ReporteResponseDto
import com.movil.contrabajo.data.remote.RevisarReporteRequestDto
import com.movil.contrabajo.data.remote.ServiciosApiService
import com.movil.contrabajo.data.remote.SolicitarCitaRequestDto
import com.movil.contrabajo.data.remote.OfertaServicioDto
import com.movil.contrabajo.data.remote.ValoracionServicioDto
import com.movil.contrabajo.data.remote.OfertaServicioRequestDto
import com.movil.contrabajo.data.remote.OfertaServicioUpdateRequestDto
import com.movil.contrabajo.data.remote.TipoReporteDto
import com.movil.contrabajo.data.remote.UsuarioRegistroRequestDto
import com.movil.contrabajo.data.remote.UsuarioUpdateRequestDto
import com.movil.contrabajo.data.remote.UsuariosApiService
import com.movil.contrabajo.data.remote.ValoracionRequestDto
import com.movil.contrabajo.data.remote.VincularCitaRequestDto
import com.movil.contrabajo.data.remote.bearer
import com.movil.contrabajo.data.remote.ejecutarApi
import com.movil.contrabajo.data.remote.ejecutarApiComunicaciones
import com.movil.contrabajo.data.remote.ejecutarApiServicios
import com.movil.contrabajo.domain.model.ComunaCatalogo
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.FotoOferta
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.EstadoCodigo
import com.movil.contrabajo.domain.model.FiltroMarketplaceConfig
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.PreguntasSeguridadCatalogo
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.Reporte
import com.movil.contrabajo.domain.model.TipoReporte
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.domain.model.Valoracion
import com.movil.contrabajo.domain.model.ValoracionesServicio
import java.math.BigDecimal
import retrofit2.Call

class RepositorioAutenticacionRemoto(
    private val api: UsuariosApiService,
    private val sessionStore: RemoteSessionStore
) : RepositorioAutenticacion {

    override fun obtenerSesionActiva(): Usuario? {
        val token = sessionStore.obtenerToken() ?: return null
        val usuario = sessionStore.obtenerUsuario() ?: return null
        val validacion = ejecutarApi(api.validarSesion(token))
        return when {
            validacion.getOrNull() == true -> usuario
            validacion.isSuccess && validacion.getOrNull() == false -> {
                // Token invalido segun el backend → limpiar y retornar nulo
                sessionStore.limpiarSesion()
                null
            }
            else -> {
                // Error de red (backend caido, timeout, etc.) → propagar para mostrar pantalla de error
                throw validacion.exceptionOrNull()
                    ?: RuntimeException("No se pudo contactar con el servidor")
            }
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
                val idUsuario = usuarioDto.id ?: throw IllegalStateException("El backend devolvio un ID nulo")

                // 1. Mapeamos los datos del usuario que devolvió el login
                val usuarioMapeado = RemoteSessionStore.usuarioDesdeDto(usuarioDto, passwordTemporal = password)

                // 2. Jugada Maestra: Vamos a buscar de inmediato la foto usando el endpoint exclusivo
                val linkFoto = ejecutarApi(api.obtenerFotoPerfil(idUsuario))
                    .map { it.enlace?.takeIf { it.isNotBlank() }?.normalizarEnlaceEmulador().orEmpty() }
                    .getOrDefault("") // Si no tiene foto o falla, dejamos un string vacío

                // 3. Unimos los dos mundos en el objeto final
                val usuario = usuarioMapeado.copy(
                    baneoFechaFin = response.baneoActivo?.fechaFin,
                    fotoPerfilUrl = linkFoto.ifBlank { usuarioMapeado.fotoPerfilUrl }
                )

                sessionStore.guardarSesion(token, usuario)
                usuario
            }
    }

    override fun obtenerComunas(): Result<List<ComunaCatalogo>> {
        return ejecutarApi(api.listarComunas()).map { comunas ->
            comunas.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val nombre = dto.nombre?.trim().orEmpty()
                if (nombre.isBlank()) return@mapNotNull null
                ComunaCatalogo(
                    id = if (normalizarTexto(nombre) == "sin comuna") 1 else id,
                    nombre = nombre,
                    idRegion = dto.idRegion,
                    region = dto.region.orEmpty()
                )
            }.sortedWith(
                compareBy<ComunaCatalogo> { if (normalizarTexto(it.nombre) == "sin comuna") 0 else 1 }
                    .thenBy { normalizarTexto(it.nombre) }
            )
        }
    }

    override fun verificarRunDisponible(run: String): Result<Boolean> {
        val runNormalizado = run.filter { it.isDigit() }
        if (runNormalizado.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa un RUN valido"))
        }
        return ejecutarApi(api.runDisponible(runNormalizado.toInt()))
    }

    override fun verificarUsernameDisponible(username: String): Result<Boolean> {
        val valor = username.trim()
        if (valor.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa un nombre de usuario"))
        }
        return ejecutarApi(api.usernameDisponible(valor))
    }

    override fun verificarCorreoDisponible(correo: String): Result<Boolean> {
        val valor = correo.trim()
        if (valor.isBlank() || !valor.contains("@")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo valido"))
        }
        return ejecutarApi(api.correoDisponible(valor.lowercase()))
    }

    override fun registrarUsuario(registro: RegistroPendiente): Result<Usuario> {
        validarRegistroRemoto(registro)?.let { return Result.failure(IllegalArgumentException(it)) }
        validarDisponibilidadRegistroRemoto(registro)?.let { return Result.failure(IllegalArgumentException(it)) }
        val request = registro.toRegistroRequest()

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
        return ejecutarApi(api.obtenerPreguntasRecuperacion(username)).map { it.toPreguntasConfig() }
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
        validarContrasenaSegura(nueva)?.let { return Result.failure(IllegalArgumentException(it)) }
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
            registro.nombre.trim().length > 60 -> "El nombre permite hasta 60 caracteres"
            registro.apellidoPaterno.trim().isBlank() -> "Ingresa tu apellido paterno"
            registro.apellidoPaterno.trim().length > 60 -> "El apellido paterno permite hasta 60 caracteres"
            registro.apellidoMaterno.trim().length > 60 -> "El apellido materno permite hasta 60 caracteres"
            registro.run.filter { it.isDigit() }.length !in 7..8 -> "El RUN debe tener 7 u 8 digitos"
            registro.dv.trim().isBlank() -> "Ingresa el DV"
            registro.username.trim().isBlank() -> "Ingresa un nombre de usuario"
            registro.username.trim().length > 20 -> "El nombre de usuario debe tener maximo 20 caracteres"
            registro.username.contains("@") -> "El nombre de usuario online no debe ser un correo"
            registro.correo.trim().isBlank() || !registro.correo.contains("@") -> "Ingresa un correo valido"
            registro.correo.trim().length > 254 -> "El correo permite hasta 254 caracteres"
            registro.telefono.normalizarTelefonoBackend().length != 8 ->
                "Ingresa los 8 digitos restantes del celular"
            registro.fechaNacimiento.trim().isBlank() -> "Ingresa tu fecha de nacimiento"
            registro.calle.trim().length > 120 -> "La calle permite hasta 120 caracteres"
            registro.numeroDireccion.trim().length > 20 -> "El numero de direccion permite hasta 20 caracteres"
            registro.preguntaSeguridad1.trim().length > 200 -> "La pregunta de seguridad 1 permite hasta 200 caracteres"
            registro.preguntaSeguridad2.trim().length > 200 -> "La pregunta de seguridad 2 permite hasta 200 caracteres"
            registro.respuestaSeguridad1.trim().length > 200 -> "La respuesta de seguridad 1 permite hasta 200 caracteres"
            registro.respuestaSeguridad2.trim().length > 200 -> "La respuesta de seguridad 2 permite hasta 200 caracteres"
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

    private fun validarDisponibilidadRegistroRemoto(registro: RegistroPendiente): String? {
        val runDisponible = verificarRunDisponible(registro.run)
            .getOrElse { return it.message ?: "No se pudo validar el RUN" }
        if (!runDisponible) return "El RUN ya existe."

        val usernameDisponible = verificarUsernameDisponible(registro.username)
            .getOrElse { return it.message ?: "No se pudo validar el nombre de usuario" }
        if (!usernameDisponible) return "El nombre de usuario ya existe."

        val correoDisponible = verificarCorreoDisponible(registro.correo)
            .getOrElse { return it.message ?: "No se pudo validar el correo" }
        if (!correoDisponible) return "El correo ya existe."

        return null
    }
}

class RepositorioPerfilRemoto(
    private val api: UsuariosApiService,
    private val sessionStore: RemoteSessionStore,
    private val context: Context
) : RepositorioPerfil {

    override fun obtenerPerfilActual(): Usuario? {
        val token = sessionStore.obtenerToken() ?: return null
        val usuario = sessionStore.obtenerUsuario() ?: return null
        return refrescarUsuarioDesdeBackend(token, usuario).getOrElse { usuario }
    }

    override fun solicitarVerificacionTrabajador(run: String, dv: String, numeroDocumento: String): Result<Usuario> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay token de sesion activo"))
        val usuario = sessionStore.obtenerUsuario()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val runNormalizado = run.filter { it.isDigit() }
        val dvNormalizado = dv.trim().uppercase()
        val numeroDocumentoNormalizado = numeroDocumento.filter { it.isDigit() }
        if (runNormalizado.length !in 7..8) {
            return Result.failure(IllegalArgumentException("El RUN debe tener 7 u 8 digitos"))
        }
        if (dvNormalizado.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa el DV del RUN"))
        }
        if (numeroDocumentoNormalizado.length != 9) {
            return Result.failure(IllegalArgumentException("El numero de documento debe tener 9 digitos"))
        }

        return ejecutarApi(
            api.verificarOcr(
                authorization = bearer(token),
                id = usuario.idUsuario.toInt(),
                request = OcrSimuladoRequestDto(
                    rutOcr = runNormalizado.toInt(),
                    dvOcr = dvNormalizado,
                    numeroDocumento = numeroDocumentoNormalizado
                )
            )
        ).mapCatching {
            val actualizado = RemoteSessionStore.usuarioDesdeDto(it, passwordTemporal = usuario.contrasenaHash)
                .copy(fotoPerfilUrl = usuario.fotoPerfilUrl)
            sessionStore.actualizarUsuario(actualizado)
            actualizado
        }
    }

    override fun obtenerPreguntasSeguridad(): List<PreguntaSeguridadConfig> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        val usuario = sessionStore.obtenerUsuario() ?: return emptyList()
        val principal = ejecutarApi(api.obtenerPreguntasSeguridadPerfilActual(bearer(token)))
            .map { it.toPreguntasConfig() }
        if (principal.isSuccess) return principal.getOrDefault(emptyList())
        return ejecutarApi(api.obtenerPreguntasRecuperacion(usuario.username))
            .map { it.toPreguntasConfig() }
            .getOrDefault(emptyList())
    }

    override fun guardarPreguntaSeguridad(
        indice: Int,
        pregunta: String,
        respuesta: String
    ): Result<List<PreguntaSeguridadConfig>> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay token de sesion activo"))
        val usuario = sessionStore.obtenerUsuario()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))

        val parcialPerfil = ejecutarApi(
            api.actualizarPreguntaSeguridadParcialPerfilActual(
                authorization = bearer(token),
                indice = indice,
                request = PreguntaSeguridadUpdateRequestDto(
                    pregunta = pregunta.trim(),
                    respuesta = respuesta.trim()
                )
            )
        )
        if (parcialPerfil.isSuccess) return parcialPerfil.map { it.toPreguntasConfig() }

        val parcialPorId = ejecutarApi(
            api.actualizarPreguntaSeguridadParcial(
                authorization = bearer(token),
                id = usuario.idUsuario.toInt(),
                indice = indice,
                request = PreguntaSeguridadUpdateRequestDto(
                    pregunta = pregunta.trim(),
                    respuesta = respuesta.trim()
                )
            )
        )
        if (parcialPorId.isSuccess) return parcialPorId.map { it.toPreguntasConfig() }

        val actuales = obtenerPreguntasSeguridad()
        val pregunta1 = if (indice == 1) pregunta.trim() else actuales.firstOrNull { it.indice == 1 }?.pregunta.orEmpty()
        val pregunta2 = if (indice == 2) pregunta.trim() else actuales.firstOrNull { it.indice == 2 }?.pregunta.orEmpty()
        return ejecutarApi(
            api.actualizarPreguntasSeguridad(
                authorization = bearer(token),
                id = usuario.idUsuario.toInt(),
                request = RecuperacionRegistroRequestDto(
                    pregunta1 = pregunta1,
                    respuesta1 = if (indice == 1) respuesta.trim() else "",
                    pregunta2 = pregunta2,
                    respuesta2 = if (indice == 2) respuesta.trim() else ""
                )
            )
        ).map { it.toPreguntasConfig() }
    }

    override fun obtenerUbicacionAjustes(): UbicacionAjustesConfig {
        val token = sessionStore.obtenerToken()
        val usuario = sessionStore.obtenerUsuario()
        if (token != null && usuario != null) {
            val remoto = ejecutarApi(api.buscarUsuario(bearer(token), usuario.idUsuario.toInt())).getOrNull()
            if (remoto != null) {
                val rangoDisponibilidadBackend = remoto.rangoDisponibilidadM ?: 20_000
                val rangoBusquedaBackend = remoto.rangoBusquedaM ?: 20_000
                val comuna = remoto.direccion?.comuna?.nombre.orEmpty().ifBlank { "Sin comuna" }
                val region = remoto.direccion?.comuna?.region.orEmpty().ifBlank { "Region Metropolitana" }
                return UbicacionAjustesConfig(
                    region = region,
                    comuna = comuna,
                    calle = remoto.direccion?.calle.orEmpty(),
                    numero = remoto.direccion?.numero.orEmpty(),
                    detalle = "",
                    latitud = remoto.direccion?.latitud,
                    longitud = remoto.direccion?.longitud,
                    rangoDisponibilidadM = rangoDisponibilidadBackend,
                    rangoBusquedaM = rangoBusquedaBackend
                )
            }
        }
        if (usuario != null) {
            return UbicacionAjustesConfig(
                region = usuario.direccionRegion.ifBlank { "Region Metropolitana" },
                comuna = usuario.direccionComuna.ifBlank { "Sin comuna" },
                calle = usuario.direccionCalle.ifBlank { "Sin calle" },
                numero = usuario.direccionNumero.ifBlank { "Sin numero" },
                detalle = "",
                latitud = usuario.direccionLatitud,
                longitud = usuario.direccionLongitud,
                rangoDisponibilidadM = usuario.rangoDisponibilidadM,
                rangoBusquedaM = usuario.rangoBusquedaM
            )
        }
        return UbicacionAjustesConfig(
            region = "Region Metropolitana",
            comuna = "Sin comuna",
            calle = "Sin calle",
            numero = "Sin numero"
        )
    }

    override fun guardarUbicacionAjustes(config: UbicacionAjustesConfig): Result<UbicacionAjustesConfig> {
        val token = sessionStore.obtenerToken()
        val usuario = sessionStore.obtenerUsuario()
        if (token != null && usuario != null) {
            val idComuna = if (normalizarTexto(config.comuna) == "sin comuna") {
                1
            } else {
                ejecutarApi(api.listarComunas()).getOrNull()
                    ?.firstOrNull { normalizarTexto(it.nombre.orEmpty()) == normalizarTexto(config.comuna) }
                    ?.id
            }
            ejecutarApi(
                api.actualizarUsuario(
                    authorization = bearer(token),
                    id = usuario.idUsuario.toInt(),
                    request = UsuarioUpdateRequestDto(
                        latitud = config.latitud,
                        longitud = config.longitud,
                        calle = config.calle,
                        numero = config.numero,
                        idComuna = idComuna,
                        rangoDisponibilidadM = config.rangoDisponibilidadM,
                        rangoBusquedaM = config.rangoBusquedaM
                    )
                )
            ).onSuccess {
                refrescarUsuarioDesdeBackend(token, usuario).onFailure { return Result.failure(it) }
            }.onFailure {
                return Result.failure(it)
            }
            return Result.success(obtenerUbicacionAjustes())
        }
        return Result.failure(IllegalStateException("No hay sesion activa"))
    }

    override fun obtenerFiltrosMarketplace(): FiltroMarketplaceConfig = FiltroMarketplaceConfig()

    override fun guardarFiltrosMarketplace(config: FiltroMarketplaceConfig): Result<FiltroMarketplaceConfig> =
        Result.success(config)

    override fun limpiarFiltrosMarketplace(): Result<FiltroMarketplaceConfig> = Result.success(FiltroMarketplaceConfig())

    override fun actualizarFotoPerfil(uriLocal: String): Result<Usuario> {
        if (uriLocal.isBlank()) return Result.failure(IllegalArgumentException("Selecciona una foto valida"))
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay token de sesion activo"))
        val usuario = sessionStore.obtenerUsuario()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val part = runCatching { uriToMultipartPart(context, uriLocal, "file") }
            .getOrElse { return Result.failure(IllegalStateException("No se pudo leer la imagen seleccionada")) }

        return runCatching {
            // ========================================================
            // PASO 1: Pedir llaves a tu API de Fotos (Puerto 8084)
            // ========================================================
            val firma = ejecutarApi(FotosApiClient.api.obtenerFirmaCloudinary(bearer(token))).getOrThrow()

            val apiKeyReq = firma.apiKey.toRequestBody("text/plain".toMediaType())
            val timestampReq = firma.timestamp.toRequestBody("text/plain".toMediaType())
            val signatureReq = firma.signature.toRequestBody("text/plain".toMediaType())

            // ========================================================
            // PASO 2: Disparo directo a Cloudinary
            // ========================================================
            val cloudinaryRes = ejecutarApi(
                CloudinaryApiClient.api.subirImagen(
                    file = part,
                    apiKey = apiKeyReq,
                    timestamp = timestampReq,
                    signature = signatureReq
                )
            ).getOrThrow()

            val secureUrl = cloudinaryRes.secureUrl

            // ========================================================
            // PASO 3: Guardar el enlace HTTPS en tu API de Usuarios
            // ========================================================
            val requestDto = FotoPerfilRequestDto(url = secureUrl)

            // OJO AQUÍ: Llamamos a guardarFotoPerfil (que ahora solo recibe el JSON)
            val dtoBackend = ejecutarApi(
                api.guardarFotoPerfil(bearer(token), requestDto)
            ).getOrThrow()

            // Actualizamos la sesión local con el enlace devuelto
            val enlaceFinal = dtoBackend.enlace?.takeIf { it.isNotBlank() }?.normalizarEnlaceEmulador() ?: secureUrl
            val actualizado = usuario.copy(fotoPerfilUrl = enlaceFinal)
            sessionStore.actualizarUsuario(actualizado)

            actualizado
        }
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
        if (correoNormalizado.length > 254) {
            return Result.failure(IllegalArgumentException("El correo permite hasta 254 caracteres"))
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
                    correo = correoNormalizado,
                    rangoBusquedaM = usuario.rangoBusquedaM
                )
            )
        ).fold(
            onSuccess = {
                refrescarUsuarioDesdeBackend(token, usuario)
            },
            onFailure = {
                val mensaje = it.message.orEmpty()
                if (mensaje.contains("(403)") || mensaje.contains("forbidden", ignoreCase = true)) {
                    sessionStore.limpiarSesion()
                    Result.failure(
                        IllegalStateException(
                            "Tu sesion ya no es valida para editar perfil. Inicia sesion de nuevo."
                        )
                    )
                } else if (
                    mensaje.contains("truncat", ignoreCase = true) ||
                    mensaje.contains("String or binary data would be truncated", ignoreCase = true) ||
                    mensaje.contains("SQL", ignoreCase = true)
                ) {
                    Result.failure(
                        IllegalArgumentException(
                            "Uno de los campos supera el limite permitido. Reduce el texto y vuelve a intentar."
                        )
                    )
                } else {
                    Result.failure(it)
                }
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

class RepositorioOfertasRemoto(
    private val api: ServiciosApiService,
    private val sessionStore: RemoteSessionStore,
    private val context: Context
) : RepositorioOfertas {

    override fun obtenerOfertaPrincipal(): OfertaServicio? =
        obtenerOfertasMarketplace().firstOrNull { it.disponible }

    override fun obtenerOfertasMarketplace(busqueda: String): List<OfertaServicio> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        val ofertasBaseDto = ejecutarApiServicios(api.listarOfertas(bearer(token))).getOrDefault(emptyList())

        // 1. Primero limpiamos la lista usando el DTO para quedarnos solo con lo que se va a mostrar
        val ofertasFiltradasDto = ofertasBaseDto.filter { dto ->
            val eliminado = dto.borrado ?: false
            val disponible = dto.disponible ?: false
            val filtro = busqueda.trim()

            (!eliminado && disponible) && (
                    filtro.isBlank() ||
                            (dto.titulo.orEmpty().contains(filtro, ignoreCase = true)) ||
                            (dto.descripcion.orEmpty().contains(filtro, ignoreCase = true)) ||
                            (dto.categoria.orEmpty().contains(filtro, ignoreCase = true))
                    )
        }

        // 2. Ahora sí, hacemos las llamadas HTTP solo para los elementos que realmente se van a pintar
        val ofertasBase = ofertasFiltradasDto.map { dto ->
            val fotoUrl = ejecutarApiServicios(api.listarFotosOferta(bearer(token), dto.id ?: 0))
                .getOrNull()?.firstOrNull()?.enlace.orEmpty()
            dto.toOfertaServicio(fotoUrl)
        }

        return enriquecerPuntuacionPromedioPorServicio(ofertasBase, token)
    }

    override fun obtenerOfertaPorId(idOfertaServicio: Long, incluirEliminadas: Boolean): OfertaServicio? {
        val token = sessionStore.obtenerToken() ?: return null
        val dto = ejecutarApiServicios(api.buscarOferta(bearer(token), idOfertaServicio.toInt())).getOrNull() ?: return null

        // Buscamos su foto correspondiente
        val fotoUrl = ejecutarApiServicios(api.listarFotosOferta(bearer(token), dto.id ?: 0))
            .getOrNull()?.firstOrNull()?.enlace.orEmpty()

        val oferta = dto.toOfertaServicio(fotoUrl).takeIf { incluirEliminadas || !it.eliminada } ?: return null
        return enriquecerPuntuacionPromedioPorServicio(listOf(oferta), token).firstOrNull()
    }

    override fun obtenerOfertasPropias(): List<OfertaServicio> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        val usuario = sessionStore.obtenerUsuario() ?: return emptyList()
        val ofertasBaseDto = ejecutarApiServicios(api.listarOfertasTrabajador(bearer(token), usuario.idUsuario.toInt()))
            .getOrDefault(emptyList())

        val ofertasBase = ofertasBaseDto.map { dto ->
            // Buscamos su foto correspondiente para la pantalla de Perfil
            val fotoUrl = ejecutarApiServicios(api.listarFotosOferta(bearer(token), dto.id ?: 0))
                .getOrNull()?.firstOrNull()?.enlace.orEmpty()
            dto.toOfertaServicio(fotoUrl)
        }.filter { !it.eliminada }
        return enriquecerPuntuacionPromedioPorServicio(ofertasBase, token)
    }

    override fun obtenerOfertaPropiaActual(): OfertaServicio? = obtenerOfertasPropias().firstOrNull()

    override fun obtenerOfertaPropiaPorId(idOfertaServicio: Long): OfertaServicio? =
        obtenerOfertasPropias().firstOrNull { it.idOfertaServicio == idOfertaServicio }

    private fun enriquecerPuntuacionPromedioPorServicio(
        ofertas: List<OfertaServicio>,
        token: String
    ): List<OfertaServicio> {
        if (ofertas.isEmpty()) return ofertas
        val cacheValoracionesPorTrabajador = mutableMapOf<Long, List<ValoracionServicioDto>>()
        val promedioPorOferta = mutableMapOf<Long, Double>()

        ofertas.forEach { oferta ->
            val valoracionesTrabajador = cacheValoracionesPorTrabajador.getOrPut(oferta.idTrabajador) {
                ejecutarApiServicios(
                    api.obtenerValoracionesTrabajador(bearer(token), oferta.idTrabajador.toInt())
                ).getOrDefault(emptyList())
            }
            val votosOferta = valoracionesTrabajador.mapNotNull { dto ->
                val idOfertaDto = dto.idOfertaServicio?.toLong() ?: return@mapNotNull null
                if (idOfertaDto == oferta.idOfertaServicio) (dto.voto ?: 0).coerceIn(1, 5).toDouble() else null
            }
            promedioPorOferta[oferta.idOfertaServicio] =
                if (votosOferta.isEmpty()) 0.0 else votosOferta.average()
        }

        return ofertas.map { oferta ->
            // 🌟 CORRECCIÓN MAESTRA: Forzamos a que la copia final mantenga intacto el 'fotoUrlReferencia' que recuperamos antes
            oferta.copy(
                puntuacionPromedio = promedioPorOferta[oferta.idOfertaServicio] ?: 0.0,
                fotoUrlReferencia = oferta.fotoUrlReferencia // 👈 ¡Garantizamos que la foto no se borre aquí!
            )
        }
    }

    override fun obtenerDisponibilidadOfertaPropia(idOfertaServicio: Long): Result<Boolean> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return ejecutarApiServicios(
            api.obtenerDisponibilidadOferta(bearer(token), idOfertaServicio.toInt())
        )
    }

    override fun obtenerIdsOfertasConTrabajoEnCursoPropias(): Set<Long> = emptySet()

    override fun obtenerValoracionesPropiasPorServicio(): List<ValoracionesServicio> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        val usuario = sessionStore.obtenerUsuario() ?: return emptyList()

        val valoracionesDto = ejecutarApiServicios(
            api.obtenerValoracionesTrabajador(bearer(token), usuario.idUsuario.toInt())
        ).getOrDefault(emptyList())

        val valoraciones = valoracionesDto.mapNotNull { dto ->
            val idOferta = dto.idOfertaServicio?.toLong() ?: return@mapNotNull null
            Valoracion(
                voto = (dto.voto ?: 0).coerceIn(1, 5),
                fechaVoto = dto.fechaVoto.orEmpty(),
                comentario = dto.comentario.orEmpty(),
                idTrabajador = (dto.idTrabajador ?: usuario.idUsuario.toInt()).toLong(),
                idCliente = (dto.idCliente ?: 0).toLong(),
                idChatCita = 0L,
                idOfertaServicio = idOferta
            )
        }

        if (valoraciones.isEmpty()) return emptyList()

        val ofertasPorId = mutableMapOf<Long, OfertaServicio>()
        obtenerOfertasPropias().forEach { ofertasPorId[it.idOfertaServicio] = it }
        valoraciones.map { it.idOfertaServicio }.distinct().forEach { idOferta ->
            if (!ofertasPorId.containsKey(idOferta)) {
                val ofertaDto = ejecutarApiServicios(api.buscarOferta(bearer(token), idOferta.toInt())).getOrNull()
                if (ofertaDto != null) {
                    ofertasPorId[idOferta] = ofertaDto.toOfertaServicio()
                }
            }
        }

        return valoraciones
            .groupBy { it.idOfertaServicio }
            .mapNotNull { (idOferta, lista) ->
                val oferta = ofertasPorId[idOferta] ?: return@mapNotNull null
                ValoracionesServicio(oferta = oferta, valoraciones = lista.sortedByDescending { it.fechaVoto })
            }
            .sortedByDescending { it.valoraciones.firstOrNull()?.fechaVoto.orEmpty() }
    }

    override fun obtenerCategoriasServicio(): List<CategoriaServicio> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        return ejecutarApiServicios(api.listarCategorias(bearer(token)))
            .getOrDefault(emptyList())
            .mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val nombre = dto.nombre?.trim().orEmpty()
                if (nombre.isBlank()) return@mapNotNull null
                CategoriaServicio(idCategoriaServicio = id.toLong(), nombre = nombre)
            }
    }

    override fun guardarOfertaPropia(formulario: FormularioServicio, idOfertaServicio: Long?): Result<OfertaServicio> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val usuario = sessionStore.obtenerUsuario()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (usuario.tipoPerfil !in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)) {
            return Result.failure(IllegalStateException("Debes verificarte como trabajador para publicar servicios"))
        }
        validarFormularioServicioRemoto(formulario)?.let { return Result.failure(IllegalArgumentException(it)) }

        val tiposPrecio = obtenerTiposPrecio(token)
        val idTipoPrecio = tiposPrecio.firstOrNull { (_, nombre) ->
            normalizarTexto(nombre) == normalizarTexto(formulario.tipoPrecio.nombreBackendTipoPrecio())
        }?.first ?: tiposPrecio.firstOrNull()?.first

        val request = OfertaServicioRequestDto(
            titulo = formulario.titulo.trim(),
            descripcion = formulario.descripcion.trim(),
            precio = formulario.precioBackend(),
            idCategoria = formulario.idCategoriaServicio!!.toInt(),
            idTipoPrecio = idTipoPrecio
        )

        val resultado = if (idOfertaServicio != null && idOfertaServicio > 0) {
            ejecutarApiServicios(
                api.actualizarOfertaPut(
                    authorization = bearer(token),
                    id = idOfertaServicio.toInt(),
                    request = OfertaServicioUpdateRequestDto(
                        titulo = request.titulo,
                        descripcion = request.descripcion,
                        precio = request.precio,
                        disponible = formulario.disponible,
                        idCategoria = request.idCategoria,
                        idTipoPrecio = request.idTipoPrecio
                    )
                )
            )
        } else {
            ejecutarApiServicios(api.crearOferta(bearer(token), request))
        }

        return resultado.mapCatching { ofertaDto ->
            val fotoLocalUri = formulario.foto?.uriLocal ?: ""
            if (fotoLocalUri.isNotBlank()) {
                val subidaFotoResult = subirFotoOferta(fotoLocalUri, ofertaDto.id?.toLong() ?: 0L)
                val linkFinal = subidaFotoResult.getOrNull()?.enlace.orEmpty()
                ofertaDto.toOfertaServicio(linkFinal)
            } else {
                ofertaDto.toOfertaServicio()
            }
        }
    }

    override fun actualizarDisponibilidadOfertaPropia(idOfertaServicio: Long, disponible: Boolean): Result<OfertaServicio> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (disponible && obtenerOfertasPropias().any { it.disponible && it.idOfertaServicio != idOfertaServicio }) {
            return Result.failure(IllegalStateException("Ya tienes un servicio activo. Desactivalo antes de activar otro."))
        }
        val call = if (disponible) {
            api.activarDisponibilidadOferta(bearer(token), idOfertaServicio.toInt())
        } else {
            api.desactivarDisponibilidadOferta(bearer(token), idOfertaServicio.toInt())
        }
        return ejecutarApiServicios(call).map { it.toOfertaServicio() }
    }

    override fun eliminarOfertaPropia(idOfertaServicio: Long): Result<Unit> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return ejecutarApiServicios(api.eliminarOferta(bearer(token), idOfertaServicio.toInt())).map { Unit }
    }

    override fun subirFotoOferta(uriString: String, idOferta: Long): Result<FotoOferta> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val part = runCatching { uriToMultipartPart(context, uriString, "file") }
            .getOrElse { return Result.failure(IllegalStateException("No se pudo leer la imagen seleccionada")) }

        return runCatching {
            // ========================================================
            // PASO 1: Pedir llaves a tu API de Fotos (Puerto 8084)
            // ========================================================
            val firma = ejecutarApi(FotosApiClient.api.obtenerFirmaCloudinary(bearer(token))).getOrThrow()

            val apiKeyReq = firma.apiKey.toRequestBody("text/plain".toMediaType())
            val timestampReq = firma.timestamp.toRequestBody("text/plain".toMediaType())
            val signatureReq = firma.signature.toRequestBody("text/plain".toMediaType())

            // ========================================================
            // PASO 2: Disparo directo a Cloudinary
            // ========================================================
            val cloudinaryRes = ejecutarApi(
                CloudinaryApiClient.api.subirImagen(
                    file = part,
                    apiKey = apiKeyReq,
                    timestamp = timestampReq,
                    signature = signatureReq
                )
            ).getOrThrow()

            // ========================================================
            // PASO 3: Guardar el enlace HTTPS en tu API de Servicios
            // ========================================================
            val requestDto = com.movil.contrabajo.data.remote.FotoRequestDTO(url = cloudinaryRes.secureUrl)

            val dtoBackend = ejecutarApiServicios(
                api.subirFotoOferta(bearer(token), idOferta.toInt(), requestDto)
            ).getOrThrow()

            dtoBackend.toFotoOferta()
        }
    }

    override fun listarFotosOferta(idOferta: Long): Result<List<FotoOferta>> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return ejecutarApiServicios(api.listarFotosOferta(bearer(token), idOferta.toInt()))
            .map { lista -> lista.map { it.toFotoOferta() } }
    }

    override fun eliminarFotoOferta(idFoto: Long): Result<Unit> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return ejecutarApiServicios(api.eliminarFotoOferta(bearer(token), idFoto.toInt())).map { Unit }
    }

    private fun obtenerTiposPrecio(token: String): List<Pair<Int, String>> {
        return ejecutarApiServicios(api.listarTiposPrecio(bearer(token)))
            .getOrDefault(emptyList())
            .mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val nombre = dto.nombre?.trim().orEmpty()
                if (nombre.isBlank()) return@mapNotNull null
                id to nombre
            }
    }
}

class RepositorioChatRemoto(
    private val comunicacionesApi: ComunicacionesApiService,
    private val serviciosApi: ServiciosApiService,
    private val sessionStore: RemoteSessionStore
) : RepositorioChats {

    // Cache en memoria: chatId → citaId  y  chatId → ofertaId
    // Se rellena cada vez que se cargan los chats o se crea/vincula una cita.
    private val citaIdPorChat: MutableMap<Long, Long> = mutableMapOf()
    private val ofertaIdPorChat: MutableMap<Long, Long> = mutableMapOf()
    private val valoracionPorChat: MutableMap<Long, Valoracion> = mutableMapOf()

    override fun obtenerIdUsuarioActual(): Long? = sessionStore.obtenerUsuario()?.idUsuario

    // ────────────────────────────────────────────────────────────────────────────
    // Lista de chats
    // ────────────────────────────────────────────────────────────────────────────
    override fun obtenerChatsActuales(): List<ChatCita> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        return ejecutarApiComunicaciones(comunicacionesApi.listarChats(bearer(token)))
            .getOrDefault(emptyList())
            .mapNotNull { dto ->
                val idChat = dto.id ?: return@mapNotNull null
                // Actualizar caches
                dto.idCita?.let { citaIdPorChat[idChat] = it.toLong() }
                dto.idOfertaServicio?.let { ofertaIdPorChat[idChat] = it.toLong() }
                dto.toChatCita(sessionStore.obtenerUsuario()?.idUsuario ?: 0L)
            }
    }

    override fun obtenerChat(idChatCita: Long): ChatCita? {
        val chat = obtenerChatsActuales().firstOrNull { it.idChatCita == idChatCita } ?: return null
        val token = sessionStore.obtenerToken() ?: return chat
        val idOferta = chat.idOfertaServicio?.toInt() ?: return chat
        val ofertaDto = ejecutarApiServicios(serviciosApi.buscarOferta(bearer(token), idOferta)).getOrNull()
        return chat.copy(servicioEliminado = ofertaDto?.borrado == true)
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Iniciar conversacion desde oferta
    // ────────────────────────────────────────────────────────────────────────────
    override fun iniciarConversacionDesdeOferta(
        idOfertaServicio: Long,
        tituloServicio: String,
        usernameTrabajador: String,
        usernameCliente: String
    ): Result<ChatCita> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val idUsuario = sessionStore.obtenerUsuario()?.idUsuario
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))

        // Necesitamos el idTrabajador de la oferta
        val ofertaResult = ejecutarApiServicios(
            serviciosApi.buscarOferta(bearer(token), idOfertaServicio.toInt())
        )
        val oferta = ofertaResult.getOrElse { return Result.failure(it) }
        val idTrabajador = oferta.idTrabajador
            ?: return Result.failure(IllegalArgumentException("La oferta no tiene trabajador asignado"))
        if (idTrabajador.toLong() == idUsuario) {
            return Result.failure(IllegalStateException("No puedes iniciar chat con tu propia publicacion"))
        }

        // usernameCliente: si el llamador no lo paso, usar el del store como fallback
        val usernameClienteFinal = usernameCliente.ifBlank {
            sessionStore.obtenerUsuario()?.username ?: ""
        }

        val dto = ChatIniciarRequestDto(
            idTrabajador       = idTrabajador,
            idOfertaServicio   = idOfertaServicio.toInt(),
            usernameTrabajador = usernameTrabajador.ifBlank { null },
            usernameCliente    = usernameClienteFinal.ifBlank { null },
            tituloServicio     = tituloServicio.ifBlank { null }
        )
        return ejecutarApiComunicaciones(comunicacionesApi.iniciarChat(bearer(token), dto))
            .map { chatDto ->
                val idChat = chatDto.id ?: 0L
                chatDto.idCita?.let { citaIdPorChat[idChat] = it.toLong() }
                ofertaIdPorChat[idChat] = idOfertaServicio
                chatDto.toChatCita(idUsuario)
            }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Mensajes
    // ────────────────────────────────────────────────────────────────────────────
    override fun obtenerMensajes(idChatCita: Long): List<MensajeChat> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        // Marcar como recibidos y leidos
        ejecutarApiComunicaciones(comunicacionesApi.marcarRecibidos(bearer(token), idChatCita))
        ejecutarApiComunicaciones(comunicacionesApi.marcarLeidos(bearer(token), idChatCita))
        return ejecutarApiComunicaciones(comunicacionesApi.obtenerHistorial(bearer(token), idChatCita))
            .getOrDefault(emptyList())
            .mapNotNull { it.toMensajeChat() }
    }

    override fun enviarMensaje(idChatCita: Long, contenido: String, tipo: Int): Result<MensajeChat> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val texto = contenido.trim()
        if (texto.isBlank()) return Result.failure(IllegalArgumentException("Escribe un mensaje"))
        val tipoSeguro = if (tipo == 1) 1 else 0
        return ejecutarApiComunicaciones(
            comunicacionesApi.enviarMensaje(bearer(token), MensajeChatEnviarDto(idChatCita, texto, tipoSeguro))
        ).mapCatching { it?.toMensajeChat() ?: throw IllegalStateException("Respuesta vacia al enviar mensaje") }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Crear cita desde chat (cliente)
    // ────────────────────────────────────────────────────────────────────────────
    override fun crearCitaDesdeChat(
        idChatCita: Long,
        fechaProgramada: String,
        comentario: String,
        precioAcordado: Int
    ): Result<CitaServicio> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val idOferta = ofertaIdPorChat[idChatCita]
            ?: return Result.failure(IllegalStateException("No se encontro la oferta asociada al chat. Abre el chat primero."))
        if (comentario.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa un comentario para la cita"))
        }

        // 1. Crear la cita en servicios_api
        val citaDto = ejecutarApiServicios(
            serviciosApi.solicitarCita(
                authorization = bearer(token),
                dto = SolicitarCitaRequestDto(
                    idOfertaServicio = idOferta.toInt(),
                    comentario = construirComentarioCita(fechaProgramada, comentario),
                    idChatOferta = idChatCita
                )
            )
        ).getOrElse { return Result.failure(it) }

        val idCita = citaDto.id?.toLong()
            ?: return Result.failure(IllegalStateException("El servidor no devolvio el ID de la cita"))

        // 2. Vincular la cita al chat
        ejecutarApiComunicaciones(
            comunicacionesApi.vincularCita(
                bearer(token), idChatCita, VincularCitaRequestDto(idCita.toInt())
            )
        )
        citaIdPorChat[idChatCita] = idCita

        return Result.success(citaDto.toCitaServicio(idChatCita))
    }

    override fun obtenerCitaPorChat(idChatCita: Long): CitaServicio? {
        val token = sessionStore.obtenerToken() ?: return null
        var idCita = citaIdPorChat[idChatCita]
        if (idCita == null) {
            val chat = obtenerChat(idChatCita)
            idCita = chat?.idCita
            if (idCita != null) {
                citaIdPorChat[idChatCita] = idCita
            }
        }
        idCita ?: return null
        return ejecutarApiServicios(serviciosApi.obtenerCita(bearer(token), idCita.toInt()))
            .getOrNull()?.toCitaServicio(idChatCita)
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Transiciones de estado de cita (todas requieren idCita del cache)
    // ────────────────────────────────────────────────────────────────────────────
    private fun transicionarCita(
        idChatCita: Long,
        llamada: (String, Int) -> Call<CitaServicioDto>
    ): Result<CitaServicio> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val idCita = citaIdPorChat[idChatCita]?.toInt()
            ?: return Result.failure(IllegalStateException("No se encontro la cita del chat. Abre el chat primero."))
        return ejecutarApiServicios(llamada(bearer(token), idCita))
            .map { it.toCitaServicio(idChatCita) }
    }

    override fun aceptarCitaTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.aceptarCita(token, id) }

    override fun rechazarCitaTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.rechazarCita(token, id) }

    override fun reenviarPropuestaCitaCliente(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.reenviarCita(token, id) }

    override fun solicitarInicioTrabajoTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.comenzarCita(token, id) }

    override fun aceptarInicioTrabajoCliente(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.confirmarInicioCita(token, id) }

    override fun solicitarFinalizarTrabajoTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.finalizarCita(token, id) }

    override fun aceptarFinalizarTrabajoCliente(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(idChatCita) { token, id -> serviciosApi.confirmarFinalizacionCita(token, id) }

    // ────────────────────────────────────────────────────────────────────────────
    // Cerrar chat
    // ────────────────────────────────────────────────────────────────────────────
    override fun cerrarChat(idChatCita: Long): Result<ChatCita> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val idUsuario = sessionStore.obtenerUsuario()?.idUsuario ?: 0L

        // Regla de cierre:
        // - Si la cita aun no ha finalizado, al finalizar chat se cancela (estado CANCELADO).
        // - Si ya esta finalizada, se conserva FINALIZADO.
        citaIdPorChat[idChatCita]?.let { idCita ->
            val citaActual = ejecutarApiServicios(serviciosApi.obtenerCita(bearer(token), idCita.toInt()))
                .getOrNull()
            if (citaActual != null) {
                val estadoLocal = citaActual.codigoEstado.toEstadoCitaLocal(citaActual.idEstado)
                val debeCancelar = estadoLocal !in setOf(EstadoCita.FINALIZADO, EstadoCita.CANCELADO, EstadoCita.CERRADO)
                if (debeCancelar) {
                    ejecutarApiServicios(serviciosApi.cancelarCita(bearer(token), idCita.toInt()))
                }
            } else {
                ejecutarApiServicios(serviciosApi.cancelarCita(bearer(token), idCita.toInt()))
            }
        }
        // Desactivar por idChat para cerrar exactamente este hilo (evita ambiguedad con chats historicos).
        ejecutarApiComunicaciones(comunicacionesApi.desactivarChatPorId(bearer(token), idChatCita))
            .getOrElse { return Result.failure(it) }

        val chat = obtenerChat(idChatCita)

        // Devolver el chat actualizado (chatCerrado = true)
        return Result.success(
            (chat ?: ChatCita(
                idChatCita = idChatCita,
                fechaCreacion = "",
                idTrabajador = 0L,
                idCliente = idUsuario
            )).copy(chatCerrado = true)
        )
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Valoraciones y notificaciones — pendientes para siguiente iteracion
    // ────────────────────────────────────────────────────────────────────────────
    override fun obtenerValoracionPorChat(idChatCita: Long): Valoracion? = valoracionPorChat[idChatCita]

    override fun guardarValoracionChat(idChatCita: Long, voto: Int, comentario: String): Result<Valoracion> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val idCliente = sessionStore.obtenerUsuario()?.idUsuario
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val idCita = citaIdPorChat[idChatCita]?.toInt()
            ?: return Result.failure(IllegalStateException("No se encontro la cita del chat. Abre el chat primero."))
        val chat = obtenerChat(idChatCita)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        val idOferta = chat.idOfertaServicio
            ?: return Result.failure(IllegalStateException("No se encontro la oferta asociada al chat"))
        if (voto !in 1..5) {
            return Result.failure(IllegalArgumentException("La valoracion debe estar entre 1 y 5 estrellas"))
        }

        return ejecutarApiServicios(
            serviciosApi.crearValoracion(
                authorization = bearer(token),
                dto = ValoracionRequestDto(
                    idCita = idCita,
                    voto = voto,
                    comentario = comentario.trim()
                )
            )
        ).map {
            val valoracion = Valoracion(
                voto = voto,
                fechaVoto = java.time.LocalDateTime.now().toString(),
                comentario = comentario.trim(),
                idTrabajador = chat.idTrabajador,
                idCliente = idCliente,
                idChatCita = idChatCita,
                idOfertaServicio = idOferta
            )
            valoracionPorChat[idChatCita] = valoracion
            valoracion
        }
    }

    override fun obtenerNotificacionesPendientes(): List<NotificacionMensajePendiente> = emptyList()

    override fun marcarNotificacionesComoMostradas(idsMensaje: List<Long>) = Unit

    // ────────────────────────────────────────────────────────────────────────────
    // Mapeos DTO → dominio
    // ────────────────────────────────────────────────────────────────────────────
    private fun ChatDto.toChatCita(idUsuarioActual: Long): ChatCita {
        val idTrab = idTrabajador?.toLong() ?: 0L
        val idCli  = idCliente?.toLong()   ?: 0L
        // El "contacto" es el otro participante (no el usuario actual)
        val esCliente       = idCli == idUsuarioActual
        val usernameContacto = if (esCliente) usernameTrabajador ?: "" else usernameCliente ?: ""
        return ChatCita(
            idChatCita        = id ?: 0L,
            fechaCreacion     = fechaCreacion ?: "",
            idTrabajador      = idTrab,
            idCliente         = idCli,
            idOfertaServicio  = idOfertaServicio?.toLong(),
            idCita            = idCita?.toLong(),
            tituloServicio    = tituloServicio ?: "",
            usernameContacto  = usernameContacto,
            ultimoMensaje     = ultimoMensaje ?: "",
            horaUltimoMensaje = fechaUltimoMensaje ?: "",
            mensajesNoLeidos  = mensajesNoLeidos?.toInt() ?: 0,
            chatCerrado       = activo == false
        )
    }

    override fun marcarRecibidos(idChatCita: Long) {
        val token = sessionStore.obtenerToken() ?: return
        ejecutarApiComunicaciones(comunicacionesApi.marcarRecibidos(bearer(token), idChatCita))
    }

    override fun marcarLeidos(idChatCita: Long) {
        val token = sessionStore.obtenerToken() ?: return
        ejecutarApiComunicaciones(comunicacionesApi.marcarLeidos(bearer(token), idChatCita))
    }

    private fun MensajeChatDto.toMensajeChat(): MensajeChat? {
        val idMsg = id ?: return null
        return MensajeChat(
            idMensajeChat = idMsg,
            fechaEnvio    = fechaEnvio ?: "",
            fechaRecibido = fechaRecibido,
            fechaLeido    = fechaLeido,
            idEmisor      = idEmisor?.toLong() ?: 0L,
            idReceptor    = idReceptor?.toLong() ?: 0L,
            idChatCita    = idChatOferta ?: 0L,
            idEstado      = when {
                fechaLeido    != null -> EstadoCodigo.MSG_LEIDO
                fechaRecibido != null -> EstadoCodigo.MSG_ENTREGADO
                else                  -> EstadoCodigo.MSG_ENVIADO
            },
            contenido     = contenido ?: "",
            tipo          = tipo ?: 0
        )
    }
}

private fun CitaServicioDto.toCitaServicio(idChatCita: Long): CitaServicio {
    val comentarioLimpio = comentario.orEmpty()
    return CitaServicio(
        idCita          = id?.toLong() ?: 0L,
        idChatCita      = idChatCita,
        fechaCreacion   = fechaSolicitud ?: "",
        fechaProgramada = extraerFechaProgramada(comentarioLimpio),
        comentario      = limpiarComentarioCita(comentarioLimpio),
        precioAcordado  = 0,
        fechaInicioTrabajo = fechaInicioTrabajo,
        fechaFinTrabajo    = fechaFinTrabajo,
        estado          = codigoEstado.toEstadoCitaLocal(idEstado)
    )
}

private fun construirComentarioCita(fechaProgramada: String, comentario: String): String {
    val fecha = fechaProgramada.trim()
    val detalle = comentario.trim()
    return if (fecha.isBlank()) detalle else "[FECHA:$fecha] $detalle".trim()
}

private fun extraerFechaProgramada(comentario: String): String {
    val regex = Regex("""^\[FECHA:([^\]]+)]\s*""")
    return regex.find(comentario)?.groupValues?.getOrNull(1)?.trim().orEmpty()
}

private fun limpiarComentarioCita(comentario: String): String {
    val regex = Regex("""^\[FECHA:[^\]]+]\s*""")
    return comentario.replace(regex, "").trim()
}

private fun String?.toEstadoCitaLocal(idEstado: Int?): Int {
    return when (this?.trim()?.uppercase()) {
        "CITA_PENDIENTE" -> EstadoCita.PENDIENTE
        "CITA_HANDSHAKE" -> EstadoCita.HANDSHAKE
        "CITA_COMENZANDO" -> EstadoCita.COMENZANDO
        "CITA_EN_PROCESO" -> EstadoCita.EN_PROCESO
        "CITA_FINALIZANDO" -> EstadoCita.FINALIZANDO
        "CITA_FINALIZADO" -> EstadoCita.FINALIZADO
        "CITA_CANCELADO" -> EstadoCita.CANCELADO
        "CITA_CERRADO" -> EstadoCita.CERRADO
        "CITA_RECHAZADO" -> EstadoCita.RECHAZADA
        else -> idEstado ?: EstadoCita.PENDIENTE
    }
}

class RepositorioChatsRecortado(
    private val sessionStore: RemoteSessionStore
) : RepositorioChats {

    override fun obtenerIdUsuarioActual(): Long? = sessionStore.obtenerUsuario()?.idUsuario

    override fun obtenerChatsActuales(): List<ChatCita> = emptyList()

    override fun obtenerMensajes(idChatCita: Long): List<MensajeChat> = emptyList()

    override fun iniciarConversacionDesdeOferta(
        idOfertaServicio: Long,
        tituloServicio: String,
        usernameTrabajador: String,
        usernameCliente: String
    ): Result<ChatCita> =
        Result.failure(IllegalStateException("Comunicaciones se integrara en una siguiente iteracion."))

    override fun obtenerChat(idChatCita: Long): ChatCita? = null

    override fun enviarMensaje(idChatCita: Long, contenido: String, tipo: Int): Result<MensajeChat> =
        Result.failure(IllegalStateException("Comunicaciones se integrara en una siguiente iteracion."))

    override fun crearCitaDesdeChat(
        idChatCita: Long,
        fechaProgramada: String,
        comentario: String,
        precioAcordado: Int
    ): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun obtenerCitaPorChat(idChatCita: Long): CitaServicio? = null

    override fun aceptarCitaTrabajador(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun rechazarCitaTrabajador(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun reenviarPropuestaCitaCliente(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun solicitarInicioTrabajoTrabajador(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun aceptarInicioTrabajoCliente(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun solicitarFinalizarTrabajoTrabajador(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun aceptarFinalizarTrabajoCliente(idChatCita: Long): Result<CitaServicio> =
        Result.failure(IllegalStateException("Las citas ligadas a chat se integraran junto con comunicaciones."))

    override fun cerrarChat(idChatCita: Long): Result<ChatCita> =
        Result.failure(IllegalStateException("Comunicaciones se integrara en una siguiente iteracion."))

    override fun obtenerValoracionPorChat(idChatCita: Long): Valoracion? = null

    override fun guardarValoracionChat(idChatCita: Long, voto: Int, comentario: String): Result<Valoracion> =
        Result.failure(IllegalStateException("Valoraciones ligadas a chat se integraran cuando citas/comunicaciones queden remotas."))

    override fun obtenerNotificacionesPendientes(): List<NotificacionMensajePendiente> = emptyList()

    override fun marcarNotificacionesComoMostradas(idsMensaje: List<Long>) = Unit

    override fun marcarRecibidos(idChatCita: Long) = Unit

    override fun marcarLeidos(idChatCita: Long) = Unit
}

class RepositorioReportesRemoto(
    private val comunicacionesApi: ComunicacionesApiService,
    private val sessionStore: RemoteSessionStore
) : RepositorioReportes {

    override fun obtenerTiposReporte(): List<TipoReporte> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        return ejecutarApiComunicaciones(comunicacionesApi.obtenerTiposReporte(bearer(token)))
            .getOrDefault(emptyList())
            .mapNotNull { dto ->
                val id = dto.id?.toLong() ?: return@mapNotNull null
                val nombre = dto.nombre?.trim().orEmpty()
                if (nombre.isBlank()) return@mapNotNull null
                TipoReporte(
                    idTipoReporte = id,
                    nombre = nombre,
                    descripcion = nombre
                )
            }
    }

    override fun crearReporteDesdeOferta(
        idOfertaServicio: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (comentario.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Debes ingresar un comentario del reporte"))
        }
        return ejecutarApiComunicaciones(
            comunicacionesApi.crearReporte(
                auth = bearer(token),
                dto = CrearReporteRequestDto(
                    idTipoReporte = idTipoReporte.toInt(),
                    idOfertaServicio = idOfertaServicio,
                    comentario = comentario.trim()
                )
            )
        ).map { it.toReporte() }
    }

    override fun crearReporteDesdeChat(
        idChatCita: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (comentario.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Debes ingresar un comentario del reporte"))
        }
        val chat = ejecutarApiComunicaciones(comunicacionesApi.listarChats(bearer(token)))
            .getOrDefault(emptyList())
            .firstOrNull { it.id == idChatCita }
            ?: return Result.failure(IllegalStateException("No se encontro el chat a reportar"))
        val idOferta = chat.idOfertaServicio?.toLong()
            ?: return Result.failure(IllegalStateException("No se encontro servicio asociado a este chat"))

        return ejecutarApiComunicaciones(
            comunicacionesApi.crearReporte(
                auth = bearer(token),
                dto = CrearReporteRequestDto(
                    idTipoReporte = idTipoReporte.toInt(),
                    idOfertaServicio = idOferta,
                    idChatCita = idChatCita,
                    comentario = comentario.trim()
                )
            )
        ).map { it.toReporte() }
    }

    override fun obtenerReportesModeracion(
        busqueda: String,
        idTipoReporte: Long?,
        estadoRevision: String?,
        ordenarRecientes: Boolean
    ): List<Reporte> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        return ejecutarApiComunicaciones(
            comunicacionesApi.listarReportesModeracion(
                auth = bearer(token),
                busqueda = busqueda.trim().ifBlank { null },
                estadoRevision = estadoRevision?.trim()?.ifBlank { null },
                idTipoReporte = idTipoReporte?.toInt(),
                ordenarRecientes = ordenarRecientes
            )
        ).getOrDefault(emptyList())
            .map { it.toReporte() }
    }

    override fun obtenerDetalleReporte(idReporte: Long): Reporte? {
        val token = sessionStore.obtenerToken() ?: return null
        return ejecutarApiComunicaciones(
            comunicacionesApi.obtenerDetalleReporte(
                auth = bearer(token),
                idReporte = idReporte
            )
        ).getOrNull()?.toReporte()
    }

    override fun aplicarMedidaModeracion(idReporte: Long, accion: String): Result<Reporte> =
        run {
            val token = sessionStore.obtenerToken()
                ?: return@run Result.failure(IllegalStateException("No hay sesion activa"))
            val medida = accion.trim()
            if (medida.isBlank()) {
                return@run Result.failure(IllegalArgumentException("Debes seleccionar una medida"))
            }
            ejecutarApiComunicaciones(
                comunicacionesApi.revisarReporte(
                    auth = bearer(token),
                    idReporte = idReporte,
                    dto = RevisarReporteRequestDto(medidaAplicada = medida)
                )
            ).map { it.toReporte() }
        }
}

class RepositorioReportesRecortado : RepositorioReportes {

    override fun obtenerTiposReporte(): List<TipoReporte> = emptyList()

    override fun crearReporteDesdeOferta(
        idOfertaServicio: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> =
        Result.failure(IllegalStateException("Reportes se integrara en una siguiente iteracion."))

    override fun crearReporteDesdeChat(
        idChatCita: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> =
        Result.failure(IllegalStateException("Reportes se integrara en una siguiente iteracion."))

    override fun obtenerReportesModeracion(
        busqueda: String,
        idTipoReporte: Long?,
        estadoRevision: String?,
        ordenarRecientes: Boolean
    ): List<Reporte> = emptyList()

    override fun obtenerDetalleReporte(idReporte: Long): Reporte? = null

    override fun aplicarMedidaModeracion(idReporte: Long, accion: String): Result<Reporte> =
        Result.failure(IllegalStateException("Moderacion de reportes se integrara en una siguiente iteracion."))
}

class RepositorioBaneosRemoto(
    private val api: com.movil.contrabajo.data.remote.UsuariosApiService,
    private val sessionStore: RemoteSessionStore
) : RepositorioBaneos {

    override fun listarBaneados(): List<com.movil.contrabajo.domain.model.UsuarioBaneado> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        return ejecutarApi(api.listarBaneados(bearer(token)))
            .getOrDefault(emptyList())
            .mapNotNull { dto ->
                val id = dto.idUsuario ?: return@mapNotNull null
                com.movil.contrabajo.domain.model.UsuarioBaneado(
                    idUsuario = id,
                    username = dto.username.orEmpty(),
                    nombre = dto.nombre.orEmpty(),
                    apellidos = dto.apellidos.orEmpty(),
                    idEstado = dto.idEstado ?: 103,
                    tipoSancion = dto.tipoSancion.orEmpty(),
                    permanente = dto.permanente ?: true,
                    fechaInicio = dto.fechaInicio,
                    fechaFin = dto.fechaFin,
                    motivo = dto.motivo
                )
            }
    }

    override fun desbanearUsuario(idUsuario: Int): Result<Unit> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return ejecutarApi(api.desbanearUsuario(bearer(token), idUsuario)).map { }
    }
}

private fun ReporteResponseDto.toReporte(): Reporte {
    return Reporte(
        idReporte = idReporte ?: 0L,
        idEmisor = (idEmisor ?: 0).toLong(),
        idUsuarioReportado = idUsuarioReportado?.toLong(),
        idOfertaServicio = idOfertaServicio,
        idChatCita = idChatCita,
        idTipoReporte = (idTipoReporte ?: 0).toLong(),
        comentario = comentario.orEmpty(),
        fechaCreacion = fechaCreacion.orEmpty(),
        estadoRevision = estadoRevision.orEmpty().ifBlank { "PENDIENTE" },
        idModeradorRevisor = idModeradorRevisor?.toLong(),
        fechaRevision = fechaRevision,
        medidaAplicada = medidaAplicada,
        tipoReporteNombre = tipoReporteNombre.orEmpty(),
        emisorUsername = emisorUsername.orEmpty(),
        usuarioReportadoUsername = usuarioReportadoUsername.orEmpty(),
        usuarioReportadoNombre = usuarioReportadoNombre.orEmpty(),
        servicioTitulo = servicioTitulo.orEmpty(),
        servicioFotoUrl = servicioFotoUrl.orEmpty()
    )
}

private fun RegistroPendiente.toRegistroRequest(): UsuarioRegistroRequestDto {
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
            idComuna = idComuna,
            latitud = latitud,
            longitud = longitud
        )
    )
}

private fun PreguntasSeguridadDto.toPreguntasConfig(): List<PreguntaSeguridadConfig> {
    return listOf(
        PreguntaSeguridadConfig(indice = 1, pregunta = pregunta1.orEmpty(), respuesta = ""),
        PreguntaSeguridadConfig(indice = 2, pregunta = pregunta2.orEmpty(), respuesta = "")
    )
}

// Cambiamos la firma para poder pasarle la URL de la foto recuperada
// 🌟 1. Reemplazar el mapeador del DTO para que lea los IDs exactos de tu SQL Server
private fun OfertaServicioDto.toOfertaServicio(fotoUrl: String = ""): OfertaServicio {
    val categoriaNombre = categoria.orEmpty()
    val tipoPrecioNombre = tipoPrecio.orEmpty()

    // Primero intentamos resolver por el ID numérico que es infalible y viene directo de la BD
    val tipoPrecioLocal = if (idTipoPrecio != null) {
        when (idTipoPrecio) {
            1 -> TipoPrecio.POR_HORA   // ID 1: Por hora
            2 -> TipoPrecio.FIJO       // ID 2: Por trabajo (Fijo en el Front)
            3 -> TipoPrecio.DESDE      // ID 3: Por día (Desde en el Front)
            4 -> TipoPrecio.CONTACTAR  // ID 4: A convenir (Contactar en el Front)
            else -> tipoPrecioNombre.toTipoPrecioLocal() // Fallback a texto si es un ID nuevo
        }
    } else {
        tipoPrecioNombre.toTipoPrecioLocal()
    }

    val monto = precio?.toInt() ?: 0
    val precioTexto = PrecioUtils.construirPrecioTexto(tipoPrecioLocal, monto)

    return OfertaServicio(
        idOfertaServicio = id?.toLong() ?: 0L,
        titulo = titulo.orEmpty(),
        descripcion = descripcion.orEmpty(),
        precioTexto = precioTexto,
        tipoPrecio = tipoPrecioLocal,
        montoBase = monto,
        disponible = disponible ?: false,
        fechaPublicacion = fechaPublicacion.orEmpty(),
        idCategoriaServicio = idCategoria?.toLong() ?: 0L,
        idTrabajador = idTrabajador?.toLong() ?: 0L,
        nombreTrabajador = nombreTrabajador.orEmpty(),
        usernameTrabajador = usernameTrabajador.orEmpty(),
        nombreCategoria = categoriaNombre,
        rangoDisponibilidadM = EscalaRango.normalizar(rangoDisponibilidadM ?: 20_000),
        ubicacionReferencia = ubicacionReferencia.orEmpty(),
        latitudReferencia = latitudReferencia,
        longitudReferencia = longitudReferencia,
        eliminada = borrado ?: false,
        fotoUrlReferencia = fotoUrl.normalizarEnlaceEmulador()
    )
}

private fun String.toTipoPrecioLocal(): Int {
    return when (normalizarTexto(this)) {
        "por hora" -> TipoPrecio.POR_HORA
        "por trabajo", "precio fijo" -> TipoPrecio.FIJO
        "por dia", "desde" -> TipoPrecio.DESDE
        "a convenir", "contactar", "contactar para saber precio" -> TipoPrecio.CONTACTAR
        else -> TipoPrecio.CONTACTAR
    }
}

// 🌟 3. Corregir el formateador inverso que viaja hacia el Backend (Línea ~590)
private fun Int.nombreBackendTipoPrecio(): String {
    return when (this) {
        TipoPrecio.POR_HORA  -> "Por hora"
        TipoPrecio.FIJO      -> "Por trabajo"
        TipoPrecio.DESDE     -> "Por día"
        TipoPrecio.CONTACTAR -> "A convenir"
        else -> "A convenir"
    }
}

private fun String.idCategoriaLocalPorNombre(): Long {
    return when (normalizarTexto(this)) {
        "electricidad" -> 1L
        "gasfiteria" -> 2L
        "limpieza" -> 3L
        "carpinteria" -> 4L
        else -> 0L
    }
}

private fun FormularioServicio.precioBackend(): BigDecimal? {
    return when (tipoPrecio) {
        TipoPrecio.CONTACTAR -> null
        else -> BigDecimal.valueOf(montoBase.toLong())
    }
}

private fun validarFormularioServicioRemoto(formulario: FormularioServicio): String? = when {
    formulario.titulo.isBlank() -> "Ingresa un titulo para tu servicio"
    formulario.titulo.trim().length > 80 -> "El titulo permite hasta 80 caracteres"
    formulario.descripcion.isBlank() -> "Ingresa la descripcion del servicio"
    formulario.descripcion.trim().length > 300 -> "La descripcion permite hasta 300 caracteres"
    formulario.idCategoriaServicio == null -> "Selecciona una categoria"
    formulario.tipoPrecio !in listOf(TipoPrecio.FIJO, TipoPrecio.POR_HORA, TipoPrecio.DESDE, TipoPrecio.CONTACTAR) ->
        "Selecciona un tipo de precio valido"
    !PrecioUtils.esMontoValido(formulario.tipoPrecio, formulario.montoBase) ->
        "El monto debe estar entre ${PrecioUtils.MIN_MONTO} y ${PrecioUtils.MAX_MONTO}"
    else -> null
}

private fun uriToMultipartPart(context: Context, uriString: String, partName: String): MultipartBody.Part {
    val uri = Uri.parse(uriString)

    // 1. Detectar si la foto viene rotada por los metadatos del celular (EXIF)
    var rotacionGrados = 0
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val exifInterface = ExifInterface(input)
            val orientacion = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            rotacionGrados = when (orientacion) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
    } catch (_: Exception) {}

    // 2. Abrimos la imagen original
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    if (bitmapOriginal == null) {
        throw IllegalStateException("No se pudo leer la imagen seleccionada")
    }

    // 3. Redimensionar manteniendo la proporción (Max 1080px)
    val maxDimension = 1080f
    val scale = minOf(maxDimension / bitmapOriginal.width, maxDimension / bitmapOriginal.height, 1f)

    // 4. Aplicamos escala y la rotación necesaria para enderezarla
    val matriz = Matrix()
    if (scale < 1f) matriz.postScale(scale, scale)
    if (rotacionGrados != 0) matriz.postRotate(rotacionGrados.toFloat())

    val bitmapFinal = Bitmap.createBitmap(
        bitmapOriginal,
        0, 0,
        bitmapOriginal.width,
        bitmapOriginal.height,
        matriz,
        true
    )

    // 5. Comprimimos a JPEG al 80% (Pasa de 16MB a ~300KB)
    val outputStream = ByteArrayOutputStream()
    bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val bytesComprimidos = outputStream.toByteArray()

    // Limpieza estricta de memoria RAM
    if (bitmapFinal != bitmapOriginal) bitmapFinal.recycle()
    bitmapOriginal.recycle()

    val requestBody = bytesComprimidos.toRequestBody("image/jpeg".toMediaType())
    return MultipartBody.Part.createFormData(partName, "foto_comprimida.jpg", requestBody)
}

private fun String.normalizarEnlaceEmulador(): String =
    replace("://localhost:", "://10.0.2.2:", ignoreCase = true)
        .replace("://127.0.0.1:", "://10.0.2.2:", ignoreCase = true)

private fun FotoOfertaResponseDto.toFotoOferta(): FotoOferta = FotoOferta(
    idFoto = idFoto?.toLong() ?: 0L,
    enlace = enlace.orEmpty().normalizarEnlaceEmulador(),
    nombreOriginal = "foto_cloudinary", // Ya no lo guardamos, dejamos un genérico
    tipoMime = "image/jpeg",           // Genérico para la UI
    tamanoBytes = 0L,                  // Genérico
    anchoPx = null,
    altoPx = null,
    fechaSubida = fechaSubida.orEmpty(),
    idOfertaServicio = idOfertaServicio?.toLong() ?: 0L,
    idUsuario = idUsuario?.toLong() ?: 0L
)

private fun normalizarTexto(valor: String): String {
    return java.text.Normalizer.normalize(valor.trim(), java.text.Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase()
}

private fun String.normalizarTelefonoBackend(): String {
    return filter { it.isDigit() }
        .let { if (it.startsWith("56")) it.drop(2) else it }
        .let { if (it.length == 9 && it.startsWith("9")) it.drop(1) else it }
        .take(8)
}

private fun validarContrasenaSegura(contrasena: String): String? {
    return when {
        contrasena.length < 8 -> "La contrasena debe tener al menos 8 caracteres"
        contrasena.none { it.isUpperCase() } -> "La contrasena debe incluir al menos 1 mayuscula"
        contrasena.none { it.isDigit() } -> "La contrasena debe incluir al menos 1 numero"
        contrasena.none { !it.isLetterOrDigit() } -> "La contrasena debe incluir al menos 1 simbolo"
        else -> null
    }
}

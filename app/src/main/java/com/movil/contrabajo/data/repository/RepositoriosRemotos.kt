package com.movil.contrabajo.data.repository

import com.movil.contrabajo.data.remote.DireccionRegistroRequestDto
import com.movil.contrabajo.data.remote.LoginRequestDto
import com.movil.contrabajo.data.remote.OcrSimuladoRequestDto
import com.movil.contrabajo.data.remote.PreguntaSeguridadUpdateRequestDto
import com.movil.contrabajo.data.remote.PreguntasSeguridadDto
import com.movil.contrabajo.data.remote.RecuperacionPasswordRequestDto
import com.movil.contrabajo.data.remote.RecuperacionRegistroRequestDto
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.ServiciosApiService
import com.movil.contrabajo.data.remote.OfertaServicioDto
import com.movil.contrabajo.data.remote.OfertaServicioRequestDto
import com.movil.contrabajo.data.remote.OfertaServicioUpdateRequestDto
import com.movil.contrabajo.data.remote.UsuarioRegistroRequestDto
import com.movil.contrabajo.data.remote.UsuarioUpdateRequestDto
import com.movil.contrabajo.data.remote.UsuariosApiService
import com.movil.contrabajo.data.remote.bearer
import com.movil.contrabajo.data.remote.ejecutarApi
import com.movil.contrabajo.data.remote.ejecutarApiServicios
import com.movil.contrabajo.domain.model.ComunaCatalogo
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.FiltroMarketplaceConfig
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PrecioUtils
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
    private val sessionStore: RemoteSessionStore
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
                val comuna = remoto.direccion?.comuna?.nombre.orEmpty().ifBlank { "Sin comuna" }
                val region = remoto.direccion?.comuna?.region.orEmpty().ifBlank { "Region Metropolitana" }
                return UbicacionAjustesConfig(
                    region = region,
                    comuna = comuna,
                    calle = remoto.direccion?.calle.orEmpty(),
                    numero = remoto.direccion?.numero.orEmpty(),
                    detalle = "",
                    latitud = remoto.direccion?.latitud,
                    longitud = remoto.direccion?.longitud
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
                longitud = usuario.direccionLongitud
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
                        idComuna = idComuna
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
                val mensaje = it.message.orEmpty()
                if (mensaje.contains("(403)") || mensaje.contains("forbidden", ignoreCase = true)) {
                    sessionStore.limpiarSesion()
                    Result.failure(
                        IllegalStateException(
                            "Tu sesion ya no es valida para editar perfil. Inicia sesion de nuevo."
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
    private val sessionStore: RemoteSessionStore
) : RepositorioOfertas {

    override fun obtenerOfertaPrincipal(): OfertaServicio? =
        obtenerOfertasMarketplace().firstOrNull { it.disponible }

    override fun obtenerOfertasMarketplace(busqueda: String): List<OfertaServicio> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        return ejecutarApiServicios(api.listarOfertas(bearer(token)))
            .getOrDefault(emptyList())
            .map { it.toOfertaServicio() }
            .filter { !it.eliminada && it.disponible }
            .filter {
                val filtro = busqueda.trim()
                filtro.isBlank() ||
                    it.titulo.contains(filtro, ignoreCase = true) ||
                    it.descripcion.contains(filtro, ignoreCase = true) ||
                    it.nombreCategoria.contains(filtro, ignoreCase = true)
            }
    }

    override fun obtenerOfertaPorId(idOfertaServicio: Long, incluirEliminadas: Boolean): OfertaServicio? {
        val token = sessionStore.obtenerToken() ?: return null
        return ejecutarApiServicios(api.buscarOferta(bearer(token), idOfertaServicio.toInt()))
            .getOrNull()
            ?.toOfertaServicio()
            ?.takeIf { incluirEliminadas || !it.eliminada }
    }

    override fun obtenerOfertasPropias(): List<OfertaServicio> {
        val token = sessionStore.obtenerToken() ?: return emptyList()
        val usuario = sessionStore.obtenerUsuario() ?: return emptyList()
        return ejecutarApiServicios(api.listarOfertasTrabajador(bearer(token), usuario.idUsuario.toInt()))
            .getOrDefault(emptyList())
            .map { it.toOfertaServicio() }
            .filter { !it.eliminada }
    }

    override fun obtenerOfertaPropiaActual(): OfertaServicio? = obtenerOfertasPropias().firstOrNull()

    override fun obtenerOfertaPropiaPorId(idOfertaServicio: Long): OfertaServicio? =
        obtenerOfertasPropias().firstOrNull { it.idOfertaServicio == idOfertaServicio }

    override fun obtenerIdsOfertasConTrabajoEnCursoPropias(): Set<Long> = emptySet()

    override fun obtenerValoracionesPropiasPorServicio(): List<ValoracionesServicio> =
        obtenerOfertasPropias().map { ValoracionesServicio(oferta = it, valoraciones = emptyList()) }

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
                api.actualizarOferta(
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
        return resultado.map { it.toOfertaServicio() }
    }

    override fun actualizarDisponibilidadOfertaPropia(idOfertaServicio: Long, disponible: Boolean): Result<OfertaServicio> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (disponible && obtenerOfertasPropias().any { it.disponible && it.idOfertaServicio != idOfertaServicio }) {
            return Result.failure(IllegalStateException("Ya tienes un servicio activo. Desactivalo antes de activar otro."))
        }
        return ejecutarApiServicios(
            api.actualizarOferta(
                authorization = bearer(token),
                id = idOfertaServicio.toInt(),
                request = OfertaServicioUpdateRequestDto(disponible = disponible)
            )
        ).map { it.toOfertaServicio() }
    }

    override fun eliminarOfertaPropia(idOfertaServicio: Long): Result<Unit> {
        val token = sessionStore.obtenerToken()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        return ejecutarApiServicios(api.eliminarOferta(bearer(token), idOfertaServicio.toInt())).map { Unit }
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

class RepositorioChatsRecortado(
    private val sessionStore: RemoteSessionStore
) : RepositorioChats {

    override fun obtenerIdUsuarioActual(): Long? = sessionStore.obtenerUsuario()?.idUsuario

    override fun obtenerChatsActuales(): List<ChatCita> = emptyList()

    override fun obtenerMensajes(idChatCita: Long): List<MensajeChat> = emptyList()

    override fun iniciarConversacionDesdeOferta(idOfertaServicio: Long): Result<ChatCita> =
        Result.failure(IllegalStateException("Comunicaciones se integrara en una siguiente iteracion."))

    override fun obtenerChat(idChatCita: Long): ChatCita? = null

    override fun enviarMensaje(idChatCita: Long, contenido: String): Result<MensajeChat> =
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

private fun OfertaServicioDto.toOfertaServicio(): OfertaServicio {
    val categoriaNombre = categoria.orEmpty()
    val tipoPrecioNombre = tipoPrecio.orEmpty()
    val tipoPrecioLocal = tipoPrecioNombre.toTipoPrecioLocal()
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
        nombreTrabajador = if (idTrabajador != null) "Trabajador $idTrabajador" else "",
        usernameTrabajador = if (idTrabajador != null) "trabajador$idTrabajador" else "",
        nombreCategoria = categoriaNombre,
        ubicacionReferencia = ubicacionReferencia.orEmpty(),
        latitudReferencia = latitudReferencia,
        longitudReferencia = longitudReferencia,
        eliminada = false
    )
}

private fun String.toTipoPrecioLocal(): Int {
    return when (normalizarTexto(this)) {
        "precio fijo" -> TipoPrecio.FIJO
        "por hora" -> TipoPrecio.POR_HORA
        "a convenir" -> TipoPrecio.CONTACTAR
        else -> TipoPrecio.CONTACTAR
    }
}

private fun Int.nombreBackendTipoPrecio(): String {
    return when (this) {
        TipoPrecio.FIJO -> "Precio Fijo"
        TipoPrecio.POR_HORA -> "Por Hora"
        TipoPrecio.DESDE, TipoPrecio.CONTACTAR -> "A convenir"
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

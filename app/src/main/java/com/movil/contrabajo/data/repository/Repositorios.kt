package com.movil.contrabajo.data.repository

import com.movil.contrabajo.data.local.ContrabajoSQLiteHelper
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.FiltroMarketplaceConfig
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.PreguntasSeguridadCatalogo
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.Reporte
import com.movil.contrabajo.domain.model.TipoReporte
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import com.movil.contrabajo.domain.model.Valoracion
import com.movil.contrabajo.domain.model.ValoracionesServicio
import com.movil.contrabajo.domain.model.AccionModeracion
import com.movil.contrabajo.domain.model.EstadoReporte
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter

interface RepositorioAutenticacion {
    fun obtenerSesionActiva(): Usuario?
    fun iniciarSesion(identificador: String, contrasena: String, recordarme: Boolean): Result<Usuario>
    fun registrarUsuario(registro: RegistroPendiente): Result<Usuario>
    fun obtenerPreguntasRecuperacion(identificador: String): Result<List<PreguntaSeguridadConfig>>
    fun validarRespuestasRecuperacion(identificador: String, respuesta1: String, respuesta2: String): Result<Unit>
    fun restablecerContrasenaRecuperacion(
        identificador: String,
        respuesta1: String,
        respuesta2: String,
        nuevaContrasena: String,
        confirmarContrasena: String
    ): Result<Unit>
    fun cerrarSesion()
}

interface RepositorioPerfil {
    fun obtenerPerfilActual(): Usuario?
    fun solicitarVerificacionTrabajador(run: String, dv: String, numeroDocumento: String): Result<Usuario>
    fun obtenerPreguntasSeguridad(): List<PreguntaSeguridadConfig>
    fun guardarPreguntaSeguridad(indice: Int, pregunta: String, respuesta: String): Result<List<PreguntaSeguridadConfig>>
    fun obtenerUbicacionAjustes(): UbicacionAjustesConfig
    fun guardarUbicacionAjustes(config: UbicacionAjustesConfig): Result<UbicacionAjustesConfig>
    fun obtenerFiltrosMarketplace(): FiltroMarketplaceConfig
    fun guardarFiltrosMarketplace(config: FiltroMarketplaceConfig): Result<FiltroMarketplaceConfig>
    fun limpiarFiltrosMarketplace(): Result<FiltroMarketplaceConfig>
    fun actualizarFotoPerfil(uriLocal: String): Result<Usuario>
    fun actualizarContactoPerfil(correo: String, telefono: String): Result<Usuario>
}

interface RepositorioReportes {
    fun obtenerTiposReporte(): List<TipoReporte>
    fun crearReporteDesdeOferta(idOfertaServicio: Long, idTipoReporte: Long, comentario: String): Result<Reporte>
    fun crearReporteDesdeChat(idChatCita: Long, idTipoReporte: Long, comentario: String): Result<Reporte>
    fun obtenerReportesModeracion(
        busqueda: String = "",
        idTipoReporte: Long? = null,
        estadoRevision: String? = null,
        ordenarRecientes: Boolean = true
    ): List<Reporte>
    fun obtenerDetalleReporte(idReporte: Long): Reporte?
    fun aplicarMedidaModeracion(idReporte: Long, accion: String): Result<Reporte>
}

interface RepositorioOfertas {
    fun obtenerOfertaPrincipal(): OfertaServicio?
    fun obtenerOfertasMarketplace(busqueda: String = ""): List<OfertaServicio>
    fun obtenerOfertaPorId(idOfertaServicio: Long, incluirEliminadas: Boolean = false): OfertaServicio?
    fun obtenerOfertasPropias(): List<OfertaServicio>
    fun obtenerOfertaPropiaActual(): OfertaServicio?
    fun obtenerOfertaPropiaPorId(idOfertaServicio: Long): OfertaServicio?
    fun obtenerIdsOfertasConTrabajoEnCursoPropias(): Set<Long>
    fun obtenerValoracionesPropiasPorServicio(): List<ValoracionesServicio>
    fun obtenerCategoriasServicio(): List<CategoriaServicio>
    fun guardarOfertaPropia(formulario: FormularioServicio, idOfertaServicio: Long? = null): Result<OfertaServicio>
    fun actualizarDisponibilidadOfertaPropia(idOfertaServicio: Long, disponible: Boolean): Result<OfertaServicio>
    fun eliminarOfertaPropia(idOfertaServicio: Long): Result<Unit>
}

interface RepositorioChats {
    fun obtenerIdUsuarioActual(): Long?
    fun obtenerChatsActuales(): List<ChatCita>
    fun obtenerMensajes(idChatCita: Long): List<MensajeChat>
    fun iniciarConversacionDesdeOferta(idOfertaServicio: Long): Result<ChatCita>
    fun obtenerChat(idChatCita: Long): ChatCita?
    fun enviarMensaje(idChatCita: Long, contenido: String): Result<MensajeChat>
    fun crearCitaDesdeChat(idChatCita: Long, fechaProgramada: String, comentario: String, precioAcordado: Int = 0): Result<CitaServicio>
    fun obtenerCitaPorChat(idChatCita: Long): CitaServicio?
    fun aceptarCitaTrabajador(idChatCita: Long): Result<CitaServicio>
    fun rechazarCitaTrabajador(idChatCita: Long): Result<CitaServicio>
    fun reenviarPropuestaCitaCliente(idChatCita: Long): Result<CitaServicio>
    fun solicitarInicioTrabajoTrabajador(idChatCita: Long): Result<CitaServicio>
    fun aceptarInicioTrabajoCliente(idChatCita: Long): Result<CitaServicio>
    fun solicitarFinalizarTrabajoTrabajador(idChatCita: Long): Result<CitaServicio>
    fun aceptarFinalizarTrabajoCliente(idChatCita: Long): Result<CitaServicio>
    fun cerrarChat(idChatCita: Long): Result<ChatCita>
    fun obtenerValoracionPorChat(idChatCita: Long): Valoracion?
    fun guardarValoracionChat(idChatCita: Long, voto: Int, comentario: String): Result<Valoracion>
    fun obtenerNotificacionesPendientes(): List<NotificacionMensajePendiente>
    fun marcarNotificacionesComoMostradas(idsMensaje: List<Long>)
}

class RepositorioAutenticacionLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioAutenticacion {

    override fun obtenerSesionActiva(): Usuario? = db.obtenerUsuarioSesionActiva()

    override fun iniciarSesion(
        identificador: String,
        contrasena: String,
        recordarme: Boolean
    ): Result<Usuario> {
        val usuario = db.obtenerUsuarioPorCorreoOCuenta(identificador.trim(), contrasena.trim())
            ?: return Result.failure(IllegalArgumentException("Credenciales invalidas"))
        db.guardarSesion(usuario.idUsuario, recordarme)
        return Result.success(usuario)
    }

    override fun registrarUsuario(registro: RegistroPendiente): Result<Usuario> {
        val error = validarRegistro(registro)
        if (error != null) return Result.failure(IllegalArgumentException(error))
        if (db.existeUsuario(registro.correo.trim(), registro.username.trim())) {
            return Result.failure(IllegalArgumentException("El correo o nombre de usuario ya esta registrado"))
        }
        if (db.existeRun(limpiarRun(registro.run), registro.dv.trim().uppercase())) {
            return Result.failure(IllegalArgumentException("El RUN ya esta registrado"))
        }

        val usuario = Usuario(
            run = limpiarRun(registro.run),
            dv = registro.dv.trim().uppercase(),
            username = registro.username.trim(),
            nombre = registro.nombre.trim(),
            apellidoPaterno = registro.apellidoPaterno.trim(),
            apellidoMaterno = registro.apellidoMaterno.trim(),
            telefono = registro.telefono.trim(),
            correo = registro.correo.trim(),
            contrasenaHash = registro.contrasena,
            fechaRegistro = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            fechaNacimiento = registro.fechaNacimiento,
            verificado = false,
            tipoPerfil = TipoPerfil.USUARIO_BASE,
            numeroDocumentoIdentidad = null,
            preguntaRecuperacion = "",
            respuestaRecuperacion = "",
            verificacionTrabajadorPendiente = false,
            fechaSolicitudVerificacionMs = null
        )

        val id = db.insertarUsuario(usuario)
        if (id <= 0) return Result.failure(IllegalStateException("No se pudo registrar el usuario"))
        db.guardarPreguntaSeguridad(
            idUsuario = id,
            indice = 1,
            pregunta = registro.preguntaSeguridad1.trim(),
            respuesta = registro.respuestaSeguridad1.trim()
        )
        db.guardarPreguntaSeguridad(
            idUsuario = id,
            indice = 2,
            pregunta = registro.preguntaSeguridad2.trim(),
            respuesta = registro.respuestaSeguridad2.trim()
        )
        db.guardarUbicacionUsuario(id, normalizarUbicacionRegistro(registro))
        db.guardarSesion(id, true)
        return Result.success(db.obtenerUsuarioPorId(id) ?: usuario.copy(idUsuario = id))
    }

    override fun obtenerPreguntasRecuperacion(identificador: String): Result<List<PreguntaSeguridadConfig>> {
        val usuario = db.obtenerUsuarioPorIdentificador(identificador.trim())
            ?: return Result.failure(IllegalArgumentException("No existe una cuenta con ese usuario o correo"))
        val preguntas = db.obtenerPreguntasSeguridad(usuario.idUsuario)
            .filter { it.indice in 1..2 }
        if (preguntas.size < 2 || preguntas.any { !it.configurada }) {
            return Result.failure(IllegalStateException("La cuenta aun no tiene preguntas de seguridad configuradas"))
        }
        return Result.success(preguntas.sortedBy { it.indice })
    }

    override fun validarRespuestasRecuperacion(identificador: String, respuesta1: String, respuesta2: String): Result<Unit> {
        val usuario = db.obtenerUsuarioPorIdentificador(identificador.trim())
            ?: return Result.failure(IllegalArgumentException("No existe una cuenta con ese usuario o correo"))
        val preguntas = db.obtenerPreguntasSeguridad(usuario.idUsuario)
            .filter { it.indice in 1..2 }
            .sortedBy { it.indice }
        if (preguntas.size < 2) {
            return Result.failure(IllegalStateException("No se pudieron cargar las preguntas de seguridad de la cuenta"))
        }
        val r1 = respuesta1.trim()
        val r2 = respuesta2.trim()
        if (r1.isBlank() || r2.isBlank()) {
            return Result.failure(IllegalArgumentException("Debes responder ambas preguntas de seguridad"))
        }
        val coincide = preguntas[0].respuesta.equals(r1, ignoreCase = true) &&
            preguntas[1].respuesta.equals(r2, ignoreCase = true)
        return if (coincide) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Las respuestas de seguridad no coinciden"))
        }
    }

    override fun restablecerContrasenaRecuperacion(
        identificador: String,
        respuesta1: String,
        respuesta2: String,
        nuevaContrasena: String,
        confirmarContrasena: String
    ): Result<Unit> {
        validarRespuestasRecuperacion(identificador, respuesta1, respuesta2)
            .onFailure { return Result.failure(it) }

        val usuario = db.obtenerUsuarioPorIdentificador(identificador.trim())
            ?: return Result.failure(IllegalArgumentException("No existe una cuenta con ese usuario o correo"))
        val nueva = nuevaContrasena.trim()
        val confirmar = confirmarContrasena.trim()
        if (nueva.length < 6) {
            return Result.failure(IllegalArgumentException("La contrasena debe tener al menos 6 caracteres"))
        }
        if (nueva != confirmar) {
            return Result.failure(IllegalArgumentException("Las contrasenas no coinciden"))
        }
        db.actualizarContrasenaUsuario(usuario.idUsuario, nueva)
        return Result.success(Unit)
    }

    override fun cerrarSesion() {
        db.cerrarSesion()
    }

    private fun validarRegistro(registro: RegistroPendiente): String? {
        val errorFechaNacimiento = if (registro.fechaNacimiento.isBlank()) {
            "Ingresa tu fecha de nacimiento"
        } else {
            validarFechaNacimiento(registro.fechaNacimiento)
        }

        return when {
            registro.nombre.isBlank() -> "Ingresa tu nombre"
            !esNombrePersonaValido(registro.nombre) -> "El nombre solo puede contener letras"
            registro.apellidoPaterno.isBlank() -> "Ingresa tu apellido paterno"
            !esNombrePersonaValido(registro.apellidoPaterno) -> "El apellido paterno solo puede contener letras"
            registro.apellidoMaterno.isBlank() -> "Ingresa tu apellido materno"
            !esNombrePersonaValido(registro.apellidoMaterno) -> "El apellido materno solo puede contener letras"
            registro.run.isBlank() || registro.dv.isBlank() -> "Ingresa un RUN valido"
            limpiarRun(registro.run).length != 8 -> "El RUN debe tener exactamente 8 digitos"
            !validarRut(registro.run, registro.dv) -> "El RUN no es valido"
            digitosTelefono(registro.telefono).length != 9 -> "Ingresa un telefono valido de 9 digitos"
            registro.username.isBlank() -> "Ingresa un nombre de usuario"
            registro.correo.isBlank() || !registro.correo.contains("@") -> "Ingresa un correo valido"
            errorFechaNacimiento != null -> errorFechaNacimiento
            registro.contrasena.length < 6 -> "La contrasena debe tener al menos 6 caracteres"
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

    private fun esNombrePersonaValido(texto: String): Boolean {
        val limpio = texto.trim()
        if (limpio.isBlank()) return false
        return limpio.all { caracter ->
            caracter.isLetter() || caracter == ' ' || caracter == '\'' || caracter == '-'
        }
    }

    private fun limpiarRun(run: String): String = run.filter { it.isDigit() }
    private fun digitosTelefono(telefono: String): String {
        var digitos = telefono.filter { it.isDigit() }
        if (digitos.startsWith("56")) digitos = digitos.drop(2)
        return digitos
    }

    private fun validarRut(runRaw: String, dvRaw: String): Boolean {
        val run = limpiarRun(runRaw)
        if (run.length != 8) return false
        val dv = dvRaw.trim().uppercase()
        if (dv.isBlank()) return false

        var suma = 0
        var multiplicador = 2
        for (i in run.length - 1 downTo 0) {
            suma += (run[i] - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
        return dv == dvEsperado
    }

    private fun validarFechaNacimiento(fecha: String): String? {
        val fechaNacimiento = try {
            LocalDate.parse(fecha.trim())
        } catch (_: DateTimeParseException) {
            return "La fecha de nacimiento debe tener formato yyyy-MM-dd"
        }
        val anio = fechaNacimiento.year
        return when {
            anio < 1926 || anio > 2026 -> "El año de nacimiento debe estar entre 1926 y 2026"
            else -> null
        }
    }

    private fun normalizarUbicacionRegistro(registro: RegistroPendiente): UbicacionAjustesConfig {
        return UbicacionAjustesConfig(
            region = registro.region.trim().ifBlank { "Region Metropolitana" },
            comuna = registro.comuna.trim().ifBlank { "Sin comuna" },
            calle = registro.calle.trim().ifBlank { "Sin calle" },
            numero = registro.numeroDireccion.trim().ifBlank { "Sin numero" },
            detalle = "Sin detalle",
            latitud = registro.latitud,
            longitud = registro.longitud
        )
    }
}

class RepositorioPerfilLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioPerfil {
    override fun obtenerPerfilActual(): Usuario? = db.obtenerUsuarioSesionActiva()

    override fun solicitarVerificacionTrabajador(
        run: String,
        dv: String,
        numeroDocumento: String
    ): Result<Usuario> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (usuario.tipoPerfil != TipoPerfil.USUARIO_BASE) {
            return Result.failure(IllegalStateException("Solo un usuario base puede solicitar verificacion"))
        }
        val documentoNormalizado = numeroDocumento.filter { it.isDigit() }.take(9)
        if (documentoNormalizado.length != 9) {
            return Result.failure(IllegalArgumentException("El numero de documento debe tener 9 digitos"))
        }
        return db.solicitarVerificacionTrabajador(
            idUsuario = usuario.idUsuario,
            run = limpiarRun(run),
            dv = dv.trim().uppercase(),
            numeroDocumento = documentoNormalizado
        ).mapCatching { db.obtenerUsuarioPorId(usuario.idUsuario) ?: usuario }
    }

    override fun obtenerPreguntasSeguridad(): List<PreguntaSeguridadConfig> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
        return db.obtenerPreguntasSeguridad(usuario.idUsuario)
    }

    override fun guardarPreguntaSeguridad(
        indice: Int,
        pregunta: String,
        respuesta: String
    ): Result<List<PreguntaSeguridadConfig>> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (indice !in 1..2) {
            return Result.failure(IllegalArgumentException("Indice de pregunta invalido"))
        }
        val preguntaNormalizada = pregunta.trim()
        if (!PreguntasSeguridadCatalogo.esValida(preguntaNormalizada)) {
            return Result.failure(IllegalArgumentException("Selecciona una pregunta valida"))
        }
        if (respuesta.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa la respuesta de seguridad"))
        }
        val indiceAlterno = if (indice == 1) 2 else 1
        val preguntasActuales = db.obtenerPreguntasSeguridad(usuario.idUsuario)
        val preguntaAlterna = preguntasActuales.firstOrNull { it.indice == indiceAlterno }?.pregunta.orEmpty()
        if (preguntaAlterna.equals(preguntaNormalizada, ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Debes elegir preguntas diferentes"))
        }
        db.guardarPreguntaSeguridad(
            idUsuario = usuario.idUsuario,
            indice = indice,
            pregunta = preguntaNormalizada,
            respuesta = respuesta.trim()
        )
        return Result.success(db.obtenerPreguntasSeguridad(usuario.idUsuario))
    }

    override fun obtenerUbicacionAjustes(): UbicacionAjustesConfig {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return UbicacionAjustesConfig()
        return db.obtenerUbicacionUsuario(usuario.idUsuario)
    }

    override fun guardarUbicacionAjustes(config: UbicacionAjustesConfig): Result<UbicacionAjustesConfig> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        db.guardarUbicacionUsuario(usuario.idUsuario, config)
        return Result.success(db.obtenerUbicacionUsuario(usuario.idUsuario))
    }

    override fun obtenerFiltrosMarketplace(): FiltroMarketplaceConfig {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return FiltroMarketplaceConfig()
        return db.obtenerFiltrosMarketplace(usuario.idUsuario)
    }

    override fun guardarFiltrosMarketplace(config: FiltroMarketplaceConfig): Result<FiltroMarketplaceConfig> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        db.guardarFiltrosMarketplace(usuario.idUsuario, config)
        return Result.success(db.obtenerFiltrosMarketplace(usuario.idUsuario))
    }

    override fun limpiarFiltrosMarketplace(): Result<FiltroMarketplaceConfig> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        db.limpiarFiltrosMarketplace(usuario.idUsuario)
        return Result.success(db.obtenerFiltrosMarketplace(usuario.idUsuario))
    }

    override fun actualizarFotoPerfil(uriLocal: String): Result<Usuario> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (uriLocal.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecciona una foto valida"))
        }
        db.actualizarFotoPerfilUsuario(usuario.idUsuario, uriLocal)
        return Result.success(db.obtenerUsuarioPorId(usuario.idUsuario) ?: usuario)
    }

    override fun actualizarContactoPerfil(correo: String, telefono: String): Result<Usuario> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val correoNormalizado = correo.trim().lowercase()
        val telefonoNormalizado = telefono.trim()
        if (correoNormalizado.isBlank() || !correoNormalizado.contains("@")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo valido"))
        }
        val digitos = telefonoNormalizado.filter { it.isDigit() }.let {
            if (it.startsWith("56")) it.drop(2) else it
        }
        if (digitos.length != 9) {
            return Result.failure(IllegalArgumentException("Ingresa un telefono valido de 9 digitos"))
        }
        if (db.existeCorreoEnOtroUsuario(correoNormalizado, usuario.idUsuario)) {
            return Result.failure(IllegalArgumentException("Ese correo ya esta registrado por otro usuario"))
        }

        db.actualizarPerfilContactoUsuario(
            idUsuario = usuario.idUsuario,
            correo = correoNormalizado,
            telefono = telefonoNormalizado
        )
        return Result.success(db.obtenerUsuarioPorId(usuario.idUsuario) ?: usuario)
    }

    private fun limpiarRun(run: String): String = run.filter { it.isDigit() }
}

class RepositorioReportesLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioReportes {

    override fun obtenerTiposReporte(): List<TipoReporte> = db.obtenerTiposReporte()

    override fun crearReporteDesdeOferta(
        idOfertaServicio: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> {
        val emisor = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val oferta = db.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = true)
            ?: return Result.failure(IllegalArgumentException("No se encontro la publicacion a reportar"))
        val comentarioLimpio = comentario.trim()
        if (comentarioLimpio.isBlank()) {
            return Result.failure(IllegalArgumentException("Debes ingresar una descripcion del incidente"))
        }
        if (db.obtenerTiposReporte().none { it.idTipoReporte == idTipoReporte }) {
            return Result.failure(IllegalArgumentException("Selecciona un tipo de reporte valido"))
        }
        val idReportado = if (oferta.idTrabajador != emisor.idUsuario) oferta.idTrabajador else null
        if (idReportado == null && oferta.idOfertaServicio <= 0L) {
            return Result.failure(IllegalStateException("No fue posible identificar la entidad reportada"))
        }

        val idReporte = db.insertarReporte(
            idEmisor = emisor.idUsuario,
            idUsuarioReportado = idReportado,
            idOfertaServicio = oferta.idOfertaServicio,
            idChatCita = null,
            idTipoReporte = idTipoReporte,
            comentario = comentarioLimpio
        )
        if (idReporte <= 0) return Result.failure(IllegalStateException("No se pudo registrar el reporte"))
        return db.obtenerReportePorId(idReporte)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar el reporte registrado"))
    }

    override fun crearReporteDesdeChat(
        idChatCita: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> {
        val emisor = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, emisor.idUsuario)
            ?: return Result.failure(IllegalArgumentException("No se encontro el chat a reportar"))
        val comentarioLimpio = comentario.trim()
        if (comentarioLimpio.isBlank()) {
            return Result.failure(IllegalArgumentException("Debes ingresar una descripcion del incidente"))
        }
        if (db.obtenerTiposReporte().none { it.idTipoReporte == idTipoReporte }) {
            return Result.failure(IllegalArgumentException("Selecciona un tipo de reporte valido"))
        }
        val idReportado = if (chat.idCliente == emisor.idUsuario) chat.idTrabajador else chat.idCliente

        val idReporte = db.insertarReporte(
            idEmisor = emisor.idUsuario,
            idUsuarioReportado = idReportado,
            idOfertaServicio = chat.idOfertaServicio,
            idChatCita = chat.idChatCita,
            idTipoReporte = idTipoReporte,
            comentario = comentarioLimpio
        )
        if (idReporte <= 0) return Result.failure(IllegalStateException("No se pudo registrar el reporte"))
        return db.obtenerReportePorId(idReporte)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar el reporte registrado"))
    }

    override fun obtenerReportesModeracion(
        busqueda: String,
        idTipoReporte: Long?,
        estadoRevision: String?,
        ordenarRecientes: Boolean
    ): List<Reporte> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
        if (usuario.tipoPerfil != TipoPerfil.MODERADOR) return emptyList()
        return db.obtenerReportesModeracion(
            busqueda = busqueda,
            idTipoReporte = idTipoReporte,
            estadoRevision = estadoRevision,
            ordenarRecientes = ordenarRecientes
        )
    }

    override fun obtenerDetalleReporte(idReporte: Long): Reporte? {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return null
        if (usuario.tipoPerfil != TipoPerfil.MODERADOR) return null
        return db.obtenerReportePorId(idReporte)
    }

    override fun aplicarMedidaModeracion(idReporte: Long, accion: String): Result<Reporte> {
        val moderador = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        if (moderador.tipoPerfil != TipoPerfil.MODERADOR) {
            return Result.failure(IllegalStateException("Solo un moderador puede aplicar medidas"))
        }
        val reporte = db.obtenerReportePorId(idReporte)
            ?: return Result.failure(IllegalArgumentException("No se encontro el reporte"))
        val idOferta = reporte.idOfertaServicio
            ?: return Result.failure(IllegalStateException("El reporte no tiene un servicio asociado"))
        when (accion) {
            AccionModeracion.DESACTIVAR_SERVICIO -> db.actualizarDisponibilidadOferta(idOferta, false)
            AccionModeracion.ELIMINAR_SERVICIO -> db.eliminarOfertaServicio(idOferta)
            else -> return Result.failure(IllegalArgumentException("Accion de moderacion invalida"))
        }
        val ok = db.actualizarEstadoRevisionReporte(
            idReporte = idReporte,
            estadoRevision = EstadoReporte.RESUELTO,
            idModeradorRevisor = moderador.idUsuario,
            medidaAplicada = accion
        )
        if (!ok) return Result.failure(IllegalStateException("No se pudo actualizar el estado del reporte"))
        return db.obtenerReportePorId(idReporte)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar el reporte actualizado"))
    }
}

class RepositorioOfertasLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioOfertas {
    override fun obtenerOfertaPrincipal(): OfertaServicio? = db.obtenerOfertaPrincipal()
    override fun obtenerOfertasMarketplace(busqueda: String): List<OfertaServicio> = db.obtenerOfertasMarketplace(busqueda)
    override fun obtenerOfertaPorId(idOfertaServicio: Long, incluirEliminadas: Boolean): OfertaServicio? =
        db.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas)

    override fun obtenerOfertasPropias(): List<OfertaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
        return db.obtenerOfertasPorTrabajador(usuario.idUsuario, incluirEliminadas = false)
    }

    override fun obtenerOfertaPropiaActual(): OfertaServicio? {
        return obtenerOfertasPropias().firstOrNull()
    }
    override fun obtenerOfertaPropiaPorId(idOfertaServicio: Long): OfertaServicio? {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return null
        return db.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = false)
            ?.takeIf { it.idTrabajador == usuario.idUsuario }
    }

    override fun obtenerIdsOfertasConTrabajoEnCursoPropias(): Set<Long> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptySet()
        return db.obtenerIdsOfertasConTrabajoEnCursoPorTrabajador(usuario.idUsuario).toSet()
    }

    override fun obtenerValoracionesPropiasPorServicio(): List<ValoracionesServicio> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
        val ofertas = db.obtenerOfertasPorTrabajador(usuario.idUsuario, incluirEliminadas = true)
        return ofertas.map { oferta ->
            ValoracionesServicio(
                oferta = oferta,
                valoraciones = db.obtenerValoracionesPorOferta(oferta.idOfertaServicio)
            )
        }
    }

    override fun obtenerCategoriasServicio(): List<CategoriaServicio> = db.obtenerCategoriasServicio()

    override fun guardarOfertaPropia(formulario: FormularioServicio, idOfertaServicio: Long?): Result<OfertaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay una sesion activa para guardar el servicio"))
        if (usuario.tipoPerfil !in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)) {
            return Result.failure(IllegalStateException("Debes verificarte como trabajador para publicar servicios"))
        }

        val error = validarFormularioServicio(formulario)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val maximoServiciosTotales = 3
        val maximoServiciosActivos = 1
        val ofertaExistente = idOfertaServicio?.takeIf { it > 0 }?.let { ofertaId ->
            db.obtenerOfertaPorId(ofertaId, incluirEliminadas = false)?.takeIf { it.idTrabajador == usuario.idUsuario }
                ?: return Result.failure(IllegalStateException("No existe el servicio que intentas editar"))
        }
        val esEdicion = ofertaExistente != null
        if (!esEdicion && db.contarOfertasPorTrabajador(usuario.idUsuario) >= maximoServiciosTotales) {
            return Result.failure(
                IllegalStateException("Puedes tener hasta $maximoServiciosTotales servicios en total")
            )
        }
        val activosSinObjetivo = if (esEdicion) {
            db.contarOfertasActivasPorTrabajadorExcluyendo(usuario.idUsuario, ofertaExistente.idOfertaServicio)
        } else {
            db.contarOfertasActivasPorTrabajador(usuario.idUsuario)
        }
        val disponibleNormalizado = if (!esEdicion && activosSinObjetivo >= maximoServiciosActivos) {
            false
        } else {
            formulario.disponible
        }
        if (esEdicion && disponibleNormalizado && activosSinObjetivo >= maximoServiciosActivos) {
            return Result.failure(
                IllegalStateException("Ya tienes un servicio activo. Desactívalo antes de activar otro.")
            )
        }
        val formularioNormalizado = formulario.copy(disponible = disponibleNormalizado)
        val idFotoPortada = when {
            formularioNormalizado.foto == null -> ofertaExistente?.idFotoPortada
            formularioNormalizado.foto.idFoto != null -> {
                db.actualizarFotoServicio(formularioNormalizado.foto.idFoto, formularioNormalizado.foto)
                formularioNormalizado.foto.idFoto
            }

            else -> db.insertarFotoServicio(formularioNormalizado.foto)
        }

        val idOferta = if (ofertaExistente == null) {
            db.insertarOfertaServicio(usuario.idUsuario, formularioNormalizado, idFotoPortada)
        } else {
            db.actualizarOfertaServicio(ofertaExistente.idOfertaServicio, formularioNormalizado, idFotoPortada)
            ofertaExistente.idOfertaServicio
        }

        val ofertaActualizada = db.obtenerOfertaPorId(idOferta, incluirEliminadas = false)
        return if (ofertaActualizada != null) {
            Result.success(ofertaActualizada)
        } else {
            Result.failure(IllegalStateException("No se pudo guardar el servicio"))
        }
    }

    override fun actualizarDisponibilidadOfertaPropia(idOfertaServicio: Long, disponible: Boolean): Result<OfertaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay una sesion activa para actualizar disponibilidad"))
        val oferta = db.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = false)
            ?.takeIf { it.idTrabajador == usuario.idUsuario }
            ?: return Result.failure(IllegalStateException("No existe un servicio propio para actualizar"))
        if (db.existeTrabajoEnCursoPorOferta(oferta.idOfertaServicio)) {
            return Result.failure(IllegalStateException("No puedes cambiar disponibilidad mientras el servicio esta En Curso"))
        }
        if (disponible) {
            val activosSinObjetivo = db.contarOfertasActivasPorTrabajadorExcluyendo(
                idTrabajador = usuario.idUsuario,
                idOfertaExcluir = oferta.idOfertaServicio
            )
            if (activosSinObjetivo >= 1) {
                return Result.failure(IllegalStateException("Ya tienes un servicio activo. Desactívalo antes de activar otro."))
            }
        }

        db.actualizarDisponibilidadOferta(oferta.idOfertaServicio, disponible)
        val actualizada = db.obtenerOfertaPorId(oferta.idOfertaServicio, incluirEliminadas = false)
        return if (actualizada != null) {
            Result.success(actualizada)
        } else {
            Result.failure(IllegalStateException("No se pudo actualizar la disponibilidad"))
        }
    }

    override fun eliminarOfertaPropia(idOfertaServicio: Long): Result<Unit> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay una sesion activa para eliminar el servicio"))
        val oferta = db.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = false)
            ?.takeIf { it.idTrabajador == usuario.idUsuario }
            ?: return Result.failure(IllegalStateException("No existe un servicio propio para eliminar"))
        db.eliminarOfertaServicio(oferta.idOfertaServicio)
        return Result.success(Unit)
    }

    private fun validarFormularioServicio(formulario: FormularioServicio): String? = when {
        formulario.titulo.isBlank() -> "Ingresa un titulo para tu servicio"
        formulario.titulo.trim().length > 80 -> "El titulo permite hasta 80 caracteres"
        formulario.descripcion.isBlank() -> "Ingresa la descripcion del servicio"
        formulario.descripcion.trim().length > 500 -> "La descripcion permite hasta 500 caracteres"
        formulario.tipoPrecio !in listOf(
            TipoPrecio.FIJO,
            TipoPrecio.POR_HORA,
            TipoPrecio.DESDE,
            TipoPrecio.CONTACTAR
        ) -> "Selecciona un tipo de precio valido"
        !PrecioUtils.esMontoValido(formulario.tipoPrecio, formulario.montoBase) ->
            "El monto debe estar entre ${PrecioUtils.MIN_MONTO} y ${PrecioUtils.MAX_MONTO}"
        formulario.idCategoriaServicio == null -> "Selecciona una categoria"
        formulario.foto == null || formulario.foto.uriLocal.isBlank() -> "Selecciona una foto para tu servicio"
        else -> null
    }
}

class RepositorioChatsLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioChats {
    override fun obtenerIdUsuarioActual(): Long? = db.obtenerUsuarioSesionActiva()?.idUsuario

    override fun obtenerChatsActuales(): List<ChatCita> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
        db.marcarMensajesRecibidos(idReceptor = usuario.idUsuario)
        return db.obtenerChatsParaUsuario(usuario.idUsuario)
    }

    override fun obtenerMensajes(idChatCita: Long): List<MensajeChat> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
        db.marcarMensajesRecibidos(idReceptor = usuario.idUsuario)
        db.marcarMensajesLeidos(idChatCita = idChatCita, idReceptor = usuario.idUsuario)
        return db.obtenerMensajesPorChat(idChatCita)
    }

    override fun iniciarConversacionDesdeOferta(idOfertaServicio: Long): Result<ChatCita> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val ofertaContacto = db.obtenerOfertaParaContacto(idOfertaServicio)
            ?: return Result.failure(IllegalArgumentException("La oferta no existe"))
        val idTrabajador = ofertaContacto.first
        if (usuario.idUsuario == idTrabajador) {
            return Result.failure(IllegalStateException("No puedes iniciar chat con tu propia publicacion"))
        }

        val idChatExistente = db.obtenerChatEntreUsuarios(
            idTrabajador = idTrabajador,
            idCliente = usuario.idUsuario,
            idOfertaServicio = idOfertaServicio
        )

        val idChat = if (idChatExistente != null) {
            val chatExistente = db.obtenerChatPorId(idChatExistente, usuario.idUsuario)
            if (chatExistente != null) {
                val estadoCita = chatExistente.estadoCita
                val citaCerrada = estadoCita in setOf(
                    EstadoCita.CERRADO,
                    EstadoCita.FINALIZADO,
                    EstadoCita.CANCELADO
                )
                if (chatExistente.chatCerrado && citaCerrada) {
                    db.crearChatCita(
                        idTrabajador = idTrabajador,
                        idCliente = usuario.idUsuario,
                        idOfertaServicio = idOfertaServicio
                    )
                } else {
                    if (chatExistente.chatCerrado) {
                        db.actualizarChatCerrado(idChatCita = idChatExistente, cerrado = false, bloqueadoHastaMs = null)
                    }
                    idChatExistente
                }
            } else {
                db.crearChatCita(
                    idTrabajador = idTrabajador,
                    idCliente = usuario.idUsuario,
                    idOfertaServicio = idOfertaServicio
                )
            }
        } else {
            db.crearChatCita(
                idTrabajador = idTrabajador,
                idCliente = usuario.idUsuario,
                idOfertaServicio = idOfertaServicio
            )
        }

        if (idChat <= 0) return Result.failure(IllegalStateException("No se pudo crear el chat"))

        return db.obtenerChatPorId(idChat, usuario.idUsuario)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo abrir el chat"))
    }

    override fun obtenerChat(idChatCita: Long): ChatCita? {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return null
        return db.obtenerChatPorId(idChatCita, usuario.idUsuario)
    }

    override fun enviarMensaje(idChatCita: Long, contenido: String): Result<MensajeChat> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (chat.chatCerrado) {
            return Result.failure(IllegalStateException("Este chat esta cerrado y es solo lectura."))
        }
        val texto = contenido.trim()
        if (texto.isBlank()) return Result.failure(IllegalArgumentException("Escribe un mensaje"))
        val idReceptor = if (chat.idCliente == usuario.idUsuario) chat.idTrabajador else chat.idCliente
        val idMensaje = db.insertarMensajeChat(
            idChatCita = idChatCita,
            idEmisor = usuario.idUsuario,
            idReceptor = idReceptor,
            contenido = texto
        )
        if (idMensaje <= 0) return Result.failure(IllegalStateException("No se pudo enviar el mensaje"))
        return db.obtenerMensajePorId(idMensaje)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo leer el mensaje enviado"))
    }

    override fun crearCitaDesdeChat(
        idChatCita: Long,
        fechaProgramada: String,
        comentario: String,
        precioAcordado: Int
    ): Result<CitaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (chat.chatCerrado) {
            return Result.failure(IllegalStateException("No puedes crear cita en un chat cerrado"))
        }
        if (chat.idCliente != usuario.idUsuario) {
            return Result.failure(IllegalStateException("Solo el cliente puede generar la cita"))
        }
        if (fechaProgramada.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa fecha y hora de cita"))
        }
        val formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val fechaProgramadaDateTime = try {
            LocalDateTime.parse(fechaProgramada.trim(), formateador)
        } catch (_: DateTimeParseException) {
            return Result.failure(IllegalArgumentException("Selecciona una fecha y hora validas para la cita"))
        }
        val ahora = LocalDateTime.now().withSecond(0).withNano(0)
        if (fechaProgramadaDateTime.isBefore(ahora)) {
            return Result.failure(IllegalArgumentException("La fecha de la cita debe ser desde el momento actual hacia adelante"))
        }
        if (comentario.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa comentario de la cita"))
        }
        if (db.obtenerCitaPorChat(idChatCita) != null) {
            return Result.failure(IllegalStateException("Este chat ya tiene una cita registrada"))
        }
        val idCita = db.crearCitaServicio(
            idChatCita = chat.idChatCita,
            fechaProgramada = fechaProgramadaDateTime.format(formateador),
            comentario = comentario.trim(),
            precioAcordado = precioAcordado.coerceAtLeast(0)
        )
        if (idCita <= 0) return Result.failure(IllegalStateException("No se pudo crear la cita"))
        val nombreCliente = "${usuario.nombre} ${usuario.apellidoPaterno}".trim()
        db.insertarMensajeChat(
            idChatCita = chat.idChatCita,
            idEmisor = usuario.idUsuario,
            idReceptor = chat.idTrabajador,
            contenido = "El usuario $nombreCliente genero la cita y esta esperando tu confirmacion."
        )
        return db.obtenerCitaPorChat(idChatCita)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la cita"))
    }

    override fun obtenerCitaPorChat(idChatCita: Long): CitaServicio? = db.obtenerCitaPorChat(idChatCita)

    override fun aceptarCitaTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(
            idChatCita = idChatCita,
            validarRol = { chat, usuario -> chat.idTrabajador == usuario.idUsuario },
            estadosPermitidos = setOf(EstadoCita.PENDIENTE),
            nuevoEstado = EstadoCita.HANDSHAKE,
            mensajeSistema = "El trabajador acepto la cita y condiciones."
        )

    override fun rechazarCitaTrabajador(idChatCita: Long): Result<CitaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (chat.chatCerrado) {
            return Result.failure(IllegalStateException("El chat esta cerrado y no admite cambios."))
        }
        if (chat.idTrabajador != usuario.idUsuario) {
            return Result.failure(IllegalStateException("No tienes permisos para esta accion."))
        }
        val cita = db.obtenerCitaPorChat(idChatCita)
            ?: return Result.failure(IllegalStateException("Este chat no tiene cita activa"))
        if (cita.estado != EstadoCita.PENDIENTE) {
            return Result.failure(IllegalStateException("Solo puedes rechazar citas pendientes."))
        }
        val estadoActualizado = db.actualizarEstadoCita(
            idCita = cita.idCita,
            nuevoEstado = EstadoCita.RECHAZADA,
            fechaInicioTrabajo = null,
            fechaFinTrabajo = null
        )
        if (!estadoActualizado) {
            return Result.failure(IllegalStateException("No se pudo actualizar la cita a rechazada."))
        }
        val idReceptor = if (chat.idCliente == usuario.idUsuario) chat.idTrabajador else chat.idCliente
        db.insertarMensajeChat(
            idChatCita = idChatCita,
            idEmisor = usuario.idUsuario,
            idReceptor = idReceptor,
            contenido = "El trabajador rechazo la propuesta actual. Puedes reenviar una nueva propuesta sobre esta misma cita."
        )
        return db.obtenerCitaPorChat(idChatCita)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la cita rechazada"))
    }

    override fun reenviarPropuestaCitaCliente(idChatCita: Long): Result<CitaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (chat.chatCerrado) {
            return Result.failure(IllegalStateException("El chat esta cerrado y no admite cambios."))
        }
        if (chat.idCliente != usuario.idUsuario) {
            return Result.failure(IllegalStateException("Solo el cliente puede reenviar la propuesta."))
        }
        val cita = db.obtenerCitaPorChat(idChatCita)
            ?: return Result.failure(IllegalStateException("Este chat no tiene cita activa"))
        if (cita.estado != EstadoCita.RECHAZADA) {
            return Result.failure(IllegalStateException("Solo puedes reenviar una cita en estado rechazada."))
        }

        val estadoActualizado = db.actualizarEstadoCita(
            idCita = cita.idCita,
            nuevoEstado = EstadoCita.PENDIENTE,
            fechaInicioTrabajo = cita.fechaInicioTrabajo,
            fechaFinTrabajo = cita.fechaFinTrabajo
        )
        if (!estadoActualizado) {
            return Result.failure(IllegalStateException("No se pudo reenviar la propuesta."))
        }
        val idReceptor = if (chat.idCliente == usuario.idUsuario) chat.idTrabajador else chat.idCliente
        db.insertarMensajeChat(
            idChatCita = idChatCita,
            idEmisor = usuario.idUsuario,
            idReceptor = idReceptor,
            contenido = "El cliente envio una nueva propuesta de cita para continuar la negociacion."
        )
        return db.obtenerCitaPorChat(idChatCita)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la cita reenviada"))
    }

    override fun solicitarInicioTrabajoTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(
            idChatCita = idChatCita,
            validarRol = { chat, usuario -> chat.idTrabajador == usuario.idUsuario },
            estadosPermitidos = setOf(EstadoCita.HANDSHAKE),
            nuevoEstado = EstadoCita.COMENZANDO,
            mensajeSistema = "El trabajador solicito iniciar el trabajo. Espera confirmacion del cliente."
        )

    override fun aceptarInicioTrabajoCliente(idChatCita: Long): Result<CitaServicio> =
        run {
            val usuario = db.obtenerUsuarioSesionActiva()
                ?: return@run Result.failure(IllegalStateException("No hay sesion activa"))
            val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
                ?: return@run Result.failure(IllegalArgumentException("Chat no encontrado"))
            if (chat.chatCerrado) {
                return@run Result.failure(IllegalStateException("El chat esta cerrado y no admite cambios."))
            }
            if (chat.idCliente != usuario.idUsuario) {
                return@run Result.failure(IllegalStateException("No tienes permisos para esta accion."))
            }
            val cita = db.obtenerCitaPorChat(idChatCita)
                ?: return@run Result.failure(IllegalStateException("Este chat no tiene cita activa"))
            if (cita.estado != EstadoCita.COMENZANDO) {
                return@run Result.failure(IllegalStateException("El estado actual no permite esta accion."))
            }
            val existeEnCurso = db.existeCitaEnProcesoTrabajador(
                idTrabajador = chat.idTrabajador,
                idCitaExcluir = cita.idCita
            )
            if (existeEnCurso) {
                return@run Result.failure(
                    IllegalStateException("Ya existe una cita en proceso para este trabajador. Finaliza esa cita antes de iniciar otra.")
                )
            }
            transicionarCita(
                idChatCita = idChatCita,
                validarRol = { chatLocal, usuarioLocal -> chatLocal.idCliente == usuarioLocal.idUsuario },
                estadosPermitidos = setOf(EstadoCita.COMENZANDO),
                nuevoEstado = EstadoCita.EN_PROCESO,
                mensajeSistema = "El cliente confirmo el inicio del trabajo.",
                fechaInicio = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now())
            )
        }

    override fun solicitarFinalizarTrabajoTrabajador(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(
            idChatCita = idChatCita,
            validarRol = { chat, usuario -> chat.idTrabajador == usuario.idUsuario },
            estadosPermitidos = setOf(EstadoCita.EN_PROCESO),
            nuevoEstado = EstadoCita.FINALIZANDO,
            mensajeSistema = "El trabajador solicito finalizar el trabajo. Espera confirmacion del cliente."
        )

    override fun aceptarFinalizarTrabajoCliente(idChatCita: Long): Result<CitaServicio> =
        transicionarCita(
            idChatCita = idChatCita,
            validarRol = { chat, usuario -> chat.idCliente == usuario.idUsuario },
            estadosPermitidos = setOf(EstadoCita.FINALIZANDO),
            nuevoEstado = EstadoCita.FINALIZADO,
            mensajeSistema = "El cliente confirmo la finalizacion del trabajo.",
            fechaFin = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now())
        )

    override fun cerrarChat(idChatCita: Long): Result<ChatCita> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        val idReceptor = if (chat.idCliente == usuario.idUsuario) chat.idTrabajador else chat.idCliente

        val cita = db.obtenerCitaPorChat(idChatCita)
        if (cita != null) {
            val estadoFinal = if (cita.estado == EstadoCita.FINALIZADO) EstadoCita.CERRADO else EstadoCita.CANCELADO
            db.actualizarEstadoCita(
                idCita = cita.idCita,
                nuevoEstado = estadoFinal,
                fechaInicioTrabajo = null,
                fechaFinTrabajo = null
            )
        }

        db.insertarMensajeChat(
            idChatCita = idChatCita,
            idEmisor = usuario.idUsuario,
            idReceptor = idReceptor,
            contenido = "El chat fue finalizado. Queda disponible solo para lectura."
        )

        val actualizado = db.actualizarChatCerrado(
            idChatCita = idChatCita,
            cerrado = true,
            bloqueadoHastaMs = null
        )
        if (!actualizado) return Result.failure(IllegalStateException("No se pudo cerrar el chat"))
        return db.obtenerChatPorId(idChatCita, usuario.idUsuario)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recargar el chat cerrado"))
    }

    override fun obtenerValoracionPorChat(idChatCita: Long): Valoracion? {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return null
        return db.obtenerValoracionPorChat(idChatCita = idChatCita, idCliente = usuario.idUsuario)
    }

    override fun guardarValoracionChat(idChatCita: Long, voto: Int, comentario: String): Result<Valoracion> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (chat.idCliente != usuario.idUsuario) {
            return Result.failure(IllegalStateException("Solo el cliente puede valorar este contacto"))
        }
        if (!chat.chatCerrado) {
            return Result.failure(IllegalStateException("Debes finalizar el chat antes de valorar"))
        }
        val cita = db.obtenerCitaPorChat(idChatCita)
        val estadoFinalizable = cita?.estado in setOf(EstadoCita.FINALIZADO, EstadoCita.CERRADO)
        if (!estadoFinalizable) {
            return Result.failure(IllegalStateException("Solo puedes valorar cuando el trabajo ya fue finalizado"))
        }
        if (voto !in 1..5) {
            return Result.failure(IllegalArgumentException("La valoracion debe estar entre 1 y 5 estrellas"))
        }
        if (db.existeValoracionPorChatCliente(idChatCita = idChatCita, idCliente = usuario.idUsuario)) {
            return Result.failure(IllegalStateException("Este chat ya fue valorado anteriormente"))
        }
        val idOferta = chat.idOfertaServicio
            ?: return Result.failure(IllegalStateException("No se encontro el servicio asociado al chat"))
        val idValoracion = db.insertarValoracion(
            voto = voto,
            comentario = comentario.trim(),
            idTrabajador = chat.idTrabajador,
            idCliente = usuario.idUsuario,
            idChatCita = idChatCita,
            idOfertaServicio = idOferta
        )
        if (idValoracion <= 0) {
            return Result.failure(IllegalStateException("No se pudo guardar la valoracion"))
        }
        return db.obtenerValoracionPorId(idValoracion)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la valoracion guardada"))
    }

    override fun obtenerNotificacionesPendientes(): List<NotificacionMensajePendiente> {
        val idUsuario = db.obtenerUsuarioSesionActiva()?.idUsuario ?: return emptyList()
        return db.obtenerMensajesPendientesNotificacion(idUsuario)
    }

    override fun marcarNotificacionesComoMostradas(idsMensaje: List<Long>) {
        db.marcarMensajesNotificados(idsMensaje)
    }

    private fun transicionarCita(
        idChatCita: Long,
        validarRol: (ChatCita, Usuario) -> Boolean,
        estadosPermitidos: Set<Int>,
        nuevoEstado: Int,
        mensajeSistema: String,
        fechaInicio: String? = null,
        fechaFin: String? = null
    ): Result<CitaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (chat.chatCerrado) {
            return Result.failure(IllegalStateException("El chat esta cerrado y no admite cambios."))
        }
        if (!validarRol(chat, usuario)) {
            return Result.failure(IllegalStateException("No tienes permisos para esta accion."))
        }
        val cita = db.obtenerCitaPorChat(idChatCita)
            ?: return Result.failure(IllegalStateException("Este chat no tiene cita activa"))
        if (cita.estado !in estadosPermitidos) {
            return Result.failure(IllegalStateException("El estado actual no permite esta accion."))
        }

        val actualizada = db.actualizarEstadoCita(
            idCita = cita.idCita,
            nuevoEstado = nuevoEstado,
            fechaInicioTrabajo = fechaInicio,
            fechaFinTrabajo = fechaFin
        )
        if (!actualizada) return Result.failure(IllegalStateException("No se pudo actualizar la cita"))

        val idReceptor = if (chat.idCliente == usuario.idUsuario) chat.idTrabajador else chat.idCliente
        db.insertarMensajeChat(
            idChatCita = idChatCita,
            idEmisor = usuario.idUsuario,
            idReceptor = idReceptor,
            contenido = mensajeSistema
        )

        return db.obtenerCitaPorChat(idChatCita)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la cita actualizada"))
    }

}

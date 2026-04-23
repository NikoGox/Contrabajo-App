package com.movil.contrabajo.data.repository

import com.movil.contrabajo.data.local.ContrabajoSQLiteHelper
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.FiltroMarketplaceConfig
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter

interface RepositorioAutenticacion {
    fun obtenerSesionActiva(): Usuario?
    fun iniciarSesion(identificador: String, contrasena: String, recordarme: Boolean): Result<Usuario>
    fun registrarUsuario(registro: RegistroPendiente): Result<Usuario>
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
}

interface RepositorioOfertas {
    fun obtenerOfertaPrincipal(): OfertaServicio?
    fun obtenerOfertasMarketplace(busqueda: String = ""): List<OfertaServicio>
    fun obtenerOfertaPorId(idOfertaServicio: Long): OfertaServicio?
    fun obtenerOfertaPropiaActual(): OfertaServicio?
    fun obtenerCategoriasServicio(): List<CategoriaServicio>
    fun guardarOfertaPropia(formulario: FormularioServicio): Result<OfertaServicio>
    fun actualizarDisponibilidadOfertaPropia(disponible: Boolean): Result<OfertaServicio>
    fun eliminarOfertaPropia(): Result<Unit>
}

interface RepositorioChats {
    fun obtenerIdUsuarioActual(): Long?
    fun obtenerChatsActuales(): List<ChatCita>
    fun obtenerMensajes(idChatCita: Long): List<MensajeChat>
    fun iniciarConversacionDesdeOferta(idOfertaServicio: Long): Result<ChatCita>
    fun obtenerChat(idChatCita: Long): ChatCita?
    fun enviarMensaje(idChatCita: Long, contenido: String): Result<MensajeChat>
    fun crearCitaDesdeChat(idChatCita: Long, fechaProgramada: String, detalle: String): Result<CitaServicio>
    fun obtenerCitaPorChat(idChatCita: Long): CitaServicio?
    fun actualizarEstadoCita(idCita: Long, nuevoEstado: Int): Result<CitaServicio>
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
        db.guardarUbicacionUsuario(id, normalizarUbicacionRegistro(registro))
        db.guardarSesion(id, true)
        return Result.success(db.obtenerUsuarioPorId(id) ?: usuario.copy(idUsuario = id))
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
        if (indice !in 1..3) {
            return Result.failure(IllegalArgumentException("Indice de pregunta invalido"))
        }
        if (pregunta.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa la pregunta de seguridad"))
        }
        if (respuesta.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa la respuesta de seguridad"))
        }
        db.guardarPreguntaSeguridad(
            idUsuario = usuario.idUsuario,
            indice = indice,
            pregunta = pregunta.trim(),
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

    private fun limpiarRun(run: String): String = run.filter { it.isDigit() }
}

class RepositorioOfertasLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioOfertas {
    override fun obtenerOfertaPrincipal(): OfertaServicio? = db.obtenerOfertaPrincipal()
    override fun obtenerOfertasMarketplace(busqueda: String): List<OfertaServicio> = db.obtenerOfertasMarketplace(busqueda)
    override fun obtenerOfertaPorId(idOfertaServicio: Long): OfertaServicio? = db.obtenerOfertaPorId(idOfertaServicio)
    override fun obtenerOfertaPropiaActual(): OfertaServicio? {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return null
        return db.obtenerOfertaPorTrabajador(usuario.idUsuario)
    }

    override fun obtenerCategoriasServicio(): List<CategoriaServicio> = db.obtenerCategoriasServicio()

    override fun guardarOfertaPropia(formulario: FormularioServicio): Result<OfertaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay una sesion activa para guardar el servicio"))
        if (usuario.tipoPerfil !in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)) {
            return Result.failure(IllegalStateException("Debes verificarte como trabajador para publicar servicios"))
        }

        val error = validarFormularioServicio(formulario)
        if (error != null) return Result.failure(IllegalArgumentException(error))

        val ofertaExistente = db.obtenerOfertaPorTrabajador(usuario.idUsuario)
        val maximoServicios = if (usuario.tipoPerfil == TipoPerfil.PREMIUM) 3 else 1
        if (ofertaExistente == null && db.contarOfertasActivasPorTrabajador(usuario.idUsuario) >= maximoServicios) {
            return Result.failure(
                IllegalStateException("Tu perfil permite hasta $maximoServicios servicio(s) activo(s)")
            )
        }
        val idFotoPortada = when {
            formulario.foto == null -> ofertaExistente?.idFotoPortada
            formulario.foto.idFoto != null -> {
                db.actualizarFotoServicio(formulario.foto.idFoto, formulario.foto)
                formulario.foto.idFoto
            }

            else -> db.insertarFotoServicio(formulario.foto)
        }

        val idOferta = if (ofertaExistente == null) {
            db.insertarOfertaServicio(usuario.idUsuario, formulario, idFotoPortada)
        } else {
            db.actualizarOfertaServicio(ofertaExistente.idOfertaServicio, formulario, idFotoPortada)
            ofertaExistente.idOfertaServicio
        }

        val ofertaActualizada = db.obtenerOfertaPorId(idOferta)
        return if (ofertaActualizada != null) {
            Result.success(ofertaActualizada)
        } else {
            Result.failure(IllegalStateException("No se pudo guardar el servicio"))
        }
    }

    override fun actualizarDisponibilidadOfertaPropia(disponible: Boolean): Result<OfertaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay una sesion activa para actualizar disponibilidad"))
        val oferta = db.obtenerOfertaPorTrabajador(usuario.idUsuario)
            ?: return Result.failure(IllegalStateException("No existe un servicio para actualizar"))

        db.actualizarDisponibilidadOferta(oferta.idOfertaServicio, disponible)
        val actualizada = db.obtenerOfertaPorId(oferta.idOfertaServicio)
        return if (actualizada != null) {
            Result.success(actualizada)
        } else {
            Result.failure(IllegalStateException("No se pudo actualizar la disponibilidad"))
        }
    }

    override fun eliminarOfertaPropia(): Result<Unit> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay una sesion activa para eliminar el servicio"))
        val oferta = db.obtenerOfertaPorTrabajador(usuario.idUsuario)
            ?: return Result.failure(IllegalStateException("No existe un servicio para eliminar"))
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
        return db.obtenerChatsParaUsuario(usuario.idUsuario)
    }

    override fun obtenerMensajes(idChatCita: Long): List<MensajeChat> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: return emptyList()
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

        val idChat = db.obtenerChatEntreUsuarios(idTrabajador = idTrabajador, idCliente = usuario.idUsuario)
            ?: db.crearChatCita(idTrabajador = idTrabajador, idCliente = usuario.idUsuario)

        if (idChat <= 0) return Result.failure(IllegalStateException("No se pudo crear el chat"))

        val mensajes = db.obtenerMensajesPorChat(idChat)
        if (mensajes.isEmpty()) {
            db.insertarMensajeChat(
                idChatCita = idChat,
                idEmisor = usuario.idUsuario,
                idReceptor = idTrabajador,
                contenido = "Hola, me interesa tu servicio. ¿Podemos coordinar?"
            )
        }

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

    override fun crearCitaDesdeChat(idChatCita: Long, fechaProgramada: String, detalle: String): Result<CitaServicio> {
        val usuario = db.obtenerUsuarioSesionActiva()
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val chat = db.obtenerChatPorId(idChatCita, usuario.idUsuario)
            ?: return Result.failure(IllegalArgumentException("Chat no encontrado"))
        if (fechaProgramada.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa fecha y hora de cita"))
        }
        if (detalle.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa el detalle de la cita"))
        }
        if (db.obtenerCitaPorChat(idChatCita) != null) {
            return Result.failure(IllegalStateException("Este chat ya tiene una cita registrada"))
        }
        val idCita = db.crearCitaServicio(
            idChatCita = chat.idChatCita,
            fechaProgramada = fechaProgramada.trim(),
            detalle = detalle.trim()
        )
        if (idCita <= 0) return Result.failure(IllegalStateException("No se pudo crear la cita"))
        return db.obtenerCitaPorChat(idChatCita)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la cita"))
    }

    override fun obtenerCitaPorChat(idChatCita: Long): CitaServicio? = db.obtenerCitaPorChat(idChatCita)

    override fun actualizarEstadoCita(idCita: Long, nuevoEstado: Int): Result<CitaServicio> {
        if (nuevoEstado !in listOf(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_PROCESO, EstadoCita.FINALIZADA)) {
            return Result.failure(IllegalArgumentException("Estado de cita invalido"))
        }
        val actualizada = db.actualizarEstadoCita(idCita, nuevoEstado)
        if (!actualizada) return Result.failure(IllegalStateException("No se pudo actualizar el estado de la cita"))
        val chat = obtenerChatsActuales().firstOrNull { it.idCita == idCita }
            ?: return Result.failure(IllegalStateException("No se encontro el chat de la cita"))
        return db.obtenerCitaPorChat(chat.idChatCita)?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No se pudo recuperar la cita actualizada"))
    }
}

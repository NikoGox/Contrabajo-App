package com.movil.contrabajo.data.repository

import com.movil.contrabajo.data.local.ContrabajoSQLiteHelper
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.TipoPerfil
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
    fun obtenerChatsActuales(): List<ChatCita>
    fun obtenerMensajes(idChatCita: Long): List<MensajeChat>
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
        db.guardarSesion(id, true)
        return Result.success(db.obtenerUsuarioPorId(id) ?: usuario.copy(idUsuario = id))
    }

    override fun cerrarSesion() {
        db.cerrarSesion()
    }

    private fun validarRegistro(registro: RegistroPendiente): String? = when {
        registro.nombre.isBlank() -> "Ingresa tu nombre"
        registro.apellidoPaterno.isBlank() -> "Ingresa tu apellido paterno"
        registro.run.isBlank() || registro.dv.isBlank() -> "Ingresa un RUN valido"
        limpiarRun(registro.run).length != 8 -> "El RUN debe tener exactamente 8 digitos"
        !validarRut(registro.run, registro.dv) -> "El RUN no es valido"
        digitosTelefono(registro.telefono).length != 9 -> "Ingresa un telefono valido de 9 digitos"
        registro.username.isBlank() -> "Ingresa un nombre de usuario"
        registro.correo.isBlank() || !registro.correo.contains("@") -> "Ingresa un correo valido"
        registro.fechaNacimiento.isBlank() -> "Ingresa tu fecha de nacimiento"
        !esFechaValida(registro.fechaNacimiento) -> "La fecha de nacimiento debe tener formato yyyy-MM-dd"
        registro.contrasena.length < 6 -> "La contrasena debe tener al menos 6 caracteres"
        registro.contrasena != registro.confirmarContrasena -> "Las contrasenas no coinciden"
        else -> null
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

    private fun esFechaValida(fecha: String): Boolean = try {
        LocalDate.parse(fecha.trim())
        true
    } catch (_: DateTimeParseException) {
        false
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
        return db.solicitarVerificacionTrabajador(
            idUsuario = usuario.idUsuario,
            run = limpiarRun(run),
            dv = dv.trim().uppercase(),
            numeroDocumento = numeroDocumento
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
        formulario.precioTexto.isBlank() -> "Ingresa una referencia de precio"
        formulario.idCategoriaServicio == null -> "Selecciona una categoria"
        formulario.foto == null || formulario.foto.uriLocal.isBlank() -> "Selecciona una foto para tu servicio"
        else -> null
    }
}

class RepositorioChatsLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioChats {
    override fun obtenerChatsActuales(): List<ChatCita> {
        val usuario = db.obtenerUsuarioSesionActiva() ?: db.obtenerUsuarioPorId(2) ?: return emptyList()
        return db.obtenerChatsParaUsuario(usuario.idUsuario)
    }

    override fun obtenerMensajes(idChatCita: Long): List<MensajeChat> = db.obtenerMensajesPorChat(idChatCita)
}

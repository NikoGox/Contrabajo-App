package com.movil.contrabajo.data.repository

import com.movil.contrabajo.data.local.ContrabajoSQLiteHelper
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface RepositorioAutenticacion {
    fun obtenerSesionActiva(): Usuario?
    fun iniciarSesion(identificador: String, contrasena: String, recordarme: Boolean): Result<Usuario>
    fun registrarUsuario(registro: RegistroPendiente): Result<Usuario>
    fun cerrarSesion()
}

interface RepositorioPerfil {
    fun obtenerPerfilActual(): Usuario?
}

interface RepositorioOfertas {
    fun obtenerOfertaPrincipal(): OfertaServicio?
    fun obtenerOfertaPorId(idOfertaServicio: Long): OfertaServicio?
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

        val usuario = Usuario(
            run = registro.run.trim(),
            dv = registro.dv.trim(),
            username = registro.username.trim(),
            nombre = registro.nombre.trim(),
            apellidoPaterno = registro.apellidoPaterno.trim(),
            apellidoMaterno = registro.apellidoMaterno.trim(),
            telefono = registro.telefono.trim(),
            correo = registro.correo.trim(),
            contrasena = registro.contrasena,
            fechaRegistro = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            fechaNacimiento = LocalDate.now().minusYears(20).toString(),
            verificado = false
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
        registro.telefono.isBlank() -> "Ingresa tu telefono"
        registro.username.isBlank() -> "Ingresa un nombre de usuario"
        registro.correo.isBlank() || !registro.correo.contains("@") -> "Ingresa un correo valido"
        registro.contrasena.length < 6 -> "La contrasena debe tener al menos 6 caracteres"
        registro.contrasena != registro.confirmarContrasena -> "Las contrasenas no coinciden"
        else -> null
    }
}

class RepositorioPerfilLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioPerfil {
    override fun obtenerPerfilActual(): Usuario? = db.obtenerUsuarioSesionActiva()
}

class RepositorioOfertasLocal(
    private val db: ContrabajoSQLiteHelper
) : RepositorioOfertas {
    override fun obtenerOfertaPrincipal(): OfertaServicio? = db.obtenerOfertaPrincipal()
    override fun obtenerOfertaPorId(idOfertaServicio: Long): OfertaServicio? = db.obtenerOfertaPorId(idOfertaServicio)
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

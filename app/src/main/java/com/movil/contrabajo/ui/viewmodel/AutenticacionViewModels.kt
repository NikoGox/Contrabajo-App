package com.movil.contrabajo.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.PreguntasSeguridadCatalogo
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.TipoPerfil

data class InicioUiState(
    val revisandoSesion: Boolean = true,
    val sesionActivaDetectada: Boolean = false
)

class InicioViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(InicioUiState())
        private set

    private var sesionRevisada = false

    fun revisarSesionActiva() {
        if (sesionRevisada) return
        sesionRevisada = true

        uiState = uiState.copy(
            revisandoSesion = false,
            sesionActivaDetectada = repositorioAutenticacion.obtenerSesionActiva() != null
        )
    }
}

data class LoginUiState(
    val identificador: String = "vale@contrabajo.cl",
    val contrasena: String = "123456",
    val recordarme: Boolean = true,
    val error: String? = null,
    val loginExitoso: Boolean = false,
    val recuperacionIdentificador: String = "",
    val recuperacionPreguntas: List<PreguntaSeguridadConfig> = emptyList(),
    val recuperacionRespuesta1: String = "",
    val recuperacionRespuesta2: String = "",
    val recuperacionValidada: Boolean = false,
    val nuevaContrasenaRecuperacion: String = "",
    val confirmarContrasenaRecuperacion: String = "",
    val errorRecuperacion: String? = null,
    val mensajeRecuperacion: String? = null
)

class LoginViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun actualizarIdentificador(valor: String) {
        uiState = uiState.copy(identificador = valor, error = null)
    }

    fun actualizarContrasena(valor: String) {
        uiState = uiState.copy(contrasena = valor, error = null)
    }

    fun actualizarRecordarme(valor: Boolean) {
        uiState = uiState.copy(recordarme = valor)
    }

    fun iniciarSesion() {
        repositorioAutenticacion
            .iniciarSesion(uiState.identificador, uiState.contrasena, uiState.recordarme)
            .onSuccess {
                uiState = uiState.copy(error = null, loginExitoso = true)
            }
            .onFailure {
                uiState = uiState.copy(error = it.message, loginExitoso = false)
            }
    }

    fun consumirNavegacionExitosa() {
        uiState = uiState.copy(loginExitoso = false)
    }

    fun actualizarIdentificadorRecuperacion(valor: String) {
        uiState = uiState.copy(
            recuperacionIdentificador = valor.trim(),
            errorRecuperacion = null,
            mensajeRecuperacion = null
        )
    }

    fun cargarPreguntasRecuperacion() {
        val identificador = uiState.recuperacionIdentificador.trim()
        if (identificador.isBlank()) {
            uiState = uiState.copy(errorRecuperacion = "Ingresa tu usuario o correo")
            return
        }
        repositorioAutenticacion.obtenerPreguntasRecuperacion(identificador)
            .onSuccess { preguntas ->
                uiState = uiState.copy(
                    recuperacionPreguntas = preguntas,
                    recuperacionRespuesta1 = "",
                    recuperacionRespuesta2 = "",
                    recuperacionValidada = false,
                    nuevaContrasenaRecuperacion = "",
                    confirmarContrasenaRecuperacion = "",
                    errorRecuperacion = null,
                    mensajeRecuperacion = null
                )
            }
            .onFailure {
                uiState = uiState.copy(errorRecuperacion = it.message, mensajeRecuperacion = null)
            }
    }

    fun actualizarRespuestaRecuperacion1(valor: String) {
        uiState = uiState.copy(
            recuperacionRespuesta1 = valor,
            recuperacionValidada = false,
            errorRecuperacion = null
        )
    }

    fun actualizarRespuestaRecuperacion2(valor: String) {
        uiState = uiState.copy(
            recuperacionRespuesta2 = valor,
            recuperacionValidada = false,
            errorRecuperacion = null
        )
    }

    fun validarRespuestasRecuperacion() {
        repositorioAutenticacion.validarRespuestasRecuperacion(
            identificador = uiState.recuperacionIdentificador,
            respuesta1 = uiState.recuperacionRespuesta1,
            respuesta2 = uiState.recuperacionRespuesta2
        ).onSuccess {
            uiState = uiState.copy(
                recuperacionValidada = true,
                errorRecuperacion = null,
                mensajeRecuperacion = "Identidad validada. Ya puedes restablecer tu contrasena."
            )
        }.onFailure {
            uiState = uiState.copy(
                recuperacionValidada = false,
                errorRecuperacion = it.message ?: "No se pudieron validar las respuestas",
                mensajeRecuperacion = null
            )
        }
    }

    fun actualizarNuevaContrasenaRecuperacion(valor: String) {
        uiState = uiState.copy(
            nuevaContrasenaRecuperacion = valor,
            errorRecuperacion = null
        )
    }

    fun actualizarConfirmarContrasenaRecuperacion(valor: String) {
        uiState = uiState.copy(
            confirmarContrasenaRecuperacion = valor,
            errorRecuperacion = null
        )
    }

    fun restablecerContrasenaRecuperacion() {
        repositorioAutenticacion.restablecerContrasenaRecuperacion(
            identificador = uiState.recuperacionIdentificador,
            respuesta1 = uiState.recuperacionRespuesta1,
            respuesta2 = uiState.recuperacionRespuesta2,
            nuevaContrasena = uiState.nuevaContrasenaRecuperacion,
            confirmarContrasena = uiState.confirmarContrasenaRecuperacion
        ).onSuccess {
            uiState = uiState.copy(
                recuperacionValidada = false,
                recuperacionPreguntas = emptyList(),
                recuperacionRespuesta1 = "",
                recuperacionRespuesta2 = "",
                nuevaContrasenaRecuperacion = "",
                confirmarContrasenaRecuperacion = "",
                errorRecuperacion = null,
                mensajeRecuperacion = "Contrasena restablecida correctamente. Ya puedes iniciar sesion."
            )
        }.onFailure {
            uiState = uiState.copy(
                errorRecuperacion = it.message ?: "No se pudo restablecer la contrasena",
                mensajeRecuperacion = null
            )
        }
    }

    fun limpiarEstadoRecuperacion() {
        uiState = uiState.copy(
            recuperacionPreguntas = emptyList(),
            recuperacionRespuesta1 = "",
            recuperacionRespuesta2 = "",
            recuperacionValidada = false,
            nuevaContrasenaRecuperacion = "",
            confirmarContrasenaRecuperacion = "",
            errorRecuperacion = null,
            mensajeRecuperacion = null
        )
    }
}

data class RegistroUiState(
    val registro: RegistroPendiente = RegistroPendiente(),
    val error: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(RegistroUiState())
        private set

    fun actualizarNombre(valor: String) {
        actualizarRegistro(uiState.registro.copy(nombre = sanitizarNombrePersona(valor)))
    }

    fun actualizarApellidoPaterno(valor: String) {
        actualizarRegistro(uiState.registro.copy(apellidoPaterno = sanitizarNombrePersona(valor)))
    }

    fun actualizarApellidoMaterno(valor: String) {
        actualizarRegistro(uiState.registro.copy(apellidoMaterno = sanitizarNombrePersona(valor)))
    }

    fun actualizarRun(valor: String) {
        val runDigitos = valor.filter { it.isDigit() }.take(8)
        actualizarRegistro(uiState.registro.copy(run = runDigitos))
    }

    fun actualizarDv(valor: String) {
        val dvNormalizado = valor
            .uppercase()
            .filter { it.isDigit() || it == 'K' }
            .take(1)
        actualizarRegistro(uiState.registro.copy(dv = dvNormalizado))
    }

    fun actualizarTelefono(valor: String) {
        val digitos = valor.filter { it.isDigit() }.take(9)
        actualizarRegistro(uiState.registro.copy(telefono = digitos))
    }

    fun actualizarRegion(valor: String) {
        actualizarRegistro(uiState.registro.copy(region = valor))
    }

    fun actualizarComuna(valor: String) {
        actualizarRegistro(uiState.registro.copy(comuna = valor))
    }

    fun actualizarCalle(valor: String) {
        actualizarRegistro(uiState.registro.copy(calle = valor))
    }

    fun actualizarNumeroDireccion(valor: String) {
        actualizarRegistro(uiState.registro.copy(numeroDireccion = valor))
    }

    fun actualizarCoordenadasRegistro(latitud: Double?, longitud: Double?) {
        actualizarRegistro(uiState.registro.copy(latitud = latitud, longitud = longitud))
    }

    fun actualizarUsername(valor: String) {
        actualizarRegistro(uiState.registro.copy(username = valor))
    }

    fun actualizarCorreo(valor: String) {
        actualizarRegistro(uiState.registro.copy(correo = valor))
    }

    fun actualizarFechaNacimiento(valor: String) {
        actualizarRegistro(uiState.registro.copy(fechaNacimiento = valor))
    }

    fun actualizarContrasena(valor: String) {
        actualizarRegistro(uiState.registro.copy(contrasena = valor))
    }

    fun actualizarConfirmarContrasena(valor: String) {
        actualizarRegistro(uiState.registro.copy(confirmarContrasena = valor))
    }

    fun actualizarPreguntaSeguridad1(valor: String) {
        actualizarRegistro(uiState.registro.copy(preguntaSeguridad1 = valor))
    }

    fun actualizarRespuestaSeguridad1(valor: String) {
        actualizarRegistro(uiState.registro.copy(respuestaSeguridad1 = valor))
    }

    fun actualizarPreguntaSeguridad2(valor: String) {
        actualizarRegistro(uiState.registro.copy(preguntaSeguridad2 = valor))
    }

    fun actualizarRespuestaSeguridad2(valor: String) {
        actualizarRegistro(uiState.registro.copy(respuestaSeguridad2 = valor))
    }

    fun registrarUsuario() {
        repositorioAutenticacion
            .registrarUsuario(uiState.registro)
            .onSuccess {
                uiState = uiState.copy(error = null, registroExitoso = true)
            }
            .onFailure {
                uiState = uiState.copy(error = it.message, registroExitoso = false)
            }
    }

    fun consumirRegistroExitoso() {
        uiState = RegistroUiState()
    }

    fun preguntasSeguridadDisponibles(): List<String> = PreguntasSeguridadCatalogo.opciones

    private fun actualizarRegistro(registro: RegistroPendiente) {
        uiState = uiState.copy(
            registro = registro.copy(tipoPerfil = TipoPerfil.USUARIO_BASE),
            error = null
        )
    }

    private fun sanitizarNombrePersona(valor: String): String {
        return valor
            .filter { caracter ->
                caracter.isLetter() || caracter == ' ' || caracter == '\'' || caracter == '-'
            }
            .take(60)
    }
}

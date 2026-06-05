package com.movil.contrabajo.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.domain.model.ComunaCatalogo
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.PreguntasSeguridadCatalogo
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.domain.model.TipoPerfil
import java.text.Normalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InicioUiState(
    val revisandoSesion: Boolean = true,
    val sesionActivaDetectada: Boolean = false,
    val errorConexion: Boolean = false
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
        uiState = uiState.copy(revisandoSesion = true, errorConexion = false)

        viewModelScope.launch {
            try {
                val sesionActiva = withContext(Dispatchers.IO) {
                    repositorioAutenticacion.obtenerSesionActiva() != null
                }
                uiState = uiState.copy(
                    revisandoSesion = false,
                    sesionActivaDetectada = sesionActiva,
                    errorConexion = false
                )
            } catch (e: Exception) {
                // Error de red (backend caido): mantener token, mostrar pantalla de reintento
                sesionRevisada = false
                uiState = uiState.copy(
                    revisandoSesion = false,
                    sesionActivaDetectada = false,
                    errorConexion = true
                )
            }
        }
    }

    /** Cierra la sesion local (limpia el token) y vuelve a la pantalla de bienvenida. */
    fun cerrarSesionLocal() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repositorioAutenticacion.cerrarSesion()
            }
            sesionRevisada = false
            uiState = InicioUiState(revisandoSesion = false, sesionActivaDetectada = false, errorConexion = false)
        }
    }
}

data class LoginUiState(
    val identificador: String = "cliente_prueba",
    val contrasena: String = "Contrabajo123!",
    val recordarme: Boolean = true,
    val error: String? = null,
    val loginExitoso: Boolean = false,
    // Bloqueo de cuenta: se setea cuando idEstado es 102 (suspendido) o 103 (baneado)
    val cuentaBloqueada: Boolean = false,
    val tipoBloqueoCuenta: String? = null, // "BANEADO" | "SUSPENDIDO"
    val fechaFinSuspension: String? = null,
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
        val identificador = uiState.identificador
        val contrasena = uiState.contrasena
        val recordarme = uiState.recordarme
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.iniciarSesion(identificador, contrasena, recordarme)
            }
            resultado
            .onSuccess { usuario ->
                when (usuario.idEstado) {
                    103 -> uiState = uiState.copy(
                        error = null,
                        loginExitoso = false,
                        cuentaBloqueada = true,
                        tipoBloqueoCuenta = "BANEADO",
                        fechaFinSuspension = null
                    )
                    102 -> uiState = uiState.copy(
                        error = null,
                        loginExitoso = false,
                        cuentaBloqueada = true,
                        tipoBloqueoCuenta = "SUSPENDIDO",
                        fechaFinSuspension = usuario.baneoFechaFin
                    )
                    else -> uiState = uiState.copy(error = null, loginExitoso = true)
                }
            }
            .onFailure {
                uiState = uiState.copy(error = it.message, loginExitoso = false)
            }
        }
    }

    fun autocompletarPerfilPrueba(perfil: String) {
        val (identificador, contrasena) = when (perfil) {
            "trabajador" -> DEMO_TRABAJADOR_USUARIO to DEMO_PASSWORD
            "moderador" -> DEMO_MODERADOR_USUARIO to DEMO_PASSWORD
            else -> DEMO_CLIENTE_USUARIO to DEMO_PASSWORD
        }
        uiState = uiState.copy(
            identificador = identificador,
            contrasena = contrasena,
            recordarme = true,
            error = null
        )
    }

    fun consumirNavegacionExitosa() {
        uiState = uiState.copy(loginExitoso = false)
    }

    fun consumirBloqueo() {
        uiState = uiState.copy(
            cuentaBloqueada = false,
            tipoBloqueoCuenta = null,
            fechaFinSuspension = null
        )
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
            uiState = uiState.copy(errorRecuperacion = "Ingresa tu nombre de usuario")
            return
        }
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.obtenerPreguntasRecuperacion(identificador)
            }
            resultado
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
        val identificador = uiState.recuperacionIdentificador
        val respuesta1 = uiState.recuperacionRespuesta1
        val respuesta2 = uiState.recuperacionRespuesta2
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.validarRespuestasRecuperacion(
                    identificador = identificador,
                    respuesta1 = respuesta1,
                    respuesta2 = respuesta2
                )
            }
            resultado.onSuccess {
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
        val identificador = uiState.recuperacionIdentificador
        val respuesta1 = uiState.recuperacionRespuesta1
        val respuesta2 = uiState.recuperacionRespuesta2
        val nueva = uiState.nuevaContrasenaRecuperacion
        val confirmar = uiState.confirmarContrasenaRecuperacion
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.restablecerContrasenaRecuperacion(
                    identificador = identificador,
                    respuesta1 = respuesta1,
                    respuesta2 = respuesta2,
                    nuevaContrasena = nueva,
                    confirmarContrasena = confirmar
                )
            }
            resultado.onSuccess {
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
    val comunas: List<ComunaCatalogo> = emptyList(),
    val cargandoComunas: Boolean = false,
    val errorComunas: String? = null,
    val validandoDisponibilidad: Boolean = false,
    val errorRunDisponible: String? = null,
    val errorUsernameDisponible: String? = null,
    val errorCorreoDisponible: String? = null,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(RegistroUiState())
        private set

    fun cargarComunas() {
        if (uiState.cargandoComunas || uiState.comunas.isNotEmpty()) return
        viewModelScope.launch {
            uiState = uiState.copy(cargandoComunas = true, errorComunas = null)
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.obtenerComunas()
            }
            resultado
                .onSuccess { comunas ->
                    uiState = uiState.copy(
                        comunas = ordenarComunasIntegracion(comunas),
                        cargandoComunas = false,
                        errorComunas = null
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        cargandoComunas = false,
                        errorComunas = it.message ?: "No se pudieron cargar las comunas"
                    )
                }
        }
    }

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
        uiState = uiState.copy(
            registro = uiState.registro.copy(run = runDigitos),
            error = null,
            errorRunDisponible = null
        )
    }

    fun actualizarDv(valor: String) {
        val dvNormalizado = valor
            .uppercase()
            .filter { it.isDigit() || it == 'K' }
            .take(1)
        uiState = uiState.copy(
            registro = uiState.registro.copy(dv = dvNormalizado),
            error = null,
            errorRunDisponible = null
        )
    }

    fun actualizarTelefono(valor: String) {
        val digitos = normalizarTelefonoMovilSinPrefijo(valor)
        actualizarRegistro(uiState.registro.copy(telefono = digitos))
    }

    fun actualizarRegion(valor: String) {
        actualizarRegistro(uiState.registro.copy(region = valor))
    }

    fun actualizarComuna(valor: String) {
        val comuna = uiState.comunas.firstOrNull { it.nombre.equals(valor, ignoreCase = true) }
        val idComuna = when {
            valor.normalizarTexto() == "sin comuna" -> 1
            else -> comuna?.id
        }
        actualizarRegistro(uiState.registro.copy(idComuna = idComuna, comuna = valor))
    }

    fun actualizarCalle(valor: String) {
        actualizarRegistro(uiState.registro.copy(calle = valor.take(120)))
    }

    fun actualizarNumeroDireccion(valor: String) {
        actualizarRegistro(uiState.registro.copy(numeroDireccion = valor.take(20)))
    }

    fun actualizarCoordenadasRegistro(latitud: Double?, longitud: Double?) {
        actualizarRegistro(uiState.registro.copy(latitud = latitud, longitud = longitud))
    }

    fun actualizarUsername(valor: String) {
        uiState = uiState.copy(
            registro = uiState.registro.copy(username = valor.trim().take(20)),
            error = null,
            errorUsernameDisponible = null
        )
    }

    fun actualizarCorreo(valor: String) {
        uiState = uiState.copy(
            registro = uiState.registro.copy(correo = valor.trim().take(254)),
            error = null,
            errorCorreoDisponible = null
        )
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
        actualizarRegistro(uiState.registro.copy(preguntaSeguridad1 = valor.take(200)))
    }

    fun actualizarRespuestaSeguridad1(valor: String) {
        actualizarRegistro(uiState.registro.copy(respuestaSeguridad1 = valor.take(200)))
    }

    fun actualizarPreguntaSeguridad2(valor: String) {
        actualizarRegistro(uiState.registro.copy(preguntaSeguridad2 = valor.take(200)))
    }

    fun actualizarRespuestaSeguridad2(valor: String) {
        actualizarRegistro(uiState.registro.copy(respuestaSeguridad2 = valor.take(200)))
    }

    private fun ordenarComunasIntegracion(comunas: List<ComunaCatalogo>): List<ComunaCatalogo> {
        return comunas.sortedWith(
            compareBy<ComunaCatalogo> { if (it.nombre.normalizarTexto() == "sin comuna") 0 else 1 }
                .thenBy { it.nombre.normalizarTexto() }
        )
    }

    fun registrarUsuario() {
        val registro = uiState.registro
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.registrarUsuario(registro)
            }
            resultado
            .onSuccess {
                uiState = uiState.copy(error = null, registroExitoso = true)
            }
            .onFailure {
                uiState = uiState.copy(error = it.message, registroExitoso = false)
            }
        }
    }

    fun verificarRunDisponibleAntesDeContinuar(onDisponible: () -> Unit) {
        val registro = uiState.registro
        val runValidado = registro.run.filter { it.isDigit() }
        if (runValidado.length !in 7..8) {
            uiState = uiState.copy(errorRunDisponible = "El RUN debe tener 7 u 8 digitos")
            return
        }
        if (!validarRut(runValidado, registro.dv)) {
            uiState = uiState.copy(errorRunDisponible = "El RUN no es valido")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(validandoDisponibilidad = true, errorRunDisponible = null)
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.verificarRunDisponible(runValidado)
            }
            resultado
                .onSuccess { disponible ->
                    if (disponible) {
                        onDisponible()
                    } else {
                        uiState = uiState.copy(errorRunDisponible = "El RUN ya existe")
                    }
                }
                .onFailure {
                    uiState = uiState.copy(errorRunDisponible = it.message ?: "No se pudo validar el RUN")
                }
            uiState = uiState.copy(validandoDisponibilidad = false)
        }
    }

    fun verificarCuentaDisponibleAntesDeContinuar(onDisponible: () -> Unit) {
        val registro = uiState.registro
        val username = registro.username.trim()
        val correo = registro.correo.trim()
        viewModelScope.launch {
            uiState = uiState.copy(
                validandoDisponibilidad = true,
                errorUsernameDisponible = null,
                errorCorreoDisponible = null
            )
            val usernameResultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.verificarUsernameDisponible(username)
            }
            val correoResultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.verificarCorreoDisponible(correo)
            }
            val usernameOk = usernameResultado.getOrNull()
            val correoOk = correoResultado.getOrNull()
            when {
                usernameOk == false -> uiState = uiState.copy(errorUsernameDisponible = "El nombre de usuario ya existe")
                usernameResultado.isFailure -> uiState = uiState.copy(
                    errorUsernameDisponible = usernameResultado.exceptionOrNull()?.message ?: "No se pudo validar el nombre de usuario"
                )
            }
            when {
                correoOk == false -> uiState = uiState.copy(errorCorreoDisponible = "El correo ya existe")
                correoResultado.isFailure -> uiState = uiState.copy(
                    errorCorreoDisponible = correoResultado.exceptionOrNull()?.message ?: "No se pudo validar el correo"
                )
            }
            if (usernameOk == true && correoOk == true) {
                onDisponible()
            }
            uiState = uiState.copy(validandoDisponibilidad = false)
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

    private fun normalizarTelefonoMovilSinPrefijo(valor: String): String {
        val digitos = valor.filter { it.isDigit() }
            .let { if (it.startsWith("56")) it.drop(2) else it }
            .let { if (it.length == 9 && it.startsWith("9")) it.drop(1) else it }
        return digitos.take(8)
    }
}

private const val DEMO_CLIENTE_USUARIO = "cliente_prueba"
private const val DEMO_TRABAJADOR_USUARIO = "trabajador_prueba"
private const val DEMO_MODERADOR_USUARIO = "moderador_prueba"
private const val DEMO_PASSWORD = "Contrabajo123!"

private fun String.normalizarTexto(): String {
    return Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase()
}

private fun validarRut(runRaw: String, dvRaw: String): Boolean {
    val run = runRaw.filter { it.isDigit() }.take(8)
    if (run.length !in 7..8) return false
    val dv = dvRaw.trim().uppercase()
    if (dv.isBlank()) return false

    val digitos = run.reversed().map { it.digitToInt() }
    val factores = listOf(2, 3, 4, 5, 6, 7)
    val suma = digitos.mapIndexed { indice, valor -> valor * factores[indice % factores.size] }.sum()
    val resto = 11 - (suma % 11)
    val dvCalculado = when (resto) {
        11 -> "0"
        10 -> "K"
        else -> resto.toString()
    }
    return dvCalculado == dv
}

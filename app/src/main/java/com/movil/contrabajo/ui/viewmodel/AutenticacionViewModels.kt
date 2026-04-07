package com.movil.contrabajo.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.domain.model.RegistroPendiente

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
    val loginExitoso: Boolean = false
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
        actualizarRegistro(uiState.registro.copy(nombre = valor))
    }

    fun actualizarApellidoPaterno(valor: String) {
        actualizarRegistro(uiState.registro.copy(apellidoPaterno = valor))
    }

    fun actualizarApellidoMaterno(valor: String) {
        actualizarRegistro(uiState.registro.copy(apellidoMaterno = valor))
    }

    fun actualizarRun(valor: String) {
        actualizarRegistro(uiState.registro.copy(run = valor))
    }

    fun actualizarDv(valor: String) {
        actualizarRegistro(uiState.registro.copy(dv = valor))
    }

    fun actualizarTelefono(valor: String) {
        actualizarRegistro(uiState.registro.copy(telefono = valor))
    }

    fun actualizarUsername(valor: String) {
        actualizarRegistro(uiState.registro.copy(username = valor))
    }

    fun actualizarCorreo(valor: String) {
        actualizarRegistro(uiState.registro.copy(correo = valor))
    }

    fun actualizarContrasena(valor: String) {
        actualizarRegistro(uiState.registro.copy(contrasena = valor))
    }

    fun actualizarConfirmarContrasena(valor: String) {
        actualizarRegistro(uiState.registro.copy(confirmarContrasena = valor))
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

    private fun actualizarRegistro(registro: RegistroPendiente) {
        uiState = uiState.copy(registro = registro, error = null)
    }
}

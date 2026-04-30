package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.CampoSecretoContrabajo
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.LoginViewModel

@Composable
fun PantallaLogin(
    onVolver: () -> Unit,
    onLoginExitoso: () -> Unit,
    onRegistrarse: () -> Unit,
    onRecuperarCuenta: () -> Unit,
    viewModel: LoginViewModel
) {
    val uiState = viewModel.uiState
    var menuAutorellenoAbierto by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            onLoginExitoso()
            viewModel.consumirNavegacionExitosa()
        }
    }

    PantallaBase(scrollable = false, mostrarFondo = false) {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(onClick = { menuAutorellenoAbierto = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Autorrelleno demo"
                        )
                    }
                    DropdownMenu(
                        expanded = menuAutorellenoAbierto,
                        onDismissRequest = { menuAutorellenoAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil cliente demo") },
                            onClick = {
                                menuAutorellenoAbierto = false
                                viewModel.autocompletarPerfilDemo("cliente")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Perfil trabajador demo") },
                            onClick = {
                                menuAutorellenoAbierto = false
                                viewModel.autocompletarPerfilDemo("trabajador")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Perfil moderador demo") },
                            onClick = {
                                menuAutorellenoAbierto = false
                                viewModel.autocompletarPerfilDemo("moderador")
                            }
                        )
                    }
                }
            }
            EncabezadoPantalla(
                titulo = "Iniciar sesion",
                subtitulo = "Accede a tu cuenta para ver publicaciones, mensajes y tu perfil de trabajo."
            )
            CampoContrabajo(
                valor = uiState.identificador,
                onValueChange = viewModel::actualizarIdentificador,
                etiqueta = "Correo o usuario"
            )
            CampoSecretoContrabajo(
                valor = uiState.contrasena,
                onValueChange = viewModel::actualizarContrasena,
                etiqueta = "Contrasena"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(checked = uiState.recordarme, onCheckedChange = viewModel::actualizarRecordarme)
                Text("Recordarme", modifier = Modifier.padding(top = 1.dp))
            }
            if (uiState.error != null) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            BotonPrimario(
                texto = "Entrar",
                onClick = viewModel::iniciarSesion
            )
            BotonSecundario(texto = "Crear cuenta", onClick = onRegistrarse)
            BotonSecundario(texto = "Recuperar cuenta", onClick = onRecuperarCuenta)
            Text(
                text = "Usuario demo: vale@contrabajo.cl / 123456",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "No tienes cuenta? Registrate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

@Composable
fun PantallaRecuperarCuenta(
    viewModel: LoginViewModel,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    val preguntasOrdenadas = uiState.recuperacionPreguntas.sortedBy { it.indice }
    val pregunta1 = preguntasOrdenadas.getOrNull(0)?.pregunta.orEmpty()
    val pregunta2 = preguntasOrdenadas.getOrNull(1)?.pregunta.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.limpiarEstadoRecuperacion()
    }

    PantallaBase(mostrarFondo = false) {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Recuperar cuenta",
                subtitulo = "Valida tus preguntas de seguridad para restablecer la contrasena."
            )
            CampoContrabajo(
                valor = uiState.recuperacionIdentificador,
                onValueChange = viewModel::actualizarIdentificadorRecuperacion,
                etiqueta = "Usuario o correo"
            )
            BotonPrimario(
                texto = "Buscar cuenta",
                onClick = viewModel::cargarPreguntasRecuperacion
            )

            if (pregunta1.isNotBlank() && pregunta2.isNotBlank()) {
                Text(
                    text = "Pregunta 1: $pregunta1",
                    style = MaterialTheme.typography.bodySmall
                )
                CampoSecretoContrabajo(
                    valor = uiState.recuperacionRespuesta1,
                    onValueChange = viewModel::actualizarRespuestaRecuperacion1,
                    etiqueta = "Respuesta 1"
                )
                Text(
                    text = "Pregunta 2: $pregunta2",
                    style = MaterialTheme.typography.bodySmall
                )
                CampoSecretoContrabajo(
                    valor = uiState.recuperacionRespuesta2,
                    onValueChange = viewModel::actualizarRespuestaRecuperacion2,
                    etiqueta = "Respuesta 2"
                )
                BotonPrimario(
                    texto = "Validar respuestas",
                    onClick = viewModel::validarRespuestasRecuperacion
                )
            }

            if (uiState.recuperacionValidada) {
                CampoSecretoContrabajo(
                    valor = uiState.nuevaContrasenaRecuperacion,
                    onValueChange = viewModel::actualizarNuevaContrasenaRecuperacion,
                    etiqueta = "Nueva contrasena"
                )
                CampoSecretoContrabajo(
                    valor = uiState.confirmarContrasenaRecuperacion,
                    onValueChange = viewModel::actualizarConfirmarContrasenaRecuperacion,
                    etiqueta = "Confirmar contrasena"
                )
                BotonPrimario(
                    texto = "Restablecer contrasena",
                    onClick = viewModel::restablecerContrasenaRecuperacion
                )
            }

            if (uiState.errorRecuperacion != null) {
                Text(
                    text = uiState.errorRecuperacion.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            if (uiState.mensajeRecuperacion != null) {
                Text(
                    text = uiState.mensajeRecuperacion.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            BotonSecundario(
                texto = "Volver al login",
                onClick = onVolver
            )
        }
    }
}

package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    // Dialogo: cuenta baneada permanentemente
    if (uiState.cuentaBloqueada && uiState.tipoBloqueoCuenta == "BANEADO") {
        AlertDialog(
            onDismissRequest = { viewModel.consumirBloqueo() },
            title = { Text("Cuenta baneada") },
            text = {
                Text("Tu cuenta ha sido baneada permanentemente de la plataforma. Si crees que es un error, contacta al soporte.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.consumirBloqueo() }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Dialogo: cuenta suspendida temporalmente
    if (uiState.cuentaBloqueada && uiState.tipoBloqueoCuenta == "SUSPENDIDO") {
        val fechaTexto = uiState.fechaFinSuspension
            ?.let { raw ->
                // Formato entrada: "2026-06-01T23:59:59" o "2026-06-01T23:59:59.123"
                val sinFraccion = raw.substringBefore(".")
                val partes = sinFraccion.split("T")
                val fecha = partes.getOrNull(0) // "2026-06-01"
                val hora  = partes.getOrNull(1) // "23:59:59"
                // Reordenar a dd/MM/yyyy
                val fechaFormateada = fecha?.split("-")?.let { d ->
                    if (d.size == 3) "${d[2]}/${d[1]}/${d[0]}" else fecha
                } ?: fecha
                val horaFormateada = hora?.substring(0, 5) // "23:59"
                if (horaFormateada != null) "$fechaFormateada a las $horaFormateada" else fechaFormateada ?: raw
            }
        AlertDialog(
            onDismissRequest = { viewModel.consumirBloqueo() },
            title = { Text("Cuenta suspendida") },
            text = {
                if (fechaTexto != null) {
                    Text("Tu cuenta ha sido suspendida hasta el $fechaTexto. Durante este periodo no podras acceder a la aplicacion.")
                } else {
                    Text("Tu cuenta ha sido suspendida temporalmente. Contacta al soporte para mas informacion.")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.consumirBloqueo() }) {
                    Text("Entendido")
                }
            }
        )
    }

    PantallaBase(mostrarFondo = false) {
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
                            contentDescription = "Accesos de prueba"
                        )
                    }
                    DropdownMenu(
                        expanded = menuAutorellenoAbierto,
                        onDismissRequest = { menuAutorellenoAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cuenta cliente") },
                            onClick = {
                                menuAutorellenoAbierto = false
                                viewModel.autocompletarPerfilPrueba("cliente")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cuenta trabajador") },
                            onClick = {
                                menuAutorellenoAbierto = false
                                viewModel.autocompletarPerfilPrueba("trabajador")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cuenta moderador") },
                            onClick = {
                                menuAutorellenoAbierto = false
                                viewModel.autocompletarPerfilPrueba("moderador")
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
                etiqueta = "Nombre de usuario"
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
        }
        Text(
            text = "¿No tienes cuenta? Regístrate",
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
                etiqueta = "Nombre de usuario"
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
                Text(
                    text = "La contrasena debe tener minimo 8 caracteres, 1 mayuscula, 1 numero y 1 simbolo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

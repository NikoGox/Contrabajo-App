package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.movil.contrabajo.R
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
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            onLoginExitoso()
            viewModel.consumirNavegacionExitosa()
        }
    }

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

    if (uiState.cuentaBloqueada && uiState.tipoBloqueoCuenta == "SUSPENDIDO") {
        val fechaTexto = uiState.fechaFinSuspension
            ?.let { raw ->
                val sinFraccion = raw.substringBefore(".")
                val partes = sinFraccion.split("T")
                val fecha = partes.getOrNull(0)
                val hora = partes.getOrNull(1)
                val fechaFormateada = fecha?.split("-")?.let { d ->
                    if (d.size == 3) "${d[2]}/${d[1]}/${d[0]}" else fecha
                } ?: fecha
                val horaFormateada = hora?.substring(0, 5)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        cs.background,
                        cs.surfaceVariant.copy(alpha = 0.5f),
                        cs.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .zIndex(10f)
        ) {
            IconButton(
                onClick = { menuAutorellenoAbierto = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Accesos de prueba",
                    tint = cs.onSurface.copy(alpha = 0.5f)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bienvenido a",
                style = MaterialTheme.typography.titleLarge,
                color = cs.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ct_icon_mini_t),
                contentDescription = "Logo Contrabajo",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Contrabajo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = cs.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Usuario",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = uiState.identificador,
                    onValueChange = viewModel::actualizarIdentificador,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ingresa tu usuario") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                        focusedContainerColor = cs.surface,
                        unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                        focusedBorderColor = cs.primary,
                        cursorColor = cs.primary,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                    )
                )

                Text(
                    text = "Contraseña",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = uiState.contrasena,
                    onValueChange = viewModel::actualizarContrasena,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ingresa tu contraseña") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (contrasenaVisible) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                            Icon(
                                imageVector = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = cs.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                        focusedContainerColor = cs.surface,
                        unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                        focusedBorderColor = cs.primary,
                        cursorColor = cs.primary,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onRecuperarCuenta,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::iniciarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary
                )
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onPrimary
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.recordarme,
                    onCheckedChange = viewModel::actualizarRecordarme,
                    colors = CheckboxDefaults.colors(
                        checkedColor = cs.primary,
                        uncheckedColor = cs.onSurface.copy(alpha = 0.4f)
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Recordarme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface.copy(alpha = 0.5f)
                )
                TextButton(onClick = onRegistrarse) {
                    Text(
                        text = "Regístrate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = cs.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onVolver) {
                Text(
                    text = "Volver",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface.copy(alpha = 0.4f)
                )
            }
        }
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
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        viewModel.limpiarEstadoRecuperacion()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        cs.background,
                        cs.surfaceVariant.copy(alpha = 0.5f),
                        cs.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Recuperar cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = cs.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Valida tus preguntas de seguridad para restablecer la contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Nombre de usuario",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = uiState.recuperacionIdentificador,
                    onValueChange = viewModel::actualizarIdentificadorRecuperacion,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tu usuario") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                        focusedContainerColor = cs.surface,
                        unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                        focusedBorderColor = cs.primary,
                        cursorColor = cs.primary,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                    )
                )

                Button(
                    onClick = viewModel::cargarPreguntasRecuperacion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                ) {
                    Text("Buscar cuenta", fontWeight = FontWeight.SemiBold, color = cs.onPrimary)
                }
            }

            if (pregunta1.isNotBlank() && pregunta2.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = pregunta1,
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = uiState.recuperacionRespuesta1,
                        onValueChange = viewModel::actualizarRespuestaRecuperacion1,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tu respuesta") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                            focusedContainerColor = cs.surface,
                            unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                            focusedBorderColor = cs.primary,
                            cursorColor = cs.primary,
                            focusedTextColor = cs.onSurface,
                            unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                        )
                    )

                    Text(
                        text = pregunta2,
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = uiState.recuperacionRespuesta2,
                        onValueChange = viewModel::actualizarRespuestaRecuperacion2,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tu respuesta") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                            focusedContainerColor = cs.surface,
                            unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                            focusedBorderColor = cs.primary,
                            cursorColor = cs.primary,
                            focusedTextColor = cs.onSurface,
                            unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                        )
                    )

                    Button(
                        onClick = viewModel::validarRespuestasRecuperacion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                    ) {
                        Text("Validar respuestas", fontWeight = FontWeight.SemiBold, color = cs.onPrimary)
                    }
                }
            }

            if (uiState.recuperacionValidada) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Nueva contraseña",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = uiState.nuevaContrasenaRecuperacion,
                        onValueChange = viewModel::actualizarNuevaContrasenaRecuperacion,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mínimo 8 caracteres, 1 mayúscula, 1 número, 1 símbolo") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                            focusedContainerColor = cs.surface,
                            unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                            focusedBorderColor = cs.primary,
                            cursorColor = cs.primary,
                            focusedTextColor = cs.onSurface,
                            unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                        )
                    )

                    Text(
                        text = "Confirmar contraseña",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = uiState.confirmarContrasenaRecuperacion,
                        onValueChange = viewModel::actualizarConfirmarContrasenaRecuperacion,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Repite tu contraseña") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = cs.surface.copy(alpha = 0.7f),
                            focusedContainerColor = cs.surface,
                            unfocusedBorderColor = cs.outline.copy(alpha = 0.4f),
                            focusedBorderColor = cs.primary,
                            cursorColor = cs.primary,
                            focusedTextColor = cs.onSurface,
                            unfocusedTextColor = cs.onSurface.copy(alpha = 0.8f)
                        )
                    )

                    Button(
                        onClick = viewModel::restablecerContrasenaRecuperacion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                    ) {
                        Text("Restablecer contraseña", fontWeight = FontWeight.SemiBold, color = cs.onPrimary)
                    }
                }
            }

            if (uiState.errorRecuperacion != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorRecuperacion.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.mensajeRecuperacion != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.mensajeRecuperacion.orEmpty(),
                    color = cs.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onVolver) {
                Text(
                    text = "Volver al inicio de sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

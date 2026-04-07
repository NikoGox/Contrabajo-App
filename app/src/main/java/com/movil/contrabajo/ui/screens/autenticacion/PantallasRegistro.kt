package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.IndicadorPasos
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.RegistroViewModel

@Composable
fun PantallaRegistroPasoUno(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    val registro = viewModel.uiState.registro

    PantallaBase {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            IndicadorPasos(pasoActual = 1, totalPasos = 2)
            EncabezadoPantalla(
                titulo = "Crear cuenta",
                subtitulo = "Datos personales"
            )
            CampoContrabajo(registro.nombre, viewModel::actualizarNombre, "Nombre")
            CampoContrabajo(registro.apellidoPaterno, viewModel::actualizarApellidoPaterno, "Apellido paterno")
            CampoContrabajo(registro.apellidoMaterno, viewModel::actualizarApellidoMaterno, "Apellido materno")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                CampoContrabajo(
                    valor = registro.run,
                    onValueChange = viewModel::actualizarRun,
                    etiqueta = "RUN",
                    modifier = Modifier.weight(1f)
                )
                CampoContrabajo(
                    valor = registro.dv,
                    onValueChange = viewModel::actualizarDv,
                    etiqueta = "DV",
                    modifier = Modifier.weight(0.35f)
                )
            }
            CampoContrabajo(registro.telefono, viewModel::actualizarTelefono, "Telefono")
            BotonPrimario(texto = "Siguiente", onClick = onContinuar)
        }
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

@Composable
fun PantallaRegistroPasoDos(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val uiState = viewModel.uiState
    val registro = uiState.registro

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            onRegistroExitoso()
            viewModel.consumirRegistroExitoso()
        }
    }

    PantallaBase {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            IndicadorPasos(pasoActual = 2, totalPasos = 2)
            EncabezadoPantalla(
                titulo = "Crear cuenta",
                subtitulo = "Datos de la cuenta"
            )
            CampoContrabajo(registro.username, viewModel::actualizarUsername, "Nombre de usuario")
            CampoContrabajo(registro.correo, viewModel::actualizarCorreo, "Correo electronico")
            CampoContrabajo(
                valor = registro.contrasena,
                onValueChange = viewModel::actualizarContrasena,
                etiqueta = "Contrasena",
                visualTransformation = PasswordVisualTransformation()
            )
            CampoContrabajo(
                valor = registro.confirmarContrasena,
                onValueChange = viewModel::actualizarConfirmarContrasena,
                etiqueta = "Confirmar contrasena",
                visualTransformation = PasswordVisualTransformation()
            )
            Text(
                text = "Acepto los terminos y condiciones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.error != null) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            BotonPrimario(
                texto = "Registrarse",
                onClick = viewModel::registrarUsuario
            )
        }
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
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
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.LoginViewModel

@Composable
fun PantallaLogin(
    onVolver: () -> Unit,
    onLoginExitoso: () -> Unit,
    onRegistrarse: () -> Unit,
    viewModel: LoginViewModel
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            onLoginExitoso()
            viewModel.consumirNavegacionExitosa()
        }
    }

    PantallaBase {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Iniciar sesion",
                subtitulo = "Accede a tu cuenta para ver publicaciones, mensajes y tu perfil de trabajo."
            )
            CampoContrabajo(
                valor = uiState.identificador,
                onValueChange = viewModel::actualizarIdentificador,
                etiqueta = "Correo o usuario"
            )
            CampoContrabajo(
                valor = uiState.contrasena,
                onValueChange = viewModel::actualizarContrasena,
                etiqueta = "Contrasena",
                visualTransformation = PasswordVisualTransformation()
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

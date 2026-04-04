package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.ui.components.TarjetaBase

@Composable
fun PantallaLogin(
    onVolver: () -> Unit,
    onLoginExitoso: () -> Unit,
    onRegistrarse: () -> Unit,
    repositorioAutenticacion: RepositorioAutenticacion
) {
    var identificador by remember { mutableStateOf("vale@contrabajo.cl") }
    var contrasena by remember { mutableStateOf("123456") }
    var recordarme by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TarjetaBase {
            Text("Iniciar sesion", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Usa el usuario demo o crea una cuenta nueva.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = identificador,
                onValueChange = { identificador = it },
                label = { Text("Correo o usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contrasena") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            androidx.compose.foundation.layout.Row {
                Checkbox(checked = recordarme, onCheckedChange = { recordarme = it })
                Text("Recordarme", modifier = Modifier.padding(top = 12.dp))
            }
            if (error != null) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    repositorioAutenticacion.iniciarSesion(identificador, contrasena, recordarme)
                        .onSuccess { onLoginExitoso() }
                        .onFailure { error = it.message }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }
            OutlinedButton(onClick = onRegistrarse, modifier = Modifier.fillMaxWidth()) {
                Text("Registrarme")
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

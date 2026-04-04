package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.ui.components.TarjetaBase

@Composable
fun PantallaRegistroPasoUno(
    estadoInicial: RegistroPendiente,
    onVolver: () -> Unit,
    onContinuar: (RegistroPendiente) -> Unit
) {
    var registro by remember { mutableStateOf(estadoInicial) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TarjetaBase {
            Text("Crear cuenta 1/2", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Datos personales del prestador o cliente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = registro.nombre,
                onValueChange = { registro = registro.copy(nombre = it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = registro.apellidoPaterno,
                onValueChange = { registro = registro.copy(apellidoPaterno = it) },
                label = { Text("Apellido paterno") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = registro.apellidoMaterno,
                onValueChange = { registro = registro.copy(apellidoMaterno = it) },
                label = { Text("Apellido materno") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = registro.run,
                    onValueChange = { registro = registro.copy(run = it) },
                    label = { Text("RUN") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = registro.dv,
                    onValueChange = { registro = registro.copy(dv = it) },
                    label = { Text("DV") },
                    modifier = Modifier.weight(0.4f)
                )
            }
            OutlinedTextField(
                value = registro.telefono,
                onValueChange = { registro = registro.copy(telefono = it) },
                label = { Text("Telefono") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onContinuar(registro) }, modifier = Modifier.fillMaxWidth()) {
                Text("Siguiente")
            }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun PantallaRegistroPasoDos(
    estadoInicial: RegistroPendiente,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit,
    repositorioAutenticacion: RepositorioAutenticacion
) {
    var registro by remember { mutableStateOf(estadoInicial) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TarjetaBase {
            Text("Crear cuenta 2/2", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Datos de acceso para tu perfil.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = registro.username,
                onValueChange = { registro = registro.copy(username = it) },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = registro.correo,
                onValueChange = { registro = registro.copy(correo = it) },
                label = { Text("Correo electronico") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = registro.contrasena,
                onValueChange = { registro = registro.copy(contrasena = it) },
                label = { Text("Contrasena") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                value = registro.confirmarContrasena,
                onValueChange = { registro = registro.copy(confirmarContrasena = it) },
                label = { Text("Confirmar contrasena") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            if (error != null) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    repositorioAutenticacion.registrarUsuario(registro)
                        .onSuccess { onRegistroExitoso() }
                        .onFailure { error = it.message }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarme")
            }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

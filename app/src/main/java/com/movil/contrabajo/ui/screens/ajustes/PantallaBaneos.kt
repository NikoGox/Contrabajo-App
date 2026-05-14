package com.movil.contrabajo.ui.screens.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.UsuarioBaneado
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.BaneosViewModel

@Composable
fun PantallaBaneos(
    viewModel: BaneosViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.mensajeExito) {
        if (uiState.mensajeExito != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.consumirMensaje()
        }
    }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(
            titulo = "Moderacion de baneos",
            onVolver = onVolver,
            iconoDerecha = Icons.Filled.Gavel
        )

        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.usuarios.isEmpty() && !uiState.cargando -> {
                TarjetaBase {
                    Text(
                        text = "No hay usuarios baneados o suspendidos actualmente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.usuarios.forEach { usuario ->
                        TarjetaBaneado(
                            usuario = usuario,
                            desbaneoEnCurso = usuario.idUsuario in uiState.desbaneoEnCurso,
                            onDesbanear = { viewModel.desbanear(usuario.idUsuario) }
                        )
                    }
                }
            }
        }

        if (uiState.mensajeExito != null) {
            Text(
                text = uiState.mensajeExito,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        OutlinedButton(
            onClick = { viewModel.cargar() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.cargando
        ) {
            Text("Actualizar lista")
        }
    }
}

@Composable
private fun TarjetaBaneado(
    usuario: UsuarioBaneado,
    desbaneoEnCurso: Boolean,
    onDesbanear: () -> Unit
) {
    val colorSancion = if (usuario.tipoSancion == "BANEADO") Color(0xFFB71C1C) else Color(0xFFE65100)

    TarjetaBase(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "@${usuario.username}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${usuario.nombre} ${usuario.apellidos}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = colorSancion.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = usuario.tipoSancion,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorSancion,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Button(
                onClick = onDesbanear,
                enabled = !desbaneoEnCurso,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (desbaneoEnCurso) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(18.dp)
                            .padding(horizontal = 4.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Desbanear")
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        val tipoBaneo = if (usuario.permanente) "Permanente" else "Temporal"
        Text(
            text = "Tipo: $tipoBaneo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!usuario.permanente && usuario.fechaFin != null) {
            Text(
                text = "Hasta: ${usuario.fechaFin}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!usuario.motivo.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Motivo: ${usuario.motivo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

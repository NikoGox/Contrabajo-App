package com.movil.contrabajo.ui.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel

@Composable
fun PantallaDetalleChat(
    idChatCita: Long,
    viewModel: ChatsViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(idChatCita) {
        viewModel.abrirChat(idChatCita)
    }

    val uiState = viewModel.uiState
    val chat = uiState.chatActivo
    var mostrarModalCita by remember { mutableStateOf(false) }
    var fechaProgramadaInput by remember { mutableStateOf("") }
    var detalleCitaInput by remember { mutableStateOf("") }

    PantallaBase(
        modifier = modifier,
        scrollable = false,
        mostrarFondo = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat?.nombreContacto ?: "Conversacion",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Chat de coordinacion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TarjetaBase(
            modifier = Modifier.weight(1f),
            llenarAlto = true
        ) {
            if (chat == null) {
                Text(
                    text = "No se pudo cargar la conversacion.",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                CitaResumen(
                    cita = uiState.citaActiva,
                    onCrear = { mostrarModalCita = true },
                    onPendiente = viewModel::marcarCitaPendiente,
                    onConfirmada = viewModel::marcarCitaConfirmada,
                    onEnProceso = viewModel::marcarCitaEnProceso,
                    onFinalizada = viewModel::marcarCitaFinalizada
                )

                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.mensajesActivos, key = { it.idMensajeChat }) { mensaje ->
                        BurbujaMensaje(
                            mensaje = mensaje,
                            esPropio = mensaje.idEmisor == uiState.idUsuarioActual
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoContrabajo(
                        valor = uiState.borradorMensaje,
                        onValueChange = viewModel::actualizarBorradorMensaje,
                        etiqueta = "Escribe un mensaje",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = viewModel::enviarMensaje) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                uiState.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                uiState.mensajeSistema?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (mostrarModalCita) {
        AlertDialog(
            onDismissRequest = { mostrarModalCita = false },
            title = { Text("Generar cita de servicio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoContrabajo(
                        valor = fechaProgramadaInput,
                        onValueChange = { fechaProgramadaInput = it },
                        etiqueta = "Fecha y hora (YYYY-MM-DD HH:mm)"
                    )
                    CampoContrabajo(
                        valor = detalleCitaInput,
                        onValueChange = { detalleCitaInput = it },
                        etiqueta = "Detalle de la cita"
                    )
                }
            },
            confirmButton = {
                BotonPrimario(
                    texto = "Guardar",
                    onClick = {
                        viewModel.crearCita(
                            fechaProgramada = fechaProgramadaInput,
                            detalle = detalleCitaInput
                        )
                        mostrarModalCita = false
                        fechaProgramadaInput = ""
                        detalleCitaInput = ""
                    }
                )
            },
            dismissButton = {
                BotonSecundario(
                    texto = "Cancelar",
                    onClick = { mostrarModalCita = false }
                )
            }
        )
    }
}

@Composable
private fun BurbujaMensaje(
    mensaje: MensajeChat,
    esPropio: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esPropio) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (esPropio) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = mensaje.contenido,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = mensaje.fechaEnvio.takeLast(5),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CitaResumen(
    cita: CitaServicio?,
    onCrear: () -> Unit,
    onPendiente: () -> Unit,
    onConfirmada: () -> Unit,
    onEnProceso: () -> Unit,
    onFinalizada: () -> Unit
) {
    if (cita == null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sin cita creada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Crea una cita para formalizar fecha y hora del servicio.",
                    style = MaterialTheme.typography.bodySmall
                )
                BotonPrimario(
                    texto = "Crear cita",
                    onClick = onCrear
                )
            }
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cita del servicio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                EtiquetaEstado(
                    texto = etiquetaEstadoCita(cita.estado),
                    enfatizada = cita.estado == EstadoCita.CONFIRMADA || cita.estado == EstadoCita.EN_PROCESO
                )
            }
            Text(
                text = "Fecha: ${cita.fechaProgramada}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = cita.detalle,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BotonEstadoCita(
                    texto = "Pend.",
                    activo = cita.estado == EstadoCita.PENDIENTE,
                    onClick = onPendiente,
                    modifier = Modifier.weight(1f)
                )
                BotonEstadoCita(
                    texto = "Conf.",
                    activo = cita.estado == EstadoCita.CONFIRMADA,
                    onClick = onConfirmada,
                    modifier = Modifier.weight(1f)
                )
                BotonEstadoCita(
                    texto = "Proceso",
                    activo = cita.estado == EstadoCita.EN_PROCESO,
                    onClick = onEnProceso,
                    modifier = Modifier.weight(1f)
                )
                BotonEstadoCita(
                    texto = "Fin",
                    activo = cita.estado == EstadoCita.FINALIZADA,
                    onClick = onFinalizada,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BotonEstadoCita(
    texto: String,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (activo) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
        ) {
        Text(
            text = texto,
            color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
        )
        }
    }
}

private fun etiquetaEstadoCita(estado: Int): String = when (estado) {
    EstadoCita.PENDIENTE -> "Pendiente"
    EstadoCita.CONFIRMADA -> "Confirmada"
    EstadoCita.EN_PROCESO -> "En proceso"
    EstadoCita.FINALIZADA -> "Finalizada"
    else -> "Pendiente"
}

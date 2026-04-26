package com.movil.contrabajo.ui.screens.chats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel

@Composable
fun PantallaCitaServicio(
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
    val cita = uiState.citaActiva
    val idUsuarioActual = uiState.idUsuarioActual
    val esCliente = chat != null && idUsuarioActual != null && chat.idCliente == idUsuarioActual
    val esTrabajador = chat != null && idUsuarioActual != null && chat.idTrabajador == idUsuarioActual

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Column {
                Text(
                    text = "Cita de servicio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = chat?.tituloServicio?.ifBlank { "Servicio" } ?: "Servicio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TarjetaBase {
            if (chat == null || cita == null) {
                Text(
                    text = "No hay cita disponible para este chat.",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado de la cita",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    EtiquetaEstado(
                        texto = etiquetaEstadoCita(cita.estado),
                        enfatizada = cita.estado == EstadoCita.HANDSHAKE || cita.estado == EstadoCita.EN_PROCESO
                    )
                }
                Text("Servicio: ${chat.tituloServicio}")
                Text("Trabajador: ${chat.nombreContacto}")
                Text("Categoria: ${chat.categoriaServicio.ifBlank { "General" }}")
                Text("Fecha solicitud: ${cita.fechaCreacion}")
                Text("Fecha programada: ${cita.fechaProgramada}")
                Text("Comentario: ${cita.comentario}")
                cita.fechaInicioTrabajo?.let { Text("Inicio trabajo: $it") }
                cita.fechaFinTrabajo?.let { Text("Fin trabajo: $it") }

                if (esTrabajador && !chat.chatCerrado) {
                    when (cita.estado) {
                        EstadoCita.PENDIENTE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BotonSecundario(
                                    texto = "Rechazar",
                                    onClick = viewModel::rechazarCitaTrabajador,
                                    modifier = Modifier.weight(1f)
                                )
                                BotonPrimario(
                                    texto = "Aceptar",
                                    onClick = viewModel::aceptarCitaTrabajador,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        EstadoCita.HANDSHAKE -> BotonPrimario(
                            texto = "Solicitar inicio",
                            onClick = viewModel::solicitarInicioTrabajoTrabajador
                        )

                        EstadoCita.EN_PROCESO -> BotonPrimario(
                            texto = "Solicitar finalizacion",
                            onClick = viewModel::solicitarFinalizarTrabajoTrabajador
                        )
                    }
                }

                if (esCliente && !chat.chatCerrado) {
                    when (cita.estado) {
                        EstadoCita.COMENZANDO -> BotonPrimario(
                            texto = "Aceptar inicio",
                            onClick = viewModel::aceptarInicioTrabajoCliente
                        )

                        EstadoCita.FINALIZANDO -> BotonPrimario(
                            texto = "Aceptar finalizacion",
                            onClick = viewModel::aceptarFinalizarTrabajoCliente
                        )

                        EstadoCita.RECHAZADA -> BotonPrimario(
                            texto = "Reenviar propuesta",
                            onClick = viewModel::reenviarPropuestaCitaCliente
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
}

private fun etiquetaEstadoCita(estado: Int): String = when (estado) {
    EstadoCita.PENDIENTE -> "Pendiente"
    EstadoCita.HANDSHAKE -> "Confirmada"
    EstadoCita.COMENZANDO -> "Pendiente de confirmacion"
    EstadoCita.EN_PROCESO -> "En proceso"
    EstadoCita.FINALIZANDO -> "Finalizando"
    EstadoCita.FINALIZADO -> "Finalizada"
    EstadoCita.RECHAZADA -> "Rechazada"
    EstadoCita.CANCELADO -> "Cancelada"
    EstadoCita.CERRADO -> "Cerrada"
    else -> "Pendiente"
}

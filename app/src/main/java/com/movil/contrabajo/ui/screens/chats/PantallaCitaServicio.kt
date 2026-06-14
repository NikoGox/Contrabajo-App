package com.movil.contrabajo.ui.screens.chats

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.EtiquetaEstado
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

    val cargando = chat == null || cita == null

    Column(modifier = modifier.fillMaxSize()) {
        TopbarCitaServicio(
            titulo = chat?.tituloServicio?.ifBlank { "Cita de servicio" } ?: "Cita de servicio",
            subtitulo = chat?.nombreContacto?.takeIf { it.isNotBlank() } ?: chat?.usernameContacto?.ifBlank { null },
            onVolver = onVolver
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (cargando) {
                SkeletonCitaServicio()
            } else {
                TarjetaBase {
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
                            texto = etiquetaEstadoCita(cita!!.estado),
                            enfatizada = cita.estado == EstadoCita.HANDSHAKE || cita.estado == EstadoCita.EN_PROCESO
                        )
                    }
                    Text("Servicio: ${chat!!.tituloServicio}")
                    Text("Trabajador: ${chat.nombreContacto}")
                    Text("Categoría: ${chat.categoriaServicio.ifBlank { "General" }}")
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
                                texto = "Solicitar finalización",
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
                                texto = "Aceptar finalización",
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
}

@Composable
private fun TopbarCitaServicio(
    titulo: String,
    subtitulo: String?,
    onVolver: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun SkeletonCitaServicio() {
    val transicion = rememberInfiniteTransition(label = "shimmerCita")
    val shimmerX by transicion.animateFloat(
        initialValue = -350f,
        targetValue = 1100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerXCita"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 350f, 350f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 140.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 60.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(0.5f), alto = 48.dp, brush = brush)
    }
}

@Composable
private fun CajaSkeleton(modifier: Modifier = Modifier, alto: Dp, brush: Brush) {
    Box(
        modifier = modifier
            .height(alto)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

private fun etiquetaEstadoCita(estado: Int): String = when (estado) {
    EstadoCita.PENDIENTE -> "Pendiente"
    EstadoCita.HANDSHAKE -> "Confirmada"
    EstadoCita.COMENZANDO -> "Pendiente de confirmación"
    EstadoCita.EN_PROCESO -> "En proceso"
    EstadoCita.FINALIZANDO -> "Finalizando"
    EstadoCita.FINALIZADO -> "Finalizada"
    EstadoCita.RECHAZADA -> "Rechazada"
    EstadoCita.CANCELADO -> "Cancelada"
    EstadoCita.CERRADO -> "Cerrada"
    else -> "Pendiente"
}

package com.movil.contrabajo.ui.screens.perfil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.ValoracionesServicio
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun PantallaValoracionesServicios(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val valoraciones = uiState.valoracionesPorServicio
    var idSeleccionado by rememberSaveable { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.recargar()
    }

    LaunchedEffect(valoraciones) {
        if (valoraciones.isEmpty()) {
            idSeleccionado = 0L
        } else if (valoraciones.none { it.oferta.idOfertaServicio == idSeleccionado }) {
            idSeleccionado = valoraciones.first().oferta.idOfertaServicio
        }
    }

    val servicioActivo = remember(valoraciones, idSeleccionado) {
        valoraciones.firstOrNull { it.oferta.idOfertaServicio == idSeleccionado }
    }

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = "Valoraciones",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        TarjetaBase {
            Text(
                text = "Servicios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (valoraciones.isEmpty()) {
                Text(
                    text = "Aun no tienes valoraciones en tus publicaciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                valoraciones.forEach { item ->
                    SelectorServicioValorado(
                        item = item,
                        activo = item.oferta.idOfertaServicio == idSeleccionado,
                        onClick = { idSeleccionado = item.oferta.idOfertaServicio }
                    )
                }
            }
        }

        TarjetaBase {
            if (servicioActivo == null) {
                Text(
                    text = "Selecciona un servicio para revisar sus valoraciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = servicioActivo.oferta.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (servicioActivo.oferta.eliminada) "Fuera de servicio" else "Servicio activo o disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (servicioActivo.valoraciones.isEmpty()) {
                    Text(
                        text = "Sin valoraciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    servicioActivo.valoraciones.forEach { valoracion ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${valoracion.voto} estrellas",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "@${valoracion.usernameCliente.ifBlank { "usuario" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Finalizada: ${formatearFechaValoracion(valoracion.fechaFinalizacionCita ?: valoracion.fechaVoto)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = valoracion.comentario.ifBlank { "Sin comentarios" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatearFechaValoracion(fecha: String): String {
    if (fecha.isBlank()) return "Sin fecha"
    return try {
        LocalDateTime
            .parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (_: DateTimeParseException) {
        fecha
    }
}

@Composable
private fun SelectorServicioValorado(
    item: ValoracionesServicio,
    activo: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (activo) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.oferta.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (item.oferta.eliminada) "Fuera de servicio" else "Disponible para historial",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${promedioServicio(item)} ★",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.valoraciones.size.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun promedioServicio(item: ValoracionesServicio): String {
    if (item.valoraciones.isEmpty()) return "0.0"
    return String.format("%.1f", item.valoraciones.map { it.voto }.average())
}

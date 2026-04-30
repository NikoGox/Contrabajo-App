package com.movil.contrabajo.ui.screens.reportes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import coil.compose.AsyncImage
import com.movil.contrabajo.domain.model.AccionModeracion
import com.movil.contrabajo.domain.model.EstadoReporte
import com.movil.contrabajo.domain.model.Reporte
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.ReportesViewModel

@Composable
fun PantallaReportesModerador(
    viewModel: ReportesViewModel,
    onAbrirDetalleReporte: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    var menuTipoAbierto by remember { mutableStateOf(false) }
    var menuEstadoAbierto by remember { mutableStateOf(false) }
    var buscadorExpandido by rememberSaveable { mutableStateOf(false) }
    val glow = rememberInfiniteTransition(label = "glowBuscadorReportes")
    val glowFase by glow.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_600, easing = LinearEasing)
        ),
        label = "glowFaseBuscadorReportes"
    )
    val glowPulso by glow.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulsoBuscadorReportes"
    )
    val progresoBuscador by animateFloatAsState(
        targetValue = if (buscadorExpandido) 1f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "progresoBuscadorModerador"
    )

    LaunchedEffect(Unit) {
        viewModel.recargar()
    }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    if (progresoBuscador <= 0.02f) return@drawWithContent
                    val fase = size.width * (glowFase * 2.3f)
                    val radio = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                    val alphaBase = (0.05f + (0.05f * glowPulso) + (0.04f * progresoBuscador)).coerceIn(0f, 0.13f)

                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E88E5).copy(alpha = alphaBase),
                                Color(0xFF00BCD4).copy(alpha = (alphaBase * 1.05f).coerceAtMost(0.17f)),
                                Color(0xFF17A673).copy(alpha = alphaBase),
                                Color(0xFF1E88E5).copy(alpha = alphaBase)
                            ),
                            start = Offset(fase - (size.width * 2f), 0f),
                            end = Offset(fase, size.height)
                        ),
                        topLeft = Offset(-2.4f, -2.4f),
                        size = Size(size.width + 4.8f, size.height + 4.8f),
                        cornerRadius = CornerRadius(19.dp.toPx(), 19.dp.toPx()),
                        style = Stroke(width = 4.4.dp.toPx())
                    )

                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E88E5).copy(alpha = 0.8f),
                                Color(0xFF00BCD4).copy(alpha = 0.88f),
                                Color(0xFF17A673).copy(alpha = 0.8f),
                                Color(0xFF1E88E5).copy(alpha = 0.8f)
                            ),
                            start = Offset(size.width - fase, 0f),
                            end = Offset(-fase, size.height)
                        ),
                        cornerRadius = radio,
                        style = Stroke(width = 2.4.dp.toPx())
                    )
                },
            color = lerp(MaterialTheme.colorScheme.primary, Color.White, progresoBuscador),
            shape = RoundedCornerShape(18.dp),
            shadowElevation = (8f + (8f * progresoBuscador)).dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            alpha = (1f - progresoBuscador).coerceIn(0f, 1f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (progresoBuscador < 0.98f) {
                        IconButton(
                            onClick = onAbrirAjustes,
                            enabled = progresoBuscador < 0.12f
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Ajustes",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                BasicTextField(
                    value = uiState.busqueda,
                    onValueChange = viewModel::actualizarBusqueda,
                    enabled = buscadorExpandido || progresoBuscador > 0.01f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 12.dp)
                        .graphicsLayer {
                            alpha = progresoBuscador.coerceIn(0f, 1f)
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF0F2124),
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(Color(0xFF0F2124)),
                    decorationBox = { innerTextField ->
                        if (uiState.busqueda.isBlank()) {
                            Text(
                                text = "Buscar reportes",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF60737A)
                            )
                        }
                        innerTextField()
                    }
                )

                IconButton(
                    onClick = { buscadorExpandido = !buscadorExpandido }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = if (progresoBuscador > 0.02f) {
                            Color(0xFF0F2124)
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }
        }

        TarjetaBase {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { menuTipoAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val nombreTipo = uiState.tiposReporte
                            .firstOrNull { it.idTipoReporte == uiState.filtroTipoReporteId }
                            ?.nombre ?: "Tipo: todos"
                        Text(nombreTipo)
                    }
                    DropdownMenu(
                        expanded = menuTipoAbierto,
                        onDismissRequest = { menuTipoAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                menuTipoAbierto = false
                                viewModel.actualizarFiltroTipo(null)
                            }
                        )
                        uiState.tiposReporte.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.nombre) },
                                onClick = {
                                    menuTipoAbierto = false
                                    viewModel.actualizarFiltroTipo(tipo.idTipoReporte)
                                }
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { menuEstadoAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Estado: ${uiState.filtroEstadoRevision ?: "todos"}")
                    }
                    DropdownMenu(
                        expanded = menuEstadoAbierto,
                        onDismissRequest = { menuEstadoAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                menuEstadoAbierto = false
                                viewModel.actualizarFiltroEstado(null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(EstadoReporte.PENDIENTE) },
                            onClick = {
                                menuEstadoAbierto = false
                                viewModel.actualizarFiltroEstado(EstadoReporte.PENDIENTE)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(EstadoReporte.EN_REVISION) },
                            onClick = {
                                menuEstadoAbierto = false
                                viewModel.actualizarFiltroEstado(EstadoReporte.EN_REVISION)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(EstadoReporte.RESUELTO) },
                            onClick = {
                                menuEstadoAbierto = false
                                viewModel.actualizarFiltroEstado(EstadoReporte.RESUELTO)
                            }
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = { viewModel.actualizarOrdenRecientes(!uiState.ordenarRecientes) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.ordenarRecientes) "Orden: mas recientes" else "Orden: mas antiguos")
            }
        }

        if (uiState.reportes.isEmpty()) {
            TarjetaBase {
                Text(
                    text = "No hay reportes con los filtros actuales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            uiState.reportes.forEach { reporte ->
                TarjetaReporteModerador(
                    reporte = reporte,
                    onAbrir = { onAbrirDetalleReporte(reporte.idReporte) }
                )
            }
        }
    }

    OverlayPantallaCarga(
        visible = uiState.cargando,
        mensaje = "Aplicando medida..."
    )
}

@Composable
fun PantallaDetalleReporteModerador(
    idReporte: Long,
    viewModel: ReportesViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val reporte = uiState.reporteActivo
    var confirmarAccion by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idReporte) {
        viewModel.abrirDetalle(idReporte)
    }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        TarjetaBase {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detalle de reporte",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = onVolver) {
                    Text("Volver")
                }
            }
        }

        if (reporte == null) {
            TarjetaBase {
                Text(
                    text = "No se pudo cargar el reporte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            TarjetaReporteExpandida(reporte = reporte)
            TarjetaBase {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Estado: ${reporte.estadoRevision}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!reporte.medidaAplicada.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = "Medida: ${reporte.medidaAplicada}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Text("Comentario del emisor", fontWeight = FontWeight.SemiBold)
                Text(reporte.comentario)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { confirmarAccion = AccionModeracion.DESACTIVAR_SERVICIO },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = reporte.idOfertaServicio != null && reporte.estadoRevision != EstadoReporte.RESUELTO
                ) {
                    Text("Desactivar servicio")
                }
                OutlinedButton(
                    onClick = { confirmarAccion = AccionModeracion.ELIMINAR_SERVICIO },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = reporte.idOfertaServicio != null && reporte.estadoRevision != EstadoReporte.RESUELTO
                ) {
                    Text("Eliminar servicio")
                }
            }
        }
    }

    if (confirmarAccion != null && reporte != null) {
        AlertDialog(
            onDismissRequest = { confirmarAccion = null },
            title = { Text("Confirmar medida") },
            text = {
                Text(
                    if (confirmarAccion == AccionModeracion.DESACTIVAR_SERVICIO) {
                        "Se desactivara la disponibilidad del servicio y el reporte quedara resuelto."
                    } else {
                        "Se eliminara logicamente el servicio y el reporte quedara resuelto."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val accion = confirmarAccion ?: return@TextButton
                        viewModel.aplicarMedidaModeracion(reporte.idReporte, accion)
                        confirmarAccion = null
                    }
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarAccion = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    OverlayPantallaCarga(
        visible = uiState.cargando,
        mensaje = "Aplicando medida..."
    )
}

@Composable
private fun TarjetaReporteModerador(
    reporte: Reporte,
    onAbrir: () -> Unit
) {
    TarjetaBase(
        modifier = Modifier.clickable { onAbrir() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                if (reporte.servicioFotoUrl.isBlank()) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    AsyncImage(
                        model = reporte.servicioFotoUrl,
                        contentDescription = reporte.servicioTitulo,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reporte.tipoReporteNombre.ifBlank { "Tipo no definido" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = reporte.servicioTitulo.ifBlank { "Servicio sin titulo disponible" },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reporte.comentario,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Servicio ofrecido por: ${reporte.usuarioReportadoNombre.ifBlank { reporte.usuarioReportadoUsername.ifBlank { "desconocido" } }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Estado: ${reporte.estadoRevision} | Fecha: ${reporte.fechaCreacion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaReporteExpandida(
    reporte: Reporte
) {
    TarjetaBase {
        Text(
            text = "Publicacion reportada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            if (reporte.servicioFotoUrl.isBlank()) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sin imagen disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = reporte.servicioFotoUrl,
                    contentDescription = reporte.servicioTitulo,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            text = reporte.servicioTitulo.ifBlank { "Servicio sin titulo disponible" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Servicio ofrecido por: ${reporte.usuarioReportadoNombre.ifBlank { reporte.usuarioReportadoUsername.ifBlank { "desconocido" } }}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "ID reporte: ${reporte.idReporte} | ID servicio: ${reporte.idOfertaServicio ?: "-"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

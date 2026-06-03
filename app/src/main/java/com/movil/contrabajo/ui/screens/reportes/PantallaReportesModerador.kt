package com.movil.contrabajo.ui.screens.reportes

import android.app.DatePickerDialog
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
import androidx.compose.material3.OutlinedTextField
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
import java.util.Calendar

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
                Text(if (uiState.ordenarRecientes) "Orden: más recientes" else "Orden: más antiguos")
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
    var mostrarDialogoSuspension by remember { mutableStateOf(false) }
    // fechaInicioSuspension: null = hoy (por defecto), string ISO = fecha elegida
    var fechaInicioSuspension by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaFinSuspension by rememberSaveable { mutableStateOf("") }
    val contexto = androidx.compose.ui.platform.LocalContext.current

    // Capturamos el error y el mensaje de éxito en estado local para mostrarlos
    // aunque el ViewModel los limpie al instante con consumirMensajes()
    var errorDialog by remember { mutableStateOf<String?>(null) }
    var mensajeExitoDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idReporte) {
        viewModel.abrirDetalle(idReporte)
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            errorDialog = uiState.error
            viewModel.consumirMensajes()
        }
    }

    LaunchedEffect(uiState.mensajeSistema) {
        if (uiState.mensajeSistema != null) {
            mensajeExitoDialog = uiState.mensajeSistema
            viewModel.consumirMensajes()
        }
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
                                text = "Medida: ${reporte.medidaAplicada.formatoHumanoMedida()}",
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
                OutlinedButton(
                    onClick = { confirmarAccion = AccionModeracion.IGNORAR_REPORTE },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = reporte.estadoRevision != EstadoReporte.RESUELTO
                ) {
                    Text("Ignorar reporte")
                }
                OutlinedButton(
                    onClick = {
                        fechaInicioSuspension = null
                        fechaFinSuspension = ""
                        mostrarDialogoSuspension = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = reporte.estadoRevision != EstadoReporte.RESUELTO
                ) {
                    Text("Suspender usuario hasta fecha")
                }
                OutlinedButton(
                    onClick = { confirmarAccion = AccionModeracion.BANEAR_USUARIO },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = reporte.estadoRevision != EstadoReporte.RESUELTO
                ) {
                    Text("Banear usuario")
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
                        "Se desactivará la disponibilidad del servicio y el reporte quedará resuelto."
                    } else if (confirmarAccion == AccionModeracion.ELIMINAR_SERVICIO) {
                        "Se eliminará lógicamente el servicio y el reporte quedará resuelto."
                    } else if (confirmarAccion == AccionModeracion.BANEAR_USUARIO) {
                        "Se baneará al usuario reportado de forma permanente y el reporte quedará resuelto."
                    } else if (confirmarAccion == AccionModeracion.IGNORAR_REPORTE) {
                        "El reporte quedará marcado como ignorado/resuelto sin aplicar castigos."
                    } else {
                        "Se aplicará la acción seleccionada."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val accionBase = confirmarAccion ?: return@TextButton
                        val accion = if (
                            accionBase == AccionModeracion.BANEAR_USUARIO &&
                            reporte.idUsuarioReportado != null
                        ) {
                            "${AccionModeracion.BANEAR_USUARIO}|USR:${reporte.idUsuarioReportado}"
                        } else {
                            accionBase
                        }
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

    if (mostrarDialogoSuspension && reporte != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoSuspension = false
                fechaInicioSuspension = null
                fechaFinSuspension = ""
            },
            title = { Text("Suspender usuario") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // --- Fecha de inicio ---
                    Text("Inicio de suspensión", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                contexto,
                                { _, y, m, d ->
                                    fechaInicioSuspension = String.format("%04d-%02d-%02dT00:00:00", y, m + 1, d)
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (fechaInicioSuspension != null)
                                "Inicio: ${fechaInicioSuspension!!.substringBefore("T")}"
                            else
                                "Inicio: Hoy (por defecto)"
                        )
                    }
                    // --- Fecha de fin ---
                    Text("Fin de suspensión", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                contexto,
                                { _, y, m, d ->
                                    fechaFinSuspension = String.format("%04d-%02d-%02dT23:59:59", y, m + 1, d)
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (fechaFinSuspension.isNotBlank())
                                "Fin: ${fechaFinSuspension.substringBefore("T")}"
                            else
                                "Seleccionar fecha de fin"
                        )
                    }
                    if (fechaFinSuspension.isBlank()) {
                        Text(
                            "Debes seleccionar la fecha de fin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = fechaFinSuspension.isNotBlank(),
                    onClick = {
                        val fin = fechaFinSuspension.trim()
                        val inicio = fechaInicioSuspension?.trim()
                        // Formato: SUSPENDER_USUARIO_HASTA:{fechaFin}
                        //      o:  SUSPENDER_USUARIO_HASTA:{fechaInicio}/{fechaFin}
                        val parametro = if (inicio != null) "$inicio/$fin" else fin
                        val accionBase = "${AccionModeracion.SUSPENDER_USUARIO_HASTA}:$parametro"
                        val accion = if (reporte.idUsuarioReportado != null) {
                            "$accionBase|USR:${reporte.idUsuarioReportado}"
                        } else {
                            accionBase
                        }
                        viewModel.aplicarMedidaModeracion(reporte.idReporte, accion)
                        mostrarDialogoSuspension = false
                        fechaInicioSuspension = null
                        fechaFinSuspension = ""
                    }
                ) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoSuspension = false
                    fechaInicioSuspension = null
                    fechaFinSuspension = ""
                }) { Text("Cancelar") }
            }
        )
    }

    // Dialogo de ERROR — se dispara cuando el ViewModel setea uiState.error
    if (errorDialog != null) {
        AlertDialog(
            onDismissRequest = { errorDialog = null },
            title = { Text("No se pudo aplicar la medida") },
            text = { Text(errorDialog ?: "") },
            confirmButton = {
                TextButton(onClick = { errorDialog = null }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Dialogo de EXITO — se dispara cuando el ViewModel setea uiState.mensajeSistema
    if (mensajeExitoDialog != null) {
        AlertDialog(
            onDismissRequest = { mensajeExitoDialog = null },
            title = { Text("Medida aplicada") },
            text = { Text(mensajeExitoDialog ?: "") },
            confirmButton = {
                TextButton(onClick = { mensajeExitoDialog = null }) {
                    Text("Aceptar")
                }
            }
        )
    }

    OverlayPantallaCarga(
        visible = uiState.cargando,
        mensaje = "Aplicando medida..."
    )
}

private fun String?.formatoHumanoMedida(): String {
    val valor = this?.trim().orEmpty()
    return when {
        valor.equals(AccionModeracion.IGNORAR_REPORTE, ignoreCase = true) -> "Ignorar reporte"
        valor.equals(AccionModeracion.DESACTIVAR_SERVICIO, ignoreCase = true) -> "Desactivar servicio"
        valor.equals(AccionModeracion.ELIMINAR_SERVICIO, ignoreCase = true) -> "Eliminar servicio"
        valor.equals(AccionModeracion.BANEAR_USUARIO, ignoreCase = true) -> "Banear usuario"
        valor.startsWith(AccionModeracion.SUSPENDER_USUARIO_HASTA, ignoreCase = true) -> {
            val fecha = valor.substringAfter(':', "").trim()
            if (fecha.isBlank()) "Suspender usuario" else "Suspender usuario hasta $fecha"
        }
        else -> valor.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
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
                    text = reporte.servicioTitulo.ifBlank { "Servicio sin título disponible" },
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
            text = "Publicación reportada",
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
            text = reporte.servicioTitulo.ifBlank { "Servicio sin título disponible" },
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

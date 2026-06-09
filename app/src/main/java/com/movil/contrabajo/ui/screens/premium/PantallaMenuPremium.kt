package com.movil.contrabajo.ui.screens.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.contrabajo.ui.components.EstrellaPremiumAnimada
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PremiumHistorialContacto
import com.movil.contrabajo.ui.viewmodel.PremiumSerieDia
import com.movil.contrabajo.ui.viewmodel.PremiumStats
import com.movil.contrabajo.ui.viewmodel.PremiumViewModel
import kotlin.math.max

@Composable
fun PantallaMenuPremium(
    viewModel: PremiumViewModel,
    onAbrirHistorial: () -> Unit,
    onAbrirLecturaRapida: () -> Unit,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    val s = uiState.stats

    LaunchedEffect(Unit) {
        viewModel.refrescarEstadoPremium()
        viewModel.cargarEstadisticas()
    }

    PantallaBase(scrollable = true, mostrarFondo = true, respetarNavegacionInferior = false) {
        Spacer(Modifier.height(0.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0D5662),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0D5662), Color(0xFF11807B))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Menú Premium",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(40.dp))
            }
        }

        Spacer(Modifier.height(2.dp))

        if (uiState.cargandoStats) {
            SkeletonMenuPremium()
        } else {
            ResumenPremium(s = s)
            Spacer(Modifier.height(2.dp))
            AccesosPremium(
                onAbrirHistorial = onAbrirHistorial,
                onAbrirLecturaRapida = onAbrirLecturaRapida
            )
            Spacer(Modifier.height(2.dp))
            DashboardSemanalDesplegable(
                titulo = "Contactos por día",
                subtitulo = "Días con más conversaciones iniciadas",
                icono = Icons.Filled.ChatBubbleOutline,
                color = Color(0xFF1F8BFF),
                serie = s.contactosPorDia,
                pie = "Mejor día: ${s.mejorDiaContactos}"
            )
            DashboardSemanalDesplegable(
                titulo = "Días más rentables",
                subtitulo = "Cierres finalizados por día de la semana",
                icono = Icons.Filled.AttachMoney,
                color = Color(0xFF0E8894),
                serie = s.ingresosPorDia,
                pie = "Mejor día: ${s.mejorDiaIngresos}"
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun PantallaHistorialContactosPremium(
    viewModel: PremiumViewModel,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }

    PantallaBase(scrollable = true, mostrarFondo = true, respetarNavegacionInferior = false) {
        Spacer(Modifier.height(0.dp))
        EncabezadoSecundarioPremium(
            titulo = "Historial de contactos",
            onVolver = onVolver,
            mostrarEstrella = false
        )
        Spacer(Modifier.height(4.dp))
        if (uiState.historialContactos.isEmpty()) {
            TarjetaBase {
                Text(
                    text = "Aún no hay contactos cerrados o registrados para mostrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            uiState.historialContactos.forEach { item ->
                TarjetaHistorialContacto(item = item)
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun PantallaLecturaRapidaPremium(
    viewModel: PremiumViewModel,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    val s = uiState.stats

    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }

    PantallaBase(scrollable = true, mostrarFondo = true, respetarNavegacionInferior = false) {
        Spacer(Modifier.height(0.dp))
        EncabezadoSecundarioPremium(
            titulo = "Lectura rápida",
            onVolver = onVolver
        )
        Spacer(Modifier.height(4.dp))
        TarjetaMiniMetricas(s = s)
        TarjetaOperativa(s = s)
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun EncabezadoSecundarioPremium(
    titulo: String,
    onVolver: () -> Unit,
    mostrarEstrella: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0D5662),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0D5662), Color(0xFF11807B))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            if (mostrarEstrella) {
                EstrellaPremiumAnimada(
                    tamano = 24.dp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Spacer(Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun ResumenPremium(s: PremiumStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TarjetaResumen(
            modifier = Modifier.weight(1f),
            titulo = "Servicios",
            valor = "${s.serviciosActivos}/3",
            detalle = "${s.serviciosTotales}/5 publicados",
            icono = Icons.Filled.Inventory2,
            color = Color(0xFF0E8894)
        )
        TarjetaResumen(
            modifier = Modifier.weight(1f),
            titulo = "Valoración",
            valor = if (s.valoracionesTotales == 0) "—" else String.format("%.1f", s.promedioValoracion),
            detalle = "${s.valoracionesTotales} reseñas",
            icono = Icons.Filled.Star,
            color = Color(0xFFF5A623)
        )
    }
}

@Composable
private fun AccesosPremium(
    onAbrirHistorial: () -> Unit,
    onAbrirLecturaRapida: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BotonAccesoPremium(
            modifier = Modifier.weight(1f),
            titulo = "Historial",
            subtitulo = "Mis contactos",
            icono = Icons.Filled.ChatBubbleOutline,
            color = Color(0xFF0E8894),
            onClick = onAbrirHistorial
        )
        BotonAccesoPremium(
            modifier = Modifier.weight(1f),
            titulo = "Lectura rápida",
            subtitulo = "Estado operativo",
            icono = Icons.Filled.AutoGraph,
            color = Color(0xFF0D5B66),
            onClick = onAbrirLecturaRapida
        )
    }
}

@Composable
private fun BotonAccesoPremium(
    modifier: Modifier = Modifier,
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    TarjetaBase(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = color.copy(alpha = 0.06f),
                shape = RoundedCornerShape(18.dp)
            ),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.14f)
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color)
                }
            }
            Column {
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TarjetaResumen(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    detalle: String,
    icono: ImageVector,
    color: Color
) {
    TarjetaBase(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.14f)
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color)
                }
            }
            Text(titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DashboardSemanal(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    serie: List<PremiumSerieDia>,
    pie: String
) {
    TarjetaBase(
        modifier = Modifier.padding(bottom = 4.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.14f)) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color)
                }
            }
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        SerieBarras(serie = serie, color = color)
        Spacer(Modifier.height(6.dp))
        Text(
            text = pie,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardSemanalDesplegable(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    color: Color,
    serie: List<PremiumSerieDia>,
    pie: String
) {
    var expandido by rememberSaveable { mutableStateOf(false) }

    TarjetaBase(
        modifier = Modifier.padding(bottom = 4.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandido = !expandido },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.14f)) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pie,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (expandido) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = color
            )
        }
        AnimatedVisibility(
            visible = expandido,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(6.dp))
                SerieBarras(serie = serie, color = color)
            }
        }
    }
}

@Composable
private fun SerieBarras(
    serie: List<PremiumSerieDia>,
    color: Color
) {
    val maximo = max(1, serie.maxOfOrNull { it.cantidad } ?: 1)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        serie.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (item.destacado) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = item.cantidad.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.destacado) color else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.cantidad / maximo.toFloat())
                            .height(10.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        color.copy(alpha = 0.75f),
                                        if (item.destacado) color else color.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaMiniMetricas(s: PremiumStats) {
    TarjetaBase(
        modifier = Modifier.padding(bottom = 4.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Lectura rápida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF114E61)
            )
            FilaMetrica("Contactos últimos 7 días", s.contactosUltimos7Dias.toString())
            FilaMetrica("Conversión a cita", "${s.tasaConversionCita}%")
            FilaMetrica("Ingreso total cerrado", "$${s.ingresoTotalCerrado}")
            FilaMetrica("Ticket promedio", if (s.ticketPromedio == 0) "—" else "$${s.ticketPromedio}")
        }
    }
}

@Composable
private fun TarjetaOperativa(s: PremiumStats) {
    TarjetaBase(
        modifier = Modifier.padding(bottom = 4.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Estado operativo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF114E61)
            )
            FilaMetricaConIcono(Icons.Filled.ChatBubbleOutline, "Chats activos", s.chatsActivos.toString(), Color(0xFF1F8BFF))
            FilaMetricaConIcono(Icons.Filled.CalendarMonth, "Citas finalizadas", s.citasFinalizadas.toString(), Color(0xFF8E24AA))
            FilaMetricaConIcono(Icons.Filled.AutoGraph, "Mensajes no leídos", s.mensajesNoLeidos.toString(), Color(0xFF0E8894))
        }
    }
}

@Composable
private fun TarjetaHistorialContacto(item: PremiumHistorialContacto) {
    TarjetaBase(
        modifier = Modifier.padding(bottom = 6.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = item.nombreContacto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = item.tituloServicio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilaMetrica("Terminó", item.fechaTermino)
            FilaMetrica("Resultado", item.resultado)
            FilaMetrica("Valoración", item.estrellas?.let { "$it estrellas" } ?: "—")
        }
    }
}

@Composable
private fun FilaMetrica(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun FilaMetricaConIcono(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.14f)) {
                Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Skeleton de carga del menú Premium ───────────────────────────────────────

@Composable
private fun rememberShimmerBrushPremium(): Brush {
    val transicion = rememberInfiniteTransition(label = "shimmerPremium")
    val x by transicion.animateFloat(
        initialValue = -350f,
        targetValue = 1100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_300, easing = LinearEasing)
        ),
        label = "shimmerX"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val brillo = MaterialTheme.colorScheme.surface
    return Brush.linearGradient(
        colors = listOf(base, brillo, base),
        start = Offset(x, 0f),
        end = Offset(x + 350f, 350f)
    )
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

@Composable
private fun SkeletonMenuPremium() {
    val brush = rememberShimmerBrushPremium()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Dos tarjetas de resumen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CajaSkeleton(Modifier.weight(1f), alto = 124.dp, brush = brush)
            CajaSkeleton(Modifier.weight(1f), alto = 124.dp, brush = brush)
        }
        // Dos accesos rápidos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CajaSkeleton(Modifier.weight(1f), alto = 80.dp, brush = brush)
            CajaSkeleton(Modifier.weight(1f), alto = 80.dp, brush = brush)
        }
        // Dos dashboards
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 92.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 92.dp, brush = brush)
    }
}

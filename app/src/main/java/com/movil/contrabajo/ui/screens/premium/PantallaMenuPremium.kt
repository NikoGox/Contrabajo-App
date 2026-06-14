package com.movil.contrabajo.ui.screens.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.movil.contrabajo.ui.theme.LocalColoresContrabajo
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
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
    onAbrirEstadisticas: () -> Unit,
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

        TopbarPremiumAnimada(onVolver = onVolver)

        Spacer(Modifier.height(4.dp))

        BannerPremium()

        Spacer(Modifier.height(4.dp))

        if (uiState.cargandoStats) {
            SkeletonMenuPremium()
        } else {
            AccesosPremium(
                onAbrirHistorial = onAbrirHistorial,
                onAbrirEstadisticas = onAbrirEstadisticas
            )
            Spacer(Modifier.height(8.dp))
            FuncionesProximamente()
        }

        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Topbar Premium con gradiente animado celeste→azul oscuro y bordes redondeados.
 */
@Composable
private fun TopbarPremiumAnimada(onVolver: () -> Unit) {
    val colores = LocalColoresContrabajo.current
    val brushGradiente = Brush.horizontalGradient(
        colors = listOf(
            colores.premiumInicio,
            colores.premiumMedio,
            colores.premiumFin
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(brush = brushGradiente, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 4.dp),
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
private fun BannerPremium() {
    val colores = LocalColoresContrabajo.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colores.premiumBrillo
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EstrellaPremiumAnimada(tamano = 42.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Premium",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colores.premiumInicio
                )
                Text(
                    text = "Funcionalidades extendidas para el trabajador",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.premiumInicio
                )
            }
        }
    }
}

/**
 * Funciones futuras del menú Premium. Se muestran en gris/opaco para comunicar
 * que no están disponibles aún, sin ser botones pulsables.
 */
@Composable
private fun FuncionesProximamente() {
    val colores = LocalColoresContrabajo.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Próximamente",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        FuncionFutura(
            titulo = "Funcionalidades IA",
            subtitulo = "Funciones especializadas potenciadas con Inteligencia Artificial",
            icono = Icons.Filled.SmartToy
        )
        FuncionFutura(
            titulo = "Campañas de publicidad",
            subtitulo = "Impulsa tus servicios para llegar a más clientes",
            icono = Icons.Filled.Campaign
        )
        FuncionFutura(
            titulo = "Puntos y canjes",
            subtitulo = "Acumula puntos por tu actividad y canjéalos",
            icono = Icons.Filled.Redeem
        )
    }
}

@Composable
private fun FuncionFutura(
    titulo: String,
    subtitulo: String,
    icono: ImageVector
) {
    val greyed = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val greyedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val greyedIcon = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = greyedBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = greyedIcon.copy(alpha = 0.15f)) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = greyedIcon, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = greyed
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = greyed.copy(alpha = 0.8f)
                )
            }
        }
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
        if (uiState.cargandoHistorial) {
            SkeletonHistorialContactos()
        } else if (uiState.historialContactos.isEmpty()) {
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
fun PantallaEstadisticasPremium(
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
            titulo = "Estadísticas",
            onVolver = onVolver
        )
        Spacer(Modifier.height(4.dp))
        if (uiState.cargandoStats) {
            SkeletonMenuPremium()
        } else {
            CarruselGraficosPremium(s = s)
            TarjetaMiniMetricas(s = s)
            TarjetaOperativa(s = s)
        }
        Spacer(Modifier.height(10.dp))
    }
}

private data class GraficoPremium(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val color: Color,
    val serie: List<PremiumSerieDia>,
    val pie: String
)

/**
 * Visor de gráficos con navegación por flechas (◀ ▶). Reúne los gráficos de la
 * actividad del trabajador: contactos y cierres por día, embudo de conversión,
 * resultados de citas y distribución de valoraciones.
 */
@Composable
private fun CarruselGraficosPremium(s: PremiumStats) {
    val info = LocalColoresContrabajo.current.info
    val advertencia = LocalColoresContrabajo.current.advertencia
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val graficos = remember(s) {
        listOf(
            GraficoPremium(
                titulo = "Contactos por día",
                subtitulo = "Conversaciones iniciadas por día de la semana",
                icono = Icons.Filled.ChatBubbleOutline,
                color = info,
                serie = s.contactosPorDia,
                pie = "Mejor día: ${s.mejorDiaContactos}"
            ),
            GraficoPremium(
                titulo = "Cierres por día",
                subtitulo = "Trabajos finalizados por día de la semana",
                icono = Icons.Filled.AttachMoney,
                color = primary,
                serie = s.ingresosPorDia,
                pie = "Mejor día: ${s.mejorDiaIngresos}"
            ),
            GraficoPremium(
                titulo = "Embudo de conversión",
                subtitulo = "De contacto a trabajo cerrado",
                icono = Icons.Filled.FilterAlt,
                color = secondary,
                serie = listOf(
                    PremiumSerieDia("Chats", s.chatsTotales),
                    PremiumSerieDia("Citas", s.citasTotales),
                    PremiumSerieDia("Finalizadas", s.citasFinalizadas, destacado = true)
                ),
                pie = "Conversión chat → cita: ${s.tasaConversionCita}%"
            ),
            GraficoPremium(
                titulo = "Resultados de citas",
                subtitulo = "Cómo terminan tus citas",
                icono = Icons.Filled.CalendarMonth,
                color = tertiary,
                serie = listOf(
                    PremiumSerieDia("Finalizadas", s.citasFinalizadas, destacado = true),
                    PremiumSerieDia("En proceso", s.citasEnProceso),
                    PremiumSerieDia("Canceladas", s.citasCanceladas),
                    PremiumSerieDia("Rechazadas", s.citasRechazadas)
                ),
                pie = "Total de citas: ${s.citasTotales}"
            ),
            GraficoPremium(
                titulo = "Valoraciones",
                subtitulo = "Distribución de tus estrellas",
                icono = Icons.Filled.Star,
                color = advertencia,
                serie = s.distribucionValoraciones
                    .mapIndexed { i, c -> PremiumSerieDia("${i + 1}★", c, destacado = i == 4) }
                    .reversed(),
                pie = if (s.valoracionesTotales == 0) "Aún sin valoraciones"
                else "Promedio: ${"%.1f".format(s.promedioValoracion)} · ${s.valoracionesTotales} reseñas"
            )
        )
    }

    var indice by rememberSaveable { mutableStateOf(0) }
    val pos = indice.coerceIn(0, graficos.lastIndex)
    val actual = graficos[pos]

    val entradaAnimada by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "chartEntrada"
    )

    TarjetaBase(modifier = Modifier.padding(bottom = 4.dp), contentPadding = PaddingValues(18.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = actual.color.copy(alpha = 0.14f)) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(actual.icono, contentDescription = null, tint = actual.color)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(actual.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(actual.subtitulo, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.graphicsLayer { alpha = entradaAnimada; scaleX = entradaAnimada; scaleY = entradaAnimada }) {
            when (pos) {
                4 -> GraficoDonut(s = s, color = actual.color)
                2 -> GraficoEmbudo(s = s, color = actual.color)
                else -> SerieBarras(serie = actual.serie, color = actual.color)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(actual.pie, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                indice = (pos - 1 + graficos.size) % graficos.size
            }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Gráfico anterior", tint = primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                graficos.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == pos) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (i == pos) actual.color else onSurfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }
            IconButton(onClick = {
                indice = (pos + 1) % graficos.size
            }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Gráfico siguiente", tint = primary)
            }
        }
    }
}

@Composable
private fun EncabezadoSecundarioPremium(
    titulo: String,
    onVolver: () -> Unit,
    mostrarEstrella: Boolean = true
) {
    val colores = LocalColoresContrabajo.current
    val brushGradiente = Brush.horizontalGradient(
        colors = listOf(
            colores.premiumInicio,
            colores.premiumMedio,
            colores.premiumFin
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(brush = brushGradiente, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 4.dp),
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            if (mostrarEstrella) {
                EstrellaPremiumAnimada(
                    tamano = 22.dp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            } else {
                Spacer(Modifier.size(46.dp))
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
            color = MaterialTheme.colorScheme.primary
        )
        TarjetaResumen(
            modifier = Modifier.weight(1f),
            titulo = "Valoración",
            valor = if (s.valoracionesTotales == 0) "—" else String.format("%.1f", s.promedioValoracion),
            detalle = "${s.valoracionesTotales} reseñas",
            icono = Icons.Filled.Star,
            color = LocalColoresContrabajo.current.advertencia
        )
    }
}

@Composable
private fun AccesosPremium(
    onAbrirHistorial: () -> Unit,
    onAbrirEstadisticas: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BotonAccesoPremium(
            titulo = "Historial de contactos",
            subtitulo = "Revisa tus interacciones anteriores",
            icono = Icons.Filled.ChatBubbleOutline,
            onClick = onAbrirHistorial
        )
        BotonAccesoPremium(
            titulo = "Estadísticas",
            subtitulo = "Gráficos, métricas y rendimiento",
            icono = Icons.Filled.QueryStats,
            onClick = onAbrirEstadisticas
        )
    }
}

@Composable
private fun BotonAccesoPremium(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    val colores = LocalColoresContrabajo.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colores.premiumInicio.copy(alpha = 0.06f),
                            colores.premiumFin.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colores.premiumInicio.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = colores.premiumInicio, modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colores.premiumInicio
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colores.premiumMedio,
                modifier = Modifier.size(22.dp)
            )
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
private fun GraficoDonut(s: PremiumStats, color: Color) {
    val total = s.distribucionValoraciones.sum().coerceAtLeast(1)
    val colores = listOf(
        Color(0xFFE53935),
        Color(0xFFFF9800),
        Color(0xFFFFC107),
        Color(0xFF8BC34A),
        color
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val grosor = 24.dp.toPx()
                val radio = (size.minDimension - grosor) / 2f
                var anguloInicio = -90f
                s.distribucionValoraciones.forEachIndexed { i, valor ->
                    val barrido = (valor.toFloat() / total) * 360f
                    if (valor > 0) {
                        drawArc(
                            color = colores[i],
                            startAngle = anguloInicio,
                            sweepAngle = barrido - 2f,
                            useCenter = false,
                            topLeft = Offset(grosor / 2f, grosor / 2f),
                            size = Size(radio * 2, radio * 2),
                            style = Stroke(width = grosor, cap = StrokeCap.Round)
                        )
                    }
                    anguloInicio += barrido
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${s.valoracionesTotales}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Text(
                    text = "reseñas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            s.distribucionValoraciones.forEachIndexed { i, valor ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colores[i]))
                    Text(
                        "${i + 1}★",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$valor",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GraficoEmbudo(s: PremiumStats, color: Color) {
    val pasos = listOf(
        Triple("Chats", s.chatsTotales, 1f),
        Triple("Citas", s.citasTotales, 0.7f),
        Triple("Finalizadas", s.citasFinalizadas, 0.4f)
    )
    val maximo = max(1, pasos.maxOf { it.second })

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        pasos.forEachIndexed { i, (etiqueta, cantidad, factorAncho) ->
            val proporcion = (cantidad.toFloat() / maximo).coerceIn(0.15f, 1f)
            val anchoAnimado by animateFloatAsState(
                targetValue = proporcion * factorAncho,
                animationSpec = tween(600, delayMillis = i * 200),
                label = "funnel$i"
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth((0.4f + 0.6f * anchoAnimado)),
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.15f + (0.2f * (i + 1) / pasos.size))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = etiqueta,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = color,
                            maxLines = 1
                        )
                        Text(
                            text = cantidad.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
                if (i < pasos.lastIndex) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = color.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
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
                color = MaterialTheme.colorScheme.primary
            )
            FilaMetrica("Contactos últimos 7 días", s.contactosUltimos7Dias.toString())
            FilaMetrica("Conversión a cita", "${s.tasaConversionCita}%")
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
                color = MaterialTheme.colorScheme.primary
            )
            FilaMetricaConIcono(Icons.Filled.ChatBubbleOutline, "Chats activos", s.chatsActivos.toString(), LocalColoresContrabajo.current.info)
            FilaMetricaConIcono(Icons.Filled.CalendarMonth, "Citas finalizadas", s.citasFinalizadas.toString(), MaterialTheme.colorScheme.tertiary)
            FilaMetricaConIcono(Icons.Filled.AutoGraph, "Mensajes no leídos", s.mensajesNoLeidos.toString(), MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TarjetaHistorialContacto(item: PremiumHistorialContacto) {
    val colores = LocalColoresContrabajo.current
    TarjetaBase(
        modifier = Modifier.padding(bottom = 6.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.nombreContacto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colores.premiumInicio.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.resultado,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colores.premiumInicio,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = item.tituloServicio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.estrellas != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i < item.estrellas!!) colores.premiumEstrella else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (!item.comentarioValoracion.isNullOrBlank()) {
                Text(
                    text = "\"${item.comentarioValoracion}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
            FilaMetrica("Fecha", item.fechaTermino)
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
private fun SkeletonHistorialContactos() {
    val brush = rememberShimmerBrushPremium()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) {
            CajaSkeleton(Modifier.fillMaxWidth(), alto = 96.dp, brush = brush)
        }
    }
}

@Composable
private fun SkeletonMenuPremium() {
    val brush = rememberShimmerBrushPremium()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 96.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 96.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 80.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 80.dp, brush = brush)
        CajaSkeleton(Modifier.fillMaxWidth(), alto = 80.dp, brush = brush)
    }
}

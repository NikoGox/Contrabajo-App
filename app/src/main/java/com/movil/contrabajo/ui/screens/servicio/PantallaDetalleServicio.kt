package com.movil.contrabajo.ui.screens.servicio

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.ui.viewmodel.DetalleServicioViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PantallaDetalleServicio(
    idOfertaServicio: Long,
    viewModel: DetalleServicioViewModel,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    var desplazamientoX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(idOfertaServicio) {
        viewModel.cargarOferta(idOfertaServicio)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            BarraSuperiorDetalle(onVolver = onVolver)

            val oferta = uiState.ofertaActual
            if (oferta == null) {
                Text(
                    text = "No se pudo cargar la oferta.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(desplazamientoX.roundToInt(), 0) }
                        .pointerInput(uiState.indiceActual, uiState.ofertas.size) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    desplazamientoX += dragAmount
                                },
                                onDragEnd = {
                                    val umbral = 130f
                                    when {
                                        desplazamientoX > umbral -> viewModel.retrocederTarjeta()
                                        desplazamientoX < -umbral -> viewModel.avanzarTarjeta()
                                    }
                                    desplazamientoX = 0f
                                },
                                onDragCancel = { desplazamientoX = 0f }
                            )
                        }
                ) {
                    TarjetaDetalleOferta(oferta = oferta)
                }
            }
        }

        ContactoFlotante(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 94.dp)
        )
    }
}

@Composable
private fun BarraSuperiorDetalle(onVolver: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun TarjetaDetalleOferta(oferta: OfertaServicio) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImagenDetalleServicio(
                referencia = oferta.fotoUrlReferencia,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Text(
                text = oferta.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (oferta.trabajadorVerificado) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Trabajador verificado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = oferta.precioTexto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${calcularDistanciaKm(oferta)} km - ${oferta.ubicacionReferencia.ifBlank { "Region Metropolitana" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Descripcion:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = oferta.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (oferta.puntuacionPromedio <= 0.0) {
                    Text(
                        text = "Sin valoraciones todavia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FilaValoracionDetalle(
                        valor = oferta.puntuacionPromedio
                    )
                }
            }

            Text(
                text = "Ubicacion:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MapaRangoOpenStreetMap(
                latitud = oferta.latitudReferencia ?: -33.4489,
                longitud = oferta.longitudReferencia ?: -70.6693,
                rangoKm = oferta.rangoDisponibilidadKm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Text(
                text = "Rango de disponibilidad: ${oferta.rangoDisponibilidadKm} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = oferta.nombreTrabajador,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (oferta.trabajadorVerificado) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = oferta.nombreCategoria.ifBlank { "Sin categoria" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContactoFlotante(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubble,
                contentDescription = "Nuevo mensaje",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun FilaValoracionDetalle(valor: Double) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = String.format("%.1f", valor),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        repeat(5) { index ->
            val fraccion = (valor - index).coerceIn(0.0, 1.0).toFloat()
            Box(modifier = Modifier.size(18.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFB0B7BF),
                    modifier = Modifier.matchParentSize()
                )
                if (fraccion > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraccion)
                            .clip(RectangleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC93C),
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagenDetalleServicio(
    referencia: String,
    modifier: Modifier = Modifier
) {
    if (referencia.startsWith("content://") || referencia.startsWith("file://") || referencia.startsWith("android.resource://")) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    clipToOutline = true
                }
            },
            update = { imageView ->
                runCatching { imageView.setImageURI(android.net.Uri.parse(referencia)) }
                if (imageView.drawable == null) {
                    imageView.setImageDrawable(null)
                }
            }
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubble,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
private fun MapaRangoOpenStreetMap(
    latitud: Double,
    longitud: Double,
    rangoKm: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val zoom = calcularZoomPorRango(rangoKm).toDouble()
    val radioMetros = rangoKm.coerceIn(0, 100) * 1000.0

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitud, longitud))
            setOnTouchListener { _, _ -> true }
        }
    }

    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            val centro = GeoPoint(latitud, longitud)
            map.controller.setZoom(zoom)
            map.controller.setCenter(centro)
            map.overlays.clear()

            val marcador = Marker(map).apply {
                position = centro
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Ubicacion del servicio"
            }

            val circulo = Polygon(map).apply {
                points = Polygon.pointsAsCircle(centro, radioMetros)
                fillColor = Color(0x3319A1A8).toArgb()
                strokeColor = Color(0xFF0E8C94).toArgb()
                strokeWidth = 2f
            }

            map.overlays.add(circulo)
            map.overlays.add(marcador)
            map.invalidate()
        }
    )
}

private fun calcularZoomPorRango(rangoKm: Int): Int = when {
    rangoKm <= 1 -> 15
    rangoKm <= 3 -> 14
    rangoKm <= 7 -> 13
    rangoKm <= 15 -> 12
    rangoKm <= 30 -> 11
    rangoKm <= 55 -> 10
    else -> 9
}

private fun calcularDistanciaKm(oferta: OfertaServicio): Int {
    val lat = oferta.latitudReferencia ?: return 0
    val lon = oferta.longitudReferencia ?: return 0
    val referenciaLat = -33.4489
    val referenciaLon = -70.6693

    val radioTierraKm = 6371.0
    val dLat = (lat - referenciaLat) * PI / 180.0
    val dLon = (lon - referenciaLon) * PI / 180.0
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(referenciaLat * PI / 180.0) * cos(lat * PI / 180.0) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return abs((radioTierraKm * c).roundToInt())
}

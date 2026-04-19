package com.movil.contrabajo.ui.screens.servicio

import android.content.Context
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.imageLoader
import com.movil.contrabajo.R
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.ui.viewmodel.DetalleServicioViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PantallaDetalleServicio(
    idOfertaServicio: Long,
    viewModel: DetalleServicioViewModel,
    onEditarServicio: () -> Unit,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var desplazamientoX by remember { mutableFloatStateOf(0f) }
    var animandoSwipe by remember { mutableStateOf(false) }
    var mostrarCta by remember { mutableStateOf(true) }
    var anchoContenedorPx by remember { mutableIntStateOf(1) }
    val desplazamientoAnimado = desplazamientoX
    val scope = rememberCoroutineScope()
    var scrollActivo by remember { mutableStateOf<androidx.compose.foundation.ScrollState?>(null) }

    val volverConScroll: () -> Unit = {
        scope.launch {
            val scroll = scrollActivo
            if (scroll != null && scroll.value > 0) {
                mostrarCta = true
                scroll.animateScrollTo(0)
            } else {
                onVolver()
            }
        }
    }

    BackHandler(onBack = volverConScroll)

    LaunchedEffect(idOfertaServicio) {
        viewModel.cargarOferta(idOfertaServicio)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.recargarOfertaActual()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.indiceActual, uiState.ofertas) {
        val indicesPrefetch = listOf(
            uiState.indiceActual - 1,
            uiState.indiceActual + 1
        )
        indicesPrefetch
            .mapNotNull { uiState.ofertas.getOrNull(it)?.fotoUrlReferencia }
            .filter { it.startsWith("http://") || it.startsWith("https://") }
            .distinct()
            .forEach { url ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(url)
                        .memoryCacheKey(url)
                        .diskCacheKey(url)
                        .build()
                )
            }
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
            BarraSuperiorDetalle(onVolver = volverConScroll)

            val oferta = uiState.ofertaActual
            if (oferta == null) {
                Text(
                    text = "No se pudo cargar la oferta.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val indiceActual = uiState.indiceActual
                val indicePrevio = (indiceActual - 1).takeIf { it >= 0 }
                val indiceSiguiente = (indiceActual + 1).takeIf { it < uiState.ofertas.size }
                val progresoSwipe = (abs(desplazamientoAnimado) / 420f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { anchoContenedorPx = it.width.coerceAtLeast(1) }
                        .pointerInput(uiState.indiceActual, uiState.ofertas.size, animandoSwipe) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    if (animandoSwipe) return@detectHorizontalDragGestures
                                    change.consume()
                                    val limite = anchoContenedorPx * 0.95f
                                    desplazamientoX = (desplazamientoX + dragAmount).coerceIn(-limite, limite)
                                },
                                onDragEnd = {
                                    if (animandoSwipe) return@detectHorizontalDragGestures
                                    val umbral = (anchoContenedorPx * 0.22f).coerceAtLeast(120f)
                                    val destino = when {
                                        desplazamientoX > umbral && indicePrevio != null -> anchoContenedorPx.toFloat()
                                        desplazamientoX < -umbral && indiceSiguiente != null -> -anchoContenedorPx.toFloat()
                                        else -> 0f
                                    }
                                    when {
                                        destino > 0f -> {
                                            scope.launch {
                                                animandoSwipe = true
                                                val anim = Animatable(desplazamientoX)
                                                anim.animateTo(
                                                    targetValue = destino,
                                                    animationSpec = tween(
                                                        durationMillis = 190,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                ) {
                                                    desplazamientoX = value
                                                }
                                                viewModel.retrocederTarjeta()
                                                desplazamientoX = 0f
                                                animandoSwipe = false
                                            }
                                        }
                                        destino < 0f -> {
                                            scope.launch {
                                                animandoSwipe = true
                                                val anim = Animatable(desplazamientoX)
                                                anim.animateTo(
                                                    targetValue = destino,
                                                    animationSpec = tween(
                                                        durationMillis = 190,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                ) {
                                                    desplazamientoX = value
                                                }
                                                viewModel.avanzarTarjeta()
                                                desplazamientoX = 0f
                                                animandoSwipe = false
                                            }
                                        }
                                        else -> {
                                            scope.launch {
                                                animandoSwipe = true
                                                val anim = Animatable(desplazamientoX)
                                                anim.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.8f,
                                                        stiffness = 420f
                                                    )
                                                ) {
                                                    desplazamientoX = value
                                                }
                                                desplazamientoX = 0f
                                                animandoSwipe = false
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    if (animandoSwipe) return@detectHorizontalDragGestures
                                    scope.launch {
                                        val anim = Animatable(desplazamientoX)
                                        anim.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.8f,
                                                stiffness = 420f
                                            )
                                        ) {
                                            desplazamientoX = value
                                        }
                                        desplazamientoX = 0f
                                    }
                                }
                            )
                        }
                ) {
                    if (desplazamientoAnimado > 0f && indicePrevio != null) {
                        key(uiState.ofertas[indicePrevio].idOfertaServicio) {
                            TarjetaDetallePrevisualizacion(
                                oferta = uiState.ofertas[indicePrevio],
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            x = (-110f * (1f - progresoSwipe)).dp.roundToPx(),
                                            y = (-24f * (1f - progresoSwipe)).dp.roundToPx()
                                        )
                                    }
                                    .graphicsLayer {
                                        scaleX = 0.95f + (0.05f * progresoSwipe)
                                        scaleY = 0.95f + (0.05f * progresoSwipe)
                                        alpha = 0.86f + (0.14f * progresoSwipe)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                        }
                    }
                    if (desplazamientoAnimado < 0f && indiceSiguiente != null) {
                        key(uiState.ofertas[indiceSiguiente].idOfertaServicio) {
                            TarjetaDetallePrevisualizacion(
                                oferta = uiState.ofertas[indiceSiguiente],
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            x = (110f * (1f - progresoSwipe)).dp.roundToPx(),
                                            y = (-12f * (1f - progresoSwipe)).dp.roundToPx()
                                        )
                                    }
                                    .graphicsLayer {
                                        scaleX = 0.95f + (0.05f * progresoSwipe)
                                        scaleY = 0.95f + (0.05f * progresoSwipe)
                                        alpha = 0.86f + (0.14f * progresoSwipe)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(
                                    x = desplazamientoAnimado.roundToInt(),
                                    y = 0
                                )
                            }
                    ) {
                        key(oferta.idOfertaServicio) {
                            TarjetaDetalleOferta(
                                oferta = oferta,
                                bloquearScrollVertical = abs(desplazamientoAnimado) > 6f,
                                onScrollEstado = { scrollActivo = it },
                                onDireccionScroll = { mostrar ->
                                    if (abs(desplazamientoAnimado) <= 6f) {
                                        mostrarCta = mostrar
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        val ofertaCta = uiState.ofertaActual ?: uiState.ofertas.getOrNull(uiState.indiceActual)
        if (ofertaCta != null) {
            val esPublicacionPropia = ofertaCta.idTrabajador == uiState.idUsuarioActual
            AnimatedVisibility(
                visible = mostrarCta,
                enter = fadeIn(animationSpec = tween(180)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(210, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(animationSpec = tween(130)) + slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .zIndex(20f)
                    .padding(end = 24.dp, bottom = 58.dp)
            ) {
                ContactoFlotante(
                    esPropia = esPublicacionPropia,
                    onClick = { if (esPublicacionPropia) onEditarServicio() }
                )
            }
        }
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
private fun TarjetaDetalleOferta(
    oferta: OfertaServicio,
    bloquearScrollVertical: Boolean = false,
    onScrollEstado: (androidx.compose.foundation.ScrollState) -> Unit = {},
    onDireccionScroll: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var posicionScroll by rememberSaveable(oferta.idOfertaServicio) { mutableIntStateOf(0) }
    val scrollState = rememberScrollState(posicionScroll)
    var ultimoScroll by rememberSaveable(oferta.idOfertaServicio) { mutableIntStateOf(posicionScroll) }
    LaunchedEffect(scrollState.value) {
        posicionScroll = scrollState.value
        val delta = scrollState.value - ultimoScroll
        when {
            scrollState.value <= 4 -> onDireccionScroll(true)
            delta > 10 -> onDireccionScroll(false)
            delta < -10 -> onDireccionScroll(true)
        }
        ultimoScroll = scrollState.value
    }
    LaunchedEffect(scrollState) {
        onScrollEstado(scrollState)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 78.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState, enabled = !bloquearScrollVertical)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                } else {
                    Text(
                        text = "Trabajador",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (oferta.puntuacionPromedio <= 0.0) {
                    Text(
                        text = "Sin valoraciones todavia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FilaValoracionDetalle(valor = oferta.puntuacionPromedio)
                }
            }

            Text(
                text = oferta.precioTexto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Publicado: ${formatearFechaPublicacion(oferta.fechaPublicacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            Text(
                text = "Ubicacion:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val coordenadaVisual = construirCoordenadaVisualPrivada(
                latitud = oferta.latitudReferencia ?: -33.4489,
                longitud = oferta.longitudReferencia ?: -70.6693,
                semilla = oferta.idOfertaServicio
            )
            MapaRangoOpenStreetMap(
                latitud = coordenadaVisual.first,
                longitud = coordenadaVisual.second,
                rangoM = oferta.rangoDisponibilidadM,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Text(
                text = "Rango de disponibilidad: ${EscalaRango.formatear(oferta.rangoDisponibilidadM)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ResumenTrabajadorDetalle(
                nombreTrabajador = oferta.nombreTrabajador,
                usernameTrabajador = oferta.usernameTrabajador,
                fotoPerfilTrabajador = oferta.fotoPerfilTrabajador,
                verificado = oferta.trabajadorVerificado
            )
        }
    }
}

@Composable
private fun TarjetaDetallePrevisualizacion(
    oferta: OfertaServicio,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 78.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = oferta.precioTexto,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = oferta.ubicacionReferencia.ifBlank { "Region Metropolitana" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
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
                text = oferta.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ContactoFlotante(
    esPropia: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .padding(14.dp)
                .background(Color.Transparent)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (esPropia) Icons.Filled.Edit else Icons.Filled.ChatBubble,
                contentDescription = if (esPropia) "Editar servicio" else "Nuevo mensaje",
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
    var errorRemoto by remember(referencia) { mutableStateOf(false) }
    if ((referencia.startsWith("http://") || referencia.startsWith("https://")) && !errorRemoto) {
        AsyncImage(
            model = referencia,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            onError = { errorRemoto = true }
        )
    } else if (referencia.startsWith("content://") || referencia.startsWith("file://") || referencia.startsWith("android.resource://")) {
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
    rangoM: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rangoNormalizadoM = EscalaRango.normalizar(rangoM)
    val zoom = calcularZoomPorRangoM(rangoNormalizadoM).toDouble()
    val radioMetros = rangoNormalizadoM.toDouble()

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
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Ubicacion del servicio"
                icon = ContextCompat.getDrawable(context, R.drawable.ic_pin_marcador_azul)
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

private fun calcularZoomPorRangoM(rangoM: Int): Int = when {
    rangoM <= 400 -> 15
    rangoM <= 900 -> 14
    rangoM <= 2_000 -> 13
    rangoM <= 5_000 -> 12
    rangoM <= 10_000 -> 11
    rangoM <= 20_000 -> 10
    rangoM <= 35_000 -> 9
    else -> 9
}

@Composable
private fun ResumenTrabajadorDetalle(
    nombreTrabajador: String,
    usernameTrabajador: String,
    fotoPerfilTrabajador: String,
    verificado: Boolean
) {
    val usernameVisible = usernameTrabajador.trim().ifBlank {
        nombreTrabajador
            .trim()
            .replace(" ", "")
            .lowercase()
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (fotoPerfilTrabajador.isBlank()) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubble,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = fotoPerfilTrabajador,
                        contentDescription = "Foto trabajador",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = nombreTrabajador,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (verificado) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "@$usernameVisible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun construirCoordenadaVisualPrivada(
    latitud: Double,
    longitud: Double,
    semilla: Long
): Pair<Double, Double> {
    val maxMetros = 500.0
    val random = kotlin.random.Random(semilla.toInt())
    val distancia = random.nextDouble(40.0, maxMetros)
    val angulo = random.nextDouble(0.0, Math.PI * 2)
    val deltaLat = (distancia * kotlin.math.cos(angulo)) / 111_320.0
    val deltaLon = (distancia * kotlin.math.sin(angulo)) / (111_320.0 * kotlin.math.cos(Math.toRadians(latitud)))
    return latitud + deltaLat to longitud + deltaLon
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

private fun formatearFechaPublicacion(fecha: String): String {
    if (fecha.length >= 16 && fecha[4] == '-' && fecha[7] == '-') {
        val dia = fecha.substring(8, 10)
        val mes = fecha.substring(5, 7)
        val anio = fecha.substring(0, 4)
        val hora = fecha.substring(11, 16)
        return "$dia/$mes/$anio $hora"
    }
    return fecha
}

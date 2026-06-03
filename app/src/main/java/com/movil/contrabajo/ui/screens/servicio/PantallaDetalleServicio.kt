package com.movil.contrabajo.ui.screens.servicio

import android.content.Context
import android.widget.Toast
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
import com.movil.contrabajo.ui.viewmodel.ReportesViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PantallaDetalleServicio(
    idOfertaServicio: Long,
    viewModel: DetalleServicioViewModel,
    reportesViewModel: ReportesViewModel,
    onEditarServicio: (Long) -> Unit,
    onContactarServicio: (Long, String, String) -> Unit,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val densidadPantalla = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val pagerState = rememberPagerState(
        initialPage = uiState.indiceActual.coerceAtLeast(0),
        pageCount = { uiState.ofertas.size.coerceAtLeast(1) }
    )
    var mostrarCta by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var scrollActivo by remember { mutableStateOf<androidx.compose.foundation.ScrollState?>(null) }
    var paginaAncla by rememberSaveable { mutableIntStateOf(uiState.indiceActual.coerceAtLeast(0)) }
    var indiceDetalleCompleto by rememberSaveable { mutableIntStateOf(uiState.indiceActual.coerceAtLeast(0)) }
    var saliendoPantalla by rememberSaveable { mutableStateOf(false) }
    var swipeEnfriamiento by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacionChat by rememberSaveable { mutableStateOf(false) }
    var ofertaPendienteChatId by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarModalReporte by rememberSaveable { mutableStateOf(false) }
    var menuTipoReporteAbierto by remember { mutableStateOf(false) }
    var menuOpcionesAbierto by rememberSaveable { mutableStateOf(false) }
    var idTipoReporteSeleccionado by rememberSaveable { mutableStateOf<Long?>(null) }
    var comentarioReporte by rememberSaveable { mutableStateOf("") }
    val paginasListas = remember { mutableStateMapOf<Int, Boolean>() }
    var direccionSwipeActiva by rememberSaveable { mutableIntStateOf(0) } // 1 avance, -1 retroceso
    val comportamientoFlingPager = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1)
    )

    val volverConScroll: () -> Unit = {
        scope.launch {
            val scroll = scrollActivo
            if (scroll != null && scroll.value > 0) {
                mostrarCta = true
                scroll.animateScrollTo(0)
            } else if (!saliendoPantalla) {
                mostrarCta = false
                saliendoPantalla = true
            } else {
                Unit
            }
        }
    }

    BackHandler(onBack = volverConScroll)

    LaunchedEffect(idOfertaServicio) {
        viewModel.cargarOferta(idOfertaServicio)
        if (reportesViewModel.uiState.tiposReporte.isEmpty()) {
            reportesViewModel.recargar()
        }
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

    LaunchedEffect(uiState.indiceActual, uiState.ofertas.size) {
        if (uiState.ofertas.isEmpty()) return@LaunchedEffect
        val indiceDestino = uiState.indiceActual.coerceIn(0, uiState.ofertas.lastIndex)
        if (pagerState.currentPage != indiceDestino) {
            pagerState.scrollToPage(indiceDestino)
        }
        if (!pagerState.isScrollInProgress) {
            paginaAncla = indiceDestino
            indiceDetalleCompleto = indiceDestino
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, uiState.ofertas.size) {
        if (uiState.ofertas.isEmpty()) return@LaunchedEffect
        if (!pagerState.isScrollInProgress && pagerState.currentPage in uiState.ofertas.indices) {
            paginaAncla = pagerState.currentPage
        }
        if (
            !pagerState.isScrollInProgress &&
            pagerState.currentPage in uiState.ofertas.indices &&
            pagerState.currentPage != uiState.indiceActual
        ) {
            viewModel.establecerIndiceActual(pagerState.currentPage)
        }
    }

    LaunchedEffect(
        pagerState.isScrollInProgress,
        pagerState.currentPage,
        pagerState.currentPageOffsetFraction,
        paginaAncla
    ) {
        if (!pagerState.isScrollInProgress) {
            direccionSwipeActiva = 0
            return@LaunchedEffect
        }
        if (direccionSwipeActiva != 0) return@LaunchedEffect
        val desplazamientoBruto = (pagerState.currentPage - paginaAncla) + pagerState.currentPageOffsetFraction
        if (abs(desplazamientoBruto) > 0.02f) {
            direccionSwipeActiva = if (desplazamientoBruto > 0f) 1 else -1
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            swipeEnfriamiento = true
            delay(70)
            indiceDetalleCompleto = pagerState.currentPage
            delay(50)
            swipeEnfriamiento = false
        }
    }

    LaunchedEffect(uiState.ofertas) {
        paginasListas.clear()
        uiState.ofertas.indices.forEach { indice -> paginasListas[indice] = false }
    }

    LaunchedEffect(pagerState.currentPage, uiState.ofertas) {
        if (uiState.ofertas.isEmpty()) return@LaunchedEffect
        val indicesPrefetch = listOf(
            pagerState.currentPage,
            pagerState.currentPage - 1,
            pagerState.currentPage + 1
        )
            .filter { it in uiState.ofertas.indices }
            .distinct()

        indicesPrefetch.forEach { indice ->
            launch {
                if (paginasListas[indice] == true) return@launch
                val referencia = uiState.ofertas[indice].fotoUrlReferencia
                if (referencia.startsWith("http://") || referencia.startsWith("https://")) {
                    runCatching {
                        context.imageLoader.execute(
                            ImageRequest.Builder(context)
                                .data(referencia)
                                .memoryCacheKey(referencia)
                                .diskCacheKey(referencia)
                                .build()
                        )
                    }
                    paginasListas[indice] = true
                } else {
                    paginasListas[indice] = true
                }
            }
        }
    }

    LaunchedEffect(saliendoPantalla) {
        if (saliendoPantalla) {
            delay(170)
            onVolver()
        }
    }

    val indiceActualVisible = pagerState.currentPage.coerceIn(0, (uiState.ofertas.size - 1).coerceAtLeast(0))
    val existeAnterior = indiceActualVisible > 0
    val existeSiguiente = indiceActualVisible < uiState.ofertas.lastIndex
    val actualLista = paginasListas[indiceActualVisible] == true
    val anteriorLista = !existeAnterior || paginasListas[indiceActualVisible - 1] == true
    val siguienteLista = !existeSiguiente || paginasListas[indiceActualVisible + 1] == true
    val puedeDeslizar = uiState.ofertas.size > 1 &&
        !saliendoPantalla &&
        !swipeEnfriamiento &&
        actualLista &&
        anteriorLista &&
        siguienteLista
    val ofertaBarra = uiState.ofertas.getOrNull(pagerState.currentPage) ?: uiState.ofertaActual
    val esPropiaBarra = ofertaBarra?.idTrabajador == uiState.idUsuarioActual
    val puedeReportarBarra = ofertaBarra != null && !ofertaBarra.eliminada && !esPropiaBarra

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = !saliendoPantalla,
            enter = fadeIn(animationSpec = tween(140)) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(180, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(
                targetOffsetY = { it / 12 },
                animationSpec = tween(160, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                BarraSuperiorDetalle(
                    onVolver = volverConScroll,
                    menuAbierto = menuOpcionesAbierto,
                    onCambiarMenu = { menuOpcionesAbierto = it },
                    mostrarAccionReportar = puedeReportarBarra,
                    onReportar = {
                        if (reportesViewModel.uiState.tiposReporte.isEmpty()) {
                            reportesViewModel.recargar()
                        }
                        mostrarModalReporte = true
                    }
                )

                val oferta = uiState.ofertaActual
                if (oferta == null) {
                    Text(
                        text = "No se pudo cargar la oferta.",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { clip = false },
                            beyondViewportPageCount = 1,
                            contentPadding = PaddingValues(horizontal = 0.dp),
                            pageSpacing = 0.dp,
                            flingBehavior = comportamientoFlingPager,
                            userScrollEnabled = puedeDeslizar
                        ) { page ->
                            val ofertaPagina = uiState.ofertas.getOrNull(page) ?: return@HorizontalPager
                            val progresoAnclaBruto = (pagerState.currentPage - paginaAncla) + pagerState.currentPageOffsetFraction
                            val progresoAncla = progresoAnclaBruto.coerceIn(-1f, 1f)
                            val intensidad = abs(progresoAncla).coerceIn(0f, 1f)
                            val avance = when {
                                direccionSwipeActiva > 0 -> intensidad
                                direccionSwipeActiva < 0 -> 0f
                                else -> progresoAncla.coerceAtLeast(0f)
                            }
                            val retroceso = when {
                                direccionSwipeActiva < 0 -> intensidad
                                direccionSwipeActiva > 0 -> 0f
                                else -> (-progresoAncla).coerceAtLeast(0f)
                            }
                            val esPaginaAncla = page == paginaAncla
                            val esPaginaSiguiente = page == paginaAncla + 1
                            val esPaginaAnterior = page == paginaAncla - 1
                            val esPaginaVecina = esPaginaAncla || esPaginaAnterior || esPaginaSiguiente

                            val traslacionY: Float
                            val escala: Float
                            val opacidad: Float
                            val zTarjeta: Float
                            val rotacionX: Float
                            val rotacionY: Float
                            val elevacionTarjeta: Float

                            when {
                                esPaginaAncla && avance > 0f -> {
                                    // Avance: la carta actual se despega y sube levemente antes de salir.
                                    traslacionY = lerpFloat(0f, -24f, avance)
                                    escala = lerpFloat(1f, 1.035f, avance)
                                    opacidad = lerpFloat(1f, 0.96f, avance)
                                    zTarjeta = 13f
                                    rotacionX = lerpFloat(0f, -2f, avance)
                                    rotacionY = lerpFloat(0f, -6f, avance)
                                    elevacionTarjeta = lerpFloat(12f, 6f, avance)
                                }

                                esPaginaSiguiente && avance > 0f -> {
                                    // La siguiente emerge desde profundidad para engancharse al centro.
                                    traslacionY = lerpFloat(52f, 0f, avance)
                                    escala = lerpFloat(0.90f, 1f, avance)
                                    opacidad = lerpFloat(0.74f, 1f, avance)
                                    zTarjeta = 11f
                                    rotacionX = lerpFloat(3f, 0f, avance)
                                    rotacionY = lerpFloat(8f, 0f, avance)
                                    elevacionTarjeta = lerpFloat(4f, 12f, avance)
                                }

                                esPaginaAncla && retroceso > 0f -> {
                                    // Retroceso: la carta actual baja/despeja para destapar la anterior.
                                    traslacionY = lerpFloat(0f, 16f, retroceso)
                                    escala = lerpFloat(1f, 0.965f, retroceso)
                                    opacidad = lerpFloat(1f, 0.95f, retroceso)
                                    zTarjeta = 10f
                                    rotacionX = lerpFloat(0f, 2f, retroceso)
                                    rotacionY = lerpFloat(0f, 4f, retroceso)
                                    elevacionTarjeta = lerpFloat(12f, 5f, retroceso)
                                }

                                esPaginaAnterior && retroceso > 0f -> {
                                    // La anterior entra desde arriba y queda superpuesta.
                                    traslacionY = lerpFloat(-56f, 0f, retroceso)
                                    escala = lerpFloat(1.03f, 1f, retroceso)
                                    opacidad = lerpFloat(0.82f, 1f, retroceso)
                                    zTarjeta = 15f
                                    rotacionX = lerpFloat(-8f, 0f, retroceso)
                                    rotacionY = lerpFloat(-5f, 0f, retroceso)
                                    elevacionTarjeta = lerpFloat(5f, 13f, retroceso)
                                }

                                esPaginaAncla -> {
                                    traslacionY = 0f
                                    escala = 1f
                                    opacidad = 1f
                                    zTarjeta = 12f
                                    rotacionX = 0f
                                    rotacionY = 0f
                                    elevacionTarjeta = 12f
                                }

                                esPaginaAnterior -> {
                                    traslacionY = -32f
                                    escala = 0.96f
                                    opacidad = 0.86f
                                    zTarjeta = 10f
                                    rotacionX = -3f
                                    rotacionY = -2f
                                    elevacionTarjeta = 6f
                                }

                                esPaginaSiguiente -> {
                                    traslacionY = 36f
                                    escala = 0.90f
                                    opacidad = 0.74f
                                    zTarjeta = 9f
                                    rotacionX = 4f
                                    rotacionY = 3f
                                    elevacionTarjeta = 4f
                                }

                                !esPaginaVecina -> {
                                    traslacionY = 64f
                                    escala = 0.84f
                                    opacidad = 0f
                                    zTarjeta = 1f
                                    rotacionX = 6f
                                    rotacionY = 0f
                                    elevacionTarjeta = 0f
                                }

                                else -> {
                                    traslacionY = 48f
                                    escala = 0.88f
                                    opacidad = 0.65f
                                    zTarjeta = 7f
                                    rotacionX = 5f
                                    rotacionY = 4f
                                    elevacionTarjeta = 2f
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { clip = false }
                                    .graphicsLayer {
                                        translationY = traslacionY
                                        scaleX = escala
                                        scaleY = escala
                                        alpha = opacidad
                                        rotationX = rotacionX
                                        rotationY = rotacionY
                                        cameraDistance = 26f * densidadPantalla.density
                                    }
                                    .zIndex(zTarjeta)
                            ) {
                                key(ofertaPagina.idOfertaServicio) {
                                    val esPaginaActual = ofertaPagina.idOfertaServicio == idOfertaServicio
                                    TarjetaDetalleOferta(
                                        oferta = ofertaPagina,
                                        latitudUsuario = uiState.latitudUsuario,
                                        longitudUsuario = uiState.longitudUsuario,
                                        idUsuarioActual = uiState.idUsuarioActual,
                                        modoLigero = !esPaginaVecina && page != indiceDetalleCompleto,
                                        elevacionTarjetaDp = elevacionTarjeta,
                                        bloquearScrollVertical = pagerState.isScrollInProgress || page != pagerState.currentPage,
                                        onScrollEstado = { scroll ->
                                            if (page == pagerState.currentPage) {
                                                scrollActivo = scroll
                                            }
                                        },
                                        onDireccionScroll = { mostrar ->
                                            if (page == pagerState.currentPage && !pagerState.isScrollInProgress) {
                                                mostrarCta = mostrar
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        if (!puedeDeslizar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(40f)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    ) { }
                            )
                        }
                    }
                }
            }
        }

        val ofertaCta = uiState.ofertas.getOrNull(pagerState.currentPage) ?: uiState.ofertaActual
        if (!saliendoPantalla && ofertaCta != null && !ofertaCta.eliminada) {
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
                    onClick = {
                        if (esPublicacionPropia) {
                            onEditarServicio(ofertaCta.idOfertaServicio)
                        } else {
                            ofertaPendienteChatId = ofertaCta.idOfertaServicio
                            mostrarConfirmacionChat = true
                        }
                    }
                )
            }

        }

        if (!saliendoPantalla && ofertaCta?.eliminada == true) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.88f)
            ) {
                Text(
                    text = "Fuera de servicio. Esta publicación no admite nuevos contactos.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (mostrarConfirmacionChat && ofertaPendienteChatId != null) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacionChat = false },
                title = { Text("Iniciar conversación") },
                text = { Text("¿Estás seguro de que deseas iniciar la conversación con este trabajador?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idOferta = ofertaPendienteChatId
                            mostrarConfirmacionChat = false
                            ofertaPendienteChatId = null
                            if (idOferta != null) {
                                val ofertaActual = uiState.ofertaActual
                                onContactarServicio(
                                    idOferta,
                                    ofertaActual?.titulo ?: "",
                                    ofertaActual?.usernameTrabajador ?: ""
                                )
                            }
                        }
                    ) {
                        Text("Sí, continuar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarConfirmacionChat = false
                            ofertaPendienteChatId = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (mostrarModalReporte && ofertaCta != null) {
            val tiposReporte = reportesViewModel.uiState.tiposReporte
            val tipoSeleccionado = tiposReporte.firstOrNull { it.idTipoReporte == idTipoReporteSeleccionado }
            AlertDialog(
                onDismissRequest = { mostrarModalReporte = false },
                title = { Text("Reportar servicio") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuTipoReporteAbierto = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = tipoSeleccionado?.nombre ?: "Selecciona un tipo de reporte",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            DropdownMenu(
                                expanded = menuTipoReporteAbierto,
                                onDismissRequest = { menuTipoReporteAbierto = false }
                            ) {
                                tiposReporte.forEach { tipo ->
                                    DropdownMenuItem(
                                        text = { Text(tipo.nombre) },
                                        onClick = {
                                            idTipoReporteSeleccionado = tipo.idTipoReporte
                                            menuTipoReporteAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = comentarioReporte,
                            onValueChange = { comentarioReporte = it },
                            label = { Text("Describe el incidente") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idTipo = idTipoReporteSeleccionado
                            if (idTipo == null || comentarioReporte.trim().isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Debes seleccionar un tipo y escribir la descripción.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TextButton
                            }
                            scope.launch {
                                val resultado = withContext(Dispatchers.IO) {
                                    reportesViewModel.crearReporteDesdeOferta(
                                        idOfertaServicio = ofertaCta.idOfertaServicio,
                                        idTipoReporte = idTipo,
                                        comentario = comentarioReporte
                                    )
                                }
                                resultado.onSuccess {
                                    Toast.makeText(context, "Reporte enviado correctamente.", Toast.LENGTH_SHORT).show()
                                    comentarioReporte = ""
                                    idTipoReporteSeleccionado = null
                                    mostrarModalReporte = false
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        it.message ?: "No se pudo enviar el reporte",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Text("Enviar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarModalReporte = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

    }
}

@Composable
private fun BarraSuperiorDetalle(
    onVolver: () -> Unit,
    menuAbierto: Boolean,
    onCambiarMenu: (Boolean) -> Unit,
    mostrarAccionReportar: Boolean,
    onReportar: () -> Unit
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
                .height(56.dp)
                .padding(horizontal = 10.dp),
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
            Box {
                IconButton(onClick = { onCambiarMenu(true) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { onCambiarMenu(false) }
                ) {
                    if (mostrarAccionReportar) {
                        DropdownMenuItem(
                            text = { Text("Reportar servicio") },
                            onClick = {
                                onCambiarMenu(false)
                                onReportar()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDetalleOferta(
    oferta: OfertaServicio,
    latitudUsuario: Double? = null,
    longitudUsuario: Double? = null,
    idUsuarioActual: Long? = null,
    modoLigero: Boolean = false,
    elevacionTarjetaDp: Float = 8f,
    bloquearScrollVertical: Boolean = false,
    onScrollEstado: (androidx.compose.foundation.ScrollState) -> Unit = {},
    onDireccionScroll: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shapeTarjeta = RoundedCornerShape(24.dp)
    val elevacionVisualDp = elevacionTarjetaDp.coerceAtLeast(8f)

    var posicionScroll by rememberSaveable(oferta.idOfertaServicio) { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    var ultimoScroll by rememberSaveable(oferta.idOfertaServicio) { mutableIntStateOf(posicionScroll) }
    LaunchedEffect(oferta.idOfertaServicio) {
        if (scrollState.value != posicionScroll) {
            scrollState.scrollTo(posicionScroll)
        }
        ultimoScroll = posicionScroll
    }
    DisposableEffect(oferta.idOfertaServicio, scrollState) {
        onDispose { posicionScroll = scrollState.value }
    }
    LaunchedEffect(scrollState, bloquearScrollVertical) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collect { valor ->
                val delta = valor - ultimoScroll
                when {
                    valor <= 4 -> onDireccionScroll(true)
                    bloquearScrollVertical -> Unit
                    delta > 3 -> onDireccionScroll(false)
                    delta < -2 -> onDireccionScroll(true)
                }
                ultimoScroll = valor
            }
    }
    LaunchedEffect(scrollState) {
        onScrollEstado(scrollState)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.93f)
            .padding(start = 2.dp, end = 2.dp, top = 4.dp, bottom = 2.dp),
        shape = shapeTarjeta,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevacionVisualDp.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState, enabled = !bloquearScrollVertical)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImagenDetalleServicio(
                referencia = oferta.fotoUrlReferencia,
                fueraDeServicio = oferta.eliminada,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(282.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Text(
                text = oferta.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (oferta.eliminada) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.74f)
                ) {
                    Text(
                        text = "Fuera de servicio",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trabajador",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (oferta.puntuacionPromedio <= 0.0) {
                    Text(
                        text = "Sin valoraciones todavía",
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
            if (!oferta.eliminada) {
                Text(
                    text = "${calcularDistanciaKm(oferta, latitudUsuario, longitudUsuario, idUsuarioActual)} km - ${oferta.ubicacionReferencia.ifBlank { "Región Metropolitana" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Descripción:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = oferta.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )

            if (oferta.eliminada) {
                Text(
                    text = "Ubicación no disponible para publicaciones eliminadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Ubicación:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val coordenadaVisual = construirCoordenadaVisualPrivada(
                    latitud = oferta.latitudReferencia ?: -33.4489,
                    longitud = oferta.longitudReferencia ?: -70.6693,
                    semilla = oferta.idOfertaServicio
                )
                if (modoLigero) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Vista previa del mapa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    MapaRangoOpenStreetMap(
                        latitud = coordenadaVisual.first,
                        longitud = coordenadaVisual.second,
                        rangoM = oferta.rangoDisponibilidadM,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                Text(
                    text = "Rango de disponibilidad: ${EscalaRango.formatear(oferta.rangoDisponibilidadM)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (modoLigero) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "@${oferta.usernameTrabajador.ifBlank { oferta.nombreTrabajador.replace(" ", "").lowercase() }}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ResumenTrabajadorDetalle(
                    nombreTrabajador = oferta.nombreTrabajador,
                    usernameTrabajador = oferta.usernameTrabajador,
                    fotoPerfilTrabajador = oferta.fotoPerfilTrabajador
                )
            }
        }
    }
}

@Composable
private fun TarjetaDetallePrevisualizacion(
    oferta: OfertaServicio,
    latitudUsuario: Double? = null,
    longitudUsuario: Double? = null,
    idUsuarioActual: Long? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImagenDetalleServicio(
                referencia = oferta.fotoUrlReferencia,
                fueraDeServicio = oferta.eliminada,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(282.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Text(
                text = oferta.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (oferta.eliminada) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.74f)
                ) {
                    Text(
                        text = "Fuera de servicio",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (oferta.eliminada) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.74f)
                ) {
                    Text(
                        text = "Fuera de servicio",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trabajador",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (oferta.puntuacionPromedio <= 0.0) {
                    Text(
                        text = "Sin valoraciones todavía",
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Publicado: ${formatearFechaPublicacion(oferta.fechaPublicacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!oferta.eliminada) {
                Text(
                    text = "${calcularDistanciaKm(oferta, latitudUsuario, longitudUsuario, idUsuarioActual)} km - ${oferta.ubicacionReferencia.ifBlank { "Región Metropolitana" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Descripción:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = oferta.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3
            )
            if (oferta.eliminada) {
                Text(
                    text = "Ubicación no disponible para publicaciones eliminadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Ubicación:",
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
                        .height(132.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Text(
                    text = "Rango de disponibilidad: ${EscalaRango.formatear(oferta.rangoDisponibilidadM)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ResumenTrabajadorDetalle(
                nombreTrabajador = oferta.nombreTrabajador,
                usernameTrabajador = oferta.usernameTrabajador,
                fotoPerfilTrabajador = oferta.fotoPerfilTrabajador
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
    fueraDeServicio: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (fueraDeServicio) {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                    )
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Fuera de servicio",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }
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
    val rangoVisualM = maxOf(rangoNormalizadoM, 1000)
    val zoom = calcularZoomPorRangoM(rangoVisualM).toDouble()
    val radioMetros = rangoVisualM.toDouble()

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
    var claveMapaAnterior by remember(mapView) { mutableStateOf<Triple<Double, Double, Int>?>(null) }

    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            val claveActual = Triple(latitud, longitud, rangoVisualM)
            if (claveMapaAnterior == claveActual) return@AndroidView
            claveMapaAnterior = claveActual

            val centro = GeoPoint(latitud, longitud)
            map.controller.setZoom(zoom)
            map.controller.setCenter(centro)
            map.overlays.clear()

            val marcador = Marker(map).apply {
                position = centro
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Ubicación del servicio"
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
    fotoPerfilTrabajador: String
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
                Text(
                    text = nombreTrabajador,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
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

private fun lerpFloat(inicio: Float, fin: Float, fraccion: Float): Float {
    return inicio + ((fin - inicio) * fraccion.coerceIn(0f, 1f))
}

private fun calcularDistanciaKm(
    oferta: OfertaServicio,
    latUsuario: Double?,
    lonUsuario: Double?,
    idUsuarioActual: Long?
): Int {
    if (idUsuarioActual != null && oferta.idTrabajador == idUsuarioActual) return 0
    val lat = oferta.latitudReferencia ?: return 0
    val lon = oferta.longitudReferencia ?: return 0
    val referenciaLat = latUsuario ?: return 0
    val referenciaLon = lonUsuario ?: return 0

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

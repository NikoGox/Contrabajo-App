package com.movil.contrabajo.ui.screens.principal

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import com.movil.contrabajo.ui.components.BotonPremiumP
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaMarketplaceCompacta
import com.movil.contrabajo.ui.theme.LocalColoresContrabajo
import com.movil.contrabajo.ui.viewmodel.OrdenMarketplace
import com.movil.contrabajo.ui.viewmodel.PrincipalViewModel
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import android.content.Context
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.movil.contrabajo.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaPrincipal(
    viewModel: PrincipalViewModel,
    onAbrirServicio: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    onAbrirUbicacionRapida: () -> Unit,
    onAbrirPremium: () -> Unit = {},
    mostrarBotonPremium: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var mostrarModalRango by rememberSaveable { mutableStateOf(false) }
    var mostrarModalFiltros by rememberSaveable { mutableStateOf(false) }
    var bloquearScrollVertical by rememberSaveable { mutableStateOf(false) }
    var posicionSliderRangoBusqueda by rememberSaveable { mutableFloatStateOf(0f) }
    var categoriaTemporal by rememberSaveable { mutableStateOf<Long?>(null) }
    var tipoPrecioTemporal by rememberSaveable { mutableStateOf<Int?>(null) }
    var filtroZonaComunaTemporal by rememberSaveable { mutableStateOf(false) }
    var comunaTemporal by rememberSaveable { mutableStateOf("") }
    var ordenTemporal by rememberSaveable { mutableStateOf(OrdenMarketplace.FECHA_RECIENTES.name) }
    var busquedaActiva by remember { mutableStateOf(false) }
    val cerrarBusquedaFuera: () -> Unit = {
        if (busquedaActiva) {
            busquedaActiva = false
            focusManager.clearFocus(force = true)
        }
    }
    val cierreBusquedaPorScroll = remember(busquedaActiva) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (
                    busquedaActiva &&
                    source == NestedScrollSource.Drag &&
                    (kotlin.math.abs(available.x) > 0.25f || kotlin.math.abs(available.y) > 0.25f)
                ) {
                    cerrarBusquedaFuera()
                }
                return Offset.Zero
            }
        }
    }
    val glow = rememberInfiniteTransition(label = "glowBuscadorPrincipal")
    val glowFase by glow.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_600, easing = LinearEasing)
        ),
        label = "glowFaseBuscador"
    )
    val glowPulso by glow.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulsoBuscador"
    )
    val progresoBuscador by animateFloatAsState(
        targetValue = if (busquedaActiva) 1f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "progresoBuscadorTopbar"
    )

    val pullToRefreshState = rememberPullToRefreshState()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(uiState.rangoBusquedaM, mostrarModalRango) {
        if (!mostrarModalRango) {
            posicionSliderRangoBusqueda = EscalaRango.posicionSliderPorValor(uiState.rangoBusquedaM)
        }
    }

    LaunchedEffect(mostrarModalFiltros) {
        if (mostrarModalFiltros) {
            categoriaTemporal = uiState.filtroCategoriaId
            tipoPrecioTemporal = uiState.filtroTipoPrecio
            filtroZonaComunaTemporal = uiState.filtroZonaComunaActivo
            comunaTemporal = uiState.comunaFiltro
            ordenTemporal = uiState.ordenMarketplace.name
        }
    }

    LaunchedEffect(uiState.mensajePrincipal) {
        val mensaje = uiState.mensajePrincipal ?: return@LaunchedEffect
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        viewModel.consumirMensajePrincipal()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> cerrarBusquedaFuera()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.refrescando,
        onRefresh = viewModel::refrescarDesdeGesto,
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = uiState.refrescando,
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
    PantallaBase(
        modifier = Modifier,
        scrollable = false,
        mostrarFondo = false,
        respetarNavegacionInferior = false,
        espaciadoVertical = 12.dp
    ) {
        val glowAzul = LocalColoresContrabajo.current.info
        val glowCyan = MaterialTheme.colorScheme.secondary
        val glowVerde = LocalColoresContrabajo.current.exito
        val colorCampoActivo = MaterialTheme.colorScheme.surface
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
                                glowAzul.copy(alpha = alphaBase),
                                glowCyan.copy(alpha = (alphaBase * 1.05f).coerceAtMost(0.17f)),
                                glowVerde.copy(alpha = alphaBase),
                                glowAzul.copy(alpha = alphaBase)
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
                                glowAzul.copy(alpha = 0.8f),
                                glowCyan.copy(alpha = 0.88f),
                                glowVerde.copy(alpha = 0.8f),
                                glowAzul.copy(alpha = 0.8f)
                            ),
                            start = Offset(size.width - fase, 0f),
                            end = Offset(-fase, size.height)
                        ),
                        cornerRadius = radio,
                        style = Stroke(width = 2.4.dp.toPx())
                    )
                },
            color = lerp(MaterialTheme.colorScheme.primary, colorCampoActivo, progresoBuscador),
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
                    enabled = busquedaActiva || progresoBuscador > 0.01f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 12.dp)
                        .graphicsLayer {
                            alpha = progresoBuscador.coerceIn(0f, 1f)
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        if (uiState.busqueda.isBlank()) {
                            Text(
                                text = "Buscar servicios",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )

                if (mostrarBotonPremium && progresoBuscador < 0.98f) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                alpha = (1f - progresoBuscador).coerceIn(0f, 1f)
                            }
                            .clickable(enabled = progresoBuscador < 0.12f) { onAbrirPremium() },
                        contentAlignment = Alignment.Center
                    ) {
                        BotonPremiumP(tamano = 34.dp)
                    }
                }

                IconButton(
                    onClick = { busquedaActiva = true }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = if (progresoBuscador > 0.02f) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(busquedaActiva) {
                    if (!busquedaActiva) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            if (event.changes.any { it.changedToDownIgnoreConsumed() }) {
                                cerrarBusquedaFuera()
                            }
                        }
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Rango de búsqueda actual: ")
                    }
                    append(EscalaRango.formatear(uiState.rangoBusquedaM))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        cerrarBusquedaFuera()
                        mostrarModalRango = true
                    }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = {
                    cerrarBusquedaFuera()
                    onAbrirUbicacionRapida()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = {
                    cerrarBusquedaFuera()
                    mostrarModalFiltros = true
                }) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = "Filtros y orden",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        if (uiState.busqueda.isNotBlank()) {
            Text(
                text = "Coincidencias: ${uiState.ofertas.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(busquedaActiva) {
                    if (!busquedaActiva) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            if (event.changes.any { it.changedToDownIgnoreConsumed() }) {
                                cerrarBusquedaFuera()
                            }
                        }
                    }
                }
                .nestedScroll(cierreBusquedaPorScroll)
        ) {
                if (uiState.ofertas.isEmpty()) {
                    val hayBusquedaOFiltrosActivos =
                        uiState.busqueda.isNotBlank() ||
                            uiState.filtroCategoriaId != null ||
                            uiState.filtroTipoPrecio != null ||
                            uiState.filtroZonaComunaActivo
                    val textoEstado = if (uiState.filtroPorCoordenadasActivo) {
                        if (hayBusquedaOFiltrosActivos) {
                            "No hay coincidencias. Ajusta tu búsqueda, filtros o rango."
                        } else {
                            "No hay servicios disponibles dentro de tu rango actual"
                        }
                    } else if (hayBusquedaOFiltrosActivos) {
                        "No hay coincidencias. Ajusta tu búsqueda o filtros."
                    } else {
                        "No hay servicios disponibles por ahora."
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(64.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(18.dp).size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = textoEstado,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Prueba ajustando el rango de busqueda, la comuna o los filtros para encontrar más publicaciones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = !bloquearScrollVertical,
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = navBarBottom + 160.dp)
                    ) {
                        items(uiState.ofertas, key = { it.idOfertaServicio }) { oferta ->
                            TarjetaMarketplaceCompacta(
                                oferta = oferta,
                                onAbrirServicio = {
                                    scope.launch {
                                        cerrarBusquedaFuera()
                                        yield()
                                        onAbrirServicio(oferta.idOfertaServicio)
                                    }
                                },
                                onMantenerPresionCambio = { activo -> bloquearScrollVertical = activo }
                            )
                        }
                    }
                }

            // Fade en el borde superior del área scrollable, justo debajo del selector de filtros
            val fadeColor = MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(28.dp)
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(fadeColor, Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            )
                        )
                    }
            )
        }
    }
    }

    if (mostrarModalRango) {
        ModalBottomSheet(
            onDismissRequest = { mostrarModalRango = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) }
        ) {
            ModalAjustesContenedor(
                titulo = "Rango de búsqueda",
                subtitulo = "Selecciona hasta dónde quieres buscar servicios cercanos.",
                onCancelar = { mostrarModalRango = false },
                onConfirmar = {
                    viewModel.guardarRangoBusqueda(
                        EscalaRango.valorPorPosicionSlider(posicionSliderRangoBusqueda)
                    )
                    mostrarModalRango = false
                }
            ) {
                Text(
                    text = "Actual: ${EscalaRango.formatear(EscalaRango.valorPorPosicionSlider(posicionSliderRangoBusqueda))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                MiniMapaRangoBusqueda(
                    latitud = uiState.latitudUsuario ?: -33.4489,
                    longitud = uiState.longitudUsuario ?: -70.6693,
                    rangoM = EscalaRango.valorPorPosicionSlider(posicionSliderRangoBusqueda),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Slider(
                    value = posicionSliderRangoBusqueda,
                    onValueChange = { posicionSliderRangoBusqueda = it },
                    valueRange = 0f..EscalaRango.valoresMetros.lastIndex.toFloat(),
                    steps = EscalaRango.valoresMetros.size - 2
                )
            }
        }
    }

    OverlayPantallaCarga(
        visible = uiState.cargandoOperacion,
        mensaje = "Guardando rango de búsqueda..."
    )

    if (mostrarModalFiltros) {
        FiltroMarketplaceDialog(
            categorias = uiState.categoriasDisponibles.map { it.idCategoriaServicio to it.nombre },
            categoriaSeleccionada = categoriaTemporal,
            tipoPrecioSeleccionado = tipoPrecioTemporal,
            filtroZonaComunaActivo = filtroZonaComunaTemporal,
            comunaSeleccionada = comunaTemporal,
            comunas = uiState.comunasDisponibles.map { it.nombre },
            ordenActual = OrdenMarketplace.valueOf(ordenTemporal),
            onCategoriaSeleccionada = { categoriaTemporal = it },
            onTipoPrecioSeleccionado = { tipoPrecioTemporal = it },
            onFiltroZonaComunaCambiado = { filtroZonaComunaTemporal = it },
            onComunaSeleccionada = { comunaTemporal = it },
            onOrdenSeleccionado = { ordenTemporal = it.name },
            onAplicar = {
                viewModel.aplicarFiltros(
                    categoriaId = categoriaTemporal,
                    tipoPrecio = tipoPrecioTemporal,
                    filtroZonaComunaActivo = filtroZonaComunaTemporal,
                    comunaFiltro = comunaTemporal,
                    orden = OrdenMarketplace.valueOf(ordenTemporal)
                )
                mostrarModalFiltros = false
            },
            onLimpiar = {
                viewModel.limpiarFiltros()
                mostrarModalFiltros = false
            },
            onDismiss = { mostrarModalFiltros = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FiltroMarketplaceDialog(
    categorias: List<Pair<Long, String>>,
    categoriaSeleccionada: Long?,
    tipoPrecioSeleccionado: Int?,
    filtroZonaComunaActivo: Boolean,
    comunaSeleccionada: String,
    comunas: List<String>,
    ordenActual: OrdenMarketplace,
    onCategoriaSeleccionada: (Long?) -> Unit,
    onTipoPrecioSeleccionado: (Int?) -> Unit,
    onFiltroZonaComunaCambiado: (Boolean) -> Unit,
    onComunaSeleccionada: (String) -> Unit,
    onOrdenSeleccionado: (OrdenMarketplace) -> Unit,
    onAplicar: () -> Unit,
    onLimpiar: () -> Unit,
    onDismiss: () -> Unit
) {
    var desplegarCategorias by rememberSaveable { mutableStateOf(false) }
    var desplegarComunas by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) }
    ) {
        ModalAjustesContenedor(
            titulo = "Filtrar y ordenar",
            onCancelar = onDismiss,
            onConfirmar = onAplicar
        ) {
            ExposedDropdownMenuBox(
                expanded = desplegarCategorias,
                onExpandedChange = { desplegarCategorias = !desplegarCategorias }
            ) {
                OutlinedTextField(
                    value = categorias.firstOrNull { it.first == categoriaSeleccionada }?.second ?: "Todas",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegarCategorias) },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = desplegarCategorias,
                    onDismissRequest = { desplegarCategorias = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            onCategoriaSeleccionada(null)
                            desplegarCategorias = false
                        }
                    )
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.second) },
                            onClick = {
                                onCategoriaSeleccionada(categoria.first)
                                desplegarCategorias = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Tipo de precio",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            val opcionesTipoPrecio = listOf(
                "Todos" to null,
                "Fijo" to TipoPrecio.FIJO,
                "Por hora" to TipoPrecio.POR_HORA,
                "Desde" to TipoPrecio.DESDE,
                "Contactar" to TipoPrecio.CONTACTAR
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                opcionesTipoPrecio.forEach { (texto, valor) ->
                    BotonOrdenCompacto(
                        texto = texto,
                        seleccionado = tipoPrecioSeleccionado == valor,
                        onClick = { onTipoPrecioSeleccionado(valor) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrar por comuna",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = filtroZonaComunaActivo,
                    onCheckedChange = { onFiltroZonaComunaCambiado(it) }
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { if (filtroZonaComunaActivo) desplegarComunas = true },
                    enabled = filtroZonaComunaActivo,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (filtroZonaComunaActivo) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        contentColor = if (filtroZonaComunaActivo) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = comunaSeleccionada.ifBlank { "Seleccionar comuna" },
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                DropdownMenu(
                    expanded = desplegarComunas && filtroZonaComunaActivo,
                    onDismissRequest = { desplegarComunas = false }
                ) {
                    comunas.forEach { comuna ->
                        DropdownMenuItem(
                            text = { Text(comuna) },
                            onClick = {
                                onComunaSeleccionada(comuna)
                                desplegarComunas = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Orden",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonOrdenCompacto(
                    texto = "A -> Z",
                    seleccionado = ordenActual == OrdenMarketplace.ALFABETICO_A_Z,
                    onClick = { onOrdenSeleccionado(OrdenMarketplace.ALFABETICO_A_Z) }
                )
                BotonOrdenCompacto(
                    texto = "Recientes",
                    seleccionado = ordenActual == OrdenMarketplace.FECHA_RECIENTES,
                    onClick = { onOrdenSeleccionado(OrdenMarketplace.FECHA_RECIENTES) }
                )
                BotonOrdenCompacto(
                    texto = "Antiguas",
                    seleccionado = ordenActual == OrdenMarketplace.FECHA_ANTIGUAS,
                    onClick = { onOrdenSeleccionado(OrdenMarketplace.FECHA_ANTIGUAS) }
                )
            }
        }
    }
}

@Composable
private fun ModalAjustesContenedor(
    titulo: String,
    subtitulo: String? = null,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (!subtitulo.isNullOrBlank()) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            ) {
                Text("Cancelar")
            }
            OutlinedButton(
                onClick = onConfirmar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Aplicar")
            }
        }
    }
}

@Composable
private fun BotonOrdenCompacto(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MiniMapaRangoBusqueda(
    latitud: Double,
    longitud: Double,
    rangoM: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val radioMetros = rangoM.toDouble()
    val zoom = calcularZoomRangoBusqueda(rangoM).toDouble()

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
            // Vista previa estatica: ocultar botones +/- y bloquear zoom/desplazamiento.
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitud, longitud))
            // Consumir el touch para que el usuario no pueda mover ni hacer zoom al mapa.
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
                title = "Mi ubicacion"
                icon = ContextCompat.getDrawable(context, R.drawable.ic_pin_marcador_azul)
            }

            val circulo = Polygon(map).apply {
                points = Polygon.pointsAsCircle(centro, radioMetros)
                fillColor = android.graphics.Color.argb(0x33, 0x19, 0xA1, 0xA8)
                strokeColor = android.graphics.Color.argb(0xFF, 0x0E, 0x8C, 0x94)
                strokeWidth = 2f
            }

            map.overlays.add(circulo)
            map.overlays.add(marcador)
            map.invalidate()
        }
    )
}

private fun calcularZoomRangoBusqueda(rangoM: Int): Int = when {
    rangoM <= 400 -> 15
    rangoM <= 900 -> 14
    rangoM <= 2_000 -> 13
    rangoM <= 5_000 -> 12
    rangoM <= 10_000 -> 11
    rangoM <= 20_000 -> 10
    rangoM <= 35_000 -> 9
    else -> 9
}

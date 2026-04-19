package com.movil.contrabajo.ui.screens.principal

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaMarketplaceCompacta
import com.movil.contrabajo.ui.viewmodel.OrdenMarketplace
import com.movil.contrabajo.ui.viewmodel.PrincipalViewModel
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun PantallaPrincipal(
    viewModel: PrincipalViewModel,
    onAbrirServicio: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var mostrarModalRango by rememberSaveable { mutableStateOf(false) }
    var mostrarModalFiltros by rememberSaveable { mutableStateOf(false) }
    var bloquearScrollVertical by rememberSaveable { mutableStateOf(false) }
    var posicionSliderRangoBusqueda by rememberSaveable { mutableFloatStateOf(0f) }
    var categoriaTemporal by rememberSaveable { mutableStateOf<Long?>(null) }
    var tipoPrecioTemporal by rememberSaveable { mutableStateOf<Int?>(null) }
    var soloVerificadosTemporal by rememberSaveable { mutableStateOf(false) }
    var ordenTemporal by rememberSaveable { mutableStateOf(OrdenMarketplace.FECHA_RECIENTES.name) }
    var busquedaActiva by rememberSaveable { mutableStateOf(false) }
    val glow = rememberInfiniteTransition(label = "glowBuscadorPrincipal")
    val glowFase by glow.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing)
        ),
        label = "glowFaseBuscador"
    )

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.refrescando,
        onRefresh = viewModel::refrescarDesdeGesto,
        refreshThreshold = 30.dp,
        refreshingOffset = 30.dp
    )
    val estiramientoContenido = if (uiState.refrescando) {
        24.dp
    } else {
        (pullRefreshState.progress.coerceIn(0f, 1.6f) * 34f).dp
    }

    LaunchedEffect(uiState.rangoBusquedaM, mostrarModalRango) {
        if (!mostrarModalRango) {
            posicionSliderRangoBusqueda = EscalaRango.posicionSliderPorValor(uiState.rangoBusquedaM)
        }
    }

    LaunchedEffect(mostrarModalFiltros) {
        if (mostrarModalFiltros) {
            categoriaTemporal = uiState.filtroCategoriaId
            tipoPrecioTemporal = uiState.filtroTipoPrecio
            soloVerificadosTemporal = uiState.soloTrabajadorVerificado
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
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.recargar()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PantallaBase(
        modifier = modifier,
        scrollable = false,
        mostrarFondo = false
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 8.dp
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAbrirAjustes,
                        enabled = !busquedaActiva
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Ajustes",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (busquedaActiva) 0.28f else 1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { busquedaActiva = true },
                        enabled = !busquedaActiva
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (busquedaActiva) 0.28f else 1f)
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = busquedaActiva,
                    enter = fadeIn(animationSpec = tween(160)),
                    exit = fadeOut(animationSpec = tween(130)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                val fase = size.width * glowFase
                                val esquinas = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF7C4DFF).copy(alpha = 0.82f),
                                            Color(0xFF00BCD4).copy(alpha = 0.86f),
                                            Color(0xFF2196F3).copy(alpha = 0.82f),
                                            Color(0xFF7C4DFF).copy(alpha = 0.82f)
                                        ),
                                        start = Offset(fase - size.width, 0f),
                                        end = Offset(fase, size.height)
                                    ),
                                    cornerRadius = esquinas,
                                    style = Stroke(width = 2.2.dp.toPx())
                                )
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF7C4DFF).copy(alpha = 0.16f),
                                            Color(0xFF00BCD4).copy(alpha = 0.18f),
                                            Color(0xFF2196F3).copy(alpha = 0.16f),
                                            Color(0xFF7C4DFF).copy(alpha = 0.16f)
                                        ),
                                        start = Offset(size.width - fase, 0f),
                                        end = Offset(-fase, size.height)
                                    ),
                                    topLeft = Offset(-2f, -2f),
                                    size = Size(size.width + 4f, size.height + 4f),
                                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                                    style = Stroke(width = 7.dp.toPx())
                                )
                            }
                            .background(Color.White, RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                        )
                        BasicTextField(
                            value = uiState.busqueda,
                            onValueChange = viewModel::actualizarBusqueda,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterStart)
                                .padding(start = 40.dp, end = 42.dp, top = 10.dp, bottom = 10.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF0F2124),
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(Color(0xFF0F2124)),
                            decorationBox = { innerTextField ->
                                if (uiState.busqueda.isBlank()) {
                                    Text(
                                        text = "Buscar servicios, categorias o trabajador",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF60737A)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        IconButton(
                            onClick = { busquedaActiva = false },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar busqueda",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Rango de busqueda actual: ")
                    }
                    append(EscalaRango.formatear(uiState.rangoBusquedaM))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { mostrarModalRango = true }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = { mostrarModalFiltros = true }) {
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
                .pullRefresh(pullRefreshState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = estiramientoContenido)
            ) {
                if (uiState.ofertas.isEmpty()) {
                    val textoEstado = if (uiState.filtroPorCoordenadasActivo) {
                        "No hay publicaciones dentro de tu rango actual."
                    } else {
                        "Obten tu ubicacion en Ajustes > Ubicacion > Obtener ubicacion."
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(86.dp))
                        Text(
                            text = textoEstado,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = !bloquearScrollVertical,
                        contentPadding = PaddingValues(bottom = 108.dp)
                    ) {
                        items(uiState.ofertas, key = { it.idOfertaServicio }) { oferta ->
                            TarjetaMarketplaceCompacta(
                                oferta = oferta,
                                onAbrirServicio = { onAbrirServicio(oferta.idOfertaServicio) },
                                onMantenerPresionCambio = { activo -> bloquearScrollVertical = activo }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.refrescando,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-2).dp),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (mostrarModalRango) {
        AlertDialog(
            onDismissRequest = { mostrarModalRango = false },
            title = { Text("Rango de busqueda") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Selecciona hasta donde quieres buscar servicios cercanos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Actual: ${EscalaRango.formatear(EscalaRango.valorPorPosicionSlider(posicionSliderRangoBusqueda))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = posicionSliderRangoBusqueda,
                        onValueChange = { posicionSliderRangoBusqueda = it },
                        valueRange = 0f..EscalaRango.valoresMetros.lastIndex.toFloat(),
                        steps = EscalaRango.valoresMetros.size - 2
                    )
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.guardarRangoBusqueda(
                            EscalaRango.valorPorPosicionSlider(posicionSliderRangoBusqueda)
                        )
                        mostrarModalRango = false
                    },
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalRango = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarModalFiltros) {
        FiltroMarketplaceDialog(
            categorias = uiState.categoriasDisponibles.map { it.idCategoriaServicio to it.nombre },
            categoriaSeleccionada = categoriaTemporal,
            tipoPrecioSeleccionado = tipoPrecioTemporal,
            soloVerificados = soloVerificadosTemporal,
            ordenActual = OrdenMarketplace.valueOf(ordenTemporal),
            onCategoriaSeleccionada = { categoriaTemporal = it },
            onTipoPrecioSeleccionado = { tipoPrecioTemporal = it },
            onSoloVerificadosCambiado = { soloVerificadosTemporal = it },
            onOrdenSeleccionado = { ordenTemporal = it.name },
            onAplicar = {
                viewModel.aplicarFiltros(
                    categoriaId = categoriaTemporal,
                    tipoPrecio = tipoPrecioTemporal,
                    soloVerificados = soloVerificadosTemporal,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroMarketplaceDialog(
    categorias: List<Pair<Long, String>>,
    categoriaSeleccionada: Long?,
    tipoPrecioSeleccionado: Int?,
    soloVerificados: Boolean,
    ordenActual: OrdenMarketplace,
    onCategoriaSeleccionada: (Long?) -> Unit,
    onTipoPrecioSeleccionado: (Int?) -> Unit,
    onSoloVerificadosCambiado: (Boolean) -> Unit,
    onOrdenSeleccionado: (OrdenMarketplace) -> Unit,
    onAplicar: () -> Unit,
    onLimpiar: () -> Unit,
    onDismiss: () -> Unit
) {
    var desplegarCategorias by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar y ordenar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = desplegarCategorias,
                    onExpandedChange = { desplegarCategorias = !desplegarCategorias }
                ) {
                    OutlinedTextField(
                        value = categorias.firstOrNull { it.first == categoriaSeleccionada }?.second ?: "Todas",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OpcionFiltro(
                        texto = "Todos",
                        seleccionada = tipoPrecioSeleccionado == null,
                        onClick = { onTipoPrecioSeleccionado(null) }
                    )
                    OpcionFiltro(
                        texto = "Fijo",
                        seleccionada = tipoPrecioSeleccionado == TipoPrecio.FIJO,
                        onClick = { onTipoPrecioSeleccionado(TipoPrecio.FIJO) }
                    )
                    OpcionFiltro(
                        texto = "Por hora",
                        seleccionada = tipoPrecioSeleccionado == TipoPrecio.POR_HORA,
                        onClick = { onTipoPrecioSeleccionado(TipoPrecio.POR_HORA) }
                    )
                    OpcionFiltro(
                        texto = "Desde",
                        seleccionada = tipoPrecioSeleccionado == TipoPrecio.DESDE,
                        onClick = { onTipoPrecioSeleccionado(TipoPrecio.DESDE) }
                    )
                    OpcionFiltro(
                        texto = "Contactar",
                        seleccionada = tipoPrecioSeleccionado == TipoPrecio.CONTACTAR,
                        onClick = { onTipoPrecioSeleccionado(TipoPrecio.CONTACTAR) }
                    )
                }

                OpcionFiltro(
                    texto = if (soloVerificados) "Solo trabajador verificado: SI" else "Solo trabajador verificado: NO",
                    seleccionada = soloVerificados,
                    onClick = { onSoloVerificadosCambiado(!soloVerificados) }
                )

                Text(
                    text = "Orden",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OpcionFiltro(
                        texto = "A -> Z",
                        seleccionada = ordenActual == OrdenMarketplace.ALFABETICO_A_Z,
                        onClick = { onOrdenSeleccionado(OrdenMarketplace.ALFABETICO_A_Z) }
                    )
                    OpcionFiltro(
                        texto = "Mas recientes",
                        seleccionada = ordenActual == OrdenMarketplace.FECHA_RECIENTES,
                        onClick = { onOrdenSeleccionado(OrdenMarketplace.FECHA_RECIENTES) }
                    )
                    OpcionFiltro(
                        texto = "Mas antiguas",
                        seleccionada = ordenActual == OrdenMarketplace.FECHA_ANTIGUAS,
                        onClick = { onOrdenSeleccionado(OrdenMarketplace.FECHA_ANTIGUAS) }
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onAplicar,
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onLimpiar) {
                    Text("Limpiar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
private fun OpcionFiltro(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (seleccionada) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

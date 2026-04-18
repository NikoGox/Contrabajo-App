package com.movil.contrabajo.ui.screens.principal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaMarketplaceCompacta
import com.movil.contrabajo.ui.viewmodel.PrincipalViewModel

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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAbrirAjustes) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rango de busqueda actual: ${uiState.rangoBusquedaKm}km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = "Filtro",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        CampoContrabajo(
            valor = uiState.busqueda,
            onValueChange = viewModel::actualizarBusqueda,
            etiqueta = "Buscar por servicio, categoria o trabajador"
        )

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
                    Text(
                        text = "Aun no hay publicaciones disponibles.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 108.dp)
                    ) {
                        items(uiState.ofertas, key = { it.idOfertaServicio }) { oferta ->
                            TarjetaMarketplaceCompacta(
                                oferta = oferta,
                                onAbrirServicio = { onAbrirServicio(oferta.idOfertaServicio) }
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
}

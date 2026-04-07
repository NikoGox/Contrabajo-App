package com.movil.contrabajo.ui.screens.principal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.components.TarjetaMarketplaceCompacta
import com.movil.contrabajo.ui.viewmodel.PrincipalViewModel

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun PantallaPrincipal(
    viewModel: PrincipalViewModel,
    onAbrirServicio: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Marketplace",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Explora servicios disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "${uiState.ofertas.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
                    TarjetaBase {
                        Text(
                            "Aun no hay publicaciones disponibles.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Publica o habilita tu servicio en perfil y luego actualiza con el gesto para recargar.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
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

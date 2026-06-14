package com.movil.contrabajo.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.movil.contrabajo.ui.navigation.RutasApp
import com.movil.contrabajo.ui.theme.Blanco
import com.movil.contrabajo.ui.theme.LocalColoresContrabajo
import kotlin.math.abs

private data class ItemNavbar(
    val ruta: String,
    val descripcion: String,
    val icono: ImageVector
)

val AlturaNavbarFlotante = 70.dp

@Composable
fun ContenedorConNavbarFlotante(
    actual: String,
    alNavegar: (String) -> Unit,
    modoModerador: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bgColor = MaterialTheme.colorScheme.background
    Box(modifier = modifier.fillMaxSize()) {
        content(PaddingValues.Zero)

        // Rellena el área de la barra del sistema con el color de fondo, haciéndola opaca
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(navBarHeight)
                .background(bgColor)
                .zIndex(1f)
        )

        NavbarFlotante(
            actual = actual,
            alNavegar = alNavegar,
            modoModerador = modoModerador,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        )
    }
}

@Composable
fun NavbarFlotante(
    actual: String,
    alNavegar: (String) -> Unit,
    modoModerador: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colores = LocalColoresContrabajo.current
    val items = if (modoModerador) {
        listOf(
            ItemNavbar(RutasApp.Perfil.ruta, "Perfil", Icons.Outlined.AccountCircle),
            ItemNavbar(RutasApp.ReportesModerador.ruta, "Reportes", Icons.Outlined.WarningAmber)
        )
    } else {
        listOf(
            ItemNavbar(RutasApp.Perfil.ruta, "Perfil", Icons.Outlined.AccountCircle),
            ItemNavbar(RutasApp.Principal.ruta, "Marketplace", Icons.Outlined.Storefront),
            ItemNavbar(RutasApp.Chats.ruta, "Mensajes", Icons.Outlined.ChatBubble)
        )
    }
    val indiceSeleccionado = items.indexOfFirst { it.ruta == actual }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.84f),
            shape = RoundedCornerShape(30.dp),
            color = colores.navbarFondo.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Blanco.copy(alpha = 0.28f)),
            shadowElevation = 16.dp
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colores.navbarFondo.copy(alpha = 0.78f),
                                colores.navbarBrillo.copy(alpha = 0.12f),
                                colores.navbarFondo.copy(alpha = 0.96f),
                                colores.navbarBrillo.copy(alpha = 0.12f),
                                colores.navbarFondo.copy(alpha = 0.78f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                val anchoSegmento = maxWidth / items.size
                val anchoIndicador = 68.dp
                val density = LocalDensity.current
                val desplazamientoIndicador by animateDpAsState(
                    targetValue = (anchoSegmento * indiceSeleccionado) + ((anchoSegmento - anchoIndicador) / 2),
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
                    label = "navbarIndicatorOffset"
                )
                val centroIndicadorPx = with(density) { (desplazamientoIndicador + (anchoIndicador / 2)).toPx() }
                val anchoSegmentoPx = with(density) { anchoSegmento.toPx() }.coerceAtLeast(1f)

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = desplazamientoIndicador)
                        .size(width = anchoIndicador, height = 50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    colores.navbarSeleccion.copy(alpha = 0.36f),
                                    Blanco.copy(alpha = 0.14f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { indice, item ->
                        val seleccionado = indice == indiceSeleccionado
                        val centroItemPx = with(density) { ((anchoSegmento * indice) + (anchoSegmento / 2)).toPx() }
                        val intensidad = (1f - (abs(centroIndicadorPx - centroItemPx) / anchoSegmentoPx))
                            .coerceIn(0f, 1f)
                        val escala = 1f + (0.08f * intensidad)
                        val elevacionIcono = (-3f * intensidad).dp
                        val colorIcono = lerp(colores.navbarIconoInactivo, Blanco, intensidad)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(28.dp))
                                .clickable(
                                    enabled = !seleccionado,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { alNavegar(item.ruta) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icono,
                                contentDescription = item.descripcion,
                                tint = colorIcono,
                                modifier = Modifier
                                    .size(30.dp)
                                    .offset(y = elevacionIcono)
                                    .graphicsLayer {
                                        scaleX = escala
                                        scaleY = escala
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

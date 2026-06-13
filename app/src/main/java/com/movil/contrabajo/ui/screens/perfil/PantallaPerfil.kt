package com.movil.contrabajo.ui.screens.perfil

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.ui.components.EstrellaPremiumAnimada
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.FilaEtiquetaValor
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    viewModel: PerfilViewModel,
    onAbrirCrearServicio: () -> Unit,
    onAbrirEditarServicio: (Long) -> Unit,
    onAbrirValoraciones: () -> Unit,
    onEditarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val usuario = uiState.usuario
    val esModerador = usuario?.tipoPerfil == TipoPerfil.MODERADOR
    val esTrabajador = usuario?.tipoPerfil == TipoPerfil.TRABAJADOR || usuario?.tipoPerfil == TipoPerfil.PREMIUM
    val context = LocalContext.current
    val serviciosActivos = uiState.ofertasPropias.count { it.disponible }
    val totalServicios = uiState.ofertasPropias.size
    val valoracionesTotales = uiState.valoracionesPorServicio.flatMap { it.valoraciones }
    val promedioPerfil = if (valoracionesTotales.isEmpty()) {
        0.0
    } else {
        valoracionesTotales.map { it.voto }.average()
    }
    val scrollPerfil = rememberScrollState()
    val selectorFotoPerfilLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.actualizarFotoPerfil(uri.toString())
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.refrescando,
            onRefresh = viewModel::refrescarDesdeGesto,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.refrescando,
                    containerColor = Color.White,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            PantallaBase(
                modifier = Modifier,
                mostrarFondo = false,
                scrollable = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollPerfil)
                        .padding(bottom = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    TarjetaBase {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    Surface(
                                        modifier = Modifier.size(84.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    ) {
                                        val fotoPerfil = usuario?.fotoPerfilUrl.orEmpty()
                                        when {
                                            uiState.subiendoFotoPerfil -> {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(32.dp),
                                                        strokeWidth = 3.dp
                                                    )
                                                }
                                            }
                                            fotoPerfil.isBlank() -> {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.AccountCircle,
                                                        contentDescription = "Sin foto de perfil",
                                                        modifier = Modifier.fillMaxSize(),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                            else -> {
                                                AsyncImage(
                                                    model = fotoPerfil,
                                                    contentDescription = "Foto de perfil",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(26.dp)
                                            .clickable { selectorFotoPerfilLauncher.launch(arrayOf("image/*")) },
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.PhotoCamera,
                                                contentDescription = "Cambiar foto de perfil",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (usuario == null) {
                                                "Mi perfil"
                                            } else {
                                                "${usuario.nombre} ${usuario.apellidoPaterno}"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (usuario?.tipoPerfil == TipoPerfil.PREMIUM) {
                                            EstrellaPremiumAnimada(tamano = 18.dp)
                                        }
                                    }
                                    if (usuario != null) {
                                        Text(
                                            text = "@${usuario.username}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = when (usuario.tipoPerfil) {
                                                    TipoPerfil.MODERADOR -> "Moderador"
                                                    TipoPerfil.PREMIUM -> "Trabajador Premium"
                                                    TipoPerfil.TRABAJADOR -> "Trabajador"
                                                    else -> "Cliente"
                                                },
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilaEtiquetaValor(
                                        etiqueta = "Correo",
                                        valor = usuario?.correo.orEmpty(),
                                        etiquetaAncho = 76.dp,
                                        valorMaxLines = 1,
                                        valorTextStyle = MaterialTheme.typography.bodySmall
                                    )
                                    FilaEtiquetaValor(
                                        etiqueta = "Teléfono",
                                        valor = usuario?.telefono.orEmpty(),
                                        prefijo = "+56 9 "
                                    )
                                }
                            }

                            if (esTrabajador) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAbrirValoraciones() },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Valoraciones",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        ValoracionPerfil(valor = promedioPerfil, onClick = onAbrirValoraciones)
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = onEditarPerfil,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editar perfil")
                            }
                        }
                    }

                    if (!esModerador) {
                        TarjetaBase(contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Mis servicios",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ResumenCupo(
                                        titulo = "Servicios disponibles",
                                        valor = "$serviciosActivos/${uiState.limiteServiciosActivos}",
                                        colorTitulo = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ResumenCupo(
                                        titulo = "Total de servicios",
                                        valor = "$totalServicios/${uiState.limiteServiciosTotales}",
                                        colorTitulo = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (uiState.ofertasPropias.isEmpty()) {
                                    Text(
                                        text = if (uiState.usuario?.tipoPerfil == TipoPerfil.USUARIO_BASE) {
                                            "Tu perfil actual no puede publicar servicios aún."
                                        } else {
                                            "Aún no has creado tu primer servicio."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (uiState.usuario?.tipoPerfil != TipoPerfil.USUARIO_BASE) {
                                        OutlinedButton(
                                            onClick = onAbrirCrearServicio,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(999.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Crear servicio")
                                        }
                                    }
                                } else {
                                    if (totalServicios < uiState.limiteServiciosTotales) {
                                        OutlinedButton(
                                            onClick = onAbrirCrearServicio,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(999.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Crear servicio")
                                        }
                                    }

                                    uiState.ofertasPropias.forEach { oferta ->
                                        val enCurso = uiState.idsOfertasEnCurso.contains(oferta.idOfertaServicio)
                                        val bloqueadoPorCupo = !oferta.disponible && serviciosActivos >= uiState.limiteServiciosActivos
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Surface(
                                                            modifier = Modifier
                                                                .size(72.dp)
                                                                .height(72.dp),
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = MaterialTheme.colorScheme.surface
                                                        ) {
                                                            if (oferta.fotoUrlReferencia.isBlank()) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.Build,
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.size(34.dp)
                                                                    )
                                                                }
                                                            } else {
                                                                AsyncImage(
                                                                    model = oferta.fotoUrlReferencia,
                                                                    contentDescription = oferta.titulo,
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                            }
                                                        }
                                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                            Text(
                                                                text = oferta.titulo,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = oferta.precioTexto,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                    if (enCurso) {
                                                        EstadoServicioEnCurso()
                                                    } else {
                                                        Switch(
                                                            checked = oferta.disponible,
                                                            onCheckedChange = { valor ->
                                                                viewModel.cambiarDisponibilidadServicioRapido(
                                                                    idOfertaServicio = oferta.idOfertaServicio,
                                                                    valor = valor
                                                                )
                                                            },
                                                            enabled = !bloqueadoPorCupo,
                                                            modifier = Modifier.alpha(if (bloqueadoPorCupo) 0.35f else 1f)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = oferta.descripcion,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = oferta.ubicacionReferencia.ifBlank { "Región Metropolitana" },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                OutlinedButton(
                                                    onClick = { onAbrirEditarServicio(oferta.idOfertaServicio) },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Edit,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Editar servicio")
                                                }
                                            }
                                        }
                                    }

                                    if (serviciosActivos >= uiState.limiteServiciosActivos) {
                                        Text(
                                            text = "Es necesario tener cupo disponible para activar un servicio.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.errorServicio != null) {
                            Text(
                                text = uiState.errorServicio,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (uiState.usuario?.tipoPerfil == TipoPerfil.USUARIO_BASE) {
                            EtiquetaEstado("Para verificarte ve a Ajustes > Seguridad y verificación")
                        }
                    }

                    TextButton(
                        onClick = onCerrarSesion,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar sesión",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        OverlayPantallaCarga(
            visible = uiState.cargandoPantalla,
            mensaje = "Actualizando perfil..."
        )
    }
}


@Composable
private fun ResumenCupo(
    titulo: String,
    valor: String,
    colorTitulo: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = colorTitulo,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EstadoServicioEnCurso() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    ) {
        Text(
            text = "En Curso",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ValoracionPerfil(
    valor: Double,
    onClick: () -> Unit
) {
    val valorNormalizado = valor.coerceIn(0.0, 5.0)
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = String.format("%.1f", valorNormalizado),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        repeat(5) { indice ->
            val relleno = (valorNormalizado - indice).coerceIn(0.0, 1.0)
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = if (relleno > 0.4) Color(0xFFFFC93C) else Color(0xFFB0B7BF),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

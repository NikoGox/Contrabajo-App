package com.movil.contrabajo.ui.screens.perfil

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel

@Composable
fun PantallaPerfil(
    viewModel: PerfilViewModel,
    onAbrirCrearServicio: () -> Unit,
    onAbrirEditarServicio: (Long) -> Unit,
    onAbrirValoraciones: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val serviciosActivos = uiState.ofertasPropias.count { it.disponible }
    val totalServicios = uiState.ofertasPropias.size
    val valoracionesTotales = uiState.valoracionesPorServicio.flatMap { it.valoraciones }
    val promedioPerfil = if (valoracionesTotales.isEmpty()) {
        0.0
    } else {
        valoracionesTotales.map { it.voto }.average()
    }
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

    LaunchedEffect(Unit) {
        viewModel.recargar()
    }

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        TarjetaBase {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ) {
                        val fotoPerfil = uiState.usuario?.fotoPerfilUrl.orEmpty()
                        if (fotoPerfil.isBlank()) {
                            LogoContrabajo(compacto = true)
                        } else {
                            AsyncImage(
                                model = fotoPerfil,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (uiState.usuario == null) {
                            "Mi perfil"
                        } else {
                            "${uiState.usuario.nombre} ${uiState.usuario.apellidoPaterno} ${uiState.usuario.apellidoMaterno}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.usuario != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "@${uiState.usuario.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.usuario.verificado) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verificado",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                ValoracionPerfil(
                    valor = promedioPerfil,
                    onClick = onAbrirValoraciones
                )
            }
        }

        TarjetaBase {
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
                    modifier = Modifier.weight(1f)
                )
                ResumenCupo(
                    titulo = "Total de servicios",
                    valor = "$totalServicios/${uiState.limiteServiciosTotales}",
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.ofertasPropias.isEmpty()) {
                Text(
                    text = if (uiState.usuario?.tipoPerfil == TipoPerfil.USUARIO_BASE) {
                        "Tu perfil actual no puede publicar servicios aun."
                    } else {
                        "Aun no has creado tu primer servicio."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.usuario?.tipoPerfil != TipoPerfil.USUARIO_BASE) {
                    BotonPrimario(
                        texto = "Crear servicio",
                        onClick = onAbrirCrearServicio
                    )
                }
            } else {
                if (totalServicios < uiState.limiteServiciosTotales) {
                    OutlinedButton(
                        onClick = onAbrirCrearServicio,
                        modifier = Modifier.fillMaxWidth()
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
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = oferta.precioTexto,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = oferta.ubicacionReferencia.ifBlank { "Region Metropolitana" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            if (uiState.errorServicio != null) {
                Text(
                    text = uiState.errorServicio,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (uiState.usuario?.tipoPerfil == TipoPerfil.USUARIO_BASE) {
                EtiquetaEstado("Para verificarte ve a Ajustes > Seguridad y verificacion")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    OverlayPantallaCarga(
        visible = uiState.cargandoPantalla,
        mensaje = "Actualizando perfil..."
    )
}

@Composable
private fun ResumenCupo(
    titulo: String,
    valor: String,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

package com.movil.contrabajo.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.ResumenPerfilLinea
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel

@Composable
fun PantallaPerfil(
    viewModel: PerfilViewModel,
    onAbrirCrearServicio: () -> Unit,
    onAbrirEditarServicio: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.recargar()
    }

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) {
            onCerrarSesion()
            viewModel.consumirCierreSesion()
        }
    }

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        EncabezadoPantalla(
            titulo = "Mi perfil de trabajo",
            subtitulo = "Administra tu servicio sin cargar esta pantalla."
        )

        TarjetaBase {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogoContrabajo(compacto = true)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (uiState.usuario == null) {
                                "Sin sesion activa"
                            } else {
                                "${uiState.usuario.nombre} ${uiState.usuario.apellidoPaterno} ${uiState.usuario.apellidoMaterno}"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.usuario != null) {
                            Text("@${uiState.usuario.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            EtiquetaEstado(if (uiState.usuario.verificado) "Verificado" else "Pendiente")
                        }
                    }
                }
                Text(
                    text = "3.5 ★",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TarjetaBase {
            Text("Mis servicios 1/1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Solo puedes mantener una publicacion por cuenta en esta iteracion.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.ofertaPropia == null) {
                Text(
                    "Aun no has creado tu servicio.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BotonPrimario(texto = "Crear mi servicio", onClick = onAbrirCrearServicio)
            } else {
                val oferta = uiState.ofertaPropia
                EtiquetaEstado(oferta.nombreCategoria.ifBlank { "Sin categoria" }, enfatizada = true)
                Text(oferta.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(oferta.descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(oferta.precioTexto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visible en marketplace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = oferta.disponible,
                        onCheckedChange = viewModel::cambiarDisponibilidadServicioRapido
                    )
                }
                EtiquetaEstado(
                    if (oferta.disponible) "Disponible y visible" else "No disponible y oculto"
                )

                BotonPrimario(texto = "Editar servicio", onClick = onAbrirEditarServicio)
                BotonSecundario(texto = "Eliminar servicio", onClick = viewModel::eliminarServicio)
            }

            if (uiState.errorServicio != null) {
                Text(
                    uiState.errorServicio,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        TarjetaBase {
            Text("Resumen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (uiState.usuario != null) {
                ResumenPerfilLinea("Correo", uiState.usuario.correo)
                ResumenPerfilLinea("Telefono", uiState.usuario.telefono)
                ResumenPerfilLinea("RUN", "${uiState.usuario.run}-${uiState.usuario.dv}")
            }
        }

        BotonSecundario(texto = "Cerrar sesion", onClick = viewModel::cerrarSesion)
    }
}

package com.movil.contrabajo.ui.screens.servicio

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.ResumenPerfilLinea
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.DetalleServicioViewModel

@Composable
fun PantallaDetalleServicio(
    idOfertaServicio: Long,
    viewModel: DetalleServicioViewModel,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState

    LaunchedEffect(idOfertaServicio) {
        viewModel.cargarOferta(idOfertaServicio)
    }

    PantallaBase {
        EncabezadoPantalla(
            titulo = "Detalle del servicio",
            subtitulo = "Revisa la publicacion y los datos del trabajador."
        )
        TarjetaBase {
            if (uiState.oferta == null) {
                Text("No se pudo cargar la oferta.")
            } else {
                val oferta = uiState.oferta
                EtiquetaEstado(if (oferta.disponible) "Disponible" else "No disponible")
                Text(oferta.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(oferta.descripcion, style = MaterialTheme.typography.titleMedium)
                ResumenPerfilLinea("Prestador", oferta.nombreTrabajador)
                ResumenPerfilLinea("Ubicacion", oferta.ubicacionReferencia.ifBlank { "Region Metropolitana" })
                ResumenPerfilLinea("Precio", oferta.precioTexto)
                ResumenPerfilLinea("Valoracion", "${oferta.puntuacionPromedio} estrellas")
                BotonSecundario(texto = "Contactar trabajador", onClick = onVolver)
            }
        }
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

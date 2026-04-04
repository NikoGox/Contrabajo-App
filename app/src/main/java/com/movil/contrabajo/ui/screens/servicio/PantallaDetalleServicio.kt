package com.movil.contrabajo.ui.screens.servicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.TarjetaBase

@Composable
fun PantallaDetalleServicio(
    oferta: OfertaServicio?,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TarjetaBase {
            Text("Detalle del servicio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (oferta == null) {
                Text("No se pudo cargar la oferta.")
            } else {
                EtiquetaEstado(if (oferta.disponible) "Disponible" else "No disponible")
                Text(oferta.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(oferta.descripcion, style = MaterialTheme.typography.titleMedium)
                Text(oferta.detalle, style = MaterialTheme.typography.bodyLarge)
                Text("Prestador: ${oferta.nombreTrabajador}")
                Text("Precio: ${oferta.precioTexto}")
                Text("Ubicacion: ${oferta.ubicacionReferencia}")
                Text("Valoracion: ${oferta.puntuacionPromedio}")
            }
            Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

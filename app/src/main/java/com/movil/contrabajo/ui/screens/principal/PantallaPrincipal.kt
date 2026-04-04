package com.movil.contrabajo.ui.screens.principal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.ui.components.BarraInferior
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.navigation.RutasApp

@Composable
fun PantallaPrincipal(
    oferta: OfertaServicio?,
    onAbrirServicio: (Long) -> Unit,
    onNavegar: (String) -> Unit
) {
    Scaffold(
        bottomBar = { BarraInferior(actual = RutasApp.Principal.ruta, alNavegar = onNavegar) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Servicios destacados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Base v0.1-Pre-Alpha lista para evolucionar a feed real conectado al backend.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (oferta == null) {
                TarjetaBase { Text("Aun no hay publicaciones disponibles.") }
            } else {
                TarjetaBase {
                    EtiquetaEstado(if (oferta.disponible) "Disponible" else "No disponible")
                    Text(oferta.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(oferta.descripcion, style = MaterialTheme.typography.bodyLarge)
                    Text(oferta.detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(oferta.ubicacionReferencia.ifBlank { "Cobertura regional" })
                    }
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Text("${oferta.puntuacionPromedio} estrellas")
                    }
                    Text("Trabajador: ${oferta.nombreTrabajador}", style = MaterialTheme.typography.titleMedium)
                    Text("Precio: ${oferta.precioTexto}", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { onAbrirServicio(oferta.idOfertaServicio) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver servicio")
                    }
                }
            }
        }
    }
}

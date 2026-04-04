package com.movil.contrabajo.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.Usuario
import com.movil.contrabajo.ui.components.BarraInferior
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.navigation.RutasApp

@Composable
fun PantallaPerfil(
    usuario: Usuario?,
    onCerrarSesion: () -> Unit,
    onNavegar: (String) -> Unit
) {
    Scaffold(
        bottomBar = { BarraInferior(actual = RutasApp.Perfil.ruta, alNavegar = onNavegar) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Mi perfil de trabajo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            TarjetaBase {
                Text(
                    text = if (usuario == null) "Sin sesion activa" else "${usuario.nombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (usuario != null) {
                    EtiquetaEstado(if (usuario.verificado) "Verificado" else "Pendiente")
                    Text("@${usuario.username}", style = MaterialTheme.typography.titleMedium)
                    Text(usuario.correo)
                    Text(usuario.telefono)
                    Text("RUN: ${usuario.run}-${usuario.dv}")
                }
                Button(onClick = onCerrarSesion) {
                    Text("Cerrar sesion")
                }
            }
        }
    }
}

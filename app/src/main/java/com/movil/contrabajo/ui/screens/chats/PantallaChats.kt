package com.movil.contrabajo.ui.screens.chats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.ui.components.BarraInferior
import com.movil.contrabajo.ui.components.ItemListaTexto
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.navigation.RutasApp

@Composable
fun PantallaChats(
    chats: List<ChatCita>,
    onNavegar: (String) -> Unit
) {
    Scaffold(
        bottomBar = { BarraInferior(actual = RutasApp.Chats.ruta, alNavegar = onNavegar) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Chats activos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (chats.isEmpty()) {
                TarjetaBase { Text("No tienes conversaciones todavia.") }
            } else {
                TarjetaBase {
                    chats.forEach { chat ->
                        ItemListaTexto(
                            titulo = chat.nombreContacto,
                            subtitulo = chat.ultimoMensaje,
                            auxiliar = chat.horaUltimoMensaje.takeLast(5)
                        )
                    }
                }
            }
        }
    }
}

package com.movil.contrabajo.ui.screens.chats

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.components.TarjetaChat
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel

@Composable
fun PantallaChats(
    viewModel: ChatsViewModel,
    onAbrirChat: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        EncabezadoPantalla(
            titulo = "Mensajes",
            subtitulo = "Conversaciones activas con clientes y prestadores."
        )
        TarjetaBase {
            if (uiState.chats.isEmpty()) {
                Text("No tienes conversaciones todavia.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Cuando alguien contacte un servicio, sus mensajes apareceran aqui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                uiState.chats.forEach { chat ->
                    TarjetaChat(
                        chat = chat,
                        onClick = { onAbrirChat(chat.idChatCita) }
                    )
                }
            }
        }
    }
}

package com.movil.contrabajo.ui.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val chatsVisibles = uiState.chatsFiltrados

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        EncabezadoPantalla(
            titulo = "Mensajes",
            subtitulo = "Conversaciones activas con clientes y prestadores."
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroChatsEtiqueta(
                texto = "Chats de contacto",
                activo = uiState.filtroChatsContacto,
                onClick = { viewModel.actualizarFiltroChatsContacto(!uiState.filtroChatsContacto) },
                modifier = Modifier.weight(1f)
            )
            FiltroChatsEtiqueta(
                texto = "Chats de trabajador",
                activo = uiState.filtroChatsTrabajador,
                onClick = { viewModel.actualizarFiltroChatsTrabajador(!uiState.filtroChatsTrabajador) },
                modifier = Modifier.weight(1f)
            )
        }
        TarjetaBase {
            if (chatsVisibles.isEmpty()) {
                Text("No tienes conversaciones todavia.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Cuando alguien contacte un servicio, sus mensajes apareceran aqui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                chatsVisibles.forEach { chat ->
                    val esChatComoTrabajador = uiState.idUsuarioActual != null && chat.idTrabajador == uiState.idUsuarioActual
                    TarjetaChat(
                        chat = chat,
                        esChatComoTrabajador = esChatComoTrabajador,
                        onClick = { onAbrirChat(chat.idChatCita) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltroChatsEtiqueta(
    texto: String,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (activo) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        }
    ) {
        Text(
            text = texto,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

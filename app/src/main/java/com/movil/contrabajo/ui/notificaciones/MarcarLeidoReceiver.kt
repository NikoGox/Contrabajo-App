package com.movil.contrabajo.ui.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.movil.contrabajo.data.remote.ComunicacionesApiClient
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.bearer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver que se dispara cuando el usuario toca "Marcar como leido"
 * en la notificacion de mensaje.
 *
 * Acciones:
 *  1. Cancela la notificacion inmediatamente (UX responsiva).
 *  2. Llama a PATCH /api/chats/{id}/recibidos y /leidos en background.
 *
 * No necesita devolver resultado; la proxima vez que se abra la app los
 * contadores se actualizan via recargar() del ViewModel.
 */
class MarcarLeidoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val idChatCita = intent.getLongExtra(EXTRA_ID_CHAT_CITA, -1L)
        val notifId    = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        if (idChatCita <= 0L) return

        // Cancelar la notificacion de inmediato para dar feedback visual al usuario
        if (notifId >= 0) {
            NotificationManagerCompat.from(context).cancel(notifId)
        }

        // Llamar a las APIs de marca en background (fire-and-forget)
        val token = RemoteSessionStore.obtenerTokenEstatico(context) ?: return
        val api   = ComunicacionesApiClient.api
        val auth  = bearer(token)

        CoroutineScope(Dispatchers.IO).launch {
            runCatching { api.marcarRecibidos(auth, idChatCita).execute() }
            runCatching { api.marcarLeidos(auth, idChatCita).execute() }
        }
    }

    companion object {
        const val ACCION_MARCAR_LEIDO = "com.movil.contrabajo.MARCAR_LEIDO"
        const val EXTRA_ID_CHAT_CITA  = "idChatCita"
        const val EXTRA_NOTIF_ID      = "notifId"
    }
}

package com.movil.contrabajo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.movil.contrabajo.ui.notificaciones.NotificacionesMensajes
import com.movil.contrabajo.ui.ContrabajoApp
import com.movil.contrabajo.ui.theme.ContrabajoTheme

class MainActivity : FragmentActivity() {
    private var chatNotificacionPendienteId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatNotificacionPendienteId = extraerChatIdDesdeIntent(intent)
        NotificacionesMensajes.crearCanalSiNoExiste(this)
        enableEdgeToEdge()
        setContent {
            val solicitarPermisoNotificaciones = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { }
            LaunchedEffect(Unit) {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    solicitarPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            ContrabajoTheme {
                ContrabajoApp(
                    chatNotificacionPendienteId = chatNotificacionPendienteId,
                    onConsumirChatNotificacionPendiente = { chatNotificacionPendienteId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        chatNotificacionPendienteId = extraerChatIdDesdeIntent(intent)
    }

    private fun extraerChatIdDesdeIntent(intent: Intent?): Long? {
        val idChat = intent?.getLongExtra(EXTRA_ID_CHAT_CITA, -1L) ?: -1L
        return idChat.takeIf { it > 0L }
    }

    companion object {
        const val ACCION_ABRIR_CHAT = "com.movil.contrabajo.ABRIR_CHAT"
        const val EXTRA_ID_CHAT_CITA = "extra_id_chat_cita"
    }
}

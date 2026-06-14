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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.movil.contrabajo.ui.notificaciones.NotificacionesMensajes
import com.movil.contrabajo.ui.ContrabajoApp
import com.movil.contrabajo.ui.theme.ContrabajoTheme
import com.movil.contrabajo.ui.theme.ControladorPreferenciasUi
import com.movil.contrabajo.ui.theme.ModoTema

class MainActivity : FragmentActivity() {
    private var chatNotificacionPendienteId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val controladorPreferencias = ControladorPreferenciasUi(this)
        val oscuro = when (controladorPreferencias.estado.modoTema) {
            ModoTema.CLARO -> false
            ModoTema.OSCURO -> true
            ModoTema.SISTEMA -> (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        chatNotificacionPendienteId = extraerChatIdDesdeIntent(intent)
        NotificacionesMensajes.crearCanalSiNoExiste(this)

        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !oscuro
        insetsController.isAppearanceLightNavigationBars = !oscuro
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
            val controladorPreferencias = remember { ControladorPreferenciasUi(this@MainActivity) }
            ContrabajoTheme(controlador = controladorPreferencias) {
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

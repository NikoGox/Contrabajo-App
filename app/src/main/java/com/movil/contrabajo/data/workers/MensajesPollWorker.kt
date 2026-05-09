package com.movil.contrabajo.data.workers

import android.app.ActivityManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.movil.contrabajo.data.remote.ComunicacionesApiClient
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente
import com.movil.contrabajo.ui.notificaciones.NotificacionesMensajes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker periodico que se ejecuta cada 15 minutos (minimo de Android).
 *
 * Cuando la app esta en segundo plano, el WebSocket no recibe mensajes
 * porque no hay un ViewModel activo consumiendo el flow.
 * Este Worker compensa consultando /api/chats y disparando notificaciones
 * locales si hay mensajes sin leer.
 *
 * Cuando la app esta en primer plano, el Worker detecta el estado y omite
 * la notificacion (el WebSocket ya actualizo la UI en tiempo real).
 */
class MensajesPollWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val token = RemoteSessionStore.obtenerTokenEstatico(applicationContext)
            ?: return Result.success() // No hay sesion activa

        val idUsuario = RemoteSessionStore.obtenerIdUsuarioEstatico(applicationContext)
            ?: return Result.success()

        val chats = withContext(Dispatchers.IO) {
            runCatching {
                ComunicacionesApiClient.api
                    .listarChats("Bearer $token")
                    .execute()
                    .body()
            }.getOrNull()
        } ?: return Result.success()

        val totalNoLeidos = chats.sumOf { it.mensajesNoLeidos?.toInt() ?: 0 }
        if (totalNoLeidos <= 0) return Result.success()

        // Solo mostrar notificacion si la app NO esta en primer plano
        if (appEnPrimerPlano()) return Result.success()

        val notificaciones = chats
            .filter { (it.mensajesNoLeidos ?: 0L) > 0L }
            .map { chat ->
                val titulo = "Nuevo mensaje"
                val contenido = chat.ultimoMensaje?.takeIf { it.isNotBlank() }
                    ?: "${chat.mensajesNoLeidos} mensaje(s) sin leer"
                NotificacionMensajePendiente(
                    idMensajeChat  = chat.id ?: 0L,
                    idChatCita     = chat.id ?: 0L,
                    titulo         = titulo,
                    contenido      = contenido
                )
            }

        NotificacionesMensajes.mostrarNotificacionesMensajes(applicationContext, notificaciones)
        return Result.success()
    }

    private fun appEnPrimerPlano(): Boolean {
        val am = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses?.any {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            it.processName == applicationContext.packageName
        } ?: false
    }

    companion object {
        private const val NOMBRE_TRABAJO = "contrabajo_poll_mensajes"

        /** Programa el polling periodico. Llamar despues del login exitoso. */
        fun programar(context: Context) {
            NotificacionesMensajes.crearCanalSiNoExiste(context)

            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val solicitud = PeriodicWorkRequestBuilder<MensajesPollWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(restricciones)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                solicitud
            )
        }

        /** Cancela el polling. Llamar en logout. */
        fun cancelar(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NOMBRE_TRABAJO)
        }
    }
}

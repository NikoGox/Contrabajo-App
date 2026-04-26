package com.movil.contrabajo.ui.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.movil.contrabajo.MainActivity
import com.movil.contrabajo.R
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente

object NotificacionesMensajes {
    private const val CANAL_ID_MENSAJES = "contrabajo_mensajes"
    private const val CANAL_NOMBRE_MENSAJES = "Mensajes"
    private const val CANAL_DESCRIPCION_MENSAJES = "Notificaciones de mensajes y citas"

    fun crearCanalSiNoExiste(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val canalExistente = manager.getNotificationChannel(CANAL_ID_MENSAJES)
        if (canalExistente != null) return
        val canal = NotificationChannel(
            CANAL_ID_MENSAJES,
            CANAL_NOMBRE_MENSAJES,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CANAL_DESCRIPCION_MENSAJES
            enableVibration(true)
        }
        manager.createNotificationChannel(canal)
    }

    fun mostrarNotificacionesMensajes(
        context: Context,
        pendientes: List<NotificacionMensajePendiente>
    ) {
        if (pendientes.isEmpty()) return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        crearCanalSiNoExiste(context)
        val manager = NotificationManagerCompat.from(context)
        pendientes.forEach { notificacion ->
            val intent = Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACCION_ABRIR_CHAT
                putExtra(MainActivity.EXTRA_ID_CHAT_CITA, notificacion.idChatCita)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificacion.idMensajeChat.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val contenido = notificacion.contenido.trim().ifBlank { "Tienes un nuevo mensaje." }
            val builder = NotificationCompat.Builder(context, CANAL_ID_MENSAJES)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(notificacion.titulo.ifBlank { "Nuevo mensaje" })
                .setContentText(contenido)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contenido))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setColor(context.getColor(R.color.teal_700))
            manager.notify(notificacion.idMensajeChat.toInt(), builder.build())
        }
    }
}

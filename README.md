# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.11.0-Pre-Alpha



> <br>• Se completó la migración de HU-09/HU-10/HU-11/HU-12 a backend real con `comunicaciones_api` + `servicios_api`.
> <br>• Se integró chat remoto con listado de conversaciones, historial, vinculación de cita y flujo de mensajes en tiempo real.
> <br>• Se consolidó la máquina de estados de citas con 9 estados (`401..409`) y transiciones por rol cliente/trabajador.
> <br>• Se habilitó WebSocket STOMP nativo Android (`/ws-comunicaciones-native`) con reconexión automática y consumo en `ChatsViewModel`.
> <br>• Se añadió polling de respaldo con WorkManager para mensajes en segundo plano y notificación local por pendientes.
> <br>• Se corrigió `NetworkOnMainThreadException` moviendo operaciones de chat/cita a `Dispatchers.IO`.
> <br>• Se corrigió el parseo de fecha/hora de mensajes (ISO 8601), separadores por día y render correcto de hora en burbujas.
> <br>• Se corrigió el sistema de ticks estilo WhatsApp (`enviado/entregado/leído`) con marcado de recibidos y leídos sincronizado.
> <br>• Se agregó refresco periódico de historial en chat para reflejar entrega/lectura cuando se pierden eventos puntuales de socket.
> <br>• Se incorporó interacción de mantener apretado mensaje (`long-press`) para abrir modal con trazabilidad de estado (enviado/entregado/leído) y fecha/hora.
> <br>• Se consolidaron separadores por día en el historial del chat (`Hoy`, `Ayer`, fecha) para lectura cronológica más clara.
> <br>• Se mantuvo la cabecera de chat con título de servicio + acceso táctil al detalle del servicio asociado.
> <br>• Se mantuvo el resumen de cita como panel expandible/colapsable dentro del chat para priorizar la conversación.
> <br>• Se aseguró modo solo lectura cuando el chat está cerrado (se oculta input de envío y se mantiene historial visible).
> <br>• Se incorporó metadata de cabecera de chat (`tituloServicio`, `usernameTrabajador`, `usernameCliente`) para mostrar contexto real.
> <br>• Se estabilizó la app al volver de segundo plano: reconexión/resync en `ON_RESUME` y recarga de chats con marcado de recibidos.
> <br>• Se introdujo cifrado backend de mensajes nuevos en `comunicaciones_api` (`AES/GCM`, prefijo `ENCv1`) con compatibilidad para históricos en claro.
> <br>• Se corrigió compilación crítica en `PantallaDetalleServicio` por cierre de bloque/alcance de funciones utilitarias.
> <br>• Se ajustó UX de usernames: en chat/listados/notificaciones se muestra `username` sin `@`; en detalle de servicio se mantiene `@usuario`.
> <br>• Se eliminó la galería secundaria y la subida de fotos desde detalle de servicio: queda una sola imagen principal visible por oferta.
> <br>• Se mantuvo compatibilidad funcional de flujos existentes sin migrar mensajes históricos ni romper contratos previos.

---

## Características principales

### Búsqueda de servicios
Explora servicios por categoría y disponibilidad, permitiendo encontrar rápidamente lo que necesitas.

### Comunicación directa
Sistema de chat integrado entre cliente y trabajador para coordinar servicios sin intermediarios.

### Gestión de perfil
Los usuarios pueden registrarse, gestionar su información y administrar sus servicios dentro de la plataforma.

### Experiencia moderna
Interfaz desarrollada en Jetpack Compose, optimizada para una navegación fluida y visualmente clara.

---

## Tecnologías utilizadas

- **Kotlin**
- **Jetpack Compose**
- **SQLite**
- **Navigation Compose**
- **Backend basado en microservicios Java**


<p align="center">
  <b>Contrabajo — Tu instrumento para trabajar</b>
</p>

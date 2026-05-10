# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.12.0-Pre-Alpha



> <br>• Se cerró la iteración N5 con consolidación de HU-11/HU-12/HU-13 sobre backend real.
> <br>• Se habilitó cierre bidireccional de chat (cliente y trabajador) por `idChat` con comportamiento idempotente.
> <br>• Se agregó propagación en tiempo real del cierre de chat a ambas partes mediante evento WS `CHAT_CERRADO`.
> <br>• Se reforzó recarga instantánea del chat activo al cerrar: estado de chat, cita y bloqueo de escritura sin reingresar.
> <br>• Se normalizó reapertura: al contactar tras un chat cerrado, siempre se crea un chat nuevo (no se reutiliza histórico inactivo).
> <br>• Se incorporó trazabilidad de cierre y cambios de cita como mensajes de sistema (`tipo=1`) en el historial.
> <br>• Se ajustó sincronización de citas en vivo para contraparte (creación/transiciones visibles en chat abierto).
> <br>• Se eliminó texto legacy de estado bajo el input del chat para evitar residuos entre conversaciones.
> <br>• Se mantuvo política HU-13: modal de valoración solo para cliente + chat cerrado + cita finalizada/cerrada + sin valoración previa.
> <br>• Se incorporó soporte de servicios eliminados en chats históricos (solo lectura + aviso explícito en chat).
> <br>• Se reforzó detalle de servicio eliminado en modo inhabilitado y sin acciones de contacto nuevas.
> <br>• Se habilitó reporte de contacto con carga remota de tipos de reporte y creación estable desde chat.
> <br>• Se amplió API de valoraciones para consulta por trabajador/cliente con control de token.
> <br>• Se añadió `idOfertaServicio` al DTO de valoraciones para cálculo robusto de rating por servicio.
> <br>• Se corrigió promedio de estrellas por servicio en marketplace/detalle (no solo promedio global del trabajador).
> <br>• Se habilitó visibilidad de valoraciones de terceros también para clientes y trabajadores al navegar tarjetas/servicios.
> <br>• En pantalla de valoraciones por servicio se muestran estado (activo/eliminado) y promedio en estrellas por cada servicio.
> <br>• Se validó compilación de app y microservicios impactados durante la iteración.

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

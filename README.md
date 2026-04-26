# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.5.0-Pre-Alpha



> <br>• Se consolidó un módulo de mensajería contextual por servicio, donde cada conversación queda ligada a una publicación específica.
> <br>• Se incorporó confirmación previa al inicio de conversación, reforzando intención de contacto antes de abrir chat.
> <br>• Se implementó flujo de cita desde chat con coordinación por roles (cliente/trabajador) y acciones diferenciadas según etapa.
> <br>• Se habilitó vista dedicada de cita con estado, servicio asociado, fechas clave y controles operativos según el rol activo.
> <br>• Se formalizó una máquina de estados de cita completa para negociación y ejecución del servicio: pendiente, handshake, comenzando, en proceso, finalizando, finalizado, cancelado y cerrado.
> <br>• Se añadió estado de negociación rechazada (409) para mantener trazabilidad en la misma cita sin perder historial ni contexto.
> <br>• Se agregó capacidad de reenvío de propuesta sobre una cita rechazada, permitiendo negociación continua sin recrear el proceso completo.
> <br>• Se integró modal de creación de cita con fecha y hora obligatorias, validación temporal (solo futuro) y comentario ampliado para acuerdos.
> <br>• Se implementaron estados de mensaje tipo mensajería moderna (enviado, entregado, leído) con persistencia local y lectura contextual.
> <br>• Se habilitaron notificaciones nativas del teléfono por mensaje individual, con deep-link directo al chat correspondiente.
> <br>• Se enriqueció el contenido de notificación con formato contextual: servicio + usuario emisor + texto del mensaje.
> <br>• Se convirtió el resumen de cita dentro del chat en panel desplegable/colapsable para optimizar espacio de lectura conversacional.
> <br>• Se incorporó organización temporal del chat con separadores por día (Hoy, Ayer, fecha) y apertura automática en los últimos mensajes.
> <br>• Se reforzó la identidad visual del módulo de chat con contraste por rol, burbujas más legibles y diferenciación de conversaciones del trabajador.
> <br>• Se añadió cabecera de chat interactiva que permite abrir directamente el detalle del servicio asociado desde la misma conversación.
> <br>• Se consolidó una matriz de estados transversal en formatos CSV/XLSX para estandarizar códigos operativos por dominio (usuarios, servicios, comunicaciones, citas/otros).

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

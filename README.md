# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.6.0-Pre-Alpha



> <br>• Se implementó edición real de servicios por id_oferta_servicio para evitar sobreescrituras incorrectas.
> <br>• Se aplicó la regla de negocio 3/1 para trabajadores no premium: máximo 3 servicios y 1 activo.
> <br>• Se bloqueó la activación de servicios cuando no hay cupo disponible (1/1 activo).
> <br>• Se rediseñó Mis servicios con métricas operativas X/1 y Y/3 más mini listado por servicio.
> <br>• Se reemplazó el switch de disponibilidad por estado En Curso cuando existe cita en EN_PROCESO.
> <br>• Se agregó el 4º paso del registro con 2 preguntas de seguridad (sin repetición) y respuestas ocultables.
> <br>• Se incorporó recuperación de cuenta por usuario/correo con validación de 2 respuestas y reset de contraseña.
> <br>• Se ajustó el registro para evitar el flash de errores rojos al completar alta exitosa.
> <br>• Se añadieron filtros de chats por categoría (contacto / trabajador) con modo combinado mostrando todos.
> <br>• Se retiró el texto Expandir/Contraer en chat detalle, manteniendo interacción por flecha.
> <br>• Se implementó modal de valoración al cerrar chat (1 a 5 estrellas + comentario opcional), una vez por chat.
> <br>• Se habilitó pantalla de valoraciones por servicio incluyendo @usuario y fecha de finalización de cita.
> <br>• Se estableció eliminación lógica de publicaciones para preservar trazabilidad de chats y valoraciones históricas.
> <br>• Se añadió protección para cambiar preguntas de seguridad con biometría/credencial y fallback a contraseña de cuenta.

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

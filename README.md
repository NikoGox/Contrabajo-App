# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.9.0-Pre-Alpha



> <br>• Se consolidó el modo online-first en front para cuenta/perfil/seguridad/ubicación, eliminando fallback offline operativo en estos flujos.
> <br>• Se integró precarga de perfil tras login para reducir cargas intermedias y mantener datos consistentes entre pantallas.
> <br>• Se conectó validación previa de disponibilidad en registro para RUN, username y correo contra backend antes del alta final.
> <br>• Se reforzó edición de perfil para sincronizar siempre desde backend y reflejar cambios de correo/teléfono con recarga posterior.
> <br>• Se mejoró verificación de trabajador con cierre de sesión controlado y retorno a login tras validación exitosa.
> <br>• Se ajustó verificación de RUN para admitir formatos reales de 7 u 8 dígitos, conservando DV y validación formal.
> <br>• Se volvió inmutable el RUN/DV de cuenta en pantalla de verificación (solo lectura), usando cédula como dato de contraste.
> <br>• Se conectó ubicación a backend con recarga activa de dirección/comuna y sincronización de coordenadas persistidas.
> <br>• Se corrigió el modal de comuna para mostrar catálogo remoto y manejar estados de carga/error sin romper la UX.
> <br>• Se rediseñó seguridad de cuenta para editar preguntas por ítem sin mostrar respuestas en pantalla.
> <br>• Se conectó lectura y actualización de preguntas de seguridad con endpoints autenticados de perfil.
> <br>• Se limpió estado sensible al cerrar sesión para evitar residuos entre usuarios (verificación, perfil y seguridad).
> <br>• Se mantuvo compatibilidad visual con la estética actual de Contrabajo durante la migración a backend.

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

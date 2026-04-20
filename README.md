# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.4.1-Pre-Alpha



> <br>• Iteración de ajuste fino UX/UI sobre `0.4-Pre-Alpha`, enfocada en estabilidad de navegación y fluidez visual en dispositivo real.
> <br>• Se agregó `SplashActivity` personalizada con `ct_icon`, barra de progreso y transición inicial para mejorar la entrada a la app.
> <br>• Se actualizó inicio de sesión automático (recordarme) con carga controlada para evitar saltos bruscos al entrar al shell principal.
> <br>• Se rehízo topbar de búsqueda en Principal con un solo contenedor estable (sin doble borde), glow azul/cyan/verde y cierre por toque fuera.
> <br>• Se normalizó cancelación de búsqueda al tocar tarjeta/rango/filtros, manteniendo el texto escrito para continuidad de uso.
> <br>• Se corrigió long-press en tarjetas de exploración para evitar solapes/deformaciones y mantener el grid estable.
> <br>• Se estabilizó swipe horizontal en detalle con enfoque de 3 tarjetas vivas (anterior/actual/siguiente) y menor tirón visual.
> <br>• Se mantuvo vinculación entre Exploración y Detalle (mismo orden/dataset/filtros al abrir publicaciones).
> <br>• Se optimizó render del mapa en detalle para reducir parpadeo e invalidaciones innecesarias durante gestos.
> <br>• Se ajustó transición de ruta Servicio: entra desde abajo y sale hacia abajo con desplazamiento medio + fade.
> <br>• Se eliminó UI bloqueante al abrir detalle (sin oscurecer, sin spinner, sin cinta/texto), dejando precarga silenciosa.
> <br>• Se retiró overlay visual interno de "Preparando tarjetas...", manteniendo solo bloqueo táctil silencioso cuando falta precarga.
> <br>• Se homologó la geometría de topbars entre Principal/Detalle/Ajustes y se desactivó animación defectuosa en Ajustes.

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
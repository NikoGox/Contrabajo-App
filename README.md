# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.3-Pre-Alpha



> <br>• Se ajustó el registro: RUN (8 dígitos) + DV, máscara visual de RUN/teléfono, fecha de nacimiento por día/mes/año.
> <br>• Se reforzó la validación de RUN y la duplicidad RUN+DV en la capa local.
> <br>• Se implementó el login con opción "Recordarme" y expiración de sesión al desactivarla (en flujo local de debugging).
> <br>• Se incorporó un menú de ajustes con acceso mediante ícono de tuerca y secciones: Seguridad y verificación, Cuenta y Ubicación.
> <br>• Se implementó la verificación de trabajador en ajustes mediante RUN y número de documento, definida temporalmente como activación automática diferida (3 minutos) en flujo local.
> <br>• Se agregó la configuración de 3 preguntas de seguridad con modal, guardado local y opción de mostrar/ocultar respuestas.
> <br>• Se rediseñó el detalle de servicio con scroll interno, botón flotante de contacto, barra superior alineada y gesto lateral tipo tarjetas.
> <br>• Se agregó una nueva pantalla para seleccionar ubicación, obteniendo coordenadas y permitiendo también ingresar la dirección manualmente.
> <br>• Se desacopló visualmente el rango, mapa y botón para evitar la distorsión del mapa al mover el slider.
> <br>• Se rediseñó el bloque "Dirección" para edición mediante modal: Región Metropolitana bloqueada, comuna mediante combobox (comunas RM), calle/número/detalle editables.
> <br>• Se integró OpenStreetMap embebido (OSMDroid) en ajustes y en detalle de servicio, con marcador y círculo de rango.
> <br>• Se agregó el guardado explícito de ubicación y rango (0–100 km) en la base de datos local por usuario.
> <br>• Se conectó la pantalla principal para leer el rango guardado desde la BD y mostrarlo en "Rango de búsqueda actual".
> <br>• Se activó el refresco de la pantalla principal al volver (`ON_RESUME`) para reflejar cambios de ajustes sin reiniciar la app.
> <br>• Se aplicó filtro local de publicaciones por rango cuando existen coordenadas del usuario.
> <br>• Se estabilizó la persistencia de la foto del servicio copiando la URI al almacenamiento interno para evitar pérdida de imagen entre vistas.
> <br>• Se movió el botón "Cerrar sesión" al menu de ajustes (blanco con borde y texto rojo).





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
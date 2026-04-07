# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.2-Pre-Alpha



> <br>• Ahora las publicaciones se muestran con foto y en forma de cuadricula en la pantalla de marketplace.
> <br>• Se agregaron animaciones a el navbar.
> <br>• Se agregaron nuevas animaciones en general para toda la la aplicación.
> <br>• Se modificó el navbar para acercarlo al diseño esperado en el prototipo.
> <br>• Se rehizo la direccion visual de la app para acercarla al mockup actual de Contrabajo.
> <br>• Se reemplazaron componentes base por componentes de producto reutilizables para botones, inputs, indicadores, tarjetas y navbar.
> <br>• Se rediseño la pantalla principal con una publicacion destacada mas cercana a la propuesta del proyecto.
> <br>• Se mejoro la experiencia visual de chats, perfil y detalle de servicio para dar continuidad al resto de la app.
> <br>• Se mantuvieron sin ruptura las rutas de navegacion, los repositorios locales y el modelo de datos SQLite.
> <br>• Se ajusto la paleta y el sistema base para acercar mas la app al prototipo original, con una lectura mas limpia, directa y cercana al mockup.
> <br>• Se retocaron `PantallaInicial`, `PantallaLogin` y el registro en dos pasos para alinearlos mejor con la composicion del prototipo.• 
> <br>• Se consolidó la base MVVM con navegación principal persistente y shell estable para perfil, marketplace y mensajes.• 
> <br>• Se implementó el flujo RF-02 en local con SQLite: creación/edición de servicio, disponibilidad visible/oculta y consulta de publicaciones en grilla.• 
> <br>• Se separó la edición/creación de servicio en una pantalla dedicada (sin navbar), reduciendo carga visual y lógica en perfil.• 
> <br>• Se simplificó el modelo de servicio para esta etapa (precio texto libre y una sola descripción principal), alineando mejor la UX real.• 
> <br>• Se reforzó el dataset demo con múltiples usuarios/publicaciones y más categorías para validar scroll, filtros visuales y comportamiento de tarjetas.• 
> <br>• Se integró recarga por gesto en el marketplace y se afinó la experiencia de desplazamiento para refrescar resultados.
> <br>• Se corrigieron insets y solapes con barra de estado, mejorando legibilidad y jerarquía visual en todas las pantallas.
> <br>• Se recuperó el acabado visual del navbar flotante con bordes/transparencia sutil sin reintroducir el artefacto de fondo.
> <br>• Se añadió captura de foto desde cámara para servicios (permiso, FileProvider, URI temporal segura) junto al selector de galería.
> <br>• Se refinaron componentes clave como `LogoContrabajo`, `CampoContrabajo`, `TarjetaOfertaServicio`, `ChipAccion` y `BarraInferior`.

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
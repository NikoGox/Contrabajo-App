# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.4-Pre-Alpha



> <br>• Se corrigió navegación lateral principal entre Perfil <-> Principal <-> Chats, eliminando desincronización de dirección y saltos en extremos.
> <br>• Se mantuvo arquitectura por pantallas y se suavizaron transiciones en Ajustes para evitar animaciones agresivas al entrar/salir de subpantallas.
> <br>• Se rediseñó gesto lateral en detalle para navegación tipo cartas (browsing izquierda/derecha), con stack visible y animaciones más fluidas.
> <br>• Se agregó precarga de imágenes adyacentes para reducir tirones visuales en cambio de tarjetas.
> <br>• Se ajustó comportamiento de back en detalle: si hay scroll interno, vuelve primero arriba; luego permite salir de la pantalla.
> <br>• Se ajustó CTA flotante (contactar/editar) para ocultarse al bajar en lectura y reaparecer al subir, mejorando lectura de contenido.
> <br>• En detalle, para publicación propia se muestra acción de editar; para publicaciones de terceros se mantiene acción de contactar.
> <br>• Se incorporó resumen de trabajador en detalle con foto de perfil, verificado y `@username`, quitando estrellas en ese bloque según definición UX.
> <br>• Se reforzó HU-01 en verificación trabajador: RUN + DV en línea, RUN con formato visual `xx.xxx.xxx`, documento con formato `xxx.xxx.xxx` y validación exacta de 9 dígitos.
> <br>• Se mejoró validación por capas (UI + ViewModel + repositorio + DB) para evitar envío de verificación inválida.
> <br>• Se separó modelo de rangos por usuario: `rango_busqueda_m` y `rango_disponibilidad_m`, ambos persistidos en SQLite (metros).
> <br>• Se acotó rango máximo operacional a 50 km en sliders de búsqueda/disponibilidad.
> <br>• Se dejó filtro de match por distancia real del buscador, con tolerancia de borde para casos límite cercanos al umbral.
> <br>• Se incorporó estado vacío guiado: "Obtén tu ubicación en Ajustes > Ubicación > Obtener ubicación" para cuentas sin coordenadas útiles.
> <br>• Se ajustó pull-to-refresh para que funcione también en estado vacío (sin depender del grid con ítems).
> <br>• Se integró captura y guardado de ubicación del dispositivo para pruebas reales/FakeGPS, con feedback visual por toast en acciones clave.
> <br>• Se mejoró OpenStreetMap embebido en ajustes/detalle, con pin azul centrado y visual de rango consistente.
> <br>• Se incorporó modal de filtros/orden para marketplace: categoría, tipo de precio, solo verificados y orden por A->Z / fecha.
> <br>• Se agregó soporte de fecha de publicación en tarjetas y detalle para ordenar y dar contexto temporal.
> <br>• Se implementó HU-03 con precio estructurado en datos: `tipo_precio` + `monto_base`, generando `precio_texto` derivado y uniforme.
> <br>• Tipos de precio soportados: Fijo, Por hora, Desde, Contactar para saber precio; con validación de monto entre 1 y 10.000.000 cuando aplica.
> <br>• Se mejoró campo de monto para mostrar `$` dentro del input (visual), manteniendo guardado numérico limpio.
> <br>• Se cargaron fotos remotas estables para publicaciones demo y se mantuvo fallback local ante error de imagen.
> <br>• Se reforzó tarjeta compacta de exploración: título/precio en una línea, marquee por long-press y bloqueo de scroll vertical durante lectura extendida.
> <br>• Se rediseñó resplandor de borde en long-press con loop largo continuo (20s) para transición suave sin corte visual.
> <br>• Se ajustó buscador en topbar para mantener mismo contenedor y tamaño, transicionando a estado input blanco con borde glow (morado/cyan/azul).
> <br>• Se incorporó overlay de carga reutilizable (fondo oscurecido + spinner) para acciones críticas y preparación de integración backend.
> <br>• Se mantuvo persistencia local frontend-first con migración SQLite y reset controlado de demo data cuando corresponde.





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
# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.10.0-Pre-Alpha



> <br>• Se integró el módulo de servicios con el backend real, reemplazando el repositorio local para marketplace, detalle y gestión de ofertas.
> <br>• Se habilitaron controles dedicados para activar y desactivar disponibilidad de oferta sin requerir edición completa del servicio.
> <br>• Se conectó el rango de búsqueda al backend; ambos rangos (búsqueda y disponibilidad) persisten de forma independiente sin sobreescribirse.
> <br>• Se agregó migración de arranque en usuarios_api para crear las columnas de rango automáticamente en bases existentes.
> <br>• Se corrigió que el marketplace se vaciara al no tener coordenadas cargadas; ahora muestra ofertas de todas formas.
> <br>• Se corrigieron las llamadas de red para ejecutarse fuera del hilo principal, eliminando crashes en dispositivos reales.
> <br>• Se estabilizó la sesión tras verificación de trabajador para no borrar el token ante errores transitorios de red.
> <br>• Se corrigió el flujo de verificación para cerrar sesión y renovar el JWT con el rol correcto de trabajador.
> <br>• Se corrigió que los servicios nuevos partían activos; ahora se crean desactivados y el trabajador los publica manualmente.
> <br>• Se corrigió que las tarjetas mostraban nombres genéricos en lugar del nombre real del trabajador.
> <br>• Se corrigió la causa raíz del filtro de rango: el valor real del trabajador no se leía y todos los servicios usaban el fallback de 20 km.
> <br>• Se corrigió la fórmula de intersección de círculos en el marketplace para considerar el rango del trabajador y no solo el del cliente.
> <br>• Se corrigió que el trabajador veía su propio servicio a 37 km en lugar de 0 km.
> <br>• Se protegió la privacidad de dirección: las ofertas exponen solo comuna y región; la dirección completa se muestra solo al propietario.
> <br>• Se agregó mini mapa con círculo al modal de rango de búsqueda para visualizar el área en tiempo real.
> <br>• Se aplicó un mínimo visual de 1.000 m en los mapas de rango de disponibilidad para no revelar la ubicación exacta del trabajador.
> <br>• Se ocultaron el rango de disponibilidad y su mapa en ajustes de ubicación para usuarios con perfil cliente.
> <br>• Se corrigió la etiqueta de tipo de cuenta: USUARIO_BASE ahora se muestra como "Cliente".

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

# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.13.0-Pre-Alpha


> <br>• Se corrigieron los bloques de información en Perfil y Ajustes para que el correo use una sola fila y no se superponga con sus etiquetas.
> <br>• Se agregó un botón de cerrar sesión en la parte inferior del perfil y también en la pantalla de cuenta de Ajustes.
> <br>• Se corrigió el texto de acceso para español: "¿No tienes cuenta? Regístrate".
> <br>• Se restauró el scroll en el flujo de creación de cuenta para evitar que el contenido se corte en pantallas pequeñas.
> <br>• Se reforzó el comportamiento de scroll en pantallas clave como inicio, login y registro para que los mensajes de error no tapen toda la vista.
> <br>• Se revisaron los puntos más sensibles de superposición visual para mantener la lectura clara en textos largos y campos de perfil.

---

### ❚❙❘ VERSIÓN 0.13.1-Pre-Alpha


> <br>• Se corrigieron los bloques de información en Perfil y Ajustes para que el correo use una sola fila y no se superponga con sus etiquetas.
> <br>• Se agregó un botón de cerrar sesión en la parte inferior del perfil y también en la pantalla de cuenta de Ajustes.
> <br>• Se corrigió el texto de acceso para español: "¿No tienes cuenta? Regístrate".
> <br>• Se restauró el scroll en el flujo de creación de cuenta para evitar que el contenido se corte en pantallas pequeñas.
> <br>• Se reforzó el comportamiento de scroll en pantallas clave como inicio, login y registro para que los mensajes de error no tapen toda la vista.
> <br>• Se revisaron los puntos más sensibles de superposición visual para mantener la lectura clara en textos largos y campos de perfil.

---

### ❚❙❘ VERSIÓN 0.13.0-Pre-Alpha


> <br>• La pantalla de información de cuenta fue rediseñada: datos agrupados con iconos, tarjetas de contacto lado a lado y sección de identificación con jerarquía visual clara.
> <br>• El perfil muestra el prefijo +56 en gris antes del número de teléfono.
> <br>• Se eliminaron RUN y Dirección del bloque visible en la pantalla de Perfil; solo se muestran Correo y Teléfono.
> <br>• Se reemplazó el pull-to-refresh por la API estándar de Material3 en PantallaPrincipal y PantallaPerfil: comportamiento más fluido, indicador centrado con fondo blanco y flecha en color primario.
> <br>• Al abrir el chat, si los datos aún no cargaron se muestra un skeleton animado con la forma de las burbujas en lugar del mensaje de error incorrecto.
> <br>• Se corrigieron todas las tildes faltantes en los textos visibles de la app (más de 90 cadenas en 11 archivos de pantallas).
> <br>• Si el servidor está caído al iniciar la app, se muestra una pantalla de error con botones Reintentar y Cerrar sesión en lugar de entrar al home con pantallas vacías.
> <br>• Si la sesión fue invalidada desde otro dispositivo, la app detecta el código 401 automáticamente y cierra sesión de forma ordenada.
> <br>• La pantalla de lista de chats fue completamente rediseñada con estética moderna tipo WhatsApp/Messenger: buscador integrado en el contenedor, selector de tipo animado con pill deslizante, filas con avatar en degradado, badge de no leídos y chip de rol. Los clientes no ven el selector de tipo.

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

# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.8.0-Pre-Alpha



> <br>• Se inició la integración real del frontend con el backend de Contrabajo para la épica de Acceso y Cuenta.
> <br>• Se descartó el selector Online/Offline y se definió una migración progresiva de funcionalidades locales hacia backend.
> <br>• Se conectó el registro de usuarios con el backend, incluyendo datos personales, cuenta, dirección y preguntas de seguridad.
> <br>• Se habilitó login contra backend usando nombre de usuario como identificador principal.
> <br>• Se incorporó sesión persistente con token JWT y validación de sesión al reabrir la app.
> <br>• Se conectó el cierre de sesión con backend y limpieza local de la sesión activa.
> <br>• Se conectó recuperación de cuenta con validación de preguntas de seguridad y cambio de contraseña según los endpoints disponibles.
> <br>• Se conectó lectura de perfil desde backend para mantener la identidad de cuenta integrada.
> <br>• Se habilitó edición de correo y teléfono de perfil con backend, reflejando en pantalla los datos realmente guardados.
> <br>• Se mantuvo el username visible pero no editable hasta que backend soporte edición de nombre de usuario.
> <br>• Se dejó foto de perfil como dato local temporal porque backend aún no expone endpoint remoto para imagen de perfil.
> <br>• Se corrigió el registro de RUN para aceptar personas con RUN de 7 u 8 dígitos antes del DV.
> <br>• Se corrigió el formato visual de RUN para representar correctamente casos como 1.234.567 y 12.345.678.
> <br>• Se reforzó la contraseña de registro con mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.
> <br>• Se agregó texto de ayuda bajo confirmar contraseña para explicar los requisitos antes de registrar.
> <br>• Se configuró el cliente REST Android con Retrofit, OkHttp y Gson apuntando al backend local desde emulador.
> <br>• Se mantuvieron servicios, marketplace, chats y reportes en repositorios locales/legacy para integrarlos en iteraciones siguientes.

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

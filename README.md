# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios


### ❚❙❘ VERSIÓN 0.10.1-Pre-Alpha



> <br>• Se implementó el flujo de verificación como trabajador con OCR real: bienvenida, captura, procesando y resultado.
> <br>• Pantalla de bienvenida con ilustración estilizada de cédula e instrucciones en tres pasos.
> <br>• Pantalla de captura con vista de cámara en vivo y marco guía animado sobre overlay oscuro.
> <br>• Extracción automática de RUT y N° de documento del carnet usando ML Kit Text Recognition on-device.
> <br>• Rotación de imagen corregida: se pasa el ángulo real del sensor a ML Kit para leer texto en cualquier orientación.
> <br>• Extractor de RUT con cuatro patrones (con puntos, sin puntos, con espacios, zona MRZ) y normalización de caracteres OCR confundibles.
> <br>• N° de documento reconocido en formato XXX.XXX.XXX propio de la cédula chilena, con fallback a dígitos consecutivos.
> <br>• Diagnóstico incluido en el error de RUT no encontrado: muestra el texto reconocido para facilitar depuración.
> <br>• Validación local del RUT antes de consultar el backend; pantalla de resultado diferenciada para éxito y rechazo.
> <br>• Corrección de compatibilidad con dispositivos Android de 16 KB de tamaño de página (useLegacyPackaging = false).

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

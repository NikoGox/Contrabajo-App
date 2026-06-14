# Contrabajo App Movil

*Conexión rápida entre clientes y prestadores de servicios.*

<p align="left">
  <img src="./docs/ct-icon.jpg" alt="Contrabajo App" width="200px">
</p>

Contrabajo es una aplicación móvil orientada a facilitar el contacto entre clientes y trabajadores independientes, permitiendo descubrir, comunicar y coordinar servicios de manera rápida y sencilla.

La plataforma busca reducir la fricción en la búsqueda de servicios técnicos, entregando una experiencia clara, directa y moderna.

---

## Últimos cambios

### ❚❙❘ VERSIÓN 0.16.0-Alpha

- Primera versión Alpha del producto.
- Modo Oscuro con switch en Ajustes, preferencia guardada, soporte para daltonismo (Estándar, Rojo-Verde, Azul-Amarillo, Alto contraste) basadas en Okabe-Ito.
- Tamaño de letra ajustable (Pequeño/Normal/Grande/Muy grande) con vista previa.
- Menú de Preferencias habilitado para opciones de accesibilidad.
- Navbar flotante unificado con colores semánticos de `LocalColoresContrabajo`.
- Botón "Cerrar sesión" migrado: sólido rojo en modo oscuro, OutlinedButton con borde rojo en modo claro.
- Auditoría de colores en toda la app. `PantallaBase` aplica siempre `background` del tema.
- Iconos no seleccionados del navbar ajustados en paleta oscura (cyan claro).
- Doble padding inferior en Perfil eliminado con `respetarNavegacionInferior = false`.
- Barra de navegación de Android configurada para modo oscuro con `SystemBarStyle.dark()`.
- Barra de estado con íconos claros en modo oscuro. Notificaciones con iconos correctos por tema y actualización en tiempo real.
- "Mis servicios" en Perfil: switch de disponibilidad movido a fila inferior.
- Skeleton de carga (shimmer animado) en Detalle de Servicio.
- Tarjetas del marketplace con borde `BorderStroke` y elevación mejorada. Fondo claro cambiado a gris neutro.
- Menú Premium rediseñado: tarjetas "Servicios"/"Valoración" eliminadas, botones apilados, topbar con degradado estático, banner simplificado, funciones próximas en estado gris, métricas de ingreso/ticket eliminadas, ruta renombrada a `PremiumEstadisticas`.
- Gráficos Premium: dona rotatoria (valoraciones) y funnel vertical (conversión) con animaciones.
- Colores premium restaurados a paleta turquesa. Bordes redondeados restaurados en topbar y encabezados.
- Historial de contactos Premium: skeleton, tarjeta con badge, estrellas doradas, valoración en cursiva y `comentarioValoracion` desde backend.
- Lista de chats: nombre real del contacto (API), avatar con imagen del servicio, título como segunda línea, cabecera fija, lista scrollable con fade superior.
- Iconos de volver/opciones en chat ajustados a `primary` para legibilidad en modo oscuro.
- Cita de servicio: topbar `primary`, skeleton shimmer, actualización inmediata por transición o WebSocket.
- Topbar de PantallaDetalleChat con altura preservada durante skeleton.
- Nombre real del contacto en chats (nombre + apellido). Checks de visto eliminados.
- Modal de valoración centrado. Título "Conversación" oculto durante skeleton.
- Ubicación en Ajustes: coordenadas como overlay en el mapa, modal completamente expandido.
- Navbar flotante con padding dinámico con `WindowInsets.navigationBars`, Box opaco en 3 pantallas.
- Fades verticales en PantallaPrincipal (28dp) y PantallaPerfil (56dp) con renderizado corregido.
- PantallaChats con cabecera fija y lista scrollable independiente.
- `espaciadoVertical` agregado a `PantallaBase`. Espaciado en PantallaPrincipal reducido de 18dp a 12dp.
- Login: botón "Comenzar" en bienvenida, inputs vacíos, "¿Olvidaste tu cuenta?" subrayado, "Volver al inicio de sesión" en recuperar cuenta.
- Login rediseñado: fondo degradado, logo real, ortografía corregida, animación slide-up/down.
- Pantalla Recuperar Cuenta rediseñada al mismo estilo.
- Empty state del marketplace con jerarquía visual mejorada (título grande, subtítulo pequeño).
- Dirección en Ubicación rediseñada: ícono, dirección estructurada, botón "Editar dirección".
- Pantalla de Bienvenida rediseñada al mismo estilo que Login (fondo degradado, logo real, tarjeta semitransparente).
- Menú de cuentas de prueba con z-index corregido.
- Animación de Login al volver corregida (solo slide down).
- Rango de disponibilidad rediseñado con badge de valor.
- Verificación de backend al iniciar: si el backend no responde, se muestra modal de servicio no disponible.
- Monitor de conexión en tiempo real cada 5 segundos en pantallas principales.
- Modal de intermitencias a los 20s sin conexión. Cierre automático a los 40s totales.
- `ComboContrabajo` reutilizable en `ComponentesBase.kt` para selects de comuna. Reemplazado en registro, ajustes y reportar servicio.
- Validación de correo con `@` y `.` en registro y edición de perfil (6 puntos: UI, repo local, repo remoto, VM, repo local edición, repo remoto edición).
- Perfil editar: toast "Perfil actualizado correctamente" al guardar y navegación atrás automática. Texto inline eliminado.
- Chats: flash de datos previos al cambiar de chat corregido. `abrirChat()` limpia estado síncronamente antes de carga async.
- Ajustes: botón "Obtener ubicación" eliminado del mapa, solo ícono Refresh + "Guardar".
- Marketplace: texto del subtitle en empty state centrado horizontalmente.
- Login: texto "¿No tienes cuenta? Regístrate" eliminado.

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

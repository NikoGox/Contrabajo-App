# Borrador para CHANGELOG

Este bloque esta listo para copiar/pegar en `Contrabajo/CHANGELOG.txt` como nueva version superior.

```text
VERSION 0.3-Pre-Alpha
───────
├─• Se ajusto HU-01 con registro mas realista: RUN (8 digitos) + DV, mascara visual de RUN/telefono, fecha de nacimiento por dia/mes/anio y tipo de perfil inicial fijo en usuario base.
├─• Se reforzo la validacion de RUN (modulo 11) y duplicidad RUN+DV en capa local.
├─• Se implemento login con opcion recordarme y expiracion de sesion al desactivar recordarme (en flujo local de debugging).
├─• Se incorporo menu de ajustes con acceso por icono de tuerca y secciones: Seguridad y verificacion, Cuenta y Ubicacion.
├─• Se implemento verificacion a trabajador desde ajustes con RUN+DV+numero de documento y activacion automatica diferida (3 minutos) en flujo local.
├─• Se agrego configuracion de 3 preguntas de seguridad con modal, guardado local y opcion de mostrar/ocultar respuesta.
├─• Se rediseno detalle de servicio (HU-05) con scroll interno, boton flotante de contacto, barra superior alineada y gesto lateral tipo tarjetas.
├─• Se corrigio la direccion de gestos: deslizar a la izquierda avanza, deslizar a la derecha retrocede.
├─• Se integro OpenStreetMap embebido (OSMDroid) en ajustes y en detalle de servicio con marcador y circulo de rango.
├─• Se desacoplo visualmente rango/mapa/boton para evitar distorsion del mapa al mover el slider.
├─• Se rediseno el bloque Direccion para edicion por modal: Region Metropolitana bloqueada, comuna por combobox (comunas RM), calle/numero/detalle editables.
├─• Se agrego guardado explicito de ubicacion y rango (0-100 km) en base de datos local por usuario.
├─• Se conecto pantalla principal para leer el rango guardado desde BD y mostrarlo en "Rango de busqueda actual".
├─• Se activo refresco de principal al volver (`ON_RESUME`) para reflejar cambios de ajustes sin reiniciar la app.
├─• Se aplico filtro local de publicaciones por rango cuando existen coordenadas del usuario.
├─• Se estabilizo persistencia de foto de servicio copiando URI a almacenamiento interno para evitar perdida de imagen entre vistas.
├─• Se agrego boton "Cerrar sesion" en ajustes (blanco con borde/texto rojo) segun mockup.
└─• Se actualizo version activa del proyecto/frontend a `0.3-Pre-Alpha`.

->>> Integrar backend para regiones/comunas dinamicas y reemplazar listas locales.
->>> Integrar OCR para validacion documental y flujo formal de verificacion.
->>> Reemplazar almacenamiento de contrasena en texto plano por hash seguro al integrar backend.
->>> Conectar boton de contacto de detalle a chat real y cerrar flujo completo HU-05/HU-09.
```


# Cierre Iteracion HU-01 a HU-05

## Metadatos
- `fecha_cierre`: `2026-04-18`
- `hora_cierre`: `05:09` (America/Santiago)
- `version_cierre`: `0.3-Pre-Alpha`
- `estado_iteracion`: `cerrada`
- `alcance`: `frontend-first con persistencia local SQLite para preparar integracion backend`

## Objetivo de la iteracion
- Cerrar el bloque HU-01 a HU-05 con comportamiento funcional en frontend.
- Dejar reglas de datos/local storage consistentes para usar luego como base del backend.

## Resultado general
- Iteracion cerrada con build funcional en entorno local.
- Se consolidaron flujos de registro, seguridad, verificacion de trabajador, ubicacion/rango, marketplace y detalle de servicio.
- Se actualizo version del proyecto a `0.3-Pre-Alpha`.

## Version actualizada
- `app/build.gradle.kts`
  - `versionName = "0.3-Pre-Alpha"`
- `PantallaInicial`
  - texto visible: `Version activa v0.3-pre-alpha`

## Cierre por historias de usuario

### HU-01 (registro, login, roles, seguridad)
- Registro en 2 pasos con:
  - RUN de 8 digitos + DV.
  - Mascara visual de RUN y telefono.
  - Fecha nacimiento por `Dia / Mes / Año`.
  - Tipo de perfil inicial fijo: usuario base (`1`).
- Validacion RUN (modulo 11) y duplicidad RUN+DV.
- Login con opcion `recordarme` y expiracion de sesion cuando no esta activo.
- Menu de ajustes con:
  - Seguridad y verificacion.
  - Cuenta.
  - Ubicacion.
- Verificacion de trabajador:
  - RUN + DV + numero documento.
  - validacion contra RUN registrado.
  - promocion automatica a trabajador en 3 minutos (simulacion local).
- Preguntas de seguridad:
  - 3 items configurables.
  - modal de edicion.
  - mostrar/ocultar respuesta.

### HU-02 / HU-03 / HU-04 (publicaciones, tarjetas, visualizacion)
- Publicaciones restringidas a trabajador/premium.
- Tarjeta marketplace compacta con imagen completa, titulo, precio, rating con estrellas fraccionadas y verificado.
- Ajustes visuales de layout flotante y continuidad con mockup.
- Persistencia robusta de foto de servicio en almacenamiento interno para evitar perdida al navegar.

### HU-05 (detalle de publicacion y navegacion por tarjetas)
- Pantalla de detalle redisenada:
  - foto grande
  - titulo
  - precio
  - distancia aproximada
  - descripcion
  - datos del trabajador y categoria
  - boton flotante de contacto (placeholder)
- Navegacion lateral entre publicaciones corregida:
  - izquierda avanza
  - derecha retrocede
- Barra superior de detalle alineada con la de inicio.
- Scroll mantenido dentro del contenedor principal de detalle.

## Ubicacion y rango (listo para backend)
- Integracion OSM embebida en ajustes y detalle (OSMDroid).
- Slider de rango `0..100 km` con visualizacion de circulo en mapa.
- Direccion por modal:
  - region fija `Region Metropolitana` (bloqueada por ahora)
  - comuna por combobox (comunas RM)
  - calle/numero/detalle editables
- Guardado explicito de `ubicacion + rango` en BD local.
- Principal consume rango guardado y lo refleja en encabezado.
- Principal filtra ofertas por rango cuando hay coordenadas del usuario.

## Cambios en base de datos y campos (para backend/MD final)

### Tabla consolidada para ubicacion por usuario
- `ubicaciones_usuario`
  - `id_ubicacion_usuario` (PK)
  - `id_usuario` (UNIQUE)
  - `region`
  - `comuna`
  - `calle`
  - `numero`
  - `detalle`
  - `latitud`
  - `longitud`
  - `rango_km`
  - `fecha_actualizacion`

### Campos usados en lectura de ofertas
- Alias/derivados en consulta:
  - `ubicacion_referencia`
  - `latitud_referencia`
  - `longitud_referencia`
  - `rango_disponibilidad_km`

### Modelo de dominio impactado
- `OfertaServicio`
  - `rangoDisponibilidadKm: Int`

## Archivos clave impactados en el cierre
- `Contrabajo/app/build.gradle.kts`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/screens/inicio/PantallaInicial.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/screens/autenticacion/PantallasRegistro.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/screens/ajustes/PantallasAjustes.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/screens/principal/PantallaPrincipal.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/screens/servicio/PantallaDetalleServicio.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/screens/servicio/PantallaEditorServicio.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/viewmodel/ContenidoViewModels.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/ui/viewmodel/ContrabajoViewModelFactory.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/data/local/ContrabajoSQLiteHelper.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/data/repository/Repositorios.kt`
- `Contrabajo/app/src/main/java/com/movil/contrabajo/domain/model/Modelos.kt`

## Estado tecnico al cierre
- Compilacion validada:
  - `:app:compileDebugKotlin` => `BUILD SUCCESSFUL`
- Warnings no bloqueantes:
  - propiedades deprecadas de OSMDroid (`fillColor`, `strokeColor`, `strokeWidth`).

## Entregables de cierre creados
- Borrador para changelog:
  - `Codex/ct-app-18-05-09-changelog-v0.3.md`
- Cierre final de iteracion:
  - `Documentacion/ITERACION_HU01_HU05_CIERRE_2026-04-18.md`

## Pendientes arrastrados a siguiente iteracion
- Integrar backend para regiones/comunas reales.
- Integrar OCR y verificacion documental real.
- Migrar contrasena a hash seguro (backend).
- Conectar boton de contacto a chat real.
- Ajuste visual final pixel-perfect segun mockup definitivo.


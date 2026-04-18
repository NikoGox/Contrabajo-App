package com.movil.contrabajo.ui.screens.ajustes

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.CampoSecretoContrabajo
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.ResumenPerfilLinea
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
fun PantallaAjustes(
    onVolver: () -> Unit,
    onAbrirSeguridad: () -> Unit,
    onAbrirCuenta: () -> Unit,
    onAbrirUbicacion: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Ajustes", onVolver = onVolver)

        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Menu de opciones",
                subtitulo = "Configura seguridad, cuenta y ubicacion."
            )
            ItemAjuste(
                titulo = "Seguridad y verificacion",
                subtitulo = "Verificar cuenta trabajador y preguntas de seguridad",
                onClick = onAbrirSeguridad
            )
            ItemAjuste(
                titulo = "Cuenta",
                subtitulo = "Datos completos de tu usuario",
                onClick = onAbrirCuenta
            )
            ItemAjuste(
                titulo = "Ubicacion",
                subtitulo = "Direccion, coordenadas y rango de disponibilidad",
                onClick = onAbrirUbicacion
            )
            ItemAjuste(
                titulo = "Preferencias",
                subtitulo = "Disponible mas adelante",
                onClick = {},
                habilitado = false
            )
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text(
                text = "Cerrar sesion",
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PantallaAjustesSeguridad(
    onVolver: () -> Unit,
    onAbrirVerificacion: () -> Unit,
    onAbrirPreguntas: () -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Seguridad y verificacion", onVolver = onVolver)

        TarjetaBase {
            ItemAjuste(
                titulo = "Verificar cuenta trabajador",
                subtitulo = "Ingresa RUN y numero de documento",
                onClick = onAbrirVerificacion
            )
            ItemAjuste(
                titulo = "Configurar preguntas de seguridad",
                subtitulo = "Configura 3 preguntas para recuperar tu cuenta",
                onClick = onAbrirPreguntas
            )
        }
    }
}

@Composable
fun PantallaVerificarCuentaTrabajador(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    LaunchedEffect(Unit) { viewModel.recargar() }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Verificar cuenta trabajador", onVolver = onVolver)

        TarjetaBase {
            Text(
                text = "Tu tipo de perfil se actualiza automaticamente despues de la validacion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoContrabajo(
                valor = uiState.runVerificacion,
                onValueChange = viewModel::actualizarRunVerificacion,
                etiqueta = "RUN (sin puntos)"
            )
            CampoContrabajo(
                valor = uiState.dvVerificacion,
                onValueChange = viewModel::actualizarDvVerificacion,
                etiqueta = "DV"
            )
            CampoContrabajo(
                valor = uiState.numeroDocumentoVerificacion,
                onValueChange = viewModel::actualizarNumeroDocumentoVerificacion,
                etiqueta = "Numero de documento"
            )

            if (uiState.usuario?.verificacionTrabajadorPendiente == true) {
                EtiquetaEstado("Verificacion en curso")
                Text(
                    text = "La cuenta se actualizara en 3 minutos aprox.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                BotonPrimario(
                    texto = "Enviar solicitud",
                    onClick = viewModel::solicitarVerificacionTrabajador
                )
            }

            if (uiState.errorVerificacion != null) {
                Text(
                    text = uiState.errorVerificacion,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (uiState.mensajeVerificacion != null) {
                Text(
                    text = uiState.mensajeVerificacion,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PantallaPreguntasSeguridad(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.recargar() }
    val uiState = viewModel.uiState
    val preguntas = if (uiState.preguntasSeguridad.isEmpty()) {
        (1..3).map { PreguntaSeguridadConfig(indice = it) }
    } else {
        uiState.preguntasSeguridad
    }

    var indiceEnEdicion by rememberSaveable { mutableIntStateOf(0) }
    var mostrarModal by rememberSaveable { mutableStateOf(false) }
    var preguntaInput by rememberSaveable { mutableStateOf("") }
    var respuestaInput by rememberSaveable { mutableStateOf("") }
    var respuestasVisibles by rememberSaveable { mutableStateOf(setOf<Int>()) }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Preguntas de seguridad", onVolver = onVolver)

        TarjetaBase {
            Text(
                text = "Configura tus 3 preguntas. Luego serviran para recuperar la cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            preguntas.forEach { item ->
                TarjetaBase(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            indiceEnEdicion = item.indice
                            preguntaInput = item.pregunta
                            respuestaInput = item.respuesta
                            mostrarModal = true
                            viewModel.limpiarMensajesPreguntasSeguridad()
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.84f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pregunta de seguridad ${item.indice}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (item.configurada) item.pregunta else "Sin configurar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.configurada) {
                                val visible = respuestasVisibles.contains(item.indice)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (visible) "Respuesta: ${item.respuesta}" else "Respuesta: ••••••••",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(onClick = {
                                        respuestasVisibles = if (visible) {
                                            respuestasVisibles - item.indice
                                        } else {
                                            respuestasVisibles + item.indice
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (visible) "Ocultar respuesta" else "Mostrar respuesta"
                                        )
                                    }
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (uiState.errorPreguntasSeguridad != null) {
            Text(
                text = uiState.errorPreguntasSeguridad,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        if (uiState.mensajePreguntasSeguridad != null) {
            Text(
                text = uiState.mensajePreguntasSeguridad,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (mostrarModal) {
        AlertDialog(
            onDismissRequest = { mostrarModal = false },
            title = { Text("Configurar pregunta $indiceEnEdicion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoContrabajo(
                        valor = preguntaInput,
                        onValueChange = { preguntaInput = it },
                        etiqueta = "Pregunta"
                    )
                    CampoSecretoContrabajo(
                        valor = respuestaInput,
                        onValueChange = { respuestaInput = it },
                        etiqueta = "Respuesta"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.guardarPreguntaSeguridad(
                        indice = indiceEnEdicion,
                        pregunta = preguntaInput,
                        respuesta = respuestaInput
                    )
                    mostrarModal = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PantallaCuenta(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.recargar() }
    val usuario = viewModel.uiState.usuario

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Cuenta", onVolver = onVolver)

        TarjetaBase {
            if (usuario == null) {
                Text(
                    text = "No hay sesion activa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ResumenPerfilLinea("Tipo de cuenta", etiquetaPerfil(usuario.tipoPerfil))
                ResumenPerfilLinea(
                    "Nombre",
                    "${usuario.nombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}"
                )
                ResumenPerfilLinea("Fecha de nacimiento", usuario.fechaNacimiento)
                ResumenPerfilLinea("Correo", usuario.correo)
                ResumenPerfilLinea("Telefono", usuario.telefono)
                ResumenPerfilLinea("Usuario", "@${usuario.username}")
                ResumenPerfilLinea("RUN", "${usuario.run}-${usuario.dv}")
                ResumenPerfilLinea("Documento", usuario.numeroDocumentoIdentidad ?: "No registrado")
            }
        }
    }
}

@Composable
fun PantallaUbicacion(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.recargar() }
    val uiState = viewModel.uiState
    val ubicacion = uiState.ubicacionAjustes
    var mostrarModalDireccion by rememberSaveable { mutableStateOf(false) }
    var comunaInput by rememberSaveable { mutableStateOf("") }
    var calleInput by rememberSaveable { mutableStateOf("") }
    var numeroInput by rememberSaveable { mutableStateOf("") }
    var detalleInput by rememberSaveable { mutableStateOf("") }

    PantallaBase(modifier = modifier, mostrarFondo = false, scrollable = false) {
        BarraSuperiorAjustes(titulo = "Ubicacion", onVolver = onVolver)

        TarjetaBase {
            Text(
                text = "Direccion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Region: ${ubicacion.region}", style = MaterialTheme.typography.bodySmall)
            Text("Comuna: ${ubicacion.comuna}", style = MaterialTheme.typography.bodySmall)
            Text("Calle: ${ubicacion.calle}", style = MaterialTheme.typography.bodySmall)
            Text("Numero: ${ubicacion.numero}", style = MaterialTheme.typography.bodySmall)
            Text("Detalle: ${ubicacion.detalle}", style = MaterialTheme.typography.bodySmall)
            BotonPrimario(
                texto = "Editar ubicacion",
                onClick = {
                    comunaInput = ubicacion.comuna
                    calleInput = ubicacion.calle
                    numeroInput = ubicacion.numero
                    detalleInput = ubicacion.detalle
                    mostrarModalDireccion = true
                }
            )
        }

        TarjetaBase {
            Text(text = "Rango de disponibilidad: ${ubicacion.rangoDisponibilidadKm} km")
            Slider(
                value = ubicacion.rangoDisponibilidadKm.toFloat(),
                onValueChange = viewModel::actualizarRangoUbicacion,
                valueRange = 0f..100f,
                steps = 99
            )
        }

        TarjetaBase(contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)) {
            MapaUbicacionOpenStreetMap(
                latitud = ubicacion.latitud ?: -33.4489,
                longitud = ubicacion.longitud ?: -70.6693,
                rangoKm = ubicacion.rangoDisponibilidadKm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            )
        }

        TarjetaBase {
            BotonPrimario(
                texto = "Obtener ubicacion",
                onClick = viewModel::obtenerUbicacionActual
            )
            BotonPrimario(
                texto = "Guardar ubicacion y rango",
                onClick = viewModel::guardarUbicacionAjustes
            )

            Text(
                text = if (ubicacion.latitud == null || ubicacion.longitud == null) {
                    "Coordenadas opcionales: aun no definidas."
                } else {
                    "Coordenadas: ${"%.5f".format(ubicacion.latitud)}, ${"%.5f".format(ubicacion.longitud)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.errorUbicacion != null) {
            Text(
                text = uiState.errorUbicacion,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        if (uiState.mensajeUbicacion != null) {
            Text(
                text = uiState.mensajeUbicacion,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (mostrarModalDireccion) {
        AlertDialog(
            onDismissRequest = { mostrarModalDireccion = false },
            title = { Text("Editar ubicacion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "Region Metropolitana",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Region") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Pronto podras elegir otras regiones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ComboComunaRM(
                        comunaSeleccionada = comunaInput.ifBlank { "Santiago" },
                        onSeleccionar = { comunaInput = it }
                    )
                    CampoContrabajo(valor = calleInput, onValueChange = { calleInput = it }, etiqueta = "Calle")
                    CampoContrabajo(valor = numeroInput, onValueChange = { numeroInput = it }, etiqueta = "Numero")
                    CampoContrabajo(valor = detalleInput, onValueChange = { detalleInput = it }, etiqueta = "Detalle")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.actualizarRegionUbicacion("Region Metropolitana")
                    viewModel.actualizarComunaUbicacion(comunaInput)
                    viewModel.actualizarCalleUbicacion(calleInput)
                    viewModel.actualizarNumeroUbicacion(numeroInput)
                    viewModel.actualizarDetalleUbicacion(detalleInput)
                    viewModel.guardarUbicacionAjustes()
                    mostrarModalDireccion = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalDireccion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComboComunaRM(
    comunaSeleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    var desplegado by rememberSaveable { mutableStateOf(false) }
    val comunas = remember {
        listOf(
            "Alhue", "Buin", "Calera de Tango", "Cerrillos", "Cerro Navia", "Colina",
            "Conchali", "Curacavi", "El Bosque", "El Monte", "Estacion Central", "Huechuraba",
            "Independencia", "Isla de Maipo", "La Cisterna", "La Florida", "La Granja", "La Pintana",
            "La Reina", "Lampa", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado",
            "Macul", "Maipu", "Maria Pinto", "Melipilla", "Nunoa", "Padre Hurtado",
            "Paine", "Pedro Aguirre Cerda", "Penaflor", "Penalolen", "Pirque", "Providencia",
            "Pudahuel", "Puente Alto", "Quilicura", "Quinta Normal", "Recoleta", "Renca",
            "San Bernardo", "San Joaquin", "San Jose de Maipo", "San Miguel", "San Pedro",
            "San Ramon", "Santiago", "Talagante", "Tiltil", "Vitacura"
        )
    }

    ExposedDropdownMenuBox(
        expanded = desplegado,
        onExpandedChange = { desplegado = !desplegado }
    ) {
        OutlinedTextField(
            value = comunaSeleccionada,
            onValueChange = {},
            readOnly = true,
            label = { Text("Comuna") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
            singleLine = true
        )
        DropdownMenu(
            expanded = desplegado,
            onDismissRequest = { desplegado = false }
        ) {
            comunas.forEach { comuna ->
                DropdownMenuItem(
                    text = { Text(comuna) },
                    onClick = {
                        onSeleccionar(comuna)
                        desplegado = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MapaUbicacionOpenStreetMap(
    latitud: Double,
    longitud: Double,
    rangoKm: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val zoom = calcularZoomPorRango(rangoKm).toDouble()
    val radioMetros = rangoKm.coerceIn(0, 100) * 1000.0

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitud, longitud))
            setOnTouchListener { _, _ -> true }
        }
    }

    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            val centro = GeoPoint(latitud, longitud)
            map.controller.setZoom(zoom)
            map.controller.setCenter(centro)
            map.overlays.clear()

            val marcador = Marker(map).apply {
                position = centro
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Mi ubicacion"
            }

            val circulo = Polygon(map).apply {
                points = Polygon.pointsAsCircle(centro, radioMetros)
                fillColor = Color(0x3319A1A8).toArgb()
                strokeColor = Color(0xFF0E8C94).toArgb()
                strokeWidth = 2f
            }

            map.overlays.add(circulo)
            map.overlays.add(marcador)
            map.invalidate()
        }
    )
}

private fun calcularZoomPorRango(rangoKm: Int): Int = when {
    rangoKm <= 1 -> 15
    rangoKm <= 3 -> 14
    rangoKm <= 7 -> 13
    rangoKm <= 15 -> 12
    rangoKm <= 30 -> 11
    rangoKm <= 55 -> 10
    else -> 9
}

@Composable
private fun BarraSuperiorAjustes(
    titulo: String,
    onVolver: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ItemAjuste(
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    habilitado: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick),
        tonalElevation = 2.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (habilitado) 0.5f else 0.25f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.84f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (habilitado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (habilitado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun etiquetaPerfil(tipoPerfil: Int): String = when (tipoPerfil) {
    TipoPerfil.MODERADOR -> "Moderador"
    TipoPerfil.TRABAJADOR -> "Trabajador"
    TipoPerfil.PREMIUM -> "Premium"
    else -> "Usuario base"
}

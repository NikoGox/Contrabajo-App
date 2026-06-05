package com.movil.contrabajo.ui.screens.ajustes

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.movil.contrabajo.R
import com.movil.contrabajo.domain.model.ComunaCatalogo
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.PreguntasSeguridadCatalogo
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.CampoSecretoContrabajo
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.FilaEtiquetaValor
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
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
import androidx.compose.ui.unit.Dp

@Composable
fun PantallaAjustes(
    onVolver: () -> Unit,
    onAbrirSeguridad: () -> Unit,
    onAbrirCuenta: () -> Unit,
    onAbrirUbicacion: () -> Unit,
    onCerrarSesion: () -> Unit,
    esModerador: Boolean = false,
    onAbrirBaneos: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Ajustes", onVolver = onVolver, iconoDerecha = Icons.Filled.Settings)

        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Menú de opciones",
                subtitulo = "Configura seguridad, cuenta y ubicación."
            )
            ItemAjuste(
                titulo = "Seguridad y verificación",
                subtitulo = "Verificar cuenta trabajador y preguntas de seguridad",
                onClick = onAbrirSeguridad
            )
            ItemAjuste(
                titulo = "Cuenta",
                subtitulo = "Datos completos de tu usuario",
                onClick = onAbrirCuenta
            )
            ItemAjuste(
                titulo = "Ubicación",
                subtitulo = "Dirección, coordenadas y rango de disponibilidad",
                onClick = onAbrirUbicacion
            )
            ItemAjuste(
                titulo = "Preferencias",
                subtitulo = "Disponible más adelante",
                onClick = {},
                habilitado = false
            )
        }

        if (esModerador) {
            TarjetaBase {
                EncabezadoPantalla(
                    titulo = "Moderación",
                    subtitulo = "Herramientas exclusivas del moderador."
                )
                ItemAjuste(
                    titulo = "Gestionar baneos",
                    subtitulo = "Ver usuarios baneados o suspendidos y desbanear",
                    onClick = onAbrirBaneos
                )
            }
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFFD32F2F)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text(
                text = "Cerrar sesión",
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PantallaAjustesSeguridad(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    onAbrirVerificacion: () -> Unit,
    onAbrirPreguntas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tipoPerfil = viewModel.uiState.usuario?.tipoPerfil
    val puedeVerificar = tipoPerfil == TipoPerfil.USUARIO_BASE
    var mostrarModalContrasena by rememberSaveable { mutableStateOf(false) }
    var contrasenaInput by rememberSaveable { mutableStateOf("") }
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.recargar()
    }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Seguridad y verificacion", onVolver = onVolver, iconoDerecha = Icons.Filled.Security)

        TarjetaBase {
            if (puedeVerificar) {
                ItemAjuste(
                    titulo = "Verificar cuenta trabajador",
                    subtitulo = "Ingresa RUN y número de documento",
                    onClick = onAbrirVerificacion
                )
            }
            ItemAjuste(
                titulo = "Configurar preguntas de seguridad",
                subtitulo = "Configura 2 preguntas para recuperar tu cuenta",
                onClick = {
                    solicitarAutenticacionPreguntasSeguridad(
                        context = context,
                        onAutenticado = onAbrirPreguntas,
                        onRequiereFallbackContrasena = {
                            contrasenaInput = ""
                            errorContrasena = null
                            mostrarModalContrasena = true
                        }
                    )
                }
            )
        }
    }

    if (mostrarModalContrasena) {
        AlertDialog(
            onDismissRequest = { mostrarModalContrasena = false },
            title = { Text("Validar con contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "No fue posible usar biometría. Ingresa la contraseña de tu cuenta para continuar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CampoSecretoContrabajo(
                        valor = contrasenaInput,
                        onValueChange = {
                            contrasenaInput = it
                            errorContrasena = null
                        },
                        etiqueta = "Contraseña de cuenta"
                    )
                    if (errorContrasena != null) {
                        Text(
                            text = errorContrasena.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.validarContrasenaCuenta(contrasenaInput)
                            .onSuccess {
                                mostrarModalContrasena = false
                                onAbrirPreguntas()
                            }
                            .onFailure {
                                errorContrasena = it.message ?: "No se pudo validar la contraseña"
                            }
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalContrasena = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPreguntasSeguridad(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.recargar() }
    val uiState = viewModel.uiState
    val preguntas = if (uiState.preguntasSeguridad.isEmpty()) {
        (1..2).map { PreguntaSeguridadConfig(indice = it) }
    } else {
        uiState.preguntasSeguridad
    }
    val catalogo = PreguntasSeguridadCatalogo.opciones

    var indiceEnEdicion by rememberSaveable { mutableIntStateOf(0) }
    var mostrarModal by rememberSaveable { mutableStateOf(false) }
    var preguntaInput by rememberSaveable { mutableStateOf("") }
    var respuestaInput by rememberSaveable { mutableStateOf("") }
    var desplegarPreguntas by rememberSaveable { mutableStateOf(false) }
    val preguntaAlterna = preguntas
        .firstOrNull { it.indice != indiceEnEdicion }
        ?.pregunta
        .orEmpty()
    val opcionesDisponibles = catalogo.filter { it == preguntaInput || it != preguntaAlterna }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Preguntas de seguridad", onVolver = onVolver, iconoDerecha = Icons.Filled.Tune)

        TarjetaBase {
            Text(
                text = "Configura 2 preguntas. Luego servirán para recuperar la cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            preguntas.forEach { item ->
                TarjetaBase(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
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
                                Text(
                                    text = if (item.respuesta.isBlank()) "Respuesta configurada" else "Respuesta: ••••••••",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedIconButton(onClick = {
                            indiceEnEdicion = item.indice
                            preguntaInput = item.pregunta
                            respuestaInput = item.respuesta
                            desplegarPreguntas = false
                            mostrarModal = true
                            viewModel.limpiarMensajesPreguntasSeguridad()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar pregunta"
                            )
                        }
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
                    ExposedDropdownMenuBox(
                        expanded = desplegarPreguntas,
                        onExpandedChange = { desplegarPreguntas = !desplegarPreguntas }
                    ) {
                        OutlinedTextField(
                            value = preguntaInput.ifBlank { "Seleccionar pregunta" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pregunta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegarPreguntas) },
                            singleLine = true
                        )
                        DropdownMenu(
                            expanded = desplegarPreguntas,
                            onDismissRequest = { desplegarPreguntas = false }
                        ) {
                            opcionesDisponibles.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        preguntaInput = opcion
                                        desplegarPreguntas = false
                                    }
                                )
                            }
                        }
                    }
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
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.recargar() }
    val usuario = viewModel.uiState.usuario

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(titulo = "Cuenta", onVolver = onVolver, iconoDerecha = Icons.Filled.Person)

        if (usuario == null) {
            TarjetaBase {
                Text(
                    text = "No hay sesión activa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            TarjetaBase {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${usuario.nombre} ${usuario.apellidoPaterno}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@${usuario.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = etiquetaPerfil(usuario.tipoPerfil),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaCuadradoCuenta(
                        icono = Icons.Filled.Email,
                        etiqueta = "Correo",
                        valor = usuario.correo,
                        modifier = Modifier.fillMaxWidth(0.58f).fillMaxHeight(),
                        etiquetaAncho = 72.dp,
                        maxLineasValor = 1
                    )
                    TarjetaCuadradoCuenta(
                        icono = Icons.Filled.Phone,
                        etiqueta = "Teléfono",
                        valor = usuario.telefono,
                        prefijo = "+56 9 ",
                        modifier = Modifier.fillMaxHeight().aspectRatio(1f)
                    )
                }

                TarjetaHorizontalCuenta(
                    icono = Icons.Filled.Badge,
                    titulo = "Identificación",
                    filas = listOf(
                        "RUN" to "${usuario.run}-${usuario.dv}",
                        "Documento" to (usuario.numeroDocumentoIdentidad ?: "No registrado"),
                        "Nacimiento" to usuario.fechaNacimiento
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val uiState = viewModel.uiState
    val ubicacion = uiState.ubicacionAjustes
    val esTrabajador = uiState.usuario?.tipoPerfil in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)
    val direccionLinea = listOf(ubicacion.calle, ubicacion.numero)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Sin dirección" }
    val resumenUbicacion = listOf(direccionLinea, ubicacion.comuna, ubicacion.region)
        .filter { it.isNotBlank() }
        .joinToString(" - ")
    var mostrarModalDireccion by rememberSaveable { mutableStateOf(false) }
    var comunaInput by rememberSaveable { mutableStateOf("") }
    var calleInput by rememberSaveable { mutableStateOf("") }
    var numeroInput by rememberSaveable { mutableStateOf("") }
    var posicionSliderDisponibilidad by rememberSaveable { mutableStateOf(0f) }
    val reportarSinUbicacion = {
        viewModel.reportarErrorUbicacion(
            "No se pudo obtener la ubicación actual. Mantuvimos tu última coordenada."
        )
    }
    val actualizarUbicacionReal: () -> Unit = {
        val tieneFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val prioridad = if (tieneFine) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val token = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(prioridad, token.token)
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    viewModel.guardarCoordenadasGps(
                        latitud = location.latitude,
                        longitud = location.longitude
                    )
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { ultima: android.location.Location? ->
                            if (ultima != null) {
                                viewModel.guardarCoordenadasGps(
                                    latitud = ultima.latitude,
                                    longitud = ultima.longitude
                                )
                            } else {
                                reportarSinUbicacion()
                            }
                        }
                        .addOnFailureListener { reportarSinUbicacion() }
                }
            }
            .addOnFailureListener {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { ultima: android.location.Location? ->
                        if (ultima != null) {
                            viewModel.guardarCoordenadasGps(
                                latitud = ultima.latitude,
                                longitud = ultima.longitude
                            )
                        } else {
                            reportarSinUbicacion()
                        }
                    }
                    .addOnFailureListener { reportarSinUbicacion() }
            }
    }
    val solicitudPermisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            actualizarUbicacionReal()
        } else {
            viewModel.reportarErrorUbicacion("Debes conceder permiso de ubicación para recalcular coordenadas.")
        }
    }

    LaunchedEffect(ubicacion.rangoDisponibilidadM) {
        posicionSliderDisponibilidad = EscalaRango.posicionSliderPorValor(ubicacion.rangoDisponibilidadM)
    }
    LaunchedEffect(mostrarModalDireccion) {
        if (mostrarModalDireccion) {
            viewModel.recargar()
            viewModel.recargarComunas()
        }
    }

    LaunchedEffect(uiState.mensajeUbicacion) {
        val mensaje = uiState.mensajeUbicacion ?: return@LaunchedEffect
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        viewModel.consumirMensajeUbicacion()
    }

    PantallaBase(modifier = modifier, mostrarFondo = false, scrollable = false) {
        BarraSuperiorAjustes(titulo = "Ubicación", onVolver = onVolver, iconoDerecha = Icons.Filled.Tune)

        TarjetaBase(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            Text(
                text = "Dirección",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = resumenUbicacion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = {
                    comunaInput = ubicacion.comuna
                    calleInput = ubicacion.calle
                    numeroInput = ubicacion.numero
                    mostrarModalDireccion = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Editar ubicación") }
        }

        if (esTrabajador) {
            TarjetaBase {
                Text(
                    text = "Rango de disponibilidad: ${EscalaRango.formatear(ubicacion.rangoDisponibilidadM)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = posicionSliderDisponibilidad,
                    onValueChange = {
                        posicionSliderDisponibilidad = it
                        viewModel.actualizarRangoUbicacion(it)
                    },
                    valueRange = 0f..EscalaRango.valoresMetros.lastIndex.toFloat(),
                    steps = EscalaRango.valoresMetros.size - 2
                )
            }

            TarjetaBase(contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)) {
                MapaUbicacionOpenStreetMap(
                    latitud = ubicacion.latitud ?: -33.4489,
                    longitud = ubicacion.longitud ?: -70.6693,
                    rangoM = ubicacion.rangoDisponibilidadM,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                )
            }
        }

        TarjetaBase(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val tieneFine = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val tieneCoarse = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (tieneFine || tieneCoarse) {
                            actualizarUbicacionReal()
                        } else {
                            solicitudPermisosLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Obtener ubicación") }
                BotonPrimario(
                    texto = "Guardar",
                    onClick = viewModel::guardarUbicacionAjustes,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = if (ubicacion.latitud == null || ubicacion.longitud == null) {
                    "Coordenadas: sin definir"
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
    }

    if (mostrarModalDireccion) {
        AlertDialog(
            onDismissRequest = { mostrarModalDireccion = false },
            title = { Text("Editar ubicación") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "Región Metropolitana",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Región") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Pronto podrás elegir otras regiones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ComboComunaRM(
                        comunaSeleccionada = comunaInput.ifBlank { "Sin comuna" },
                        comunas = uiState.comunasDisponibles,
                        onSeleccionar = { comunaInput = it }
                    )
                    CampoContrabajo(valor = calleInput, onValueChange = { calleInput = it }, etiqueta = "Calle")
                    CampoContrabajo(valor = numeroInput, onValueChange = { numeroInput = it }, etiqueta = "Número")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.actualizarRegionUbicacion("Región Metropolitana")
                    viewModel.actualizarComunaUbicacion(comunaInput)
                    viewModel.actualizarCalleUbicacion(calleInput)
                    viewModel.actualizarNumeroUbicacion(numeroInput)
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

    OverlayPantallaCarga(
        visible = uiState.cargandoPantalla,
        mensaje = "Actualizando datos..."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComboComunaRM(
    comunaSeleccionada: String,
    comunas: List<ComunaCatalogo>,
    onSeleccionar: (String) -> Unit
) {
    var desplegado by rememberSaveable { mutableStateOf(false) }
    val comunasOrdenadas = remember(comunas) {
        comunas.sortedWith(
            compareBy<ComunaCatalogo> { if (it.nombre.equals("Sin comuna", ignoreCase = true)) 0 else 1 }
                .thenBy { it.nombre.lowercase() }
        ).map { it.nombre }
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
            if (comunasOrdenadas.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin comunas disponibles") },
                    onClick = { desplegado = false }
                )
            } else {
                comunasOrdenadas.forEach { comuna ->
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
}

@Composable
private fun MapaUbicacionOpenStreetMap(
    latitud: Double,
    longitud: Double,
    rangoM: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rangoNormalizadoM = EscalaRango.normalizar(rangoM)
    val rangoVisualM = maxOf(rangoNormalizadoM, 1000)
    val zoom = calcularZoomPorRangoM(rangoVisualM).toDouble()
    val radioMetros = rangoVisualM.toDouble()

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
            setOnTouchListener { _, _ -> false }
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
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Mi ubicación"
                icon = ContextCompat.getDrawable(context, R.drawable.ic_pin_marcador_azul)
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

private fun calcularZoomPorRangoM(rangoM: Int): Int = when {
    rangoM <= 400 -> 15
    rangoM <= 900 -> 14
    rangoM <= 2_000 -> 13
    rangoM <= 5_000 -> 12
    rangoM <= 10_000 -> 11
    rangoM <= 20_000 -> 10
    rangoM <= 35_000 -> 9
    else -> 9
}

@Composable
private fun TarjetaCuadradoCuenta(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    prefijo: String? = null,
    maxLineasValor: Int = 1,
    etiquetaAncho: Dp = 0.dp
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (etiquetaAncho > 0.dp) Modifier.width(etiquetaAncho) else Modifier
                )
                if (prefijo != null && valor.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prefijo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = valor,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = maxLineasValor,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = valor.ifBlank { "-" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = maxLineasValor,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaHorizontalCuenta(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    filas: List<Pair<String, String>>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            filas.forEach { (etiqueta, valor) ->
                FilaEtiquetaValor(
                    etiqueta = etiqueta,
                    valor = valor,
                    etiquetaAncho = 76.dp,
                    valorMaxLines = 1
                )
            }
        }
    }
}

@Composable
internal fun BarraSuperiorAjustes(
    titulo: String,
    onVolver: () -> Unit,
    iconoDerecha: ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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
            Icon(
                imageVector = iconoDerecha,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(end = 12.dp)
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

private fun solicitarAutenticacionPreguntasSeguridad(
    context: Context,
    onAutenticado: () -> Unit,
    onRequiereFallbackContrasena: () -> Unit
) {
    val activity = context.findFragmentActivity()
    if (activity == null) {
        Toast.makeText(context, "No fue posible iniciar autenticación del dispositivo.", Toast.LENGTH_SHORT).show()
        onRequiereFallbackContrasena()
        return
    }
    val autenticadores = BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val biometricManager = BiometricManager.from(activity)
    val estado = biometricManager.canAuthenticate(autenticadores)
    if (estado != BiometricManager.BIOMETRIC_SUCCESS) {
        Toast.makeText(
            context,
            "Tu dispositivo no tiene autenticación biométrica o credencial habilitada.",
            Toast.LENGTH_SHORT
        ).show()
        onRequiereFallbackContrasena()
        return
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onAutenticado()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val canceladoPorUsuario = errorCode == BiometricPrompt.ERROR_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                if (canceladoPorUsuario) {
                    Toast.makeText(context, "Acceso cancelado.", Toast.LENGTH_SHORT).show()
                } else {
                    onRequiereFallbackContrasena()
                }
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Confirmar identidad")
        .setSubtitle("Valida tu identidad para configurar preguntas de seguridad")
        .setAllowedAuthenticators(autenticadores)
        .build()
    prompt.authenticate(promptInfo)
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

private fun etiquetaPerfil(tipoPerfil: Int): String = when (tipoPerfil) {
    TipoPerfil.MODERADOR -> "Moderador"
    TipoPerfil.TRABAJADOR -> "Trabajador"
    TipoPerfil.PREMIUM -> "Premium"
    else -> "Cliente"
}

private object FormatoRunVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(8)
        val formateado = formatearConPuntos(digitos, intArrayOf(2, 5))
        val mapping = crearOffsetMapping(digitos, formateado)
        return TransformedText(AnnotatedString(formateado), mapping)
    }
}

private object FormatoDocumentoVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(9)
        val formateado = formatearConPuntos(digitos, intArrayOf(3, 6))
        val mapping = crearOffsetMapping(digitos, formateado)
        return TransformedText(AnnotatedString(formateado), mapping)
    }
}

private fun formatearConPuntos(digitos: String, cortes: IntArray): String {
    if (digitos.isBlank()) return ""
    val ordenados = cortes.sorted().distinct()
    return buildString {
        digitos.forEachIndexed { index, c ->
            if (index in ordenados) append('.')
            append(c)
        }
    }
}

private fun crearOffsetMapping(originalDigits: String, transformed: String): OffsetMapping {
    return object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 0) return 0
            var consumidos = 0
            for (i in transformed.indices) {
                if (transformed[i].isDigit()) consumidos++
                if (consumidos == offset) return i + 1
            }
            return transformed.length
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 0) return 0
            val limite = offset.coerceAtMost(transformed.length)
            var consumidos = 0
            for (i in 0 until limite) {
                if (transformed[i].isDigit()) consumidos++
            }
            return consumidos.coerceIn(0, originalDigits.length)
        }
    }
}

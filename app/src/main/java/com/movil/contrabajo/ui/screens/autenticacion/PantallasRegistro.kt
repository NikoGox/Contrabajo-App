package com.movil.contrabajo.ui.screens.autenticacion

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.CampoSecretoContrabajo
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.IndicadorPasos
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.RegistroViewModel
import java.time.LocalDate

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaRegistroPasoUno(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    val registro = viewModel.uiState.registro
    val partesFecha = remember(registro.fechaNacimiento) { descomponerFecha(registro.fechaNacimiento) }
    val anioMinimo = 1926
    val anioMaximo = 2026
    val scrollPasoUno = rememberScrollState()

    var diaSeleccionado by rememberSaveable(registro.fechaNacimiento) { mutableIntStateOf(partesFecha.first) }
    var mesSeleccionado by rememberSaveable(registro.fechaNacimiento) { mutableIntStateOf(partesFecha.second) }
    var anioInput by rememberSaveable(registro.fechaNacimiento) { mutableStateOf(partesFecha.third) }
    var intentoContinuar by rememberSaveable { mutableStateOf(false) }
    var bloquearSiguiente by rememberSaveable { mutableStateOf(false) }

    fun desbloquearValidacionPaso() {
        intentoContinuar = false
        bloquearSiguiente = false
    }

    fun actualizarFechaDesdePartes() {
        val anioLimpio = anioInput.filter { it.isDigit() }.take(4)
        anioInput = anioLimpio
        if (anioLimpio.length != 4) {
            viewModel.actualizarFechaNacimiento("")
            return
        }

        val anio = anioLimpio.toIntOrNull() ?: run {
            viewModel.actualizarFechaNacimiento("")
            return
        }
        val diaMaximoMes = obtenerMaximoDiaDelMes(anio, mesSeleccionado)
        if (diaSeleccionado > diaMaximoMes) {
            diaSeleccionado = diaMaximoMes
        }
        val fecha = runCatching {
            LocalDate.of(anio, mesSeleccionado, diaSeleccionado)
        }.getOrNull() ?: run {
            viewModel.actualizarFechaNacimiento("")
            return
        }
        if (anio !in anioMinimo..anioMaximo) {
            viewModel.actualizarFechaNacimiento("")
            return
        }
        viewModel.actualizarFechaNacimiento("%04d-%02d-%02d".format(anio, mesSeleccionado, diaSeleccionado))
    }

    val errorNombre = if (registro.nombre.isBlank()) "Ingresa tu nombre" else null
    val errorApellidoPaterno = if (registro.apellidoPaterno.isBlank()) "Ingresa tu apellido paterno" else null
    val errorRun = when {
        registro.run.length !in 7..8 -> "El RUN debe tener 7 u 8 dígitos"
        registro.dv.isBlank() -> null
        !validarRut(registro.run, registro.dv) -> "El RUN no es válido"
        else -> null
    }
    val errorDv = if (registro.dv.isBlank()) "Ingresa el DV" else null
    val errorTelefono = if (registro.telefono.length != 8) "Ingresa los 8 dígitos restantes del celular" else null
    val errorFecha = validarFechaNacimientoRegistro(
        fechaTexto = registro.fechaNacimiento,
        anioTexto = anioInput,
        anioMinimo = anioMinimo,
        anioMaximo = anioMaximo
    )

    val formularioPasoUnoValido = listOf(
        errorNombre,
        errorApellidoPaterno,
        errorRun,
        errorDv,
        errorTelefono,
        errorFecha
    ).all { it == null } && registro.apellidoMaterno.isNotBlank()

    PantallaBase(
        scrollable = false,
        mostrarFondo = false
    ) {
        EncabezadoRegistroAnimado()
        TarjetaBase(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            llenarAlto = true
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollPasoUno),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicadorPasos(pasoActual = 1, totalPasos = 4)
                EncabezadoPantalla(titulo = "Crear cuenta", subtitulo = "Datos personales")

                CampoContrabajo(
                    valor = registro.nombre,
                    onValueChange = {
                        desbloquearValidacionPaso()
                        viewModel.actualizarNombre(it)
                    },
                    etiqueta = "Nombre"
                )
                if (intentoContinuar) TextoErrorCampo(errorNombre)

                CampoContrabajo(
                    valor = registro.apellidoPaterno,
                    onValueChange = {
                        desbloquearValidacionPaso()
                        viewModel.actualizarApellidoPaterno(it)
                    },
                    etiqueta = "Apellido paterno"
                )
                if (intentoContinuar) TextoErrorCampo(errorApellidoPaterno)

                CampoContrabajo(
                    valor = registro.apellidoMaterno,
                    onValueChange = {
                        desbloquearValidacionPaso()
                        viewModel.actualizarApellidoMaterno(it)
                    },
                    etiqueta = "Apellido materno"
                )
                if (intentoContinuar) TextoErrorCampo(if (registro.apellidoMaterno.isBlank()) "Ingresa tu apellido materno" else null)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CampoContrabajo(
                        valor = registro.run,
                        onValueChange = {
                            desbloquearValidacionPaso()
                            viewModel.actualizarRun(it)
                        },
                        etiqueta = "RUN",
                        modifier = Modifier.weight(1f),
                        visualTransformation = FormatoRunVisualTransformation
                    )
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.08f)
                    )
                    CampoContrabajo(
                        valor = registro.dv,
                        onValueChange = {
                            desbloquearValidacionPaso()
                            viewModel.actualizarDv(it)
                        },
                        etiqueta = "DV",
                        modifier = Modifier.weight(0.35f)
                    )
                }
                if (intentoContinuar) TextoErrorCampo(errorRun ?: errorDv ?: viewModel.uiState.errorRunDisponible)

                CampoContrabajo(
                    valor = registro.telefono,
                    onValueChange = {
                        desbloquearValidacionPaso()
                        viewModel.actualizarTelefono(it)
                    },
                    etiqueta = "Teléfono (+56)",
                    visualTransformation = FormatoTelefonoVisualTransformation,
                    prefijo = "+56 9"
                )
                if (intentoContinuar) TextoErrorCampo(errorTelefono)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ComboRegistro(
                        etiqueta = "Dia",
                        valor = diaSeleccionado.toString(),
                        opciones = (1..31).map { it.toString() },
                        modifier = Modifier.weight(0.30f)
                    ) { seleccionado ->
                        desbloquearValidacionPaso()
                        diaSeleccionado = seleccionado.toIntOrNull() ?: diaSeleccionado
                        actualizarFechaDesdePartes()
                    }
                    ComboRegistro(
                        etiqueta = "Mes",
                        valor = mesLabel(mesSeleccionado),
                        opciones = (1..12).map { mesLabel(it) },
                        modifier = Modifier.weight(0.42f)
                    ) { seleccionado ->
                        desbloquearValidacionPaso()
                        mesSeleccionado = (1..12).firstOrNull { mesLabel(it) == seleccionado } ?: mesSeleccionado
                        actualizarFechaDesdePartes()
                    }
                    CampoContrabajo(
                        valor = anioInput,
                        onValueChange = {
                            desbloquearValidacionPaso()
                            anioInput = it.filter { c -> c.isDigit() }.take(4)
                            actualizarFechaDesdePartes()
                        },
                        etiqueta = "Año",
                        modifier = Modifier.weight(0.28f)
                    )
                }
                if (intentoContinuar) TextoErrorCampo(errorFecha)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonSecundario(
                    texto = "Volver",
                    onClick = onVolver,
                    modifier = Modifier.weight(1f)
                )
                BotonPrimario(
                    texto = "Siguiente",
                    enabled = !bloquearSiguiente && !viewModel.uiState.validandoDisponibilidad,
                    onClick = {
                        intentoContinuar = true
                        if (formularioPasoUnoValido) {
                            viewModel.verificarRunDisponibleAntesDeContinuar(onContinuar)
                        } else {
                            bloquearSiguiente = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroPasoDireccion(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    val registro = viewModel.uiState.registro
    val comunas = viewModel.uiState.comunas
    val context = LocalContext.current
    val scrollPasoDireccion = rememberScrollState()
    var desplegarComunas by rememberSaveable { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        viewModel.cargarComunas()
        if (registro.region.isBlank()) {
            viewModel.actualizarRegion("Región Metropolitana")
        }
    }

    val reportarErrorUbicacion: () -> Unit = {
        Toast.makeText(
            context,
            "No se pudo obtener ubicación actual. Puedes continuar igual.",
            Toast.LENGTH_SHORT
        ).show()
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
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.actualizarCoordenadasRegistro(location.latitude, location.longitude)
                    Toast.makeText(context, "Ubicación actual capturada.", Toast.LENGTH_SHORT).show()
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { ultima ->
                            if (ultima != null) {
                                viewModel.actualizarCoordenadasRegistro(ultima.latitude, ultima.longitude)
                                Toast.makeText(context, "Ubicación actual capturada.", Toast.LENGTH_SHORT).show()
                            } else {
                                reportarErrorUbicacion()
                            }
                        }
                        .addOnFailureListener { reportarErrorUbicacion() }
                }
            }
            .addOnFailureListener { reportarErrorUbicacion() }
    }
    val solicitudPermisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            actualizarUbicacionReal()
        } else {
            reportarErrorUbicacion()
        }
    }

    PantallaBase(
        scrollable = false,
        mostrarFondo = false
    ) {
        EncabezadoRegistroAnimado()
        TarjetaBase(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            llenarAlto = true
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollPasoDireccion),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicadorPasos(pasoActual = 2, totalPasos = 4)
                EncabezadoPantalla(titulo = "Crear cuenta", subtitulo = "Dirección (opcional)")

                OutlinedTextField(
                    value = "Región Metropolitana",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Región") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = desplegarComunas,
                    onExpandedChange = { desplegarComunas = !desplegarComunas }
                ) {
                    OutlinedTextField(
                        value = registro.comuna.ifBlank { "Seleccionar comuna" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comuna") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegarComunas) },
                        singleLine = true
                    )
                DropdownMenu(
                    expanded = desplegarComunas,
                    onDismissRequest = { desplegarComunas = false }
                ) {
                        comunas
                            .sortedWith(
                                compareBy<com.movil.contrabajo.domain.model.ComunaCatalogo> {
                                    if (it.nombre.equals("Sin comuna", ignoreCase = true)) 0 else 1
                                }.thenBy { it.nombre }
                            )
                            .forEach { comuna ->
                            DropdownMenuItem(
                                text = { Text(comuna.nombre) },
                                onClick = {
                                    viewModel.actualizarComuna(comuna.nombre)
                                    desplegarComunas = false
                                }
                            )
                        }
                    }
                }

                if (viewModel.uiState.cargandoComunas) {
                    Text(
                        text = "Cargando comunas...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (viewModel.uiState.errorComunas != null) {
                    Text(
                        text = viewModel.uiState.errorComunas.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoContrabajo(
                        valor = registro.calle,
                        onValueChange = viewModel::actualizarCalle,
                        etiqueta = "Calle",
                        modifier = Modifier.weight(1f)
                    )
                    CampoContrabajo(
                        valor = registro.numeroDireccion,
                        onValueChange = viewModel::actualizarNumeroDireccion,
                        etiqueta = "N°",
                        modifier = Modifier.weight(0.33f)
                    )
                }

                BotonPrimario(
                    texto = "Obtener ubicación actual",
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
                    }
                )

                Text(
                    text = "La dirección es opcional. Si no la ingresas, usaremos datos genéricos para continuar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonSecundario(
                    texto = "Atrás",
                    onClick = onVolver,
                    modifier = Modifier.weight(1f)
                )
                BotonPrimario(
                    texto = "Siguiente",
                    onClick = onContinuar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PantallaRegistroPasoDos(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    val uiState = viewModel.uiState
    val registro = uiState.registro
    val scrollPasoDos = rememberScrollState()
    var intentoRegistro by rememberSaveable { mutableStateOf(false) }

    val errorUsername = if (registro.username.isBlank()) "Ingresa un nombre de usuario" else null
    val errorCorreo = if (registro.correo.isBlank() || !registro.correo.contains("@")) "Ingresa un correo válido" else null
    val errorContrasena = validarContrasenaRegistro(registro.contrasena)
    val errorConfirmacion = if (registro.contrasena != registro.confirmarContrasena) "Las contraseñas no coinciden" else null
    val formularioPasoTresValido = listOf(
        errorUsername,
        errorCorreo,
        errorContrasena,
        errorConfirmacion
    ).all { it == null }

    PantallaBase(
        scrollable = false,
        mostrarFondo = false
    ) {
        EncabezadoRegistroAnimado()
        TarjetaBase(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            llenarAlto = true
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollPasoDos),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicadorPasos(pasoActual = 3, totalPasos = 4)
                EncabezadoPantalla(
                    titulo = "Crear cuenta",
                    subtitulo = "Datos de la cuenta"
                )

                CampoContrabajo(
                    registro.username,
                    onValueChange = {
                        intentoRegistro = false
                        viewModel.actualizarUsername(it)
                    },
                    etiqueta = "Nombre de usuario"
                )
                if (intentoRegistro) {
                    TextoErrorCampo(errorUsername)
                    TextoErrorCampo(uiState.errorUsernameDisponible)
                }

                CampoContrabajo(
                    registro.correo,
                    onValueChange = {
                        intentoRegistro = false
                        viewModel.actualizarCorreo(it)
                    },
                    etiqueta = "Correo electrónico"
                )
                if (intentoRegistro) {
                    TextoErrorCampo(errorCorreo)
                    TextoErrorCampo(uiState.errorCorreoDisponible)
                }

                CampoSecretoContrabajo(
                    valor = registro.contrasena,
                    onValueChange = {
                        intentoRegistro = false
                        viewModel.actualizarContrasena(it)
                    },
                    etiqueta = "Contraseña"
                )
                if (intentoRegistro) {
                    TextoErrorCampo(errorContrasena)
                }

                CampoSecretoContrabajo(
                    valor = registro.confirmarContrasena,
                    onValueChange = {
                        intentoRegistro = false
                        viewModel.actualizarConfirmarContrasena(it)
                    },
                    etiqueta = "Confirmar contraseña"
                )
                Text(
                    text = "La contraseña debe tener mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (intentoRegistro) {
                    TextoErrorCampo(errorConfirmacion)
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonSecundario(
                    texto = "Volver",
                    onClick = onVolver,
                    modifier = Modifier.weight(1f)
                )
                BotonPrimario(
                    texto = "Siguiente",
                    enabled = formularioPasoTresValido && !uiState.validandoDisponibilidad,
                    onClick = {
                        intentoRegistro = true
                        if (formularioPasoTresValido) {
                            viewModel.verificarCuentaDisponibleAntesDeContinuar(onContinuar)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroPasoSeguridad(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val uiState = viewModel.uiState
    val registro = uiState.registro
    val scrollPasoSeguridad = rememberScrollState()
    var intentoRegistro by rememberSaveable { mutableStateOf(false) }
    val preguntasCatalogo = viewModel.preguntasSeguridadDisponibles()
    val opcionesPreguntaDos = preguntasCatalogo.filter { it != registro.preguntaSeguridad1 || it == registro.preguntaSeguridad2 }

    val errorPregunta1 = if (registro.preguntaSeguridad1.isBlank()) "Selecciona la primera pregunta" else null
    val errorRespuesta1 = if (registro.respuestaSeguridad1.isBlank()) "Ingresa la respuesta 1" else null
    val errorPregunta2 = when {
        registro.preguntaSeguridad2.isBlank() -> "Selecciona la segunda pregunta"
        registro.preguntaSeguridad2 == registro.preguntaSeguridad1 -> "Las preguntas deben ser distintas"
        else -> null
    }
    val errorRespuesta2 = if (registro.respuestaSeguridad2.isBlank()) "Ingresa la respuesta 2" else null
    val formularioValido = listOf(errorPregunta1, errorRespuesta1, errorPregunta2, errorRespuesta2).all { it == null }

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            intentoRegistro = false
            onRegistroExitoso()
            viewModel.consumirRegistroExitoso()
        }
    }

    PantallaBase(
        scrollable = false,
        mostrarFondo = false
    ) {
        EncabezadoRegistroAnimado()
        TarjetaBase(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            llenarAlto = true
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollPasoSeguridad),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicadorPasos(pasoActual = 4, totalPasos = 4)
                EncabezadoPantalla(
                    titulo = "Crear cuenta",
                    subtitulo = "Preguntas de seguridad"
                )

                ComboRegistro(
                    etiqueta = "Pregunta 1",
                    valor = registro.preguntaSeguridad1.ifBlank { "Seleccionar pregunta" },
                    opciones = preguntasCatalogo
                ) {
                    intentoRegistro = false
                    viewModel.actualizarPreguntaSeguridad1(it)
                }
                if (intentoRegistro) TextoErrorCampo(errorPregunta1)

                CampoSecretoContrabajo(
                    valor = registro.respuestaSeguridad1,
                    onValueChange = {
                        intentoRegistro = false
                        viewModel.actualizarRespuestaSeguridad1(it)
                    },
                    etiqueta = "Respuesta 1"
                )
                if (intentoRegistro) TextoErrorCampo(errorRespuesta1)

                ComboRegistro(
                    etiqueta = "Pregunta 2",
                    valor = registro.preguntaSeguridad2.ifBlank { "Seleccionar pregunta" },
                    opciones = opcionesPreguntaDos
                ) {
                    intentoRegistro = false
                    viewModel.actualizarPreguntaSeguridad2(it)
                }
                if (intentoRegistro) TextoErrorCampo(errorPregunta2)

                CampoSecretoContrabajo(
                    valor = registro.respuestaSeguridad2,
                    onValueChange = {
                        intentoRegistro = false
                        viewModel.actualizarRespuestaSeguridad2(it)
                    },
                    etiqueta = "Respuesta 2"
                )
                if (intentoRegistro) TextoErrorCampo(errorRespuesta2)

                if (uiState.error != null) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonSecundario(
                    texto = "Volver",
                    onClick = onVolver,
                    modifier = Modifier.weight(1f)
                )
                BotonPrimario(
                    texto = "Registrarse",
                    enabled = formularioValido,
                    onClick = {
                        intentoRegistro = true
                        if (formularioValido) {
                            viewModel.registrarUsuario()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EncabezadoRegistroAnimado(modifier: Modifier = Modifier) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(durationMillis = 260)
                )
        ) {
            LogoContrabajo(
                tamanoPersonalizado = 148.dp,
                mostrarTitulo = false
            )
        }
    }
}

@Composable
private fun TextoErrorCampo(error: String?) {
    if (error == null) return
    Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.Medium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComboRegistro(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    modifier: Modifier = Modifier,
    onSeleccionar: (String) -> Unit
) {
    var desplegado by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = desplegado,
        onExpandedChange = { desplegado = !desplegado },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
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
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccionar(opcion)
                        desplegado = false
                    }
                )
            }
        }
    }
}

private fun mesLabel(mes: Int): String {
    return when (mes.coerceIn(1, 12)) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        else -> "Diciembre"
    }
}

private fun descomponerFecha(fecha: String): Triple<Int, Int, String> {
    val partes = fecha.split("-")
    if (partes.size != 3) return Triple(1, 1, "")

    val anio = partes[0].filter { it.isDigit() }.take(4)
    val mes = partes[1].toIntOrNull()?.coerceIn(1, 12) ?: 1
    val dia = partes[2].toIntOrNull()?.coerceIn(1, 31) ?: 1
    return Triple(dia, mes, anio)
}

private fun validarFechaNacimientoRegistro(
    fechaTexto: String,
    anioTexto: String,
    anioMinimo: Int,
    anioMaximo: Int
): String? {
    val anioNumerico = anioTexto.toIntOrNull()
    if (anioTexto.length == 4 && anioNumerico != null && (anioNumerico < anioMinimo || anioNumerico > anioMaximo)) {
        return "El año debe estar entre $anioMinimo y $anioMaximo"
    }
    if (fechaTexto.isBlank()) return "Ingresa una fecha de nacimiento válida"
    val fecha = runCatching { LocalDate.parse(fechaTexto) }.getOrNull()
        ?: return "Ingresa una fecha de nacimiento válida"
    val anio = fecha.year
    return when {
        anio !in anioMinimo..anioMaximo -> "El año debe estar entre $anioMinimo y $anioMaximo"
        else -> null
    }
}

private fun obtenerMaximoDiaDelMes(anio: Int, mes: Int): Int {
    return runCatching {
        LocalDate.of(anio, mes.coerceIn(1, 12), 1).lengthOfMonth()
    }.getOrElse { 31 }
}

private fun validarRut(runRaw: String, dvRaw: String): Boolean {
    val run = runRaw.filter { it.isDigit() }.take(8)
    if (run.length !in 7..8) return false
    val dv = dvRaw.trim().uppercase()
    if (dv.isBlank()) return false

    var suma = 0
    var multiplicador = 2
    for (i in run.length - 1 downTo 0) {
        suma += (run[i] - '0') * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }
    val resto = 11 - (suma % 11)
    val esperado = when (resto) {
        11 -> "0"
        10 -> "K"
        else -> resto.toString()
    }
    return dv == esperado
}

private fun validarContrasenaRegistro(contrasena: String): String? {
    return when {
        contrasena.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
        contrasena.none { it.isUpperCase() } -> "La contraseña debe incluir al menos 1 mayúscula"
        contrasena.none { it.isDigit() } -> "La contraseña debe incluir al menos 1 número"
        contrasena.none { !it.isLetterOrDigit() } -> "La contraseña debe incluir al menos 1 símbolo"
        else -> null
    }
}

private object FormatoRunVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(8)
        val formateado = formatearRunVisual(digitos)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                mapOriginalATransformado(digitos, formateado, offset, 0)

            override fun transformedToOriginal(offset: Int): Int =
                mapTransformadoAOriginal(digitos, formateado, offset, 0)
        }
        return TransformedText(AnnotatedString(formateado), offsetMapping)
    }
}

private object FormatoTelefonoVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(8)
        val primeraParte = digitos.take(4)
        val segundaParte = digitos.drop(4).take(4)
        val formateado = buildString {
            append(primeraParte)
            if (segundaParte.isNotBlank()) append(" $segundaParte")
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                mapOriginalATransformado(digitos, formateado, offset, 0)

            override fun transformedToOriginal(offset: Int): Int =
                mapTransformadoAOriginal(digitos, formateado, offset, 0)
        }
        return TransformedText(AnnotatedString(formateado), offsetMapping)
    }
}

private fun formatearRunVisual(digitos: String): String {
    if (digitos.isBlank()) return ""
    return digitos.reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

private fun mapOriginalATransformado(
    originalDigits: String,
    transformed: String,
    originalOffset: Int,
    transformedPrefixLength: Int
): Int {
    if (originalOffset <= 0) return transformedPrefixLength
    var consumidos = 0
    for (i in transformedPrefixLength until transformed.length) {
        if (transformed[i].isDigit()) consumidos++
        if (consumidos == originalOffset) return i + 1
    }
    return transformed.length
}

private fun mapTransformadoAOriginal(
    originalDigits: String,
    transformed: String,
    transformedOffset: Int,
    transformedPrefixLength: Int
): Int {
    if (transformedOffset <= transformedPrefixLength) return 0
    var consumidos = 0
    val limite = transformedOffset.coerceAtMost(transformed.length)
    for (i in transformedPrefixLength until limite) {
        if (transformed[i].isDigit()) consumidos++
    }
    return consumidos.coerceIn(0, originalDigits.length)
}

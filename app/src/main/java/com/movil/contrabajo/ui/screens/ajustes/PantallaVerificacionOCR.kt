package com.movil.contrabajo.ui.screens.ajustes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.movil.contrabajo.ui.theme.LocalColoresContrabajo
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.FondoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.theme.AzulPetroleo
import com.movil.contrabajo.ui.theme.GrisAcero
import com.movil.contrabajo.ui.theme.TurquesaBrillante
import com.movil.contrabajo.ui.theme.TurquesaSuave
import android.graphics.Bitmap
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

// ---------------------------------------------------------------------------
// Máquina de estados interna del flujo de verificación OCR
// ---------------------------------------------------------------------------
private enum class VerificacionPaso { BIENVENIDA, CAPTURA, ANALIZANDO, RESULTADO }

// ---------------------------------------------------------------------------
// Punto de entrada — reemplaza a la pantalla anterior de formulario manual
// ---------------------------------------------------------------------------
@Composable
fun PantallaVerificarCuentaTrabajador(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    var paso by rememberSaveable { mutableStateOf(VerificacionPaso.BIENVENIDA) }
    var resultadoExitoso by rememberSaveable { mutableStateOf(false) }
    var mensajeResultado by rememberSaveable { mutableStateOf("") }
    var bitmapCapturado by remember { mutableStateOf<Bitmap?>(null) }
    var solicitudBackendPendiente by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.limpiarEstadoVerificacion()
        viewModel.recargar()
    }

    // Escuchar respuesta del backend → transición a RESULTADO
    LaunchedEffect(uiState.mensajeVerificacion) {
        if (paso == VerificacionPaso.ANALIZANDO && uiState.mensajeVerificacion != null) {
            resultadoExitoso = true
            mensajeResultado = uiState.mensajeVerificacion!!
            solicitudBackendPendiente = false
            paso = VerificacionPaso.RESULTADO
        }
    }
    LaunchedEffect(uiState.errorVerificacion) {
        if (paso == VerificacionPaso.ANALIZANDO && uiState.errorVerificacion != null) {
            resultadoExitoso = false
            mensajeResultado = uiState.errorVerificacion!!
            solicitudBackendPendiente = false
            paso = VerificacionPaso.RESULTADO
        }
    }

    AnimatedContent(
        targetState = paso,
        transitionSpec = {
            (slideInHorizontally(tween(280)) { it } + fadeIn(tween(220)))
                .togetherWith(slideOutHorizontally(tween(250)) { -it } + fadeOut(tween(180)))
        },
        modifier = modifier,
        label = "verificacionPaso"
    ) { pasoActual ->
        when (pasoActual) {
            VerificacionPaso.BIENVENIDA -> PantallaBienvenidaVerificacion(
                onVolver = onVolver,
                onContinuar = {
                    viewModel.limpiarEstadoVerificacion()
                    bitmapCapturado = null
                    solicitudBackendPendiente = false
                    paso = VerificacionPaso.CAPTURA
                }
            )
            VerificacionPaso.CAPTURA -> PantallaCapturaOcr(
                runEsperado = uiState.runVerificacion,
                dvEsperado = uiState.dvVerificacion,
                onVolver = { paso = VerificacionPaso.BIENVENIDA },
                onImagenCapturada = { bitmap ->
                    viewModel.limpiarEstadoVerificacion()
                    bitmapCapturado = bitmap
                    solicitudBackendPendiente = false
                    paso = VerificacionPaso.ANALIZANDO
                },
                onErrorOcr = { mensaje ->
                    resultadoExitoso = false
                    mensajeResultado = mensaje
                    solicitudBackendPendiente = false
                    paso = VerificacionPaso.RESULTADO
                }
            )
            VerificacionPaso.ANALIZANDO -> {
                val bitmap = bitmapCapturado
                val contextLocal = LocalContext.current
                if (bitmap != null) {
                    PantallaAnalizandoDocumento()
                    LaunchedEffect(bitmap) {
                        procesarBitmapOcr(
                            contextLocal, bitmap,
                            uiState.runVerificacion, uiState.dvVerificacion,
                            uiState.usuario?.fechaNacimiento.orEmpty(),
                            onDatosExtraidos = { run, dv, numDoc ->
                                viewModel.actualizarRunVerificacion(run)
                                viewModel.actualizarDvVerificacion(dv)
                                viewModel.actualizarNumeroDocumentoVerificacion(numDoc)
                                solicitudBackendPendiente = true
                            },
                            onErrorOcr = { mensaje ->
                                resultadoExitoso = false
                                mensajeResultado = mensaje
                                solicitudBackendPendiente = false
                                paso = VerificacionPaso.RESULTADO
                            },
                            onExitoOcr = { }
                        )
                    }
                    // Cuando el OCR deja datos válidos listos, disparamos una sola solicitud al backend.
                    LaunchedEffect(solicitudBackendPendiente) {
                        if (paso == VerificacionPaso.ANALIZANDO && solicitudBackendPendiente) {
                            delay(5500L)
                            solicitudBackendPendiente = false
                            viewModel.solicitarVerificacionTrabajador()
                        }
                    }
                } else {
                    LaunchedEffect(Unit) {
                        solicitudBackendPendiente = false
                        paso = VerificacionPaso.CAPTURA
                    }
                }
            }
            VerificacionPaso.RESULTADO -> PantallaResultadoVerificacion(
                exitoso = resultadoExitoso,
                mensaje = mensajeResultado,
                onReintentar = {
                    viewModel.limpiarEstadoVerificacion()
                    bitmapCapturado = null
                    solicitudBackendPendiente = false
                    paso = VerificacionPaso.BIENVENIDA
                },
                onFinalizarExito = onVolver
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla 1: Bienvenida e instrucciones
// ---------------------------------------------------------------------------
@Composable
private fun PantallaBienvenidaVerificacion(
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    PantallaBase(mostrarFondo = false) {
        BarraSuperiorAjustes(
            titulo = "Verificar cuenta trabajador",
            onVolver = onVolver,
            iconoDerecha = Icons.Filled.Security
        )

        Spacer(Modifier.height(8.dp))

        IlustracionCedula(modifier = Modifier.align(Alignment.CenterHorizontally))

        Text(
            text = "Bienvenido a la verificación como trabajador",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AzulPetroleo,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Para continuar necesitas tener tu carnet de identidad a mano.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrisAcero,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Pasos de instrucción
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InstruccionItem("1", "Ten tu cédula de identidad a mano")
            InstruccionItem("2", "Encuadra el frente de la cédula en el marco")
            InstruccionItem("3", "La app leerá tu RUT y tu fecha de nacimiento automáticamente")
        }

        BotonPrimario(texto = "Continuar", onClick = onContinuar)
    }
}

@Composable
private fun InstruccionItem(numero: String, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AzulPetroleo),
            contentAlignment = Alignment.Center
        ) {
            Text(
                numero,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = GrisAcero)
    }
}

@Composable
private fun IlustracionCedula(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(175.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
            )
            .padding(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "REPÚBLICA DE CHILE",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Icon(
                Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
        // Cuerpo: foto + datos
        Row(
            modifier = Modifier.align(Alignment.Center).padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp, 62.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    Modifier.width(110.dp).height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                )
                Box(
                    Modifier.width(85.dp).height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        "RUN",
                        color = TurquesaBrillante,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        Modifier.width(80.dp).height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TurquesaBrillante.copy(alpha = 0.55f))
                    )
                }
            }
        }
        // Pie: N° documento
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("N° DOC", color = Color.White.copy(alpha = 0.45f), fontSize = 7.sp)
            Box(
                Modifier.width(95.dp).height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla 2: Cámara con guía de encuadre
// ---------------------------------------------------------------------------
@Composable
private fun PantallaCapturaOcr(
    runEsperado: String,
    dvEsperado: String,
    onVolver: () -> Unit,
    onImagenCapturada: (Bitmap) -> Unit,
    onErrorOcr: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var procesando by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraPermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermiso = granted }

    LaunchedEffect(Unit) {
        if (!cameraPermiso) permisosLauncher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember { PreviewView(context) }

    // Animación del marco (pulso suave)
    val infiniteTransition = rememberInfiniteTransition(label = "framePulse")
    val frameAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "frameAlpha"
    )

    // Configurar cámara cuando tenemos permiso
    LaunchedEffect(cameraPermiso) {
        if (!cameraPermiso) return@LaunchedEffect
        try {
            val cameraProvider = suspendCancellableCoroutine<ProcessCameraProvider> { cont ->
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    try { cont.resume(future.get()) }
                    catch (e: Exception) { cont.resumeWithException(e) }
                }, ContextCompat.getMainExecutor(context))
            }
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val captureUseCase = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCapture = captureUseCase
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                captureUseCase
            )
        } catch (_: Exception) {
            onErrorOcr("No se pudo iniciar la cámara.")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get()?.unbindAll()
            } catch (_: Exception) { }
        }
    }

    // Función para capturar bitmap de la cámara
    fun capturarBitmap() {
        val ic = imageCapture ?: return
        procesando = true
        ic.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    procesando = false
                    onImagenCapturada(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    procesando = false
                    onErrorOcr("No se pudo capturar la imagen. Intenta nuevamente.")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermiso) {
            // Preview de cámara
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            // Overlay oscuro con ventana transparente para la cédula
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { }
            ) {
                val cardW = 300.dp.toPx()
                val cardH = 190.dp.toPx()
                val left = (size.width - cardW) / 2f
                val top = (size.height - cardH) / 2f - 40.dp.toPx()
                drawRect(Color.Black.copy(alpha = 0.6f))
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
            }

            // Borde del marco (animado)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-40).dp)
                    .size(300.dp, 190.dp)
                    .border(
                        2.dp,
                        TurquesaBrillante.copy(alpha = frameAlpha),
                        RoundedCornerShape(12.dp)
                    )
            )

            // Controles superpuestos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Barra superior
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Text(
                        "Escanear cédula",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Instrucción + botón de captura
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Text(
                        "Encuadra el frente de tu cédula dentro del marco",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (procesando) Color.White.copy(alpha = 0.55f) else Color.White
                            )
                            .clickable(enabled = !procesando && imageCapture != null) {
                                capturarBitmap()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = AzulPetroleo,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Capturar",
                                modifier = Modifier.size(32.dp),
                                tint = AzulPetroleo
                            )
                        }
                    }
                }
            }
        } else {
            // Sin permiso de cámara
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Se requiere acceso a la cámara para escanear tu cédula.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))
                BotonPrimario(
                    texto = "Conceder permiso",
                    onClick = { permisosLauncher.launch(Manifest.permission.CAMERA) }
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla 2b: Analizando documento (OCR + validación)
// ---------------------------------------------------------------------------
@Composable
private fun PantallaAnalizandoDocumento() {
    Box(modifier = Modifier.fillMaxSize()) {
        FondoContrabajo(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(84.dp),
                    color = AzulPetroleo,
                    strokeWidth = 5.dp
                )
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = AzulPetroleo,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Estamos analizando tu documento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Un momento por favor mientras confirmamos los datos de tu cédula de identidad...",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisAcero,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla 3: Procesando (llamada al backend)
// ---------------------------------------------------------------------------
@Composable
private fun PantallaProcesandoVerificacion() {
    Box(modifier = Modifier.fillMaxSize()) {
        FondoContrabajo(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(84.dp),
                    color = AzulPetroleo,
                    strokeWidth = 5.dp
                )
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = AzulPetroleo,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Verificando tu documento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzulPetroleo,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Tu documento se está verificando. Esto puede tomar unos segundos...",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisAcero,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla 4: Resultado (éxito o rechazo)
// ---------------------------------------------------------------------------
@Composable
private fun PantallaResultadoVerificacion(
    exitoso: Boolean,
    mensaje: String,
    onReintentar: () -> Unit,
    onFinalizarExito: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "resultadoAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 80f,
        animationSpec = tween(420),
        label = "resultadoTranslateY"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        FondoContrabajo(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BarraSuperiorAjustes(
                titulo = if (exitoso) "Verificación exitosa" else "Verificación fallida",
                onVolver = if (exitoso) onFinalizarExito else onReintentar,
                iconoDerecha = if (exitoso) Icons.Filled.CheckCircle else Icons.Filled.Cancel
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        this.alpha = alpha
                        translationY = translateY
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Ícono de resultado
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                if (exitoso) LocalColoresContrabajo.current.exitoContenedor else MaterialTheme.colorScheme.errorContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (exitoso) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = if (exitoso) LocalColoresContrabajo.current.exito else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(68.dp)
                        )
                    }
                    // Título
                    Text(
                        text = if (exitoso) "¡Documento verificado!" else "Documento rechazado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (exitoso) AzulPetroleo else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    // Detalle
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrisAcero,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    // Acción principal
                    if (exitoso) {
                        BotonPrimario(
                            texto = "Volver",
                            onClick = onFinalizarExito
                        )
                    } else {
                        BotonPrimario(
                            texto = "Intentar nuevamente",
                            onClick = onReintentar
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// OCR: captura y extracción de RUT + N° documento
// ---------------------------------------------------------------------------
private fun procesarImagenOcr(
    context: Context,
    imageCapture: ImageCapture,
    runEsperado: String,
    dvEsperado: String,
    onDatosExtraidos: (String, String, String) -> Unit,
    onErrorOcr: (String) -> Unit,
    onFinish: () -> Unit
) {
    val handler = android.os.Handler(android.os.Looper.getMainLooper())

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // Capturar rotación ANTES de cerrar la imagen
                val rotacionGrados = image.imageInfo.rotationDegrees
                val bitmap = image.toBitmap()
                image.close()

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                // Pasar la rotación real para que ML Kit corrija la orientación del texto
                val inputImage = InputImage.fromBitmap(bitmap, rotacionGrados)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val texto = visionText.text
                        val resultado = extraerDatosCedula(texto, runEsperado, dvEsperado)
                        when (resultado) {
                            is ResultadoOcr.Exito -> {
                                onDatosExtraidos(resultado.run, resultado.dv, resultado.numDoc)
                                onFinish()
                            }
                            is ResultadoOcr.RutNoEncontrado -> {
                                handler.postDelayed({
                                    onErrorOcr(
                                        "No se pudo leer el documento. Asegúrate de encuadrar bien " +
                                            "el frente de la cédula, con buena iluminación y sin reflejos. " +
                                            "Intenta nuevamente."
                                    )
                                    onFinish()
                                }, 3000L)
                            }
                            is ResultadoOcr.RutNoCoincide -> {
                                handler.postDelayed({
                                    onErrorOcr(
                                        "El RUT del carnet no coincide con el de tu cuenta.\n" +
                                            "Carnet leído: ${resultado.rutLeido}-${resultado.dvLeido}\n" +
                                            "Verifica que estás usando tu propio carnet."
                                    )
                                    onFinish()
                                }, 3000L)
                            }
                            is ResultadoOcr.DocNoEncontrado -> {
                                handler.postDelayed({
                                    onErrorOcr(
                                        "RUT verificado, pero no se pudo leer el N° de documento.\n" +
                                            "Asegúrate de que toda la cédula esté visible y bien iluminada."
                                    )
                                    onFinish()
                                }, 3000L)
                            }
                            is ResultadoOcr.DocumentoVencido -> {
                                handler.postDelayed({
                                    onErrorOcr(
                                        "Tu documento de identidad está vencido.\n" +
                                            "No es posible verificar una cédula de identidad caducada. " +
                                            "Debes renovar tu documento en el Registro Civil."
                                    )
                                    onFinish()
                                }, 3000L)
                            }
                            is ResultadoOcr.FechaNacimientoNoCoincide -> {
                                handler.postDelayed({
                                    onErrorOcr(
                                        "La fecha de nacimiento del carnet no coincide con la registrada " +
                                            "en tu cuenta, por lo que no se pudo verificar tu documento.\n" +
                                            "Fecha leída del carnet: ${resultado.fechaLeida}\n" +
                                            "Si ingresaste una fecha de nacimiento incorrecta al registrarte, " +
                                            "contáctanos por el canal de soporte para corregirla."
                                    )
                                    onFinish()
                                }, 3000L)
                            }
                        }
                    }
                    .addOnFailureListener {
                        onErrorOcr("Error al procesar la imagen. Intenta nuevamente.")
                        onFinish()
                    }
            }

            override fun onError(exception: ImageCaptureException) {
                onErrorOcr("No se pudo capturar la imagen. Intenta nuevamente.")
                onFinish()
            }
        }
    )
}

// ---------------------------------------------------------------------------
// OCR: procesa un Bitmap ya capturado (nuevo flujo con previsualización)
// ---------------------------------------------------------------------------
private fun procesarBitmapOcr(
    context: Context,
    bitmap: Bitmap,
    runEsperado: String,
    dvEsperado: String,
    fechaNacimientoEsperada: String,
    onDatosExtraidos: (String, String, String) -> Unit,
    onErrorOcr: (String) -> Unit,
    onExitoOcr: () -> Unit
) {
    val handler = android.os.Handler(android.os.Looper.getMainLooper())
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            val texto = visionText.text
            val resultado = extraerDatosCedula(texto, runEsperado, dvEsperado, fechaNacimientoEsperada)
            when (resultado) {
                is ResultadoOcr.Exito -> {
                    onDatosExtraidos(resultado.run, resultado.dv, resultado.numDoc)
                    onExitoOcr()
                }
                is ResultadoOcr.RutNoEncontrado -> {
                    handler.postDelayed({
                        onErrorOcr(
                            "No se pudo leer el documento. Asegúrate de encuadrar bien " +
                                "el frente de la cédula, con buena iluminación y sin reflejos. " +
                                "Intenta nuevamente."
                        )
                    }, 3000L)
                }
                is ResultadoOcr.RutNoCoincide -> {
                    handler.postDelayed({
                        onErrorOcr(
                            "El RUT del carnet no coincide con el de tu cuenta.\n" +
                                "Carnet leído: ${resultado.rutLeido}-${resultado.dvLeido}\n" +
                                "Verifica que estás usando tu propio carnet."
                        )
                    }, 3000L)
                }
                is ResultadoOcr.DocNoEncontrado -> {
                    handler.postDelayed({
                        onErrorOcr(
                            "RUT verificado, pero no se pudo leer el N° de documento.\n" +
                                "Asegúrate de que toda la cédula esté visible y bien iluminada."
                        )
                    }, 3000L)
                }
                is ResultadoOcr.DocumentoVencido -> {
                    handler.postDelayed({
                        onErrorOcr(
                            "Tu documento de identidad está vencido.\n" +
                                "No es posible verificar una cédula de identidad caducada. " +
                                "Debes renovar tu documento en el Registro Civil."
                        )
                    }, 3000L)
                }
                is ResultadoOcr.FechaNacimientoNoCoincide -> {
                    handler.postDelayed({
                        onErrorOcr(
                            "La fecha de nacimiento del carnet no coincide con la registrada " +
                                "en tu cuenta, por lo que no se pudo verificar tu documento.\n" +
                                "Fecha leída del carnet: ${resultado.fechaLeida}\n" +
                                "Si ingresaste una fecha de nacimiento incorrecta al registrarte, " +
                                "contáctanos por el canal de soporte para corregirla."
                        )
                    }, 3000L)
                }
            }
        }
        .addOnFailureListener {
            onErrorOcr("Error al procesar la imagen. Intenta nuevamente.")
        }
}

// ---------------------------------------------------------------------------
// Modelos de resultado de extracción OCR
// ---------------------------------------------------------------------------
private sealed class ResultadoOcr {
    data class Exito(val run: String, val dv: String, val numDoc: String) : ResultadoOcr()
    object RutNoEncontrado : ResultadoOcr()
    data class RutNoCoincide(val rutLeido: String, val dvLeido: String) : ResultadoOcr()
    object DocNoEncontrado : ResultadoOcr()
    object DocumentoVencido : ResultadoOcr()
    data class FechaNacimientoNoCoincide(val fechaLeida: String) : ResultadoOcr()
}

// ---------------------------------------------------------------------------
// Extracción robusta de RUT y N° documento del texto OCR
// ---------------------------------------------------------------------------
private fun extraerDatosCedula(
    texto: String,
    runEsperado: String,
    dvEsperado: String,
    fechaNacimientoEsperada: String = ""
): ResultadoOcr {
    // Normalizar: unir líneas y limpiar caracteres problemáticos del OCR
    val textoNorm = texto
        .replace("\r\n", " ")
        .replace("\n", " ")
        .replace("\r", " ")
        // OCR confunde O↔0, l↔1, I↔1 en el contexto numérico
        .replace(Regex("""(?<=\d)[Oo](?=[\d\s\-–])"""), "0")
        .replace(Regex("""(?<=[\d\s\-–])[Oo](?=\d)"""), "0")

    val rutCoincidencia = intentarExtraccion(textoNorm)
        ?: intentarExtraccion(texto) // intentar también con el texto original sin normalizar

    if (rutCoincidencia == null) return ResultadoOcr.RutNoEncontrado

    val (rutExtraido, dvExtraido) = rutCoincidencia

    if (rutExtraido != runEsperado || dvExtraido != dvEsperado.uppercase()) {
        return ResultadoOcr.RutNoCoincide(rutExtraido, dvExtraido)
    }

    // Validar fecha de caducidad del documento
    val fechaVencimiento = extraerFechaVencimiento(texto)
    if (fechaVencimiento != null) {
        val hoy = java.time.LocalDate.now()
        if (fechaVencimiento.isBefore(hoy)) {
            return ResultadoOcr.DocumentoVencido
        }
    }

    // Cotejar la fecha de nacimiento del documento contra la registrada en la cuenta
    val fechaRegistrada = parsearFechaRegistrada(fechaNacimientoEsperada)
    if (fechaRegistrada != null) {
        val fechaNacimientoLeida = extraerFechaNacimiento(texto)
        if (fechaNacimientoLeida != null && fechaNacimientoLeida != fechaRegistrada) {
            val formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return ResultadoOcr.FechaNacimientoNoCoincide(fechaNacimientoLeida.format(formato))
        }
    }

    // N° documento: 9 dígitos consecutivos que no sean el RUN mismo
    val runPadded = runEsperado.padStart(9, '0')
    // Intentar primero el formato visual de la cédula chilena: XXX.XXX.XXX
    // El lookahead negativo evita capturar accidentalmente la parte numérica de un RUT
    val docConPuntosRegex = Regex("""(\d{3})[.,](\d{3})[.,](\d{3})(?!\s*[-–—]\s*[0-9KkOo])""")
    val numDocConPuntos = docConPuntosRegex.findAll(texto)
        .map { m -> m.groupValues[1] + m.groupValues[2] + m.groupValues[3] }
        .firstOrNull { it != runPadded && it != runEsperado }

    // Fallback: 9 dígitos consecutivos sin separadores
    val numDocSinPuntos = if (numDocConPuntos == null) {
        Regex("""\b(\d{9})\b""").findAll(texto)
            .map { it.groupValues[1] }
            .firstOrNull { it != runPadded && it != runEsperado }
    } else null

    val numDoc = numDocConPuntos ?: numDocSinPuntos ?: ""

    if (numDoc.isEmpty()) return ResultadoOcr.DocNoEncontrado

    return ResultadoOcr.Exito(rutExtraido, dvExtraido, numDoc)
}

/**
 * Meses abreviados tal como aparecen impresos en la cédula chilena.
 * Se incluye SET como variante de SEP por errores comunes del OCR.
 */
private val MESES_CEDULA = mapOf(
    "ENE" to 1, "FEB" to 2, "MAR" to 3, "ABR" to 4, "MAY" to 5, "JUN" to 6,
    "JUL" to 7, "AGO" to 8, "SEP" to 9, "SET" to 9, "OCT" to 10, "NOV" to 11, "DIC" to 12
)

/**
 * Extrae la fecha de nacimiento de la cédula chilena.
 * Solo busca fechas ancladas a la etiqueta "NACIMIENTO" para no capturar
 * por error las fechas de emisión o vencimiento del documento.
 * Soporta el formato impreso "DD MMM AAAA" (ej: 22 SEP 1990) y variantes
 * numéricas "DD MM AAAA" con distintos separadores.
 * Retorna la fecha como LocalDate o null si no se encuentra.
 */
private fun extraerFechaNacimiento(texto: String): java.time.LocalDate? {
    val textoNorm = texto
        .replace("\r\n", " ")
        .replace("\n", " ")
        .replace("\r", " ")

    // Patrón 1: NACIMIENTO seguido de DD MMM AAAA (mes abreviado en español)
    val mesesAlt = MESES_CEDULA.keys.joinToString("|")
    val patronMesTexto = Regex(
        """(?i)nacimiento\s+.{0,20}?(\d{1,2})\s+($mesesAlt)[A-ZÁÉÍÓÚ]*\.?\s+(\d{4})""",
        RegexOption.IGNORE_CASE
    )
    patronMesTexto.find(textoNorm)?.let { m ->
        val mes = MESES_CEDULA[m.groupValues[2].uppercase()] ?: return@let
        return try {
            java.time.LocalDate.of(m.groupValues[3].toInt(), mes, m.groupValues[1].toInt())
        } catch (e: Exception) {
            null
        }
    }

    // Patrón 2: NACIMIENTO seguido de DD MM AAAA (numérico, año de 4 dígitos)
    val patronNumerico = Regex(
        """(?i)nacimiento\s+.{0,20}?(\d{1,2})[\s./-](\d{1,2})[\s./-](\d{4})""",
        RegexOption.IGNORE_CASE
    )
    patronNumerico.find(textoNorm)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }

    return null
}

/**
 * Parsea la fecha de nacimiento registrada en la cuenta del usuario.
 * El backend la entrega en formato ISO (yyyy-MM-dd); se aceptan además
 * variantes dd/MM/yyyy y dd-MM-yyyy por robustez.
 */
private fun parsearFechaRegistrada(valor: String): java.time.LocalDate? {
    val limpio = valor.trim()
    if (limpio.isEmpty()) return null
    // Formato ISO: yyyy-MM-dd (el que persiste el backend y SQLite)
    try {
        return java.time.LocalDate.parse(limpio)
    } catch (_: Exception) { }
    // Variantes dd/MM/yyyy o dd-MM-yyyy
    Regex("""(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{4})""").find(limpio)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }
    return null
}

/**
 * Extrae la fecha de vencimiento de la cédula chilena.
 * Busca patrones como "FECHA DE VENCIMIENTO" seguido de DD MM AAAA,
 * "VÁLIDA HASTA DD/MM/AAAA", "HASTA DD.MM.AAAA", etc.
 * Retorna la fecha como LocalDate o null si no se encuentra.
 */
private fun extraerFechaVencimiento(texto: String): java.time.LocalDate? {
    val textoNorm = texto
        .replace("\r\n", " ")
        .replace("\n", " ")
        .replace("\r", " ")

    // Patrón 1: FECHA DE VENCIMIENTO seguido de DD MM AAAA (con o sin separadores)
    val patron1 = Regex(
        """(?i)fecha\s+de\s+vencimiento\s+(\d{1,2})[\s./-](\d{1,2})[\s./-](\d{2,4})""",
        RegexOption.IGNORE_CASE
    )
    patron1.find(textoNorm)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }

    // Patrón 2: FECHA DE VENCIMIENTO con la fecha en la siguiente línea/espacio
    // Busca "FECHA DE VENCIMIENTO" y luego busca la fecha en los siguientes 20 caracteres
    val patron2 = Regex(
        """(?i)fecha\s+de\s+vencimiento\s+.{0,20}?(\d{1,2})[\s./-](\d{1,2})[\s./-](\d{2,4})""",
        RegexOption.IGNORE_CASE
    )
    patron2.find(textoNorm)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }

    // Patrón 3: VÁLIDA HASTA DD/MM/AAAA o VÁLIDA HASTA DD.MM.AAAA
    val patron3 = Regex(
        """(?i)valida\s+hasta\s+(\d{1,2})[/.\-](\d{1,2})[/.\-](\d{2,4})""",
        RegexOption.IGNORE_CASE
    )
    patron3.find(textoNorm)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }

    // Patrón 4: HASTA DD/MM/AAAA o HASTA DD.MM.AAAA
    val patron4 = Regex(
        """(?i)hasta\s+(\d{1,2})[/.\-](\d{1,2})[/.\-](\d{2,4})""",
        RegexOption.IGNORE_CASE
    )
    patron4.find(textoNorm)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }

    // Patrón 5: Buscar "VENCIMIENTO" y luego una fecha cercana
    val patron5 = Regex(
        """(?i)vencimiento\s+.{0,20}?(\d{1,2})[\s./-](\d{1,2})[\s./-](\d{2,4})""",
        RegexOption.IGNORE_CASE
    )
    patron5.find(textoNorm)?.let { m ->
        return parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
    }

    // Patrón 6: Solo fecha en formato DD/MM/AAAA (como último recurso)
    val patron6 = Regex("""(\d{2})[/.\-](\d{2})[/.\-](\d{4})""")
    patron6.findAll(textoNorm).forEach { m ->
        val fecha = parsearFecha(m.groupValues[1], m.groupValues[2], m.groupValues[3])
        if (fecha != null) return fecha
    }

    return null
}

/**
 * Parsea una fecha desde componentes día, mes, año.
 * Maneja años de 2 o 4 dígitos (si son 2, asume siglo XXI).
 * Retorna null si la fecha es inválida.
 */
private fun parsearFecha(diaStr: String, mesStr: String, anioStr: String): java.time.LocalDate? {
    return try {
        val dia = diaStr.toInt()
        val mes = mesStr.toInt()
        val anio = if (anioStr.length == 2) {
            2000 + anioStr.toInt()
        } else {
            anioStr.toInt()
        }
        java.time.LocalDate.of(anio, mes, dia)
    } catch (e: Exception) {
        null
    }
}

/**
 * Intenta encontrar un RUT chileno en el texto usando múltiples patrones.
 * Retorna (run_solo_digitos, dv_uppercase) o null si no encuentra ninguno.
 */
private fun intentarExtraccion(texto: String): Pair<String, String>? {
    // Patrón 1 — con puntos: 12.345.678-9 o 12.345.678-K
    Regex("""(\d{1,2})[.,](\d{3})[.,](\d{3})\s*[-–—]\s*([0-9KkOo])""")
        .find(texto)?.let { m ->
            val run = m.groupValues[1] + m.groupValues[2] + m.groupValues[3]
            val dv = m.groupValues[4].replace("O", "0").replace("o", "0").uppercase()
            return Pair(run, dv)
        }

    // Patrón 2 — sin puntos: 12345678-9
    Regex("""(\d{7,8})\s*[-–—]\s*([0-9KkOo])(?!\d)""")
        .find(texto)?.let { m ->
            val run = m.groupValues[1]
            val dv = m.groupValues[2].replace("O", "0").replace("o", "0").uppercase()
            return Pair(run, dv)
        }

    // Patrón 3 — con espacios en lugar de puntos: 12 345 678-9
    Regex("""(\d{1,2})\s(\d{3})\s(\d{3})\s*[-–—]\s*([0-9KkOo])""")
        .find(texto)?.let { m ->
            val run = m.groupValues[1] + m.groupValues[2] + m.groupValues[3]
            val dv = m.groupValues[4].replace("O", "0").replace("o", "0").uppercase()
            return Pair(run, dv)
        }

    // Patrón 4 — sin separador de grupos, solo dígitos seguidos de espacio y DV
    // Útil cuando la cámara captura la zona MRZ
    Regex("""(\d{7,8})\s([0-9KkOo])(?!\w)""")
        .find(texto)?.let { m ->
            val run = m.groupValues[1]
            val dv = m.groupValues[2].replace("O", "0").replace("o", "0").uppercase()
            return Pair(run, dv)
        }

    return null
}

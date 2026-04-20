package com.movil.contrabajo.ui.components

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.ui.navigation.RutasApp
import com.movil.contrabajo.ui.theme.AzulPetroleo
import com.movil.contrabajo.ui.theme.AzulPetroleoOscuro
import com.movil.contrabajo.ui.theme.Blanco
import com.movil.contrabajo.ui.theme.CoralSuave
import com.movil.contrabajo.ui.theme.GrisLinea
import com.movil.contrabajo.ui.theme.SombraPetroleo
import com.movil.contrabajo.ui.theme.TurquesaBrillante
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.drawWithContent
import kotlinx.coroutines.delay

@Composable
fun FondoContrabajo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEAF7FA),
                        Color(0xFFDFEFF4),
                        Color(0xFFD2E6EE),
                        Color(0xFFC8DEE8)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 208.dp, y = (-56).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF78C6D6).copy(alpha = 0.22f),
                            Color(0xFF4BAFC5).copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-74).dp, y = 530.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF7FBFD4).copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
fun PantallaBase(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    mostrarFondo: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    if (mostrarFondo) {
        FondoContrabajo()
    }
    val scrollModifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .then(scrollModifier)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content
    )
}

@Composable
fun LogoContrabajo(modifier: Modifier = Modifier, compacto: Boolean = false) {
    val size = if (compacto) 66.dp else 92.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compacto) 4.dp else 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(TurquesaBrillante, AzulPetroleoOscuro)
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = 0.75f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (compacto) "C" else "Cb",
                style = if (compacto) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                color = Blanco,
                fontWeight = FontWeight.Bold
            )
        }
        if (!compacto) {
            Text(
                text = "Contrabajo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TarjetaBase(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun EncabezadoPantalla(
    titulo: String,
    subtitulo: String? = null,
    centrado: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (centrado) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (!subtitulo.isNullOrBlank()) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun CampoContrabajo(
    valor: String,
    onValueChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
            focusedContainerColor = Color.White,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun CampoSecretoContrabajo(
    valor: String,
    onValueChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Ocultar" else "Mostrar"
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
            focusedContainerColor = Color.White,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun IndicadorPasos(pasoActual: Int, totalPasos: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(totalPasos) { index ->
                val activo = index + 1 <= pasoActual
                Surface(
                    shape = CircleShape,
                    color = if (activo) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (activo) Blanco else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalPasos) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (index + 1 <= pasoActual) MaterialTheme.colorScheme.primary else GrisLinea.copy(alpha = 0.55f),
                            RoundedCornerShape(99.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun EtiquetaEstado(texto: String, modificador: Modifier = Modifier, enfatizada: Boolean = false) {
    Surface(
        modifier = modificador,
        shape = CircleShape,
        color = if (enfatizada) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (enfatizada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun TarjetaOfertaServicio(
    oferta: OfertaServicio,
    onAbrirServicio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(182.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                Color.White
                            )
                        ),
                        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                    )
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.Center)
                        .background(Color.White.copy(alpha = 0.96f), RoundedCornerShape(18.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔧",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                EtiquetaEstado(
                    texto = if (oferta.disponible) "Estoy disponible" else "No disponible",
                    modificador = Modifier.align(Alignment.TopStart)
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(oferta.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(oferta.descripcion, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(oferta.nombreTrabajador, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Ubicacion:", style = MaterialTheme.typography.titleSmall)
                    Text(oferta.ubicacionReferencia.ifBlank { "Region Metropolitana" }, style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Precio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(oferta.precioTexto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "${oferta.puntuacionPromedio} ★",
                        style = MaterialTheme.typography.titleMedium,
                        color = CoralSuave,
                        fontWeight = FontWeight.Bold
                    )
                }
                BotonPrimario(texto = "Contactar", onClick = onAbrirServicio, modifier = Modifier.padding(bottom = 14.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaMarketplaceCompacta(
    oferta: OfertaServicio,
    onAbrirServicio: () -> Unit,
    onMantenerPresionCambio: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interaccionTarjeta = remember { MutableInteractionSource() }
    val presionada by interaccionTarjeta.collectIsPressedAsState()
    var mantenerActiva by rememberSaveable(oferta.idOfertaServicio) { mutableStateOf(false) }
    val elevacionY by animateFloatAsState(
        targetValue = if (mantenerActiva) -10f else 0f,
        animationSpec = tween(durationMillis = if (mantenerActiva) 220 else 170),
        label = "elevacionTarjetaCompacta"
    )
    LaunchedEffect(presionada) {
        if (presionada) {
            delay(380)
            if (presionada) {
                mantenerActiva = true
            }
        } else {
            mantenerActiva = false
        }
    }

    LaunchedEffect(mantenerActiva) {
        onMantenerPresionCambio(mantenerActiva)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = elevacionY
            }
            .zIndex(if (mantenerActiva) 2f else 0f)
            .clip(RoundedCornerShape(20.dp))
            .drawWithContent {
                drawContent()
                if (mantenerActiva) {
                    val strokeFuerte = 2.8f
                    val strokeAura = 4.2f
                    val strokeInterno = 1.3f

                    val topLeftAura = Offset(strokeAura / 2f, strokeAura / 2f)
                    val sizeAura = androidx.compose.ui.geometry.Size(
                        width = (size.width - strokeAura).coerceAtLeast(0f),
                        height = (size.height - strokeAura).coerceAtLeast(0f)
                    )
                    val topLeftFuerte = Offset(strokeFuerte / 2f, strokeFuerte / 2f)
                    val sizeFuerte = androidx.compose.ui.geometry.Size(
                        width = (size.width - strokeFuerte).coerceAtLeast(0f),
                        height = (size.height - strokeFuerte).coerceAtLeast(0f)
                    )
                    val topLeftInterno = Offset(strokeInterno / 2f, strokeInterno / 2f)
                    val sizeInterno = androidx.compose.ui.geometry.Size(
                        width = (size.width - strokeInterno).coerceAtLeast(0f),
                        height = (size.height - strokeInterno).coerceAtLeast(0f)
                    )
                    val corner = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx(), 20.dp.toPx())

                    drawRoundRect(
                        color = TurquesaBrillante.copy(alpha = 0.36f),
                        topLeft = topLeftAura,
                        size = sizeAura,
                        cornerRadius = corner,
                        style = Stroke(width = strokeAura)
                    )
                    drawRoundRect(
                        color = AzulPetroleoOscuro,
                        topLeft = topLeftFuerte,
                        size = sizeFuerte,
                        cornerRadius = corner,
                        style = Stroke(width = strokeFuerte)
                    )
                    drawRoundRect(
                        color = TurquesaBrillante.copy(alpha = 0.78f),
                        topLeft = topLeftInterno,
                        size = sizeInterno,
                        cornerRadius = corner,
                        style = Stroke(width = strokeInterno)
                    )
                }
            }
            .combinedClickable(
                interactionSource = interaccionTarjeta,
                indication = null,
                onClick = onAbrirServicio,
                onLongClick = {}
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(184.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            ) {
                VistaPreviaImagenServicio(
                    referencia = oferta.fotoUrlReferencia,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )

                Surface(
                    modifier = Modifier.padding(10.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = oferta.nombreCategoria.ifBlank { "Servicio" },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = oferta.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = if (mantenerActiva) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = if (mantenerActiva) {
                        Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    } else {
                        Modifier
                    }
                )
                Text(
                    text = oferta.precioTexto,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = if (mantenerActiva) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = if (mantenerActiva) {
                        Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    } else {
                        Modifier
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilaValoracion(
                        valor = oferta.puntuacionPromedio,
                        tamanoEstrella = 16.dp
                    )
                    if (oferta.trabajadorVerificado) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Trabajador verificado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = formatearFechaPublicacionCompacta(oferta.fechaPublicacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun VistaPreviaImagenServicio(
    referencia: String,
    modifier: Modifier = Modifier
) {
    var errorRemoto by remember(referencia) { mutableStateOf(false) }
    if ((referencia.startsWith("http://") || referencia.startsWith("https://")) && !errorRemoto) {
        AsyncImage(
            model = referencia,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            onError = { errorRemoto = true }
        )
    } else if (referencia.startsWith("content://") || referencia.startsWith("file://") || referencia.startsWith("android.resource://")) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    clipToOutline = true
                }
            },
            update = { imageView ->
                runCatching { imageView.setImageURI(Uri.parse(referencia)) }
                if (imageView.drawable == null) {
                    imageView.setImageDrawable(null)
                }
            }
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun FilaValoracion(
    valor: Double,
    tamanoEstrella: Dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = String.format("%.1f", valor),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        repeat(5) { index ->
            val relleno = (valor - index).coerceIn(0.0, 1.0).toFloat()
            EstrellaFraccion(
                fraccion = relleno,
                tamano = tamanoEstrella
            )
        }
    }
}

@Composable
private fun EstrellaFraccion(
    fraccion: Float,
    tamano: Dp
) {
    Box(modifier = Modifier.size(tamano)) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = Color(0xFFB0B7BF),
            modifier = Modifier.matchParentSize()
        )
        if (fraccion > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .clip(RectangleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC93C),
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
fun TarjetaChat(chat: ChatCita, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(TurquesaBrillante, AzulPetroleo)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.nombreContacto.take(1).uppercase(),
                color = Blanco,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(chat.nombreContacto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                chat.ultimoMensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = chat.horaUltimoMensaje.takeLast(5),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BarraInferior(actual: String, alNavegar: (String) -> Unit) {
    NavigationBar(
        containerColor = TurquesaBrillante.copy(alpha = 0.9f),
        tonalElevation = 0.dp
    ) {
        listOf(
            Triple(RutasApp.Perfil.ruta, "Perfil", Icons.Default.Person),
            Triple(RutasApp.Principal.ruta, "Inicio", Icons.Default.Home),
            Triple(RutasApp.Chats.ruta, "Chats", Icons.Default.ChatBubbleOutline)
        ).forEach { (ruta, titulo, icono) ->
            val seleccionado = actual == ruta
            NavigationBarItem(
                selected = seleccionado,
                onClick = { alNavegar(ruta) },
                icon = {
                    Surface(
                        color = if (seleccionado) Blanco.copy(alpha = 0.22f) else Color.Transparent,
                        shape = CircleShape
                    ) {
                        Icon(
                            icono,
                            contentDescription = titulo,
                            modifier = Modifier.padding(8.dp),
                            tint = Blanco
                        )
                    }
                },
                label = { Text(titulo, color = Blanco) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blanco,
                    selectedTextColor = Blanco,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Blanco.copy(alpha = 0.82f),
                    unselectedTextColor = Blanco.copy(alpha = 0.82f)
                )
            )
        }
    }
}

@Composable
fun ResumenPerfilLinea(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(valor, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OverlayPantallaCarga(
    visible: Boolean,
    mensaje: String = "Cargando...",
    mostrarIndicador: Boolean = true,
    modoSuave: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 100)),
        exit = fadeOut(animationSpec = tween(durationMillis = 170))
    ) {
        if (modoSuave) {
            val animacion = rememberInfiniteTransition(label = "overlaySuaveCarga")
            val fase by animacion.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = LinearEasing)
                ),
                label = "faseOverlaySuave"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
                    .drawWithContent {
                        drawContent()
                        val desplazamiento = size.width * (fase * 2f)
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                start = Offset(desplazamiento - size.width, 0f),
                                end = Offset(desplazamiento, size.height)
                            )
                        )
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.44f)
                        .padding(bottom = 86.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.34f)
                )
            }
            return@AnimatedVisibility
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (mostrarIndicador) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChipAccion(texto: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 3.dp
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatearFechaPublicacionCompacta(fecha: String): String {
    if (fecha.length >= 16 && fecha[4] == '-' && fecha[7] == '-') {
        val dia = fecha.substring(8, 10)
        val mes = fecha.substring(5, 7)
        val hora = fecha.substring(11, 16)
        return "$dia/$mes $hora"
    }
    return fecha.take(16)
}

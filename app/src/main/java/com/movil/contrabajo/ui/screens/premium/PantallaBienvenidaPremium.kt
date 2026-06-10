package com.movil.contrabajo.ui.screens.premium

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import com.movil.contrabajo.ui.components.EstrellaPremiumAnimada
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.components.bordeBrilloPremium
import com.movil.contrabajo.ui.viewmodel.EstadoPagoPremium
import com.movil.contrabajo.ui.viewmodel.PremiumViewModel

private data class VentajaPremium(val icono: ImageVector, val titulo: String, val detalle: String)

private val ventajasPremium = listOf(
    VentajaPremium(Icons.Filled.Inventory2, "Más capacidad", "Publica hasta 5 servicios y mantén 3 activos al mismo tiempo."),
    VentajaPremium(Icons.Filled.AutoGraph, "Dashboard real", "Revisa contactos, cierres, días fuertes y métricas útiles de tu actividad."),
    VentajaPremium(Icons.Filled.Star, "Perfil destacado", "Tu perfil se muestra como Trabajador Premium con una distinción visible."),
    VentajaPremium(Icons.AutoMirrored.Filled.TrendingUp, "Mejor seguimiento", "Toma decisiones rápidas con estadísticas simples y accionables.")
)

@Composable
fun PantallaBienvenidaPremium(
    viewModel: PremiumViewModel,
    onPremiumActivado: () -> Unit,
    onIrAMenu: () -> Unit,
    onVolver: () -> Unit
) {
    val uiState = viewModel.uiState
    val yaEsPremium = uiState.esPremium && uiState.estadoPago != EstadoPagoPremium.LISTO
    var redireccionExito by rememberSaveable { mutableStateOf(false) }
    var verificacionAutomaticaPendiente by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.estadoPago) {
        if (uiState.estadoPago == EstadoPagoPremium.LISTO && !redireccionExito) {
            redireccionExito = true
            onPremiumActivado()
        }
    }

    LaunchedEffect(uiState.checkoutUrl) {
        val url = uiState.checkoutUrl ?: return@LaunchedEffect
        runCatching {
            verificacionAutomaticaPendiente = true
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            verificacionAutomaticaPendiente = false
            viewModel.reiniciarPago()
        }
        viewModel.consumirCheckoutUrl()
    }

    LaunchedEffect(uiState.estadoPago) {
        if (uiState.estadoPago != EstadoPagoPremium.ESPERANDO_CONFIRMACION) {
            verificacionAutomaticaPendiente = false
        }
    }

    DisposableEffect(lifecycleOwner, uiState.estadoPago, verificacionAutomaticaPendiente) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                uiState.estadoPago == EstadoPagoPremium.ESPERANDO_CONFIRMACION &&
                verificacionAutomaticaPendiente
            ) {
                verificacionAutomaticaPendiente = false
                viewModel.verificarEstadoPagoPremium()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PantallaBase(scrollable = true, mostrarFondo = true, respetarNavegacionInferior = false) {
            val pulso = rememberInfiniteTransition(label = "pulsoHeaderPremium")
            val escala by pulso.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1_500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "escalaHeaderPremium"
            )

            Spacer(Modifier.height(2.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EstrellaPremiumAnimada(
                            modifier = Modifier.scale(escala),
                            tamano = 52.dp
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Contrabajo Premium",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF114E61),
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Impulsa tu perfil de trabajador con más capacidad, mejor visibilidad y métricas reales de tu actividad.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 21.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF0E8894).copy(alpha = 0.10f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "\$1.990 CLP",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF0D5B66),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Precio referencial, puede variar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            TarjetaBase {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Lo que obtienes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF114E61)
                    )
                    ventajasPremium.forEachIndexed { index, ventaja ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(280, delayMillis = index * 70)) +
                                slideInVertically(tween(280, delayMillis = index * 70)) { it / 5 }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF0E8894).copy(alpha = 0.12f)
                                ) {
                                    Box(
                                        modifier = Modifier.size(42.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = ventaja.icono,
                                            contentDescription = null,
                                            tint = Color(0xFF0E8894)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ventaja.titulo,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ventaja.detalle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(3.dp))

            if (yaEsPremium) {
                BloquePremiumActivo(onIrAMenu = onIrAMenu)
            } else {
                BloqueActivacionPremium(
                    estadoPago = uiState.estadoPago,
                    error = uiState.errorPago,
                    onActivar = viewModel::procesarPagoPremium,
                    onVerificar = viewModel::verificarEstadoPagoPremium
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BloqueActivacionPremium(
    estadoPago: EstadoPagoPremium,
    error: String?,
    onActivar: () -> Unit,
    onVerificar: () -> Unit
) {
    val procesando = estadoPago in listOf(
        EstadoPagoPremium.CREANDO_PREFERENCIA,
        EstadoPagoPremium.VERIFICANDO
    )
    val esperandoConfirmacion = estadoPago == EstadoPagoPremium.ESPERANDO_CONFIRMACION

    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )
    }

    Text(
        text = if (esperandoConfirmacion) {
            "Cuando vuelvas desde Mercado Pago verificaremos el estado real del pago con el backend."
        } else {
            "Disponible para trabajadores verificados que quieran potenciar su perfil."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 2.dp)
    )

    Spacer(Modifier.height(4.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF0D5B66), Color(0xFF118A7E))
                )
            )
            .bordeBrilloPremium(cornerRadius = 20.dp, anchoTrazo = 2.6.dp)
            .clickable(enabled = !procesando) {
                if (esperandoConfirmacion) onVerificar() else onActivar()
            },
        contentAlignment = Alignment.Center
    ) {
        if (procesando) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Text(
                    text = if (estadoPago == EstadoPagoPremium.VERIFICANDO) {
                        "Verificando pago..."
                    } else {
                        "Abriendo checkout..."
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        } else {
            Text(
                text = if (esperandoConfirmacion) "Ya pagué, verificar estado" else "Quiero ser premium",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun PantallaPremiumActivado(
    viewModel: PremiumViewModel,
    onIniciarSesion: () -> Unit,
    onVolver: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PantallaBase(scrollable = true, mostrarFondo = true, respetarNavegacionInferior = false) {
            Spacer(Modifier.height(2.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Pago procesado correctamente",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0D5B66),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF17A673),
                            modifier = Modifier.size(78.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Ya eres Trabajador Premium",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tu perfil ya fue activado. Para terminar de habilitar tus beneficios Premium, vuelve a iniciar sesión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 21.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            BotonIniciarSesionPremium(onIniciarSesion = onIniciarSesion)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BloquePremiumActivo(onIrAMenu: () -> Unit) {
    TarjetaBase {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF17A673),
                modifier = Modifier.size(58.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Ya eres Trabajador Premium",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Entra a tu menú Premium para revisar tus estadísticas y tu rendimiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(Modifier.height(20.dp))
    BotonIrAMenuPremium(onIrAMenu = onIrAMenu)
}

@Composable
private fun BotonIniciarSesionPremium(onIniciarSesion: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF0D5B66), Color(0xFF118A8F))
                )
            )
            .clickable { onIniciarSesion() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Iniciar sesión",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun BotonIrAMenuPremium(onIrAMenu: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF0D5B66), Color(0xFF118A8F))
                )
            )
            .clickable { onIrAMenu() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ir al menú Premium",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}

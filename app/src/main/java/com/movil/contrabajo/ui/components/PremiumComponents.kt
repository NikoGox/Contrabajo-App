package com.movil.contrabajo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Borde brillante animado reutilizable (estilo del buscador activo de la pantalla principal),
 * en tonos cyan + verde claro para resaltar acciones Premium. Aplica el mismo patron de dos
 * trazos en degradado que se deslizan alrededor del contenedor con un pulso suave.
 *
 * Uso: `Modifier.bordeBrilloPremium(cornerRadius = 18.dp)`
 */
fun Modifier.bordeBrilloPremium(
    cornerRadius: Dp = 18.dp,
    anchoTrazo: Dp = 2.4.dp
): Modifier = composed {
    val glow = rememberInfiniteTransition(label = "glowPremium")
    val fase by glow.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = LinearEasing)
        ),
        label = "glowFasePremium"
    )
    val pulso by glow.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulsoPremium"
    )

    val cyan = Color(0xFF00E5D0)
    val cyanFuerte = Color(0xFF00BCD4)
    val verdeClaro = Color(0xFF5DE0B0)

    drawWithContent {
        drawContent()
        val faseX = size.width * (fase * 2.3f)
        val radio = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        val alphaHalo = (0.10f + 0.08f * pulso).coerceIn(0f, 0.22f)

        // Halo exterior suave
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    cyanFuerte.copy(alpha = alphaHalo),
                    cyan.copy(alpha = (alphaHalo * 1.2f).coerceAtMost(0.30f)),
                    verdeClaro.copy(alpha = alphaHalo),
                    cyanFuerte.copy(alpha = alphaHalo)
                ),
                start = Offset(faseX - (size.width * 2f), 0f),
                end = Offset(faseX, size.height)
            ),
            topLeft = Offset(-3f, -3f),
            size = Size(size.width + 6f, size.height + 6f),
            cornerRadius = CornerRadius(cornerRadius.toPx() + 1.dp.toPx(), cornerRadius.toPx() + 1.dp.toPx()),
            style = Stroke(width = (anchoTrazo.toPx() * 1.9f))
        )

        // Trazo principal nitido (sentido opuesto)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    cyanFuerte.copy(alpha = 0.95f),
                    cyan.copy(alpha = 1f),
                    verdeClaro.copy(alpha = 0.95f),
                    cyanFuerte.copy(alpha = 0.95f)
                ),
                start = Offset(size.width - faseX, 0f),
                end = Offset(-faseX, size.height)
            ),
            cornerRadius = radio,
            style = Stroke(width = anchoTrazo.toPx())
        )
    }
}

/**
 * Botón "P" Premium: octágono en degradado teal con la letra P en cyan brillante.
 * Es un placeholder visual fiel a la marca; puede reemplazarse luego por el asset real.
 */
@Composable
fun BotonPremiumP(
    modifier: Modifier = Modifier,
    tamano: Dp = 34.dp
) {
    val glow = rememberInfiniteTransition(label = "glowBotonP")
    val brillo by glow.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brilloP"
    )

    Box(
        modifier = modifier
            .size(tamano)
            .drawBehind {
                val w = size.width
                val h = size.height
                val r = min(w, h) / 2f
                val cx = w / 2f
                val cy = h / 2f
                // Octágono regular
                val path = Path()
                for (i in 0 until 8) {
                    val ang = Math.toRadians((45.0 * i) - 22.5)
                    val x = cx + r * cos(ang).toFloat()
                    val y = cy + r * sin(ang).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                // Relleno teal
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0E5A66), Color(0xFF0A3F49))
                    )
                )
                // Borde cyan con brillo pulsante
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF35E0F0).copy(alpha = brillo),
                            Color(0xFF14B8C8).copy(alpha = brillo)
                        )
                    ),
                    style = Stroke(width = 2.2.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "P",
            color = Color(0xFF3DE7F2),
            fontWeight = FontWeight.Black,
            fontSize = (tamano.value * 0.5f).sp
        )
    }
}

/**
 * Estrella de distinción Premium con brillo animado. Placeholder a refinar más adelante.
 */
@Composable
fun EstrellaPremiumAnimada(
    modifier: Modifier = Modifier,
    tamano: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .size(tamano)
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val rExt = min(size.width, size.height) / 2f
                val rInt = rExt * 0.42f
                val path = Path()
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) rExt else rInt
                    val ang = Math.toRadians((36.0 * i) - 90.0)
                    val x = cx + r * cos(ang).toFloat()
                    val y = cy + r * sin(ang).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                // Halo
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7CFFEA).copy(alpha = 0.28f),
                            Color(0xFF35E0F0).copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = rExt * 1.55f
                    )
                )

                // Relleno estrella estático
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8EFFFF),
                            Color(0xFF35E0F0),
                            Color(0xFF17B9C8),
                            Color(0xFF0A7E8D)
                        ),
                        start = Offset(cx - rExt * 0.65f, cy - rExt),
                        end = Offset(cx + rExt * 0.8f, cy + rExt)
                    )
                )

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD8FFFF).copy(alpha = 0.28f),
                            Color.Transparent,
                            Color(0xFF0A6977).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        start = Offset(cx - rExt * 0.25f, cy - rExt * 0.95f),
                        end = Offset(cx + rExt * 0.95f, cy + rExt * 0.95f)
                    ),
                    alpha = 0.95f
                )

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE7FFFF).copy(alpha = 0.34f),
                            Color.Transparent,
                            Color(0xFF0A5E6B).copy(alpha = 0.26f)
                        ),
                        start = Offset(cx - rExt * 0.10f, cy - rExt * 0.90f),
                        end = Offset(cx + rExt * 0.55f, cy + rExt * 0.85f)
                    ),
                    style = Stroke(width = rExt * 0.09f)
                )
            }
    )
}

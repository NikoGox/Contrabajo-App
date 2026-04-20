package com.movil.contrabajo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.movil.contrabajo.ui.theme.ContrabajoTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContrabajoTheme {
                PantallaSplash(
                    onFinalizar = {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun PantallaSplash(
    onFinalizar: () -> Unit
) {
    var progreso by remember { mutableFloatStateOf(0f) }
    val glow = rememberInfiniteTransition(label = "glowSplash")
    val faseGlow by glow.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4600, easing = LinearEasing)
        ),
        label = "faseGlowSplash"
    )

    LaunchedEffect(Unit) {
        repeat(20) {
            delay(70)
            progreso = (progreso + 0.03f).coerceAtMost(0.60f)
        }
        repeat(10) {
            delay(85)
            progreso = (progreso + 0.025f).coerceAtMost(0.85f)
        }
        repeat(4) {
            delay(95)
            progreso = (progreso + 0.03f).coerceAtMost(0.97f)
        }
        delay(160)
        progreso = 1f
        delay(120)
        onFinalizar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEAF7FA),
                        Color(0xFFDFEFF4),
                        Color(0xFFD2E6EE),
                        Color(0xFFC8DEE8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(142.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .drawWithContent {
                        drawContent()
                        val fase = size.width * (faseGlow * 2.2f)
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5).copy(alpha = 0.18f),
                                    Color(0xFF00BCD4).copy(alpha = 0.24f),
                                    Color(0xFF17A673).copy(alpha = 0.20f),
                                    Color(0xFF1E88E5).copy(alpha = 0.18f)
                                ),
                                start = Offset(fase - (size.width * 2f), 0f),
                                end = Offset(fase, size.height)
                            ),
                            topLeft = Offset(2.5.dp.toPx(), 2.5.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                width = size.width - 5.dp.toPx(),
                                height = size.height - 5.dp.toPx()
                            ),
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.ct_icon,
                    contentDescription = "Icono Contrabajo",
                    modifier = Modifier
                        .size(104.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Contrabajo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF11474C)
            )
            Text(
                text = "Cargando experiencia",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF35656A)
            )

            Spacer(modifier = Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Color(0xFF19A1A8),
                trackColor = Color.White.copy(alpha = 0.74f)
            )
            Text(
                text = "${(progreso * 100f).toInt().coerceIn(0, 100)}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF35656A),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

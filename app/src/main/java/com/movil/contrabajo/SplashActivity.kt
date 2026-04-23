package com.movil.contrabajo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var mensaje by remember { mutableStateOf("Iniciando app") }
    val versionApp = recordarVersionApp()
    val glow = rememberInfiniteTransition(label = "glowSplash")
    val pulsoGlow by glow.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1650, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulsoGlowSplash"
    )

    LaunchedEffect(Unit) {
        val etapas = listOf(
            Triple("Iniciando app", 0.20f, 420L),
            Triple("Preparando interfaz", 0.45f, 480L),
            Triple("Cargando servicios", 0.72f, 560L),
            Triple("Cargando mensajes", 0.88f, 520L),
            Triple("Finalizando", 1.00f, 420L)
        )
        etapas.forEach { (texto, objetivo, duracion) ->
            mensaje = texto
            val inicio = progreso
            val pasos = 10
            repeat(pasos) { paso ->
                delay(duracion / pasos)
                val factor = (paso + 1) / pasos.toFloat()
                progreso = (inicio + ((objetivo - inicio) * factor)).coerceIn(0f, 1f)
            }
        }
        delay(100)
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
                    .size(176.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD4DAE1).copy(alpha = 0.96f))
                    .drawWithContent {
                        drawContent()
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5).copy(alpha = (0.10f * pulsoGlow).coerceAtMost(0.16f)),
                                    Color(0xFF00BCD4).copy(alpha = (0.12f * pulsoGlow).coerceAtMost(0.18f)),
                                    Color(0xFF17A673).copy(alpha = (0.11f * pulsoGlow).coerceAtMost(0.17f)),
                                    Color(0xFF1E88E5).copy(alpha = (0.10f * pulsoGlow).coerceAtMost(0.16f))
                                )
                            ),
                            radius = ((size.minDimension / 2f) - 4.dp.toPx()) * pulsoGlow.coerceAtMost(1.03f),
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ct_icon_mini_t),
                    contentDescription = "Icono Contrabajo",
                    modifier = Modifier
                        .size(138.dp)
                        .clip(CircleShape)
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
                text = mensaje,
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
            Text(
                text = "v$versionApp",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF35656A),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

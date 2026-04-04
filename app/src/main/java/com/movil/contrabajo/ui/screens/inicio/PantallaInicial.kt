package com.movil.contrabajo.ui.screens.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.Usuario
import com.movil.contrabajo.ui.components.LogoContrabajo
import kotlinx.coroutines.delay

@Composable
fun PantallaInicial(
    obtenerSesionActiva: () -> Usuario?,
    irALogin: () -> Unit,
    irARegistro: () -> Unit,
    irAPrincipal: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var revisandoSesion by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        visible = true
        delay(650)
        if (obtenerSesionActiva() != null) {
            irAPrincipal()
        } else {
            revisandoSesion = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .padding(24.dp)
    ) {
        if (revisandoSesion) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    LogoContrabajo()
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = "Bienvenido a Contrabajo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Encuentra servicios confiables, conversa con prestadores y mueve la app desde una base moderna en Compose.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = irALogin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Iniciar sesion")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = irARegistro,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear cuenta")
                    }
                }
            }
        }
    }
}

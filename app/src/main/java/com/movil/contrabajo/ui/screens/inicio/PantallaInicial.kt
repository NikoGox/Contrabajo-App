package com.movil.contrabajo.ui.screens.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.InicioViewModel
import kotlinx.coroutines.delay

@Composable
fun PantallaInicial(
    viewModel: InicioViewModel,
    irALogin: () -> Unit,
    irARegistro: () -> Unit,
    irAPrincipal: () -> Unit
) {
    val uiState = viewModel.uiState
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(700)
        viewModel.revisarSesionActiva()
    }

    LaunchedEffect(uiState.sesionActivaDetectada, uiState.revisandoSesion) {
        if (!uiState.revisandoSesion && uiState.sesionActivaDetectada) {
            irAPrincipal()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.revisandoSesion) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 })
            ) {
                PantallaBase(
                    modifier = Modifier.fillMaxSize(),
                    scrollable = false
                ) {
                    Spacer(modifier = Modifier.weight(0.35f))
                    Text(
                        text = "Bienvenido a",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally))
                    TarjetaBase {
                        Text(
                            text = "Servicios y oficios cerca de ti",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Encuentra trabajadores, revisa publicaciones destacadas y entra rapido a tus conversaciones desde una experiencia simple y directa.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BotonPrimario(texto = "Iniciar sesion", onClick = irALogin)
                        BotonSecundario(texto = "Crear cuenta", onClick = irARegistro)
                    }
                    Text(
                        text = "v0.4.1-Pre-Alpha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.weight(0.30f))
                }
            }
        }
    }
}

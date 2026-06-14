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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.contrabajo.R
import com.movil.contrabajo.recordarVersionApp
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
    val versionApp = recordarVersionApp()
    val cs = MaterialTheme.colorScheme

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
        when {
            uiState.revisandoSesion -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.errorConexion -> {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        cs.primary.copy(alpha = 0.18f),
                                        cs.surfaceVariant,
                                        cs.surfaceVariant.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ct_icon_mini_t),
                                contentDescription = "Logo Contrabajo",
                                modifier = Modifier.size(96.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = cs.surface.copy(alpha = 0.5f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Sin conexión al servidor",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = cs.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "No se pudo verificar tu sesión. Asegúrate de tener conexión a internet y de que el servidor esté disponible.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.revisarSesionActiva() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                            ) {
                                Text("Reintentar", fontWeight = FontWeight.SemiBold, color = cs.onPrimary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.cerrarSesionLocal() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = cs.primary
                                )
                            ) {
                                Text("Cerrar sesión", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            else -> {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        cs.primary.copy(alpha = 0.18f),
                                        cs.surfaceVariant,
                                        cs.surfaceVariant.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.weight(0.15f))

                            Text(
                                text = "Bienvenido a",
                                style = MaterialTheme.typography.titleLarge,
                                color = cs.onBackground.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ct_icon_mini_t),
                                contentDescription = "Logo Contrabajo",
                                modifier = Modifier.size(148.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Contrabajo",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = cs.primary
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = cs.surface.copy(alpha = 0.5f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Servicios y oficios cerca de ti",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = cs.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Encuentra trabajadores, revisa publicaciones destacadas y entra rápido a tus conversaciones.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = {
                                    viewModel.verificarBackend(onBackendDisponible = irALogin)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cs.primary
                                )
                            ) {
                                Text(
                                    text = "Comenzar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Versión $versionApp",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.primary.copy(alpha = 0.4f)
                            )

                            Spacer(modifier = Modifier.weight(0.18f))
                        }
                    }
                }
            }
        }

        // Modal de backend no disponible
        if (!uiState.backendDisponible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Servicios no disponibles",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Los servicios de Contrabajo no se encuentran disponibles actualmente, por favor intente mas tarde.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.reiniciarEstadoBackend() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                        ) {
                            Text("Entendido", fontWeight = FontWeight.SemiBold, color = cs.onPrimary)
                        }
                    }
                }
            }
        }


    }
}

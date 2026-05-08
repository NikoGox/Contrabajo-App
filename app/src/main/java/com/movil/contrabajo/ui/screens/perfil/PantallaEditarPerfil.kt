package com.movil.contrabajo.ui.screens.perfil

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel

@Composable
fun PantallaEditarPerfil(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val usuario = uiState.usuario
    val context = LocalContext.current
    val selectorFotoPerfilLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.actualizarFotoPerfil(uri.toString())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.recargar()
    }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        TarjetaBase {
            if (usuario == null) {
                Text(
                    text = "No hay sesion activa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            val fotoPerfil = usuario.fotoPerfilUrl.orEmpty()
                            if (fotoPerfil.isBlank()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = fotoPerfil,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            IconButton(onClick = { selectorFotoPerfilLauncher.launch(arrayOf("image/*")) }) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "Cambiar foto",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    CampoContrabajo(
                        valor = "${usuario.nombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}",
                        onValueChange = {},
                        etiqueta = "Nombre",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )
                    CampoContrabajo(
                        valor = "@${usuario.username}",
                        onValueChange = {},
                        etiqueta = "Usuario",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )
                    CampoContrabajo(
                        valor = uiState.correoPerfilInput,
                        onValueChange = viewModel::actualizarCorreoPerfil,
                        etiqueta = "Correo",
                        modifier = Modifier.fillMaxWidth()
                    )
                    CampoContrabajo(
                        valor = uiState.telefonoPerfilInput,
                        onValueChange = viewModel::actualizarTelefonoPerfil,
                        etiqueta = "Telefono",
                        modifier = Modifier.fillMaxWidth(),
                        prefijo = "+56 9"
                    )
                    CampoContrabajo(
                        valor = "${usuario.run}-${usuario.dv}",
                        onValueChange = {},
                        etiqueta = "RUN",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )
                    CampoContrabajo(
                        valor = run {
                            val calleNumero = listOf(usuario.direccionCalle, usuario.direccionNumero)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            listOf(calleNumero, usuario.direccionComuna, usuario.direccionRegion)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                                .ifBlank { "Sin direccion" }
                        },
                        onValueChange = {},
                        etiqueta = "Direccion",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )
                    CampoContrabajo(
                        valor = usuario.fechaNacimiento,
                        onValueChange = {},
                        etiqueta = "Fecha de nacimiento",
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )

                    uiState.errorPerfilEdicion?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    uiState.mensajePerfilEdicion?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    BotonPrimario(
                        texto = "Guardar cambios",
                        onClick = viewModel::guardarEdicionPerfil,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = onVolver,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
    }

    OverlayPantallaCarga(
        visible = uiState.cargandoPantalla,
        mensaje = "Actualizando perfil..."
    )
}

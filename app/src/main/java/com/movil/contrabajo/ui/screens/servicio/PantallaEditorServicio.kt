package com.movil.contrabajo.ui.screens.servicio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaEditorServicio(
    modo: String,
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    var formularioInicializado by rememberSaveable { mutableStateOf(false) }
    var formularioActivoVisto by rememberSaveable { mutableStateOf(false) }
    var categoriasDesplegadas by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUriTexto by rememberSaveable { mutableStateOf<String?>(null) }

    val selectorFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }

            viewModel.actualizarFotoServicio(
                uriLocal = uri.toString(),
                nombreArchivo = obtenerNombreArchivo(context, uri.toString()),
                mimeType = context.contentResolver.getType(uri).orEmpty()
            )
        }
    }
    val tomarFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { guardado ->
        val uriTexto = pendingCameraUriTexto
        pendingCameraUriTexto = null
        if (guardado && !uriTexto.isNullOrBlank()) {
            val uri = Uri.parse(uriTexto)
            viewModel.actualizarFotoServicio(
                uriLocal = uri.toString(),
                nombreArchivo = obtenerNombreArchivo(context, uri.toString()),
                mimeType = context.contentResolver.getType(uri).orEmpty().ifBlank { "image/jpeg" }
            )
        }
    }
    val capturarDesdeCamara = {
        val uri = crearUriTemporalFoto(context)
        pendingCameraUriTexto = uri.toString()
        tomarFotoLauncher.launch(uri)
    }
    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) capturarDesdeCamara()
    }

    LaunchedEffect(modo) {
        if (modo == "editar") {
            viewModel.mostrarFormularioEdicion()
        } else {
            viewModel.mostrarFormularioCreacion()
        }
        formularioInicializado = true
    }

    LaunchedEffect(uiState.mostrandoFormularioServicio, formularioInicializado) {
        if (!formularioInicializado) return@LaunchedEffect
        if (uiState.mostrandoFormularioServicio) {
            formularioActivoVisto = true
        } else if (formularioActivoVisto) {
            formularioActivoVisto = false
            onVolver()
        }
    }

    PantallaBase(
        modifier = modifier,
        mostrarFondo = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.cancelarFormularioServicio() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Text(
                text = if (modo == "editar") "Editar servicio" else "Crear servicio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        TarjetaBase {
            CampoContrabajo(
                valor = uiState.formularioServicio.titulo,
                onValueChange = viewModel::actualizarTituloServicio,
                etiqueta = "Titulo del servicio"
            )
            OutlinedTextField(
                value = uiState.formularioServicio.descripcion,
                onValueChange = viewModel::actualizarDescripcionServicio,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripcion del servicio") },
                minLines = 4
            )
            CampoContrabajo(
                valor = uiState.formularioServicio.precioTexto,
                onValueChange = viewModel::actualizarPrecioServicio,
                etiqueta = "Precio (texto libre)"
            )

            val categoriaSeleccionada = uiState.categorias
                .firstOrNull { it.idCategoriaServicio == uiState.formularioServicio.idCategoriaServicio }
                ?.nombre
                .orEmpty()
            ExposedDropdownMenuBox(
                expanded = categoriasDesplegadas,
                onExpandedChange = { categoriasDesplegadas = !categoriasDesplegadas }
            ) {
                OutlinedTextField(
                    value = categoriaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriasDesplegadas) }
                )
                ExposedDropdownMenu(
                    expanded = categoriasDesplegadas,
                    onDismissRequest = { categoriasDesplegadas = false }
                ) {
                    uiState.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                viewModel.actualizarCategoriaServicio(categoria.idCategoriaServicio)
                                categoriasDesplegadas = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Foto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { selectorFotoLauncher.launch(arrayOf("image/*")) },
                    shape = MaterialTheme.shapes.small,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Agregar foto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            if (
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                capturarDesdeCamara()
                            } else {
                                permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    shape = MaterialTheme.shapes.small,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Abrir camara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = uiState.formularioServicio.foto?.nombreArchivo?.ifBlank { "Imagen lista" }
                        ?: "Sin imagen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.errorServicio != null) {
                Text(
                    uiState.errorServicio,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            BotonPrimario(
                texto = if (modo == "editar") "Guardar cambios" else "Guardar servicio",
                onClick = viewModel::guardarServicio
            )
            BotonSecundario(
                texto = "Cancelar",
                onClick = { viewModel.cancelarFormularioServicio() }
            )
        }
    }
}

private fun obtenerNombreArchivo(context: android.content.Context, uriTexto: String): String {
    val uri = Uri.parse(uriTexto)
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index).orEmpty()
        }
    }
    return uri.lastPathSegment.orEmpty()
}

private fun crearUriTemporalFoto(context: android.content.Context): Uri {
    val directorio = File(context.cacheDir, "capturas_servicio").apply {
        if (!exists()) mkdirs()
    }
    val archivo = File.createTempFile(
        "servicio_${System.currentTimeMillis()}_",
        ".jpg",
        directorio
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}

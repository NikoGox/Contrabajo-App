package com.movil.contrabajo.ui.screens.servicio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
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
    var mostrarConfirmacionEliminar by rememberSaveable { mutableStateOf(false) }

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

            val foto = persistirFotoEnApp(context, uri)
            viewModel.actualizarFotoServicio(
                uriLocal = foto.uriLocal,
                nombreArchivo = foto.nombreArchivo,
                mimeType = foto.mimeType
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
            val foto = persistirFotoEnApp(context, uri)
            viewModel.actualizarFotoServicio(
                uriLocal = foto.uriLocal,
                nombreArchivo = foto.nombreArchivo,
                mimeType = foto.mimeType
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
            SelectorTipoPrecio(
                tipoPrecio = uiState.formularioServicio.tipoPrecio,
                onSeleccionar = viewModel::actualizarTipoPrecioServicio
            )
            EntradaPrecio(
                tipoPrecio = uiState.formularioServicio.tipoPrecio,
                montoBase = uiState.formularioServicio.montoBase,
                onMontoChange = viewModel::actualizarPrecioServicio
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
            if (modo == "editar") {
                Button(
                    onClick = { mostrarConfirmacionEliminar = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar servicio")
                }
            }
        }
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            title = { Text("Eliminar servicio") },
            text = { Text("Esta accion eliminara tu publicacion. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacionEliminar = false
                    viewModel.eliminarServicio()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    OverlayPantallaCarga(
        visible = uiState.cargandoPantalla,
        mensaje = if (modo == "editar") "Guardando cambios..." else "Guardando servicio..."
    )
}

@Composable
private fun SelectorTipoPrecio(
    tipoPrecio: Int,
    onSeleccionar: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tipo de precio",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpcionTipoPrecio("Fijo", tipoPrecio == TipoPrecio.FIJO) { onSeleccionar(TipoPrecio.FIJO) }
            OpcionTipoPrecio("/hora", tipoPrecio == TipoPrecio.POR_HORA) { onSeleccionar(TipoPrecio.POR_HORA) }
            OpcionTipoPrecio("Desde", tipoPrecio == TipoPrecio.DESDE) { onSeleccionar(TipoPrecio.DESDE) }
            OpcionTipoPrecio("Contactar", tipoPrecio == TipoPrecio.CONTACTAR) { onSeleccionar(TipoPrecio.CONTACTAR) }
        }
    }
}

@Composable
private fun RowScope.OpcionTipoPrecio(
    texto: String,
    activa: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = if (activa) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                color = if (activa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EntradaPrecio(
    tipoPrecio: Int,
    montoBase: Int,
    onMontoChange: (String) -> Unit
) {
    val textoMonto = if (montoBase <= 0) "" else PrecioUtils.formatearMonto(montoBase)
    when (tipoPrecio) {
        TipoPrecio.CONTACTAR -> {
            OutlinedTextField(
                value = "Contactar para saber precio",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Precio") },
                readOnly = true,
                enabled = false
            )
        }

        TipoPrecio.DESDE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Desde",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                CampoMontoConMoneda(
                    valor = textoMonto,
                    onValueChange = onMontoChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        TipoPrecio.POR_HORA -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoMontoConMoneda(
                    valor = textoMonto,
                    onValueChange = onMontoChange,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "/hora",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            CampoMontoConMoneda(
                valor = textoMonto,
                onValueChange = onMontoChange
            )
        }
    }
}

@Composable
private fun CampoMontoConMoneda(
    valor: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        modifier = modifier.fillMaxWidth(),
        label = { Text("Monto") },
        leadingIcon = {
            Text(
                text = "$",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        singleLine = true
    )
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

private data class FotoPersistida(
    val uriLocal: String,
    val nombreArchivo: String,
    val mimeType: String
)

private fun persistirFotoEnApp(context: android.content.Context, uriOrigen: Uri): FotoPersistida {
    val contentResolver = context.contentResolver
    val mime = contentResolver.getType(uriOrigen).orEmpty().ifBlank { "image/jpeg" }
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime).orEmpty().ifBlank { "jpg" }
    val nombreBase = obtenerNombreArchivo(context, uriOrigen.toString())
        .substringBeforeLast(".")
        .ifBlank { "servicio_foto" }
        .replace(Regex("[^A-Za-z0-9_-]"), "_")
    val archivoDestino = File(
        File(context.filesDir, "fotos_servicio").apply { if (!exists()) mkdirs() },
        "${nombreBase}_${System.currentTimeMillis()}.$extension"
    )

    return runCatching {
        contentResolver.openInputStream(uriOrigen)?.use { input ->
            archivoDestino.outputStream().use { output -> input.copyTo(output) }
        }
        FotoPersistida(
            uriLocal = Uri.fromFile(archivoDestino).toString(),
            nombreArchivo = archivoDestino.name,
            mimeType = mime
        )
    }.getOrElse {
        FotoPersistida(
            uriLocal = uriOrigen.toString(),
            nombreArchivo = obtenerNombreArchivo(context, uriOrigen.toString()),
            mimeType = mime
        )
    }
}

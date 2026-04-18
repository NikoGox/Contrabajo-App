package com.movil.contrabajo.ui.screens.autenticacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.CampoSecretoContrabajo
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.IndicadorPasos
import com.movil.contrabajo.ui.components.LogoContrabajo
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.RegistroViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaRegistroPasoUno(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    val registro = viewModel.uiState.registro
    val partesFecha = remember(registro.fechaNacimiento) { descomponerFecha(registro.fechaNacimiento) }

    var diaSeleccionado by rememberSaveable(registro.fechaNacimiento) { mutableIntStateOf(partesFecha.first) }
    var mesSeleccionado by rememberSaveable(registro.fechaNacimiento) { mutableIntStateOf(partesFecha.second) }
    var anioInput by rememberSaveable(registro.fechaNacimiento) { mutableStateOf(partesFecha.third) }

    fun actualizarFechaDesdePartes() {
        val anioLimpio = anioInput.filter { it.isDigit() }.take(4)
        anioInput = anioLimpio
        if (anioLimpio.length == 4) {
            viewModel.actualizarFechaNacimiento(
                "%04d-%02d-%02d".format(anioLimpio.toInt(), mesSeleccionado, diaSeleccionado)
            )
        } else {
            viewModel.actualizarFechaNacimiento("")
        }
    }

    PantallaBase {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            IndicadorPasos(pasoActual = 1, totalPasos = 2)
            EncabezadoPantalla(
                titulo = "Crear cuenta",
                subtitulo = "Datos personales"
            )
            CampoContrabajo(registro.nombre, viewModel::actualizarNombre, "Nombre")
            CampoContrabajo(registro.apellidoPaterno, viewModel::actualizarApellidoPaterno, "Apellido paterno")
            CampoContrabajo(registro.apellidoMaterno, viewModel::actualizarApellidoMaterno, "Apellido materno")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CampoContrabajo(
                    valor = registro.run,
                    onValueChange = viewModel::actualizarRun,
                    etiqueta = "RUN",
                    modifier = Modifier.weight(1f),
                    visualTransformation = FormatoRunVisualTransformation
                )
                Text(
                    text = "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.08f)
                )
                CampoContrabajo(
                    valor = registro.dv,
                    onValueChange = viewModel::actualizarDv,
                    etiqueta = "DV",
                    modifier = Modifier.weight(0.35f)
                )
            }

            CampoContrabajo(
                valor = registro.telefono,
                onValueChange = viewModel::actualizarTelefono,
                etiqueta = "Telefono (+56)",
                visualTransformation = FormatoTelefonoVisualTransformation
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComboRegistro(
                    etiqueta = "Dia",
                    valor = diaSeleccionado.toString(),
                    opciones = (1..31).map { it.toString() },
                    modifier = Modifier.weight(0.30f)
                ) { seleccionado ->
                    diaSeleccionado = seleccionado.toIntOrNull() ?: diaSeleccionado
                    actualizarFechaDesdePartes()
                }
                ComboRegistro(
                    etiqueta = "Mes",
                    valor = mesLabel(mesSeleccionado),
                    opciones = (1..12).map { mesLabel(it) },
                    modifier = Modifier.weight(0.42f)
                ) { seleccionado ->
                    mesSeleccionado = (1..12).firstOrNull { mesLabel(it) == seleccionado } ?: mesSeleccionado
                    actualizarFechaDesdePartes()
                }
                CampoContrabajo(
                    valor = anioInput,
                    onValueChange = {
                        anioInput = it.filter { c -> c.isDigit() }.take(4)
                        actualizarFechaDesdePartes()
                    },
                    etiqueta = "Año",
                    modifier = Modifier.weight(0.28f)
                )
            }

            BotonPrimario(texto = "Siguiente", onClick = onContinuar)
        }
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

@Composable
fun PantallaRegistroPasoDos(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val uiState = viewModel.uiState
    val registro = uiState.registro

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            onRegistroExitoso()
            viewModel.consumirRegistroExitoso()
        }
    }

    PantallaBase {
        LogoContrabajo(modifier = Modifier.align(Alignment.CenterHorizontally), compacto = true)
        TarjetaBase {
            IndicadorPasos(pasoActual = 2, totalPasos = 2)
            EncabezadoPantalla(
                titulo = "Crear cuenta",
                subtitulo = "Datos de la cuenta"
            )
            CampoContrabajo(registro.username, viewModel::actualizarUsername, "Nombre de usuario")
            CampoContrabajo(registro.correo, viewModel::actualizarCorreo, "Correo electronico")
            CampoSecretoContrabajo(
                valor = registro.contrasena,
                onValueChange = viewModel::actualizarContrasena,
                etiqueta = "Contrasena"
            )
            CampoSecretoContrabajo(
                valor = registro.confirmarContrasena,
                onValueChange = viewModel::actualizarConfirmarContrasena,
                etiqueta = "Confirmar contrasena"
            )
            Text(
                text = "Acepto los terminos y condiciones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.error != null) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            BotonPrimario(
                texto = "Registrarse",
                onClick = viewModel::registrarUsuario
            )
        }
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComboRegistro(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    modifier: Modifier = Modifier,
    onSeleccionar: (String) -> Unit
) {
    var desplegado by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = desplegado,
        onExpandedChange = { desplegado = !desplegado },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = desplegado,
            onDismissRequest = { desplegado = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccionar(opcion)
                        desplegado = false
                    }
                )
            }
        }
    }
}

private fun mesLabel(mes: Int): String {
    return when (mes.coerceIn(1, 12)) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        else -> "Diciembre"
    }
}

private fun descomponerFecha(fecha: String): Triple<Int, Int, String> {
    val partes = fecha.split("-")
    if (partes.size != 3) return Triple(1, 1, "")

    val anio = partes[0].filter { it.isDigit() }.take(4)
    val mes = partes[1].toIntOrNull()?.coerceIn(1, 12) ?: 1
    val dia = partes[2].toIntOrNull()?.coerceIn(1, 31) ?: 1
    return Triple(dia, mes, anio)
}

private object FormatoRunVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(8)
        val formateado = formatearRunVisual(digitos)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                mapOriginalATransformado(digitos, formateado, offset, 0)

            override fun transformedToOriginal(offset: Int): Int =
                mapTransformadoAOriginal(digitos, formateado, offset, 0)
        }
        return TransformedText(AnnotatedString(formateado), offsetMapping)
    }
}

private object FormatoTelefonoVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(9)
        val prefijo = "+56 "
        val primeraParte = digitos.take(1)
        val segundaParte = digitos.drop(1).take(4)
        val terceraParte = digitos.drop(5).take(4)
        val formateado = buildString {
            append(prefijo)
            append(primeraParte)
            if (segundaParte.isNotBlank()) append(" $segundaParte")
            if (terceraParte.isNotBlank()) append(" $terceraParte")
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                mapOriginalATransformado(digitos, formateado, offset, prefijo.length)

            override fun transformedToOriginal(offset: Int): Int =
                mapTransformadoAOriginal(digitos, formateado, offset, prefijo.length)
        }
        return TransformedText(AnnotatedString(formateado), offsetMapping)
    }
}

private fun formatearRunVisual(digitos: String): String {
    if (digitos.isBlank()) return ""
    val primera = digitos.take(2)
    val segunda = digitos.drop(2).take(3)
    val tercera = digitos.drop(5).take(3)
    return buildString {
        append(primera)
        if (segunda.isNotBlank()) append(".$segunda")
        if (tercera.isNotBlank()) append(".$tercera")
    }
}

private fun mapOriginalATransformado(
    originalDigits: String,
    transformed: String,
    originalOffset: Int,
    transformedPrefixLength: Int
): Int {
    if (originalOffset <= 0) return transformedPrefixLength
    var consumidos = 0
    for (i in transformedPrefixLength until transformed.length) {
        if (transformed[i].isDigit()) consumidos++
        if (consumidos == originalOffset) return i + 1
    }
    return transformed.length
}

private fun mapTransformadoAOriginal(
    originalDigits: String,
    transformed: String,
    transformedOffset: Int,
    transformedPrefixLength: Int
): Int {
    if (transformedOffset <= transformedPrefixLength) return 0
    var consumidos = 0
    val limite = transformedOffset.coerceAtMost(transformed.length)
    for (i in transformedPrefixLength until limite) {
        if (transformed[i].isDigit()) consumidos++
    }
    return consumidos.coerceIn(0, originalDigits.length)
}

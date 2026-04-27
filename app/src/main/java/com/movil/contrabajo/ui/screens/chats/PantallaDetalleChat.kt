package com.movil.contrabajo.ui.screens.chats

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.BotonSecundario
import com.movil.contrabajo.ui.components.CampoContrabajo
import com.movil.contrabajo.ui.components.EtiquetaEstado
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PantallaDetalleChat(
    idChatCita: Long,
    viewModel: ChatsViewModel,
    onVolver: () -> Unit,
    onAbrirCita: (Long) -> Unit,
    onAbrirServicioAsociado: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(idChatCita) {
        viewModel.abrirChat(idChatCita)
    }

    val uiState = viewModel.uiState
    val chat = uiState.chatActivo
    val idUsuarioActual = uiState.idUsuarioActual
    val esCliente = chat != null && idUsuarioActual != null && chat.idCliente == idUsuarioActual
    val esTrabajador = chat != null && idUsuarioActual != null && chat.idTrabajador == idUsuarioActual
    val mostrarModalValoracion = uiState.mostrarModalValoracion && esCliente

    var mostrarModalCita by remember { mutableStateOf(false) }
    var mostrarMenuOpciones by remember { mutableStateOf(false) }
    var confirmarCerrarChat by remember { mutableStateOf(false) }
    var fechaSeleccionada by remember(chat?.idChatCita) { mutableStateOf<LocalDate?>(null) }
    var horaSeleccionada by remember(chat?.idChatCita) { mutableStateOf<LocalTime?>(null) }
    var comentarioInput by rememberSaveable(chat?.idChatCita) { mutableStateOf("") }
    var mostrarErroresCita by rememberSaveable(chat?.idChatCita) { mutableStateOf(false) }
    var citaResumenExpandida by rememberSaveable(chat?.idChatCita) { mutableStateOf(false) }
    val estadoListaMensajes = rememberLazyListState()
    val context = LocalContext.current

    val errorCita = validarFormularioCita(
        fecha = fechaSeleccionada,
        hora = horaSeleccionada,
        comentario = comentarioInput
    )
    val formularioCitaValido = errorCita == null

    LaunchedEffect(chat?.idChatCita, uiState.mensajesActivos.size) {
        val total = uiState.mensajesActivos.size
        if (total > 0) {
            estadoListaMensajes.scrollToItem(total - 1)
        }
    }

    PantallaBase(
        modifier = modifier,
        scrollable = false,
        mostrarFondo = false
    ) {
        CabeceraChat(
            chat = chat,
            onVolver = onVolver,
            menuAbierto = mostrarMenuOpciones,
            onCambiarMenu = { mostrarMenuOpciones = it },
            onFinalizarChat = { confirmarCerrarChat = true },
            onAbrirServicioAsociado = {
                val idOferta = chat?.idOfertaServicio
                if (idOferta != null) onAbrirServicioAsociado(idOferta)
            }
        )

        TarjetaBase(
            modifier = Modifier.weight(1f),
            llenarAlto = true
        ) {
            if (chat == null) {
                Text(
                    text = "No se pudo cargar la conversacion.",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                ResumenCitaChat(
                    chat = chat,
                    cita = uiState.citaActiva,
                    esCliente = esCliente,
                    esTrabajador = esTrabajador,
                    citaExpandida = citaResumenExpandida,
                    onCambiarCitaExpandida = { citaResumenExpandida = it },
                    onCrear = { mostrarModalCita = true },
                    onAbrirCita = { onAbrirCita(chat.idChatCita) },
                    onAceptarCitaTrabajador = viewModel::aceptarCitaTrabajador,
                    onRechazarCitaTrabajador = viewModel::rechazarCitaTrabajador,
                    onReenviarPropuestaCliente = viewModel::reenviarPropuestaCitaCliente,
                    onSolicitarInicioTrabajador = viewModel::solicitarInicioTrabajoTrabajador,
                    onAceptarInicioCliente = viewModel::aceptarInicioTrabajoCliente,
                    onSolicitarFinalizarTrabajador = viewModel::solicitarFinalizarTrabajoTrabajador,
                    onAceptarFinalizarCliente = viewModel::aceptarFinalizarTrabajoCliente
                )

                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = estadoListaMensajes,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.mensajesActivos, key = { _, mensaje -> mensaje.idMensajeChat }) { indice, mensaje ->
                        val mensajePrevio = uiState.mensajesActivos.getOrNull(indice - 1)
                        val fechaActual = fechaSoloMensaje(mensaje.fechaEnvio)
                        val fechaPrevia = mensajePrevio?.let { fechaSoloMensaje(it.fechaEnvio) }
                        val mostrarSeparadorDia = fechaActual != null && fechaActual != fechaPrevia
                        if (mostrarSeparadorDia) {
                            SeparadorDiaChat(fechaActual)
                        }
                        BurbujaMensaje(
                            mensaje = mensaje,
                            esPropio = mensaje.idEmisor == uiState.idUsuarioActual
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                if (chat.chatCerrado) {
                    Text(
                        text = "Chat cerrado: puedes leer mensajes, pero no escribir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampoContrabajo(
                            valor = uiState.borradorMensaje,
                            onValueChange = viewModel::actualizarBorradorMensaje,
                            etiqueta = "Escribe un mensaje",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::enviarMensaje) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar mensaje",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                uiState.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                uiState.mensajeSistema?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (mostrarModalCita && chat != null) {
        AlertDialog(
            onDismissRequest = { mostrarModalCita = false },
            title = { Text("Generar cita") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecciona una fecha y hora futura para la cita.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            val hoy = LocalDate.now()
                            DatePickerDialog(
                                context,
                                { _, anio, mes, dia ->
                                    fechaSeleccionada = LocalDate.of(anio, mes + 1, dia)
                                },
                                fechaSeleccionada?.year ?: hoy.year,
                                (fechaSeleccionada?.monthValue ?: hoy.monthValue) - 1,
                                fechaSeleccionada?.dayOfMonth ?: hoy.dayOfMonth
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis()
                            }.show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = fechaSeleccionada?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                ?: "Seleccionar fecha"
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val ahora = LocalTime.now()
                            TimePickerDialog(
                                context,
                                { _, hora, minuto ->
                                    horaSeleccionada = LocalTime.of(hora, minuto)
                                },
                                horaSeleccionada?.hour ?: ahora.hour,
                                horaSeleccionada?.minute ?: ahora.minute,
                                true
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = horaSeleccionada?.format(DateTimeFormatter.ofPattern("HH:mm"))
                                ?: "Seleccionar hora"
                        )
                    }
                    OutlinedTextField(
                        value = comentarioInput,
                        onValueChange = { comentarioInput = it },
                        label = { Text("Comentario") },
                        placeholder = { Text("Escribe detalles del acuerdo") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6
                    )
                    if ((mostrarErroresCita || !formularioCitaValido) && errorCita != null) {
                        Text(
                            text = errorCita,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                BotonPrimario(
                    texto = "Generar cita",
                    enabled = formularioCitaValido,
                    onClick = {
                        if (!formularioCitaValido) {
                            mostrarErroresCita = true
                        } else {
                            val fechaProgramada = formatearFechaProgramada(fechaSeleccionada, horaSeleccionada)
                            if (fechaProgramada != null) {
                                viewModel.crearCita(
                                    fechaProgramada = fechaProgramada,
                                    comentario = comentarioInput
                                )
                                mostrarModalCita = false
                                fechaSeleccionada = null
                                horaSeleccionada = null
                                comentarioInput = ""
                                mostrarErroresCita = false
                            } else {
                                mostrarErroresCita = true
                            }
                        }
                    }
                )
            },
            dismissButton = {
                BotonSecundario(
                    texto = "Cancelar",
                    onClick = { mostrarModalCita = false }
                )
            }
        )
    }

    if (confirmarCerrarChat && chat != null) {
        AlertDialog(
            onDismissRequest = { confirmarCerrarChat = false },
            title = { Text("Finalizar chat") },
            text = { Text("Esta accion cerrara la conversacion y quedara en solo lectura. ¿Deseas continuar?") },
            confirmButton = {
                BotonPrimario(
                    texto = "Finalizar",
                    onClick = {
                        confirmarCerrarChat = false
                        viewModel.cerrarChatActivo()
                    }
                )
            },
            dismissButton = {
                BotonSecundario(
                    texto = "Cancelar",
                    onClick = { confirmarCerrarChat = false }
                )
            }
        )
    }

    if (mostrarModalValoracion && chat != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { }
        ) {
            ModalValoracionCierre(
                nombreContacto = chat.nombreContacto,
                voto = uiState.votoValoracion,
                comentario = uiState.comentarioValoracion,
                onCambiarVoto = viewModel::actualizarVotoValoracion,
                onCambiarComentario = viewModel::actualizarComentarioValoracion,
                onCerrar = viewModel::cerrarModalValoracion,
                onGuardar = viewModel::guardarValoracionChat,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun CabeceraChat(
    chat: ChatCita?,
    onVolver: () -> Unit,
    menuAbierto: Boolean,
    onCambiarMenu: (Boolean) -> Unit,
    onFinalizarChat: () -> Unit,
    onAbrirServicioAsociado: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver"
            )
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = chat?.idOfertaServicio != null, onClick = onAbrirServicioAsociado),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = chat?.tituloServicio?.ifBlank { "Conversacion" } ?: "Conversacion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val subtitulo = if (chat == null) {
                    ""
                } else {
                    "@${chat.usernameContacto.ifBlank { "usuario" }}"
                }
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
        Box {
            IconButton(onClick = { onCambiarMenu(true) }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones"
                )
            }
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { onCambiarMenu(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Finalizar chat") },
                    onClick = {
                        onCambiarMenu(false)
                        onFinalizarChat()
                    }
                )
            }
        }
    }
}

@Composable
private fun BurbujaMensaje(
    mensaje: MensajeChat,
    esPropio: Boolean
) {
    val colorBurbuja = if (esPropio) {
        Color(0xFFE7F0FF)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val colorTexto = if (esPropio) MaterialTheme.colorScheme.onSurface else Color.White
    val colorMeta = if (esPropio) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.84f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esPropio) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colorBurbuja
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = mensaje.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = mensaje.fechaEnvio.takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorMeta
                    )
                    if (esPropio) {
                        val (simbolo, color) = simboloEstadoMensaje(mensaje.idEstado)
                        Text(
                            text = simbolo,
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCitaChat(
    chat: ChatCita,
    cita: CitaServicio?,
    esCliente: Boolean,
    esTrabajador: Boolean,
    citaExpandida: Boolean,
    onCambiarCitaExpandida: (Boolean) -> Unit,
    onCrear: () -> Unit,
    onAbrirCita: () -> Unit,
    onAceptarCitaTrabajador: () -> Unit,
    onRechazarCitaTrabajador: () -> Unit,
    onReenviarPropuestaCliente: () -> Unit,
    onSolicitarInicioTrabajador: () -> Unit,
    onAceptarInicioCliente: () -> Unit,
    onSolicitarFinalizarTrabajador: () -> Unit,
    onAceptarFinalizarCliente: () -> Unit
) {
    if (cita == null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sin cita creada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (esCliente) {
                        "Genera una cita cuando llegues a acuerdo con el trabajador."
                    } else {
                        "Esperando que el cliente genere la cita de servicio."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (esCliente && !chat.chatCerrado) {
                    BotonPrimario(
                        texto = "Generar cita",
                        onClick = onCrear
                    )
                }
            }
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCambiarCitaExpandida(!citaExpandida) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Cita de servicio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (citaExpandida) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (citaExpandida) "Contraer cita" else "Expandir cita",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(0.05f))
                EtiquetaEstado(
                    texto = etiquetaEstadoCita(cita.estado),
                    enfatizada = cita.estado == EstadoCita.HANDSHAKE || cita.estado == EstadoCita.EN_PROCESO
                )
            }
            if (citaExpandida) {
                Text(
                    text = "Servicio: ${chat.tituloServicio.ifBlank { "Servicio" }}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Trabajador: ${chat.nombreContacto}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Categoria: ${chat.categoriaServicio.ifBlank { "General" }}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Fecha programada: ${cita.fechaProgramada}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Comentario: ${cita.comentario}",
                    style = MaterialTheme.typography.bodySmall
                )
                BotonPrimario(
                    texto = "Abrir cita",
                    onClick = onAbrirCita
                )
                if (esTrabajador && !chat.chatCerrado) {
                    AccionesTrabajador(
                        cita = cita,
                        onAceptarCita = onAceptarCitaTrabajador,
                        onRechazarCita = onRechazarCitaTrabajador,
                        onSolicitarInicio = onSolicitarInicioTrabajador,
                        onSolicitarFinalizar = onSolicitarFinalizarTrabajador
                    )
                }
                if (esCliente && !chat.chatCerrado) {
                    AccionesCliente(
                        cita = cita,
                        onAceptarInicio = onAceptarInicioCliente,
                        onAceptarFinalizacion = onAceptarFinalizarCliente,
                        onReenviarPropuesta = onReenviarPropuestaCliente
                    )
                }
            }
        }
    }
}

@Composable
private fun AccionesTrabajador(
    cita: CitaServicio,
    onAceptarCita: () -> Unit,
    onRechazarCita: () -> Unit,
    onSolicitarInicio: () -> Unit,
    onSolicitarFinalizar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (cita.estado) {
            EstadoCita.PENDIENTE -> {
                BotonSecundario(
                    texto = "Rechazar",
                    onClick = onRechazarCita,
                    modifier = Modifier.weight(1f)
                )
                BotonPrimario(
                    texto = "Aceptar",
                    onClick = onAceptarCita,
                    modifier = Modifier.weight(1f)
                )
            }

            EstadoCita.HANDSHAKE -> {
                BotonPrimario(
                    texto = "Solicitar inicio",
                    onClick = onSolicitarInicio,
                    modifier = Modifier.weight(1f)
                )
            }

            EstadoCita.EN_PROCESO -> {
                BotonPrimario(
                    texto = "Solicitar finalizacion",
                    onClick = onSolicitarFinalizar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AccionesCliente(
    cita: CitaServicio,
    onAceptarInicio: () -> Unit,
    onAceptarFinalizacion: () -> Unit,
    onReenviarPropuesta: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (cita.estado) {
            EstadoCita.COMENZANDO -> {
                BotonPrimario(
                    texto = "Aceptar inicio",
                    onClick = onAceptarInicio,
                    modifier = Modifier.weight(1f)
                )
            }

            EstadoCita.FINALIZANDO -> {
                BotonPrimario(
                    texto = "Aceptar finalizacion",
                    onClick = onAceptarFinalizacion,
                    modifier = Modifier.weight(1f)
                )
            }

            EstadoCita.RECHAZADA -> {
                BotonPrimario(
                    texto = "Reenviar propuesta",
                    onClick = onReenviarPropuesta,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModalValoracionCierre(
    nombreContacto: String,
    voto: Int,
    comentario: String,
    onCambiarVoto: (Int) -> Unit,
    onCambiarComentario: (String) -> Unit,
    onCerrar: () -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "¿Que tal te parecio el contacto con \"$nombreContacto\"?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { indice ->
                    val estrellas = indice + 1
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "$estrellas estrellas",
                        tint = if (estrellas <= voto) Color(0xFFFFC93C) else Color(0xFFB0B7BF),
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clickable { onCambiarVoto(estrellas) }
                    )
                }
            }
            OutlinedTextField(
                value = comentario,
                onValueChange = onCambiarComentario,
                label = { Text("Comentario (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonSecundario(
                    texto = "Despues",
                    onClick = onCerrar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrimario(
                    texto = "Guardar",
                    onClick = onGuardar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun simboloEstadoMensaje(idEstado: Long): Pair<String, Color> = when (idEstado) {
    301L -> "✓" to Color(0xFF8A95A3)
    302L -> "✓✓" to Color(0xFF8A95A3)
    303L -> "✓✓" to Color(0xFF00A8C8)
    else -> "" to Color.Transparent
}

private fun etiquetaEstadoCita(estado: Int): String = when (estado) {
    EstadoCita.PENDIENTE -> "Pendiente"
    EstadoCita.HANDSHAKE -> "Confirmada"
    EstadoCita.COMENZANDO -> "Pendiente de confirmacion"
    EstadoCita.EN_PROCESO -> "En proceso"
    EstadoCita.FINALIZANDO -> "Finalizando"
    EstadoCita.FINALIZADO -> "Finalizada"
    EstadoCita.RECHAZADA -> "Rechazada"
    EstadoCita.CANCELADO -> "Cancelada"
    EstadoCita.CERRADO -> "Cerrada"
    else -> "Pendiente"
}

@Composable
private fun SeparadorDiaChat(fecha: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        ) {
            Text(
                text = etiquetaDiaChat(fecha),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun fechaSoloMensaje(fechaEnvio: String): LocalDate? {
    return try {
        LocalDateTime.parse(fechaEnvio, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun etiquetaDiaChat(fecha: LocalDate): String {
    val hoy = LocalDate.now()
    val ayer = hoy.minusDays(1)
    return when (fecha) {
        hoy -> "Hoy"
        ayer -> "Ayer"
        else -> {
            val mes = fecha.month.getDisplayName(TextStyle.FULL, Locale("es", "CL"))
            "${fecha.dayOfMonth} de $mes"
        }
    }
}

private fun validarFormularioCita(
    fecha: LocalDate?,
    hora: LocalTime?,
    comentario: String
): String? {
    if (fecha == null) return "Selecciona una fecha."
    if (hora == null) return "Selecciona una hora."
    if (comentario.trim().isBlank()) return "Ingresa un comentario."
    val fechaHoraSeleccionada = LocalDateTime.of(fecha, hora)
    val ahora = LocalDateTime.now().withSecond(0).withNano(0)
    if (fechaHoraSeleccionada.isBefore(ahora)) {
        return "La fecha/hora debe ser desde este momento hacia adelante."
    }
    return null
}

private fun formatearFechaProgramada(fecha: LocalDate?, hora: LocalTime?): String? {
    if (fecha == null || hora == null) return null
    return LocalDateTime.of(fecha, hora)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

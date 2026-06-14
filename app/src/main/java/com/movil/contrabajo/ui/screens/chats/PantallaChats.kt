package com.movil.contrabajo.ui.screens.chats

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.theme.AzulPetroleo
import com.movil.contrabajo.ui.theme.TurquesaBrillante
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel

@Composable
fun AvatarUsuarioAsync(
    idUsuario: Int,
    nombreParaFallback: String,
    modifier: Modifier = Modifier
) {
    var urlFoto by remember(idUsuario) { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var cargado by remember(idUsuario) { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(idUsuario) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Hacemos el GET público a tu API de usuarios
                val response = com.movil.contrabajo.data.remote.UsuariosApiClient.api.obtenerFotoPerfil(idUsuario).execute()
                if (response.isSuccessful) {
                    urlFoto = response.body()?.enlace
                }
            } catch (e: Exception) {
                // Falla silenciosa de red: se mantendrá urlFoto en null
            } finally {
                cargado = true
            }
        }
    }

    if (!cargado) {
        // Estado 1: Skeleton cargando el JSON
        Box(
            modifier = modifier.background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(com.movil.contrabajo.ui.theme.TurquesaBrillante, com.movil.contrabajo.ui.theme.AzulPetroleo)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        }
    } else if (urlFoto.isNullOrBlank()) {
        // Estado 2: Fallback (No tiene foto o dio 404), mostramos inicial
        Box(
            modifier = modifier.background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(com.movil.contrabajo.ui.theme.TurquesaBrillante, com.movil.contrabajo.ui.theme.AzulPetroleo)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombreParaFallback.take(1).uppercase().ifBlank { "?" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        }
    } else {
        // Estado 3: Carga exitosa, le pasamos la URL de Cloudinary a Coil
        coil.compose.SubcomposeAsyncImage(
            model = urlFoto,
            contentDescription = "Avatar de $nombreParaFallback",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            error = {
                // Si la URL falla (ej: fue borrada en Cloudinary), volvemos al Fallback
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(com.movil.contrabajo.ui.theme.TurquesaBrillante, com.movil.contrabajo.ui.theme.AzulPetroleo)
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nombreParaFallback.take(1).uppercase().ifBlank { "?" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun AvatarServicioAsync(
    idOfertaServicio: Long?,
    tituloFallback: String,
    modifier: Modifier = Modifier
) {
    if (idOfertaServicio == null || idOfertaServicio <= 0L) {
        Box(
            modifier = modifier.background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(com.movil.contrabajo.ui.theme.TurquesaBrillante, com.movil.contrabajo.ui.theme.AzulPetroleo)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tituloFallback.take(1).uppercase().ifBlank { "?" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        }
        return
    }

    var urlFoto by remember(idOfertaServicio) { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var cargado by remember(idOfertaServicio) { androidx.compose.runtime.mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(idOfertaServicio) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val token = com.movil.contrabajo.data.remote.RemoteSessionStore(context.applicationContext).obtenerToken()
                    ?: return@withContext
                val response = com.movil.contrabajo.data.remote.ServiciosApiClient.api
                    .listarFotosOferta(
                        authorization = com.movil.contrabajo.data.remote.bearer(token),
                        idOferta = idOfertaServicio.toInt()
                    ).execute()
                if (response.isSuccessful) {
                    urlFoto = response.body()?.firstOrNull()?.enlace
                }
            } catch (_: Exception) {
            } finally {
                cargado = true
            }
        }
    }

    val gradiente = listOf(com.movil.contrabajo.ui.theme.TurquesaBrillante, com.movil.contrabajo.ui.theme.AzulPetroleo)
    val fallback: @Composable () -> Unit = {
        Box(
            modifier = modifier.background(androidx.compose.ui.graphics.Brush.linearGradient(gradiente)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tituloFallback.take(1).uppercase().ifBlank { "?" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        }
    }

    if (!cargado) {
        Box(
            modifier = modifier.background(androidx.compose.ui.graphics.Brush.linearGradient(gradiente)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        }
    } else if (urlFoto.isNullOrBlank()) {
        fallback()
    } else {
        coil.compose.SubcomposeAsyncImage(
            model = urlFoto,
            contentDescription = tituloFallback,
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            error = {
                Box(
                    modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.linearGradient(gradiente)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tituloFallback.take(1).uppercase().ifBlank { "?" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                }
            }
        )
    }
}

@Composable
fun PantallaChats(
    viewModel: ChatsViewModel,
    onAbrirChat: (Long) -> Unit,
    esTrabajador: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState
    val chatsVisibles = uiState.chatsFiltrados

    PantallaBase(modifier = modifier, mostrarFondo = false, scrollable = false) {
        val fadeColor = MaterialTheme.colorScheme.surface

        // ── Encabezado ─────────────────────────────────────────────
        TarjetaBase {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Mensajes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tus conversaciones activas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (uiState.totalMensajesNoLeidos > 0) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = uiState.totalMensajesNoLeidos.coerceAtMost(99).toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── Contenedor de mensajes: buscador + selector + lista ────
        // Column weight(1f) + Spacer(58dp) = el card termina justo en el borde del navbar flotante
        Column(modifier = Modifier.weight(1f)) {
        TarjetaBase(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp),
            llenarAlto = true
        ) {
            // Columna única como hijo directo para evitar el spacedBy(12dp) de TarjetaBase
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Sección de filtros fija (con padding propio) ───
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BuscadorChats(
                        query = uiState.busquedaChats,
                        onQueryChange = viewModel::actualizarBusquedaChats,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (esTrabajador) {
                        SelectorTipoChat(
                            seleccionado = uiState.tipoFiltroChat,
                            onSeleccionar = viewModel::actualizarTipoFiltroChat,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )

                // ── Sección de lista con scroll independiente ──────
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (chatsVisibles.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 28.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "No tienes conversaciones todavía.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (uiState.busquedaChats.isNotBlank())
                                    "Ningún chat coincide con \"${uiState.busquedaChats}\"."
                                else
                                    "Cuando alguien contacte un servicio, sus mensajes aparecerán aquí.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 6.dp, bottom = 4.dp)
                        ) {
                            chatsVisibles.forEachIndexed { index, chat ->
                                val esComoTrabajador = uiState.idUsuarioActual != null &&
                                    chat.idTrabajador == uiState.idUsuarioActual
                                FilaChatModerna(
                                    chat = chat,
                                    esComoTrabajador = esComoTrabajador,
                                    mostrarRolChip = esTrabajador,
                                    onClick = { onAbrirChat(chat.idChatCita) }
                                )
                                if (index < chatsVisibles.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 78.dp, end = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }

                    // Fade en el borde superior de la lista (sobre la línea divisoria)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(20.dp)
                            .drawBehind {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(fadeColor, Color.Transparent),
                                        startY = 0f,
                                        endY = size.height
                                    )
                                )
                            }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Buscador
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BuscadorChats(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(21.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(17.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isBlank()) {
                    Text(
                        text = "Buscar por nombre o servicio...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotBlank()) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Limpiar búsqueda",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Selector de tipo — segmented pill con animación deslizante
// ─────────────────────────────────────────────────────────────────────────────

private data class OpcionFiltroChat(
    val tipo: String?,
    val icono: ImageVector,
    val etiqueta: String
)

@Composable
private fun SelectorTipoChat(
    seleccionado: String?,
    onSeleccionar: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val opciones = remember {
        listOf(
            OpcionFiltroChat(null, Icons.Outlined.ChatBubbleOutline, "Todos"),
            OpcionFiltroChat("contacto", Icons.Filled.Person, "Cliente"),
            OpcionFiltroChat("trabajador", Icons.Filled.Work, "Servicios")
        )
    }
    val selectedIndex = opciones.indexOfFirst { it.tipo == seleccionado }.coerceAtLeast(0)
    val pillAnimado by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "pillSelectorChat"
    )

    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        val segmentWidth = maxWidth / opciones.size
        val pillOffsetX = segmentWidth * pillAnimado

        // Pill deslizante
        Box(
            modifier = Modifier
                .offset(x = pillOffsetX)
                .width(segmentWidth)
                .fillMaxHeight()
                .padding(all = 3.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(17.dp), clip = true)
                .background(MaterialTheme.colorScheme.surface)
        )

        // Etiquetas táctiles (encima de la pill)
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            opciones.forEach { opcion ->
                val isSelected = seleccionado == opcion.tipo
                val colorContenido by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    animationSpec = tween(durationMillis = 200),
                    label = "colorSegmentoChat"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSeleccionar(opcion.tipo) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = opcion.icono,
                        contentDescription = opcion.etiqueta,
                        modifier = Modifier.size(13.dp),
                        tint = colorContenido
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = opcion.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = colorContenido
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fila de chat — estilo moderno dentro del sistema de TarjetaBase
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FilaChatModerna(
    chat: ChatCita,
    esComoTrabajador: Boolean,
    mostrarRolChip: Boolean,
    onClick: () -> Unit
) {
    val tieneNoLeidos = chat.mensajesNoLeidos > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Avatar: imagen del servicio ────────────────────────────
        AvatarServicioAsync(
            idOfertaServicio = chat.idOfertaServicio,
            tituloFallback = chat.tituloServicio.ifBlank { chat.nombreContacto },
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )

        // ── Contenido
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Línea 1: nombre del contacto + hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.nombreContacto.ifBlank { "@${chat.usernameContacto}" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (tieneNoLeidos) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatearHoraChat(chat.horaUltimoMensaje),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tieneNoLeidos)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (tieneNoLeidos) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            // Línea 2: título del servicio
            if (chat.tituloServicio.isNotBlank()) {
                Text(
                    text = chat.tituloServicio,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Línea 3: último mensaje + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        chat.chatCerrado -> "Chat cerrado"
                        chat.ultimoMensaje.isBlank() -> "Sin mensajes aún"
                        else -> chat.ultimoMensaje
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = if (chat.chatCerrado) FontStyle.Italic else FontStyle.Normal,
                    color = when {
                        chat.chatCerrado -> MaterialTheme.colorScheme.error.copy(alpha = 0.65f)
                        tieneNoLeidos -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (tieneNoLeidos) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))

                // Badge de no leídos
                if (tieneNoLeidos) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = chat.mensajesNoLeidos.coerceAtMost(99).toString(),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (mostrarRolChip) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (esComoTrabajador)
                            TurquesaBrillante.copy(alpha = 0.14f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = if (esComoTrabajador) "Servicio" else "Cliente",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (esComoTrabajador)
                                AzulPetroleo
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatearHoraChat(hora: String): String {
    if (hora.isBlank()) return ""
    val parteHora = hora.split("T").getOrNull(1) ?: return hora.takeLast(5).trim()
    return parteHora.take(5)
}

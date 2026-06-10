package com.movil.contrabajo.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.ComunaCatalogo
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.data.repository.RepositorioChats
import com.movil.contrabajo.data.repository.RepositorioOfertas
import com.movil.contrabajo.data.repository.RepositorioPerfil
import com.movil.contrabajo.data.repository.RepositorioReportes
import com.movil.contrabajo.domain.model.AccionModeracion
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.FotoOferta
import com.movil.contrabajo.domain.model.FotoServicioLocal
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.Reporte
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.TipoReporte
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import com.movil.contrabajo.domain.model.Valoracion
import com.movil.contrabajo.domain.model.ValoracionesServicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.text.Normalizer
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class PrincipalUiState(
    val busqueda: String = "",
    val ofertas: List<OfertaServicio> = emptyList(),
    val categoriasDisponibles: List<CategoriaServicio> = emptyList(),
    val refrescando: Boolean = false,
    val cargandoOperacion: Boolean = false,
    val rangoBusquedaM: Int = 20_000,
    val filtroPorCoordenadasActivo: Boolean = false,
    val filtroCategoriaId: Long? = null,
    val filtroTipoPrecio: Int? = null,
    val soloTrabajadorVerificado: Boolean = false,
    val filtroZonaComunaActivo: Boolean = false,
    val comunaFiltro: String = "",
    val comunasDisponibles: List<ComunaCatalogo> = emptyList(),
    val ordenMarketplace: OrdenMarketplace = OrdenMarketplace.FECHA_RECIENTES,
    val mensajePrincipal: String? = null,
    val latitudUsuario: Double? = null,
    val longitudUsuario: Double? = null
)

enum class OrdenMarketplace {
    ALFABETICO_A_Z,
    FECHA_RECIENTES,
    FECHA_ANTIGUAS
}

class PrincipalViewModel(
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioPerfil: RepositorioPerfil,
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(PrincipalUiState())
        private set

    init {
        cargarComunas()
        recargar()
    }

    private fun cargarComunas() {
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.obtenerComunas()
            }
            resultado.onSuccess { comunas ->
                uiState = uiState.copy(comunasDisponibles = comunas)
            }
        }
    }

    fun recargar() {
        viewModelScope.launch {
            try {
            val snapshot = withContext(Dispatchers.IO) {
                val ubicacionActual = repositorioPerfil.obtenerUbicacionAjustes()
                val usuarioActual = repositorioAutenticacion.obtenerSesionActiva()
                val ofertas = repositorioOfertas.obtenerOfertasMarketplace(uiState.busqueda)
                val categorias = repositorioOfertas.obtenerCategoriasServicio()
                Triple(ubicacionActual, Pair(usuarioActual, ofertas), categorias)
            }
            val ubicacionActual = snapshot.first
            val usuarioActual = snapshot.second.first
            val ofertas = snapshot.second.second
            val categorias = snapshot.third
            val filtroActivo = ubicacionActual.latitud != null && ubicacionActual.longitud != null
            val rangoBusqueda = EscalaRango.normalizar(ubicacionActual.rangoBusquedaM)
            val ofertasFiltradas = filtrarPorMatchRangos(
                ofertas = ofertas,
                latitudBase = ubicacionActual.latitud,
                longitudBase = ubicacionActual.longitud,
                rangoBusquedaM = rangoBusqueda,
                idUsuarioActual = usuarioActual?.idUsuario
            )
            val ofertasFinales = aplicarFiltrosYOrden(ofertasFiltradas)

            uiState = uiState.copy(
                ofertas = ofertasFinales,
                categoriasDisponibles = categorias,
                rangoBusquedaM = rangoBusqueda,
                filtroPorCoordenadasActivo = filtroActivo,
                latitudUsuario = ubicacionActual.latitud,
                longitudUsuario = ubicacionActual.longitud
            )
            } catch (_: Exception) {
            }
        }
    }

    fun refrescarDesdeGesto() {
        if (uiState.refrescando) return
        uiState = uiState.copy(refrescando = true)
        viewModelScope.launch {
            delay(220)
            recargar()
            uiState = uiState.copy(refrescando = false)
        }
    }

    fun actualizarBusqueda(valor: String) {
        uiState = uiState.copy(busqueda = valor)
        recargar()
    }

    fun guardarRangoBusqueda(valorMetros: Int) {
        val rangoNormalizado = EscalaRango.normalizar(valorMetros)
        viewModelScope.launch {
            uiState = uiState.copy(cargandoOperacion = true)
            val resultado = withContext(Dispatchers.IO) {
                val ubicacionActual = repositorioPerfil.obtenerUbicacionAjustes()
                repositorioPerfil.guardarUbicacionAjustes(
                    ubicacionActual.copy(
                        rangoBusquedaM = rangoNormalizado
                    )
                )
            }
            resultado.onSuccess {
                recargar()
                uiState = uiState.copy(mensajePrincipal = "Rango de busqueda actualizado a ${EscalaRango.formatear(rangoNormalizado)}.")
            }.onFailure {
                uiState = uiState.copy(mensajePrincipal = it.message ?: "No se pudo guardar el rango de busqueda")
            }
            uiState = uiState.copy(cargandoOperacion = false)
        }
    }

    fun consumirMensajePrincipal() {
        uiState = uiState.copy(mensajePrincipal = null)
    }

    fun aplicarFiltros(
        categoriaId: Long?,
        tipoPrecio: Int?,
        soloVerificados: Boolean,
        filtroZonaComunaActivo: Boolean,
        comunaFiltro: String,
        orden: OrdenMarketplace
    ) {
        val comunaNormalizada = if (filtroZonaComunaActivo) {
            comunaFiltro.trim().ifBlank { "Santiago" }
        } else {
            ""
        }
        uiState = uiState.copy(
            filtroCategoriaId = categoriaId,
            filtroTipoPrecio = tipoPrecio,
            soloTrabajadorVerificado = soloVerificados,
            filtroZonaComunaActivo = filtroZonaComunaActivo,
            comunaFiltro = comunaNormalizada,
            ordenMarketplace = orden
        )
        recargar()
    }

    fun limpiarFiltros() {
        uiState = uiState.copy(
            filtroCategoriaId = null,
            filtroTipoPrecio = null,
            soloTrabajadorVerificado = false,
            filtroZonaComunaActivo = false,
            comunaFiltro = "",
            ordenMarketplace = OrdenMarketplace.FECHA_RECIENTES
        )
        recargar()
    }

    private fun filtrarPorMatchRangos(
        ofertas: List<OfertaServicio>,
        latitudBase: Double?,
        longitudBase: Double?,
        rangoBusquedaM: Int,
        idUsuarioActual: Long?
    ): List<OfertaServicio> {
        if (latitudBase == null || longitudBase == null) {
            return ofertas.filter { idUsuarioActual != null && it.idTrabajador == idUsuarioActual }
        }

        return ofertas.filter { oferta ->
            if (idUsuarioActual != null && oferta.idTrabajador == idUsuarioActual) {
                return@filter true
            }
            val latitudOferta = oferta.latitudReferencia
            val longitudOferta = oferta.longitudReferencia
            if (latitudOferta == null || longitudOferta == null) return@filter false

            val distanciaM = calcularDistanciaM(
                lat1 = latitudBase,
                lon1 = longitudBase,
                lat2 = latitudOferta,
                lon2 = longitudOferta
            )
            distanciaM <= rangoBusquedaM + oferta.rangoDisponibilidadM
        }
    }

    private fun aplicarFiltrosYOrden(ofertas: List<OfertaServicio>): List<OfertaServicio> {
        val filtradas = ofertas
            .asSequence()
            .filter { oferta ->
                uiState.filtroCategoriaId?.let { oferta.idCategoriaServicio == it } ?: true
            }
            .filter { oferta ->
                uiState.filtroTipoPrecio?.let { oferta.tipoPrecio == it } ?: true
            }
            .filter { oferta ->
                if (uiState.soloTrabajadorVerificado) oferta.trabajadorVerificado else true
            }
            .filter { oferta ->
                if (!uiState.filtroZonaComunaActivo) return@filter true
                coincideComunaFiltro(oferta, uiState.comunaFiltro)
            }
            .toList()

        return when (uiState.ordenMarketplace) {
            OrdenMarketplace.ALFABETICO_A_Z -> filtradas.sortedBy { it.titulo.lowercase() }
            OrdenMarketplace.FECHA_ANTIGUAS -> filtradas.sortedBy { it.fechaPublicacion }
            OrdenMarketplace.FECHA_RECIENTES -> filtradas.sortedByDescending { it.fechaPublicacion }
        }
    }

    private fun calcularDistanciaM(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Int {
        val radioTierraM = 6_371_000.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (radioTierraM * c).roundToInt()
    }

    private fun coincideComunaFiltro(oferta: OfertaServicio, comunaFiltro: String): Boolean {
        val filtroNormalizado = normalizarTexto(comunaFiltro)
        if (filtroNormalizado.isBlank()) return true
        val comunaOferta = oferta.ubicacionReferencia
            .substringBefore(",")
            .trim()
        return normalizarTexto(comunaOferta) == filtroNormalizado
    }

    private fun normalizarTexto(valor: String): String {
        val decomposed = Normalizer.normalize(valor, Normalizer.Form.NFD)
        return decomposed
            .replace("\\p{M}+".toRegex(), "")
            .replace("[^A-Za-z0-9 ]".toRegex(), " ")
            .lowercase()
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

}

data class ChatsUiState(
    val idUsuarioActual: Long? = null,
    val chats: List<ChatCita> = emptyList(),
    val filtroChatsContacto: Boolean = false,
    val filtroChatsTrabajador: Boolean = false,
    // Filtro de tipo moderno: null = todos, "contacto" = como cliente, "trabajador" = como prestador
    val tipoFiltroChat: String? = null,
    // Búsqueda en tiempo real por nombre de contacto, @username o título de servicio
    val busquedaChats: String = "",
    val totalMensajesNoLeidos: Int = 0,
    val idPrimerChatPendiente: Long? = null,
    val notificacionesPendientes: List<NotificacionMensajePendiente> = emptyList(),
    val chatActivo: ChatCita? = null,
    val mensajesActivos: List<MensajeChat> = emptyList(),
    val citaActiva: CitaServicio? = null,
    val valoracionExistente: Valoracion? = null,
    val mostrarModalValoracion: Boolean = false,
    val votoValoracion: Int = 5,
    val comentarioValoracion: String = "",
    val borradorMensaje: String = "",
    val mensajeSistema: String? = null,
    val error: String? = null,
    // ID de chat recien creado/iniciado; se consume una vez para navegar
    val pendingNavChatId: Long? = null
) {
    val chatsFiltrados: List<ChatCita> get() {
        val idActual = idUsuarioActual
        var resultado = chats

        // Filtro por tipo (selector segmentado moderno)
        if (tipoFiltroChat != null && idActual != null) {
            resultado = resultado.filter { chat ->
                when (tipoFiltroChat) {
                    "contacto"   -> chat.idCliente == idActual
                    "trabajador" -> chat.idTrabajador == idActual
                    else         -> true
                }
            }
        }

        // Filtro por búsqueda (nombre, username, título de servicio)
        if (busquedaChats.isNotBlank()) {
            val query = busquedaChats.trim().lowercase()
            resultado = resultado.filter { chat ->
                chat.nombreContacto.lowercase().contains(query) ||
                    chat.usernameContacto.lowercase().contains(query) ||
                    chat.tituloServicio.lowercase().contains(query)
            }
        }

        return resultado
    }
}

class ChatsViewModel(
    private val repositorioChats: RepositorioChats
) : ViewModel() {
    var uiState by mutableStateOf(ChatsUiState())
        private set

    init {
        recargar()
        escucharWebSocket()
        escucharRecibos()
        escucharConexion()
        escucharCierreChat()
    }

    /**
     * Colecta el flow de WsManager para actualizar la UI en tiempo real.
     *
     * Si el mensaje pertenece al chat activo:
     *   1. Lo agrega de inmediato a mensajesActivos (UX responsiva).
     *   2. Recarga el historial desde el servidor, que llama marcarRecibidos+marcarLeidos,
     *      y devuelve los mensajes con los campos fechaRecibido/fechaLeido poblados
     *      para que los ticks se muestren correctamente.
     *
     * Si el mensaje pertenece a otro chat:
     *   - Llama marcarRecibidos en el servidor para registrar la entrega (tick entregado).
     *   - Si el chat no existe aun en la lista (mensaje de sistema de nuevo chat), recarga primero
     *     para descubrir el chat y luego muestra la notificacion con el nombre correcto.
     *
     * En ambos casos refresca la lista de chats para actualizar contadores de no leidos.
     */
    private fun escucharWebSocket() {
        viewModelScope.launch {
            com.movil.contrabajo.data.remote.WsManager.mensajesEntrantes.collect { mensaje ->
                val chatActivo = uiState.chatActivo
                if (chatActivo != null && mensaje.idChatCita == chatActivo.idChatCita) {
                    // Agrega el mensaje de inmediato para que aparezca sin esperar al servidor
                    uiState = uiState.copy(mensajesActivos = uiState.mensajesActivos + mensaje)
                    // Recarga desde el servidor: aplica marks y devuelve ticks actualizados
                    val (mensajesActualizados, citaActualizada) = withContext(Dispatchers.IO) {
                        repositorioChats.obtenerMensajes(chatActivo.idChatCita) to
                            repositorioChats.obtenerCitaPorChat(chatActivo.idChatCita)
                    }
                    uiState = uiState.copy(
                        mensajesActivos = mensajesActualizados,
                        citaActiva = citaActualizada ?: uiState.citaActiva
                    )
                } else {
                    // El chat no esta abierto: registrar entrega
                    withContext(Dispatchers.IO) {
                        repositorioChats.marcarRecibidos(mensaje.idChatCita)
                    }
                    // Buscar el username del contacto en la lista de chats ya cargada.
                    // Si no existe todavia (nuevo chat por mensaje de sistema), recargar primero.
                    var chatEnLista = uiState.chats.firstOrNull { it.idChatCita == mensaje.idChatCita }
                    if (chatEnLista == null) {
                        // Chat nuevo (trabajador recibe notificacion de nuevo contacto): recargar
                        val nuevosChatsList = withContext(Dispatchers.IO) {
                            repositorioChats.obtenerChatsActuales()
                        }
                        uiState = uiState.copy(
                            chats = nuevosChatsList,
                            totalMensajesNoLeidos = nuevosChatsList.sumOf { it.mensajesNoLeidos.coerceAtLeast(0) }
                        )
                        chatEnLista = nuevosChatsList.firstOrNull { it.idChatCita == mensaje.idChatCita }
                    }
                    val usernameContacto = chatEnLista?.usernameContacto
                    val tituloNotif = when {
                        !usernameContacto.isNullOrBlank() -> usernameContacto
                        mensaje.tipo == 1                 -> "Nuevo chat"
                        else                              -> "Nuevo mensaje"
                    }
                    val contenidoNotif = when {
                        mensaje.tipo == 1 -> mensaje.contenido.take(80).ifBlank { "Tienes un nuevo chat." }
                        else              -> mensaje.contenido.take(80).ifBlank { "Tienes un nuevo mensaje." }
                    }
                    // Alimenta el mecanismo de notificaciones existente en ShellPrincipal.
                    // El LaunchedEffect(notificacionesPendientes) de ShellPrincipal lo captura
                    // y muestra el banner del sistema antes de que recargar() lo limpie.
                    val notif = NotificacionMensajePendiente(
                        idMensajeChat = mensaje.idMensajeChat,
                        idChatCita    = mensaje.idChatCita,
                        titulo        = tituloNotif,
                        contenido     = contenidoNotif
                    )
                    uiState = uiState.copy(notificacionesPendientes = listOf(notif))
                }
                // Refrescar lista de chats (sobreescribe notificacionesPendientes con emptyList,
                // pero el LaunchedEffect de ShellPrincipal ya habra disparado para entonces)
                recargar()
            }
        }
    }

    /**
     * Limpia el chat activo. Debe llamarse cuando el usuario sale de PantallaDetalleChat
     * para evitar que los mensajes entrantes se marquen automaticamente como leidos
     * mientras el usuario no esta viendo el chat.
     */
    fun limpiarChatActivo() {
        uiState = uiState.copy(chatActivo = null)
    }

    /**
     * Cuando el WebSocket (re)conecta, recarga la lista de chats y el historial
     * del chat activo para recuperar mensajes que llegaron mientras estabamos offline.
     */
    private fun escucharConexion() {
        viewModelScope.launch {
            com.movil.contrabajo.data.remote.WsManager.conexionEstablecida.collect {
                recargar()
                val chatActivo = uiState.chatActivo ?: return@collect
                val mensajes = withContext(Dispatchers.IO) {
                    repositorioChats.obtenerMensajes(chatActivo.idChatCita)
                }
                uiState = uiState.copy(mensajesActivos = mensajes)
            }
        }
    }

    /**
     * Colecta eventos de recibo/lectura del backend para actualizar los ticks
     * en tiempo real cuando el receptor confirma que recibio o leyo los mensajes.
     */
    private fun escucharRecibos() {
        viewModelScope.launch {
            com.movil.contrabajo.data.remote.WsManager.recibosActualizados.collect { idChat ->
                // Solo refrescar si el chat afectado es el que esta activo actualmente
                if (uiState.chatActivo?.idChatCita == idChat) {
                    val mensajes = withContext(Dispatchers.IO) {
                        repositorioChats.obtenerMensajes(idChat)
                    }
                    uiState = uiState.copy(mensajesActivos = mensajes)
                }
            }
        }
    }

    private fun escucharCierreChat() {
        viewModelScope.launch {
            com.movil.contrabajo.data.remote.WsManager.chatsCerrados.collect { idChat ->
                recargar()
                if (uiState.chatActivo?.idChatCita == idChat) {
                    recargarEstadoChatActivo(idChat)
                }
            }
        }
    }

    /**
     * Recarga la lista de chats y contadores.
     *
     * Si [notificarNoLeidos] es true y hay chats con mensajes sin leer,
     * genera notificaciones in-app para cada uno. Usar al iniciar sesion
     * para alertar de mensajes previos al usuario.
     */
    fun recargar(notificarNoLeidos: Boolean = false) {
        viewModelScope.launch {
            val (chats, notificaciones) = withContext(Dispatchers.IO) {
                repositorioChats.obtenerChatsActuales() to repositorioChats.obtenerNotificacionesPendientes()
            }
            // Al abrir la app: marcar como recibidos en background todos los chats con no leidos.
            // Esto hace que el emisor vea el doble check gris sin que el receptor abra cada chat.
            if (notificarNoLeidos) {
                chats.filter { it.mensajesNoLeidos > 0 }.forEach { chat ->
                    launch(Dispatchers.IO) {
                        runCatching { repositorioChats.marcarRecibidos(chat.idChatCita) }
                    }
                }
            }
            // Al iniciar sesion, si hay chats con mensajes no leidos, generar notificaciones
            val notificacionesFinales = if (notificarNoLeidos && notificaciones.isEmpty()) {
                chats.filter { it.mensajesNoLeidos > 0 }.map { chat ->
                    NotificacionMensajePendiente(
                        idMensajeChat = chat.idChatCita,  // usado como ID de la notificacion Android
                        idChatCita    = chat.idChatCita,
                        titulo        = chat.usernameContacto.takeIf {
                            chat.usernameContacto.isNotBlank()
                        } ?: "Mensajes pendientes",
                        contenido     = "${chat.mensajesNoLeidos} mensaje(s) sin leer"
                    )
                }
            } else {
                notificaciones
            }
            uiState = uiState.copy(
                idUsuarioActual = repositorioChats.obtenerIdUsuarioActual(),
                chats = chats,
                totalMensajesNoLeidos = chats.sumOf { it.mensajesNoLeidos.coerceAtLeast(0) },
                idPrimerChatPendiente = chats.firstOrNull { it.mensajesNoLeidos > 0 }?.idChatCita,
                notificacionesPendientes = notificacionesFinales
            )
        }
    }

    /**
     * Recarga solo los mensajes del chat activo desde el servidor.
     * Usado por el refresh periodico de PantallaDetalleChat para que el emisor
     * vea los ticks actualizados (entregado/leido) sin tener que reabrir el chat.
     */
    fun refrescarMensajes(idChatCita: Long) {
        viewModelScope.launch {
            val mensajes = withContext(Dispatchers.IO) {
                repositorioChats.obtenerMensajes(idChatCita)
            }
            if (uiState.chatActivo?.idChatCita == idChatCita) {
                uiState = uiState.copy(mensajesActivos = mensajes)
            }
        }
    }

    private suspend fun recargarEstadoChatActivo(idChatCita: Long) {
        val chat = withContext(Dispatchers.IO) { repositorioChats.obtenerChat(idChatCita) } ?: return
        val idActual = repositorioChats.obtenerIdUsuarioActual()
        val esCliente = chat.idCliente == idActual
        val (valoracion, cita, mensajes) = withContext(Dispatchers.IO) {
            Triple(
                repositorioChats.obtenerValoracionPorChat(idChatCita),
                repositorioChats.obtenerCitaPorChat(idChatCita),
                repositorioChats.obtenerMensajes(idChatCita)
            )
        }
        val mensajesVisibles = anexarAvisoServicioEliminado(chat, mensajes)
        val permiteValorar = cita?.estado in setOf(EstadoCita.FINALIZADO, EstadoCita.CERRADO)
        val mostrarModalValoracion = chat.chatCerrado && esCliente && valoracion == null && permiteValorar
        uiState = uiState.copy(
            chatActivo = chat,
            mensajesActivos = mensajesVisibles,
            citaActiva = cita,
            valoracionExistente = valoracion,
            mostrarModalValoracion = mostrarModalValoracion,
            votoValoracion = valoracion?.voto ?: uiState.votoValoracion,
            comentarioValoracion = valoracion?.comentario.orEmpty(),
            idUsuarioActual = idActual,
            error = null
        )
    }

    fun actualizarFiltroChatsContacto(activo: Boolean) {
        uiState = uiState.copy(filtroChatsContacto = activo)
    }

    fun actualizarFiltroChatsTrabajador(activo: Boolean) {
        uiState = uiState.copy(filtroChatsTrabajador = activo)
    }

    fun actualizarTipoFiltroChat(tipo: String?) {
        uiState = uiState.copy(tipoFiltroChat = tipo)
    }

    fun actualizarBusquedaChats(busqueda: String) {
        uiState = uiState.copy(busquedaChats = busqueda)
    }

    /**
     * El ID del chat creado se comunica via uiState.pendingNavChatId; consumir con consumirNavChatId().
     * [tituloServicio] y [usernameTrabajador] se almacenan en el backend para la cabecera del chat.
     * [usernameCliente] es opcional — el repositorio lo toma del store si se omite.
     */
    fun iniciarConversacionDesdeOferta(
        idOfertaServicio: Long,
        tituloServicio: String = "",
        usernameTrabajador: String = "",
        usernameCliente: String = ""
    ) {
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioChats.iniciarConversacionDesdeOferta(
                    idOfertaServicio   = idOfertaServicio,
                    tituloServicio     = tituloServicio,
                    usernameTrabajador = usernameTrabajador,
                    usernameCliente    = usernameCliente
                )
            }
            resultado.onSuccess { chat ->
                uiState = uiState.copy(
                    mensajeSistema = "Chat iniciado correctamente.",
                    error = null,
                    pendingNavChatId = chat.idChatCita
                )
                recargar()
            }.onFailure {
                uiState = uiState.copy(
                    error = it.message ?: "No se pudo iniciar la conversacion",
                    mensajeSistema = null
                )
            }
        }
    }

    /** Llamar desde la UI despues de consumir pendingNavChatId para navegar. */
    fun consumirNavChatId() {
        uiState = uiState.copy(pendingNavChatId = null)
    }

    fun abrirChat(idChatCita: Long) {
        viewModelScope.launch {
            val chat = withContext(Dispatchers.IO) { repositorioChats.obtenerChat(idChatCita) }
            if (chat == null) {
                uiState = uiState.copy(error = "No se pudo abrir el chat")
                return@launch
            }
            val idActual = repositorioChats.obtenerIdUsuarioActual()
            val esCliente = chat.idCliente == idActual
            val (valoracion, cita, mensajes) = withContext(Dispatchers.IO) {
                Triple(
                    repositorioChats.obtenerValoracionPorChat(idChatCita),
                    repositorioChats.obtenerCitaPorChat(idChatCita),
                    repositorioChats.obtenerMensajes(idChatCita)
                )
            }
            val mensajesVisibles = anexarAvisoServicioEliminado(chat, mensajes)
            val permiteValorar = cita?.estado in setOf(EstadoCita.FINALIZADO, EstadoCita.CERRADO)
            val mostrarModalValoracion = chat.chatCerrado && esCliente && valoracion == null && permiteValorar
            uiState = uiState.copy(
                chatActivo = chat,
                mensajesActivos = mensajesVisibles,
                citaActiva = cita,
                valoracionExistente = valoracion,
                mostrarModalValoracion = mostrarModalValoracion,
                votoValoracion = valoracion?.voto ?: 5,
                comentarioValoracion = valoracion?.comentario.orEmpty(),
                borradorMensaje = "",
                idUsuarioActual = idActual,
                error = null
            )
            recargar()
        }
    }

    fun actualizarBorradorMensaje(valor: String) {
        uiState = uiState.copy(borradorMensaje = valor, error = null)
    }

    fun enviarMensaje() {
        val chat = uiState.chatActivo ?: return
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioChats.enviarMensaje(chat.idChatCita, uiState.borradorMensaje)
            }
            resultado.onSuccess {
                val mensajes = withContext(Dispatchers.IO) { repositorioChats.obtenerMensajes(chat.idChatCita) }
                uiState = uiState.copy(
                    borradorMensaje = "",
                    mensajesActivos = mensajes,
                    error = null
                )
                recargar()
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo enviar el mensaje")
            }
        }
    }

    fun crearCita(fechaProgramada: String, comentario: String) {
        val chat = uiState.chatActivo ?: return
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioChats.crearCitaDesdeChat(
                    idChatCita = chat.idChatCita,
                    fechaProgramada = fechaProgramada,
                    comentario = comentario
                )
            }
            resultado.onSuccess { cita ->
                enviarMensajeSistemaCambioCita(
                    idChatCita = chat.idChatCita,
                    texto = "Se creo una cita para ${fechaProgramada.trim()}."
                )
                uiState = uiState.copy(
                    citaActiva = cita,
                    mensajeSistema = "Cita creada correctamente.",
                    error = null
                )
                abrirChat(chat.idChatCita)
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo crear la cita")
            }
        }
    }

    fun aceptarCitaTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.aceptarCitaTrabajador(chat.idChatCita) },
            mensajeExito = "Cita aceptada. Estado: Handshake.",
            mensajeSistemaChat = "El trabajador acepto la cita."
        )
    }

    fun rechazarCitaTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.rechazarCitaTrabajador(chat.idChatCita) },
            mensajeExito = "Propuesta rechazada. Puedes seguir negociando sobre la misma cita.",
            mensajeSistemaChat = "El trabajador rechazo la propuesta de cita."
        )
    }

    fun reenviarPropuestaCitaCliente() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.reenviarPropuestaCitaCliente(chat.idChatCita) },
            mensajeExito = "Propuesta reenviada. Estado actualizado a pendiente.",
            mensajeSistemaChat = "El cliente reenvi\u00f3 la propuesta de cita."
        )
    }

    fun solicitarInicioTrabajoTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.solicitarInicioTrabajoTrabajador(chat.idChatCita) },
            mensajeExito = "Solicitud de inicio enviada al cliente.",
            mensajeSistemaChat = "El trabajador solicito iniciar el servicio."
        )
    }

    fun aceptarInicioTrabajoCliente() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.aceptarInicioTrabajoCliente(chat.idChatCita) },
            mensajeExito = "Inicio del trabajo confirmado.",
            mensajeSistemaChat = "El cliente confirmo el inicio del servicio."
        )
    }

    fun solicitarFinalizarTrabajoTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.solicitarFinalizarTrabajoTrabajador(chat.idChatCita) },
            mensajeExito = "Solicitud de finalizacion enviada al cliente.",
            mensajeSistemaChat = "El trabajador solicito finalizar el servicio."
        )
    }

    fun aceptarFinalizarTrabajoCliente() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            idChatCita = chat.idChatCita,
            accion = { repositorioChats.aceptarFinalizarTrabajoCliente(chat.idChatCita) },
            mensajeExito = "Trabajo finalizado correctamente.",
            mensajeSistemaChat = "El cliente confirmo la finalizacion del servicio."
        )
    }

    fun cerrarChatActivo() {
        val chat = uiState.chatActivo ?: return
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) { repositorioChats.cerrarChat(chat.idChatCita) }
            resultado.onSuccess {
                uiState = uiState.copy(
                    mensajeSistema = "Chat finalizado. Queda en solo lectura.",
                    error = null
                )
                abrirChat(chat.idChatCita)
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo finalizar el chat")
            }
        }
    }

    fun actualizarVotoValoracion(voto: Int) {
        uiState = uiState.copy(votoValoracion = voto.coerceIn(1, 5), error = null)
    }

    fun actualizarComentarioValoracion(comentario: String) {
        uiState = uiState.copy(comentarioValoracion = comentario, error = null)
    }

    fun cerrarModalValoracion() {
        uiState = uiState.copy(mostrarModalValoracion = false)
    }

    fun guardarValoracionChat() {
        val chat = uiState.chatActivo ?: return
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioChats.guardarValoracionChat(
                    idChatCita = chat.idChatCita,
                    voto = uiState.votoValoracion,
                    comentario = uiState.comentarioValoracion
                )
            }
            resultado.onSuccess { valoracion ->
                uiState = uiState.copy(
                    valoracionExistente = valoracion,
                    mostrarModalValoracion = false,
                    mensajeSistema = "Gracias por tu valoracion.",
                    error = null
                )
                abrirChat(chat.idChatCita)
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo guardar la valoracion")
            }
        }
    }

    private fun procesarTransicionCita(
        idChatCita: Long,
        accion: () -> Result<CitaServicio>,
        mensajeExito: String,
        mensajeSistemaChat: String
    ) {
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) { accion() }
            resultado.onSuccess { citaActualizada ->
                enviarMensajeSistemaCambioCita(idChatCita, mensajeSistemaChat)
                uiState = uiState.copy(
                    citaActiva = citaActualizada,
                    mensajeSistema = mensajeExito,
                    error = null
                )
                abrirChat(idChatCita)
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo actualizar la cita")
            }
        }
    }

    private suspend fun enviarMensajeSistemaCambioCita(idChatCita: Long, texto: String) {
        withContext(Dispatchers.IO) {
            repositorioChats.enviarMensaje(idChatCita, texto, tipo = 1)
        }
    }

    private fun anexarAvisoServicioEliminado(chat: ChatCita, mensajes: List<MensajeChat>): List<MensajeChat> {
        if (!chat.servicioEliminado) return mensajes
        val yaExiste = mensajes.any {
            it.tipo == 1 && it.contenido.contains("servicio eliminado", ignoreCase = true)
        }
        if (yaExiste) return mensajes
        val aviso = MensajeChat(
            idMensajeChat = -chat.idChatCita,
            fechaEnvio = java.time.LocalDateTime.now().toString(),
            fechaRecibido = null,
            fechaLeido = null,
            idEmisor = 0L,
            idReceptor = 0L,
            idChatCita = chat.idChatCita,
            idEstado = 0L,
            contenido = "Servicio eliminado. Este chat se mantiene solo como historial.",
            tipo = 1
        )
        return mensajes + aviso
    }

    fun consumirMensajes() {
        uiState = uiState.copy(mensajeSistema = null, error = null)
    }

    fun marcarNotificacionesMostradas(idsMensaje: List<Long>) {
        if (idsMensaje.isEmpty()) return
        repositorioChats.marcarNotificacionesComoMostradas(idsMensaje)
        recargar()
    }
}

data class PerfilUiState(
    val usuario: Usuario? = null,
    val sesionCerrada: Boolean = false,
    val ofertasPropias: List<OfertaServicio> = emptyList(),
    val categorias: List<CategoriaServicio> = emptyList(),
    val comunasDisponibles: List<ComunaCatalogo> = emptyList(),
    val formularioServicio: FormularioServicio = FormularioServicio(),
    val mostrandoFormularioServicio: Boolean = false,
    val idOfertaEditando: Long? = null,
    val limiteServiciosActivos: Int = 1,
    val limiteServiciosTotales: Int = 3,
    val idsOfertasEnCurso: Set<Long> = emptySet(),
    val valoracionesPorServicio: List<ValoracionesServicio> = emptyList(),
    val errorServicio: String? = null,
    val runVerificacion: String = "",
    val dvVerificacion: String = "",
    val numeroDocumentoVerificacion: String = "",
    val ubicacionAjustes: UbicacionAjustesConfig = UbicacionAjustesConfig(),
    val errorUbicacion: String? = null,
    val mensajeUbicacion: String? = null,
    val preguntasSeguridad: List<PreguntaSeguridadConfig> = emptyList(),
    val errorPreguntasSeguridad: String? = null,
    val mensajePreguntasSeguridad: String? = null,
    val correoPerfilInput: String = "",
    val telefonoPerfilInput: String = "",
    val errorPerfilEdicion: String? = null,
    val mensajePerfilEdicion: String? = null,
    val errorVerificacion: String? = null,
    val mensajeVerificacion: String? = null,
    val cargandoPantalla: Boolean = false,
    val subiendoFotoPerfil: Boolean = false,
    val refrescando: Boolean = false
)

class PerfilViewModel(
    private val repositorioPerfil: RepositorioPerfil,
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(PerfilUiState())
        private set
    private val preguntasSeguridadCache = mutableMapOf<Int, String>()
    private var usuarioCacheId: Long? = null

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) {
                val usuario = repositorioPerfil.obtenerPerfilActual()
                val ofertasPropias = repositorioOfertas.obtenerOfertasPropias()
                val categorias = repositorioOfertas.obtenerCategoriasServicio()
                val comunasDisponibles = repositorioAutenticacion.obtenerComunas().getOrNull()
                    ?.takeIf { it.isNotEmpty() }
                val idsOfertasEnCurso = repositorioOfertas.obtenerIdsOfertasConTrabajoEnCursoPropias()
                val valoracionesPorServicio = repositorioOfertas.obtenerValoracionesPropiasPorServicio()
                val preguntasSeguridad = repositorioPerfil.obtenerPreguntasSeguridad()
                val ubicacionAjustes = repositorioPerfil.obtenerUbicacionAjustes()
                listOf(
                    usuario,
                    ofertasPropias,
                    categorias,
                    comunasDisponibles,
                    idsOfertasEnCurso,
                    valoracionesPorServicio,
                    preguntasSeguridad,
                    ubicacionAjustes
                )
            }

            val usuario = snapshot[0] as Usuario?
            val ofertasPropias = snapshot[1] as List<OfertaServicio>
            val categorias = snapshot[2] as List<CategoriaServicio>
            val comunasDisponiblesRemotas = snapshot[3] as List<ComunaCatalogo>?
            val idsOfertasEnCurso = snapshot[4] as Set<Long>
            val valoracionesPorServicio = snapshot[5] as List<ValoracionesServicio>
            val preguntasRemotas = snapshot[6] as List<PreguntaSeguridadConfig>
            val ubicacionAjustes = snapshot[7] as UbicacionAjustesConfig

            if (usuario?.idUsuario != usuarioCacheId) {
                preguntasSeguridadCache.clear()
                usuarioCacheId = usuario?.idUsuario
            }
            val ofertaPrincipal = ofertasPropias.firstOrNull()
            val formularioServicio = if (uiState.mostrandoFormularioServicio) {
                uiState.formularioServicio
            } else {
                ofertaPrincipal.toFormularioServicio()
            }

            val esPremium = usuario?.tipoPerfil == TipoPerfil.PREMIUM

            uiState = uiState.copy(
                usuario = usuario,
                ofertasPropias = ofertasPropias,
                categorias = categorias,
                comunasDisponibles = comunasDisponiblesRemotas ?: uiState.comunasDisponibles,
                idsOfertasEnCurso = idsOfertasEnCurso,
                valoracionesPorServicio = valoracionesPorServicio,
                limiteServiciosActivos = if (esPremium) 3 else 1,
                limiteServiciosTotales = if (esPremium) 5 else 3,
                formularioServicio = formularioServicio,
                runVerificacion = usuario?.run.orEmpty(),
                dvVerificacion = usuario?.dv.orEmpty(),
                correoPerfilInput = usuario?.correo.orEmpty(),
                telefonoPerfilInput = usuario?.telefono.orEmpty().normalizarTelefonoMovilSinPrefijo(),
                preguntasSeguridad = preguntasRemotas.map { pregunta ->
                    pregunta.copy(respuesta = preguntasSeguridadCache[pregunta.indice].orEmpty())
                },
                ubicacionAjustes = ubicacionAjustes
            )
        }
    }

    fun refrescarDesdeGesto() {
        if (uiState.refrescando) return
        uiState = uiState.copy(refrescando = true)
        viewModelScope.launch {
            delay(220)
            recargar()
            uiState = uiState.copy(refrescando = false)
        }
    }

    fun recargarComunas() {
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioAutenticacion.obtenerComunas()
            }
            val comunas = resultado.getOrNull()
            if (comunas.isNullOrEmpty()) {
                uiState = uiState.copy(
                    errorUbicacion = resultado.exceptionOrNull()?.message
                        ?: "No se pudieron cargar las comunas desde backend."
                )
                return@launch
            }
            uiState = uiState.copy(
                comunasDisponibles = comunas,
                errorUbicacion = null
            )
        }
    }

    fun mostrarFormularioCreacion() {
        val usuario = uiState.usuario
        if (usuario == null || usuario.tipoPerfil !in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)) {
            uiState = uiState.copy(errorServicio = "Debes verificarte como trabajador para publicar servicios")
            return
        }
        if (uiState.ofertasPropias.size >= uiState.limiteServiciosTotales) {
            uiState = uiState.copy(errorServicio = "Puedes tener hasta ${uiState.limiteServiciosTotales} servicios en total")
            return
        }
        uiState = uiState.copy(
            mostrandoFormularioServicio = true,
            formularioServicio = FormularioServicio(),
            idOfertaEditando = null,
            errorServicio = null
        )
    }

    fun mostrarFormularioEdicion(idOfertaServicio: Long) {
        val usuario = uiState.usuario
        if (usuario == null || usuario.tipoPerfil !in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)) {
            uiState = uiState.copy(errorServicio = "Debes verificarte como trabajador para editar servicios")
            return
        }
        val oferta = uiState.ofertasPropias.firstOrNull { it.idOfertaServicio == idOfertaServicio }
        if (oferta == null) {
            uiState = uiState.copy(errorServicio = "No se encontro el servicio a editar")
            return
        }
        uiState = uiState.copy(
            mostrandoFormularioServicio = true,
            formularioServicio = oferta.toFormularioServicio(),
            idOfertaEditando = oferta.idOfertaServicio,
            errorServicio = null
        )
    }

    fun cancelarFormularioServicio() {
        val ofertaRecuperada = uiState.idOfertaEditando?.let { id ->
            uiState.ofertasPropias.firstOrNull { it.idOfertaServicio == id }
        } ?: uiState.ofertasPropias.firstOrNull()
        uiState = uiState.copy(
            mostrandoFormularioServicio = false,
            formularioServicio = ofertaRecuperada.toFormularioServicio(),
            idOfertaEditando = null,
            errorServicio = null
        )
    }

    fun actualizarTituloServicio(valor: String) {
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(titulo = valor),
            errorServicio = null
        )
    }

    fun actualizarDescripcionServicio(valor: String) {
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(descripcion = valor),
            errorServicio = null
        )
    }

    fun actualizarPrecioServicio(valor: String) {
        val monto = valor.filter { it.isDigit() }.toIntOrNull() ?: 0
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(
                montoBase = monto,
                precioTexto = PrecioUtils.construirPrecioTexto(
                    uiState.formularioServicio.tipoPrecio,
                    monto
                )
            ),
            errorServicio = null
        )
    }

    fun actualizarTipoPrecioServicio(tipoPrecio: Int) {
        val montoAjustado = if (tipoPrecio == TipoPrecio.CONTACTAR) {
            0
        } else {
            uiState.formularioServicio.montoBase.coerceIn(PrecioUtils.MIN_MONTO, PrecioUtils.MAX_MONTO)
        }
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(
                tipoPrecio = tipoPrecio,
                montoBase = montoAjustado,
                precioTexto = PrecioUtils.construirPrecioTexto(tipoPrecio, montoAjustado)
            ),
            errorServicio = null
        )
    }

    fun actualizarCategoriaServicio(idCategoriaServicio: Long) {
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(idCategoriaServicio = idCategoriaServicio),
            errorServicio = null
        )
    }

    fun cambiarDisponibilidadServicioRapido(idOfertaServicio: Long, valor: Boolean) {
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(180)
            val resultado = withContext(Dispatchers.IO) {
                repositorioOfertas.actualizarDisponibilidadOfertaPropia(idOfertaServicio, valor)
            }
            resultado.onSuccess {
                recargar()
                uiState = uiState.copy(errorServicio = null)
            }.onFailure {
                uiState = uiState.copy(errorServicio = it.message ?: "No se pudo actualizar disponibilidad")
            }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun actualizarFotoServicio(
        uriLocal: String,
        nombreArchivo: String,
        mimeType: String
    ) {
        val fotoActual = uiState.formularioServicio.foto
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(
                foto = FotoServicioLocal(
                    idFoto = fotoActual?.idFoto,
                    uriLocal = uriLocal,
                    nombreArchivo = nombreArchivo,
                    mimeType = mimeType,
                    pendienteSincronizacion = true,
                    urlRemota = fotoActual?.urlRemota
                )
            ),
            errorServicio = null
        )
    }

    fun quitarFotoServicio() {
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(foto = null),
            errorServicio = null
        )
    }

    fun guardarServicio() {
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(220)
            val resultado = withContext(Dispatchers.IO) {
                repositorioOfertas.guardarOfertaPropia(
                    formulario = uiState.formularioServicio,
                    idOfertaServicio = uiState.idOfertaEditando
                )
            }
            resultado
                .onSuccess { oferta ->
                    recargar()
                    uiState = uiState.copy(
                        formularioServicio = oferta.toFormularioServicio(),
                        mostrandoFormularioServicio = false,
                        idOfertaEditando = null,
                        errorServicio = null
                    )
                }
                .onFailure {
                    uiState = uiState.copy(errorServicio = it.message ?: "No se pudo guardar el servicio")
                }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun actualizarFotoPerfil(uriLocal: String) {
        viewModelScope.launch {
            uiState = uiState.copy(subiendoFotoPerfil = true)
            withContext(Dispatchers.IO) {
                repositorioPerfil.actualizarFotoPerfil(uriLocal)
            }
                .onSuccess { usuarioActualizado ->
                    uiState = uiState.copy(usuario = usuarioActualizado, errorServicio = null)
                }
                .onFailure {
                    uiState = uiState.copy(errorServicio = it.message ?: "No se pudo actualizar la foto de perfil")
                }
            uiState = uiState.copy(subiendoFotoPerfil = false)
        }
    }

    fun eliminarServicio() {
        val idOferta = uiState.idOfertaEditando
        if (idOferta == null) {
            uiState = uiState.copy(errorServicio = "No hay un servicio seleccionado para eliminar")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(220)
            val resultado = withContext(Dispatchers.IO) {
                repositorioOfertas.eliminarOfertaPropia(idOferta)
            }
            resultado
                .onSuccess {
                    recargar()
                    uiState = uiState.copy(
                        formularioServicio = FormularioServicio(),
                        mostrandoFormularioServicio = false,
                        idOfertaEditando = null,
                        errorServicio = null
                    )
                }
                .onFailure {
                    uiState = uiState.copy(errorServicio = it.message ?: "No se pudo eliminar el servicio")
                }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun actualizarRunVerificacion(valor: String) {
        uiState = uiState.copy(
            runVerificacion = valor.filter { it.isDigit() }.take(8),
            errorVerificacion = null,
            mensajeVerificacion = null
        )
    }

    fun actualizarDvVerificacion(valor: String) {
        val dvNormalizado = valor.uppercase().filter { it.isDigit() || it == 'K' }.take(1)
        uiState = uiState.copy(dvVerificacion = dvNormalizado, errorVerificacion = null, mensajeVerificacion = null)
    }

    fun actualizarNumeroDocumentoVerificacion(valor: String) {
        uiState = uiState.copy(
            numeroDocumentoVerificacion = valor.filter { it.isDigit() }.take(9),
            errorVerificacion = null,
            mensajeVerificacion = null
        )
    }

    fun limpiarEstadoVerificacion() {
        uiState = uiState.copy(
            errorVerificacion = null,
            mensajeVerificacion = null
        )
    }

    fun actualizarRegionUbicacion(valor: String) {
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(region = valor),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun actualizarComunaUbicacion(valor: String) {
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(comuna = valor),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun actualizarCalleUbicacion(valor: String) {
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(calle = valor),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun actualizarNumeroUbicacion(valor: String) {
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(numero = valor),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun actualizarDetalleUbicacion(valor: String) {
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(detalle = valor),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun actualizarRangoUbicacion(valor: Float) {
        val rangoNormalizado = EscalaRango.valorPorPosicionSlider(valor)
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(rangoDisponibilidadM = rangoNormalizado),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun guardarCoordenadasGps(latitud: Double, longitud: Double) {
        val baseActual = uiState.ubicacionAjustes
        val normalizada = baseActual.copy(
            latitud = latitud,
            longitud = longitud,
            region = "Region Metropolitana",
            comuna = baseActual.comuna.valorUbicacionPorDefecto("Santiago"),
            calle = baseActual.calle.valorUbicacionPorDefecto("Sin calle"),
            numero = baseActual.numero.valorUbicacionPorDefecto("Sin numero"),
            detalle = baseActual.detalle.valorUbicacionPorDefecto("Sin detalle"),
            rangoDisponibilidadM = EscalaRango.normalizar(baseActual.rangoDisponibilidadM),
            rangoBusquedaM = EscalaRango.normalizar(baseActual.rangoBusquedaM)
        )
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(180)
            withContext(Dispatchers.IO) {
                repositorioPerfil.guardarUbicacionAjustes(normalizada)
            }
                .onSuccess { guardada ->
                    uiState = uiState.copy(
                        ubicacionAjustes = guardada,
                        errorUbicacion = null,
                        mensajeUbicacion = "Ubicacion actualizada correctamente."
                    )
                    recargar()
                }
                .onFailure {
                    uiState = uiState.copy(
                        errorUbicacion = it.message ?: "No se pudo actualizar la ubicacion",
                        mensajeUbicacion = null
                    )
                }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun reportarErrorUbicacion(mensaje: String) {
        uiState = uiState.copy(
            errorUbicacion = null,
            mensajeUbicacion = mensaje
        )
    }

    fun guardarUbicacionAjustes() {
        val actual = uiState.ubicacionAjustes
        val normalizada = actual.copy(
            region = "Region Metropolitana",
            comuna = actual.comuna.valorUbicacionPorDefecto("Santiago"),
            calle = actual.calle.valorUbicacionPorDefecto("Sin calle"),
            numero = actual.numero.valorUbicacionPorDefecto("Sin numero"),
            detalle = actual.detalle.valorUbicacionPorDefecto("Sin detalle"),
            rangoDisponibilidadM = EscalaRango.normalizar(actual.rangoDisponibilidadM),
            rangoBusquedaM = EscalaRango.normalizar(actual.rangoBusquedaM)
        )

        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(180)
            withContext(Dispatchers.IO) {
                repositorioPerfil.guardarUbicacionAjustes(normalizada)
            }
                .onSuccess { guardada ->
                    uiState = uiState.copy(
                        ubicacionAjustes = guardada,
                        errorUbicacion = null,
                        mensajeUbicacion = "Direccion y rango guardados."
                    )
                    recargar()
                }
                .onFailure {
                    uiState = uiState.copy(
                        errorUbicacion = it.message ?: "No se pudo guardar la ubicacion",
                        mensajeUbicacion = null
                    )
                }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun consumirMensajeUbicacion() {
        uiState = uiState.copy(mensajeUbicacion = null)
    }

    fun guardarPreguntaSeguridad(indice: Int, pregunta: String, respuesta: String) {
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            val resultado = withContext(Dispatchers.IO) {
                repositorioPerfil.guardarPreguntaSeguridad(indice, pregunta, respuesta)
            }
            resultado
                .onSuccess { preguntas ->
                    preguntasSeguridadCache[indice] = respuesta
                    val preguntasActualizadas = preguntas.map { item ->
                        if (item.indice == indice) {
                            item.copy(
                                pregunta = pregunta,
                                respuesta = respuesta
                            )
                        } else {
                            item
                        }
                    }
                    uiState = uiState.copy(
                        preguntasSeguridad = preguntasActualizadas,
                        errorPreguntasSeguridad = null,
                        mensajePreguntasSeguridad = "Pregunta de seguridad $indice guardada."
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        errorPreguntasSeguridad = it.message ?: "No se pudo guardar la pregunta",
                        mensajePreguntasSeguridad = null
                    )
                }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun limpiarMensajesPreguntasSeguridad() {
        uiState = uiState.copy(
            errorPreguntasSeguridad = null,
            mensajePreguntasSeguridad = null
        )
    }

    fun validarContrasenaCuenta(contrasena: String): Result<Unit> {
        val usuario = uiState.usuario
            ?: return Result.failure(IllegalStateException("No hay sesion activa"))
        val valor = contrasena.trim()
        if (valor.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa tu contrasena de cuenta"))
        }
        return if (usuario.contrasenaHash == valor) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Contrasena incorrecta"))
        }
    }

    fun actualizarCorreoPerfil(valor: String) {
        uiState = uiState.copy(
            correoPerfilInput = valor.trim().take(254),
            errorPerfilEdicion = null,
            mensajePerfilEdicion = null
        )
    }

    fun actualizarTelefonoPerfil(valor: String) {
        uiState = uiState.copy(
            telefonoPerfilInput = valor.normalizarTelefonoMovilSinPrefijo(),
            errorPerfilEdicion = null,
            mensajePerfilEdicion = null
        )
    }

    fun guardarEdicionPerfil() {
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(180)
            withContext(Dispatchers.IO) {
                repositorioPerfil.actualizarContactoPerfil(
                    correo = uiState.correoPerfilInput,
                    telefono = uiState.telefonoPerfilInput
                )
            }.onSuccess { usuarioActualizado ->
                uiState = uiState.copy(
                    usuario = usuarioActualizado,
                    correoPerfilInput = usuarioActualizado.correo,
                    telefonoPerfilInput = usuarioActualizado.telefono,
                    errorPerfilEdicion = null,
                    mensajePerfilEdicion = "Perfil actualizado correctamente."
                )
                recargar()
            }.onFailure {
                val mensaje = it.message.orEmpty()
                uiState = uiState.copy(
                    errorPerfilEdicion = when {
                        mensaje.contains("truncat", ignoreCase = true) ||
                            mensaje.contains("String or binary data would be truncated", ignoreCase = true) ||
                            mensaje.contains("SQL", ignoreCase = true) ->
                            "Uno de los campos supera el limite permitido. Reduce el texto y vuelve a intentar."
                        else -> it.message ?: "No se pudo actualizar el perfil"
                    },
                    mensajePerfilEdicion = null
                )
            }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun limpiarMensajesPerfilEdicion() {
        uiState = uiState.copy(
            errorPerfilEdicion = null,
            mensajePerfilEdicion = null
        )
    }

    fun solicitarVerificacionTrabajador() {
        if (uiState.runVerificacion.length !in 7..8) {
            uiState = uiState.copy(errorVerificacion = "El RUN debe tener 7 u 8 digitos")
            return
        }
        if (uiState.dvVerificacion.isBlank()) {
            uiState = uiState.copy(errorVerificacion = "Ingresa el DV del RUN")
            return
        }
        if (uiState.numeroDocumentoVerificacion.length != 9) {
            uiState = uiState.copy(errorVerificacion = "El numero de documento debe tener 9 digitos")
            return
        }
        val credencialesSesion = uiState.usuario?.let { usuario ->
            usuario.username to usuario.contrasenaHash
        }
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(180)
            withContext(Dispatchers.IO) {
                repositorioPerfil.solicitarVerificacionTrabajador(
                    run = uiState.runVerificacion,
                    dv = uiState.dvVerificacion,
                    numeroDocumento = uiState.numeroDocumentoVerificacion
                )
            }.onSuccess { usuarioActualizado ->
                val usernameSesion = credencialesSesion?.first
                val contrasenaSesion = credencialesSesion?.second
                val reautenticado = if (!usernameSesion.isNullOrBlank() && !contrasenaSesion.isNullOrBlank()) {
                    withContext(Dispatchers.IO) {
                        repositorioAutenticacion.iniciarSesion(
                            identificador = usernameSesion,
                            contrasena = contrasenaSesion,
                            recordarme = true
                        )
                    }
                } else {
                    Result.failure(IllegalStateException("No se encontro la credencial temporal para renovar la sesion."))
                }

                reautenticado.onSuccess { usuarioSesionRenovada ->
                    uiState = uiState.copy(
                        usuario = usuarioSesionRenovada,
                        errorVerificacion = null,
                        mensajeVerificacion = "Cuenta verificada. Sesion renovada como trabajador."
                    )
                    recargar()
                }.onFailure {
                    uiState = uiState.copy(
                        usuario = usuarioActualizado,
                        errorVerificacion = null,
                        mensajeVerificacion = "Cuenta verificada, pero no se pudo renovar la sesion. Vuelve a iniciar sesion."
                    )
                    cerrarSesion()
                }
            }.onFailure {
                uiState = uiState.copy(
                    errorVerificacion = it.message ?: "No se pudo iniciar la verificacion",
                    mensajeVerificacion = null
                )
            }
            uiState = uiState.copy(cargandoPantalla = false)
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repositorioAutenticacion.cerrarSesion()
            }
            preguntasSeguridadCache.clear()
            usuarioCacheId = null
            uiState = PerfilUiState(sesionCerrada = true)
        }
    }

    fun consumirCierreSesion() {
        uiState = uiState.copy(sesionCerrada = false)
    }

    private fun String.normalizarTelefonoMovilSinPrefijo(): String {
        val digitos = filter { it.isDigit() }
            .let { if (it.startsWith("56")) it.drop(2) else it }
            .let { if (it.length == 9 && it.startsWith("9")) it.drop(1) else it }
        return digitos.take(8)
    }

    private fun OfertaServicio?.toFormularioServicio(): FormularioServicio = if (this == null) {
        FormularioServicio(
            tipoPrecio = TipoPrecio.FIJO,
            montoBase = 20_000,
            precioTexto = PrecioUtils.construirPrecioTexto(TipoPrecio.FIJO, 20_000)
        )
    } else {
        FormularioServicio(
            titulo = titulo,
            descripcion = descripcion,
            precioTexto = precioTexto,
            tipoPrecio = tipoPrecio,
            montoBase = montoBase,
            idCategoriaServicio = idCategoriaServicio,
            disponible = disponible,
            foto = if (fotoUrlReferencia.isBlank()) {
                null
            } else {
                FotoServicioLocal(
                    idFoto = idFotoPortada,
                    uriLocal = fotoUrlReferencia,
                    nombreArchivo = fotoNombreArchivo,
                    mimeType = fotoMimeType,
                    pendienteSincronizacion = fotoPendienteSincronizacion
                )
            }
        )
    }

    private fun String.valorUbicacionPorDefecto(defecto: String): String {
        return trim().ifBlank { defecto }
    }
}

data class ReportesUiState(
    val tiposReporte: List<TipoReporte> = emptyList(),
    val reportes: List<Reporte> = emptyList(),
    val reporteActivo: Reporte? = null,
    val busqueda: String = "",
    val filtroTipoReporteId: Long? = null,
    val filtroEstadoRevision: String? = null,
    val ordenarRecientes: Boolean = true,
    val cargando: Boolean = false,
    val mensajeSistema: String? = null,
    val error: String? = null
)

class ReportesViewModel(
    private val repositorioReportes: RepositorioReportes
) : ViewModel() {
    var uiState by mutableStateOf(ReportesUiState())
        private set

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            val snapshot = uiState
            val (tipos, reportes) = withContext(Dispatchers.IO) {
                repositorioReportes.obtenerTiposReporte() to
                    repositorioReportes.obtenerReportesModeracion(
                        busqueda = snapshot.busqueda,
                        idTipoReporte = snapshot.filtroTipoReporteId,
                        estadoRevision = snapshot.filtroEstadoRevision,
                        ordenarRecientes = snapshot.ordenarRecientes
                    )
            }
            uiState = uiState.copy(
                tiposReporte = tipos,
                reportes = reportes
            )
        }
    }

    fun actualizarBusqueda(valor: String) {
        uiState = uiState.copy(busqueda = valor, error = null)
        recargar()
    }

    fun actualizarFiltroTipo(idTipo: Long?) {
        uiState = uiState.copy(filtroTipoReporteId = idTipo, error = null)
        recargar()
    }

    fun actualizarFiltroEstado(estado: String?) {
        uiState = uiState.copy(filtroEstadoRevision = estado, error = null)
        recargar()
    }

    fun actualizarOrdenRecientes(recientes: Boolean) {
        uiState = uiState.copy(ordenarRecientes = recientes)
        recargar()
    }

    fun abrirDetalle(idReporte: Long) {
        viewModelScope.launch {
            val detalle = withContext(Dispatchers.IO) {
                repositorioReportes.obtenerDetalleReporte(idReporte)
            }
            if (detalle == null) {
                uiState = uiState.copy(error = "No se pudo cargar el detalle del reporte")
                return@launch
            }
            uiState = uiState.copy(reporteActivo = detalle, error = null)
        }
    }

    fun cerrarDetalle() {
        uiState = uiState.copy(reporteActivo = null)
    }

    fun crearReporteDesdeOferta(
        idOfertaServicio: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> {
        val resultado = repositorioReportes.crearReporteDesdeOferta(
            idOfertaServicio = idOfertaServicio,
            idTipoReporte = idTipoReporte,
            comentario = comentario
        )
        resultado.onSuccess {
            uiState = uiState.copy(
                mensajeSistema = "Reporte enviado correctamente.",
                error = null
            )
            recargar()
        }.onFailure {
            uiState = uiState.copy(error = it.message ?: "No se pudo enviar el reporte")
        }
        return resultado
    }

    fun crearReporteDesdeChat(
        idChatCita: Long,
        idTipoReporte: Long,
        comentario: String
    ): Result<Reporte> {
        val resultado = repositorioReportes.crearReporteDesdeChat(
            idChatCita = idChatCita,
            idTipoReporte = idTipoReporte,
            comentario = comentario
        )
        resultado.onSuccess {
            uiState = uiState.copy(
                mensajeSistema = "Reporte enviado correctamente.",
                error = null
            )
            recargar()
        }.onFailure {
            uiState = uiState.copy(error = it.message ?: "No se pudo enviar el reporte")
        }
        return resultado
    }

    fun aplicarMedidaModeracion(idReporte: Long, accion: String) {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            delay(120)
            val resultado = withContext(Dispatchers.IO) {
                repositorioReportes.aplicarMedidaModeracion(idReporte, accion)
            }
            resultado
                .onSuccess { actualizado ->
                    val etiquetaAccion = when (accion) {
                        AccionModeracion.DESACTIVAR_SERVICIO -> "Servicio desactivado y reporte resuelto."
                        AccionModeracion.ELIMINAR_SERVICIO -> "Servicio eliminado logicamente y reporte resuelto."
                        AccionModeracion.IGNORAR_REPORTE -> "Reporte marcado como ignorado."
                        AccionModeracion.BANEAR_USUARIO -> "Usuario baneado y reporte resuelto."
                        else -> if (accion.startsWith(AccionModeracion.SUSPENDER_USUARIO_HASTA)) {
                            "Usuario suspendido hasta fecha definida y reporte resuelto."
                        } else {
                            "Reporte resuelto."
                        }
                    }
                    recargar()
                    uiState = uiState.copy(
                        reporteActivo = actualizado,
                        mensajeSistema = etiquetaAccion,
                        error = null
                    )
                }
                .onFailure {
                    uiState = uiState.copy(error = it.message ?: "No se pudo aplicar la medida")
                }
            uiState = uiState.copy(cargando = false)
        }
    }

    fun consumirMensajes() {
        uiState = uiState.copy(mensajeSistema = null, error = null)
    }
}

data class DetalleServicioUiState(
    val ofertas: List<OfertaServicio> = emptyList(),
    val indiceActual: Int = 0,
    val idUsuarioActual: Long? = null,
    val latitudUsuario: Double? = null,
    val longitudUsuario: Double? = null,
    val fotosOferta: List<FotoOferta> = emptyList(),
    val subiendoFoto: Boolean = false,
    val errorFoto: String? = null
) {
    val ofertaActual: OfertaServicio? get() = ofertas.getOrNull(indiceActual)
}

class DetalleServicioViewModel(
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioPerfil: RepositorioPerfil
) : ViewModel() {
    var uiState by mutableStateOf(DetalleServicioUiState())
        private set

    private var ofertaActualId: Long? = null
    private var ofertasContextoMarketplace: List<OfertaServicio>? = null

    fun prepararContextoMarketplace(ofertasVisibles: List<OfertaServicio>) {
        ofertasContextoMarketplace = ofertasVisibles
    }

    fun cargarOferta(idOfertaServicio: Long, forzarRecarga: Boolean = false) {
        if (!forzarRecarga && ofertaActualId == idOfertaServicio && uiState.ofertaActual != null) return
        ofertaActualId = idOfertaServicio
        viewModelScope.launch {
            data class Snapshot(
                val idUsuarioActual: Long?,
                val latitudUsuario: Double?,
                val longitudUsuario: Double?,
                val ofertas: List<OfertaServicio>,
                val ofertaEliminada: OfertaServicio?,
                val ofertasContexto: List<OfertaServicio>?
            )
            val snapshot = withContext(Dispatchers.IO) {
                val idUsuarioActual = repositorioPerfil.obtenerPerfilActual()?.idUsuario
                val ubicacion = repositorioPerfil.obtenerUbicacionAjustes()
                val ofertasContexto = ofertasContextoMarketplace?.takeIf { it.isNotEmpty() }
                val ofertas = ofertasContexto ?: repositorioOfertas.obtenerOfertasMarketplace()
                val ofertaEliminada = if (ofertas.isEmpty() || ofertas.indexOfFirst { it.idOfertaServicio == idOfertaServicio } < 0) {
                    repositorioOfertas.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = true)
                } else null
                Snapshot(idUsuarioActual, ubicacion.latitud, ubicacion.longitud, ofertas, ofertaEliminada, ofertasContexto)
            }
            val idUsuarioActual = snapshot.idUsuarioActual
            val latitudUsuario = snapshot.latitudUsuario
            val longitudUsuario = snapshot.longitudUsuario
            val ofertas = snapshot.ofertas
            val ofertaEliminada = snapshot.ofertaEliminada
            val ofertasContexto = snapshot.ofertasContexto
            if (ofertas.isEmpty()) {
                uiState = uiState.copy(
                    ofertas = listOfNotNull(ofertaEliminada),
                    indiceActual = 0,
                    idUsuarioActual = idUsuarioActual,
                    latitudUsuario = latitudUsuario,
                    longitudUsuario = longitudUsuario
                )
                return@launch
            }
            val indice = ofertas.indexOfFirst { it.idOfertaServicio == idOfertaServicio }
            if (indice < 0) {
                if (ofertaEliminada != null) {
                    uiState = uiState.copy(
                        ofertas = listOf(ofertaEliminada),
                        indiceActual = 0,
                        idUsuarioActual = idUsuarioActual,
                        latitudUsuario = latitudUsuario,
                        longitudUsuario = longitudUsuario
                    )
                } else if (ofertasContexto != null) {
                    uiState = uiState.copy(
                        ofertas = ofertasContexto,
                        indiceActual = 0,
                        idUsuarioActual = idUsuarioActual,
                        latitudUsuario = latitudUsuario,
                        longitudUsuario = longitudUsuario
                    )
                } else {
                    val fallback = withContext(Dispatchers.IO) {
                        repositorioOfertas.obtenerOfertasMarketplace()
                    }
                    val indiceFallback = fallback.indexOfFirst { it.idOfertaServicio == idOfertaServicio }.takeIf { it >= 0 } ?: 0
                    uiState = uiState.copy(
                        ofertas = fallback,
                        indiceActual = indiceFallback,
                        idUsuarioActual = idUsuarioActual,
                        latitudUsuario = latitudUsuario,
                        longitudUsuario = longitudUsuario
                    )
                }
            } else {
                uiState = uiState.copy(
                    ofertas = ofertas,
                    indiceActual = indice,
                    idUsuarioActual = idUsuarioActual,
                    latitudUsuario = latitudUsuario,
                    longitudUsuario = longitudUsuario
                )
            }
        }
    }

    fun recargarOfertaActual() {
        val id = ofertaActualId ?: uiState.ofertaActual?.idOfertaServicio ?: return
        cargarOferta(idOfertaServicio = id, forzarRecarga = true)
    }

    fun avanzarTarjeta() {
        val nuevoIndice = (uiState.indiceActual + 1).coerceAtMost(uiState.ofertas.lastIndex)
        uiState = uiState.copy(indiceActual = nuevoIndice)
    }

    fun retrocederTarjeta() {
        val nuevoIndice = (uiState.indiceActual - 1).coerceAtLeast(0)
        uiState = uiState.copy(indiceActual = nuevoIndice)
    }

    fun establecerIndiceActual(indice: Int) {
        if (uiState.ofertas.isEmpty()) return
        val indiceNormalizado = indice.coerceIn(0, uiState.ofertas.lastIndex)
        if (indiceNormalizado != uiState.indiceActual) {
            uiState = uiState.copy(indiceActual = indiceNormalizado)
        }
    }

    fun cargarFotosOferta(idOferta: Long) {
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioOfertas.listarFotosOferta(idOferta)
            }
            resultado
                .onSuccess { fotos -> uiState = uiState.copy(fotosOferta = fotos, errorFoto = null) }
                .onFailure { uiState = uiState.copy(errorFoto = it.message) }
        }
    }

    fun subirFoto(uriString: String, idOferta: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(subiendoFoto = true, errorFoto = null)
            val resultado = withContext(Dispatchers.IO) {
                repositorioOfertas.subirFotoOferta(uriString, idOferta)
            }
            resultado
                .onSuccess { nuevaFoto ->
                    uiState = uiState.copy(
                        fotosOferta = uiState.fotosOferta + nuevaFoto,
                        errorFoto = null
                    )
                }
                .onFailure { uiState = uiState.copy(errorFoto = it.message ?: "No se pudo subir la foto") }
            uiState = uiState.copy(subiendoFoto = false)
        }
    }

    fun eliminarFoto(idFoto: Long, idOferta: Long) {
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                repositorioOfertas.eliminarFotoOferta(idFoto)
            }
            resultado
                .onSuccess { cargarFotosOferta(idOferta) }
                .onFailure { uiState = uiState.copy(errorFoto = it.message ?: "No se pudo eliminar la foto") }
        }
    }

    fun consumirErrorFoto() {
        uiState = uiState.copy(errorFoto = null)
    }
}

// ── Pantalla de moderacion de baneos ──────────────────────────────────────────

data class BaneosUiState(
    val cargando: Boolean = false,
    val usuarios: List<com.movil.contrabajo.domain.model.UsuarioBaneado> = emptyList(),
    val error: String? = null,
    val mensajeExito: String? = null,
    val desbaneoEnCurso: Set<Int> = emptySet()
)

class BaneosViewModel(
    private val repositorioBaneos: com.movil.contrabajo.data.repository.RepositorioBaneos
) : ViewModel() {

    var uiState by mutableStateOf(BaneosUiState())
        private set

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true, error = null)
            val lista = withContext(Dispatchers.IO) {
                repositorioBaneos.listarBaneados()
            }
            uiState = uiState.copy(cargando = false, usuarios = lista)
        }
    }

    fun desbanear(idUsuario: Int) {
        if (idUsuario in uiState.desbaneoEnCurso) return
        viewModelScope.launch {
            uiState = uiState.copy(desbaneoEnCurso = uiState.desbaneoEnCurso + idUsuario, error = null)
            val resultado = withContext(Dispatchers.IO) {
                repositorioBaneos.desbanearUsuario(idUsuario)
            }
            resultado
                .onSuccess {
                    uiState = uiState.copy(
                        mensajeExito = "Usuario desbaneado correctamente.",
                        usuarios = uiState.usuarios.filter { it.idUsuario != idUsuario },
                        desbaneoEnCurso = uiState.desbaneoEnCurso - idUsuario
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        error = it.message ?: "No se pudo desbanear al usuario.",
                        desbaneoEnCurso = uiState.desbaneoEnCurso - idUsuario
                    )
                }
        }
    }

    fun consumirMensaje() {
        uiState = uiState.copy(mensajeExito = null, error = null)
    }
}

// ============================================================================
// PREMIUM
// ============================================================================

enum class EstadoPagoPremium { INICIAL, CREANDO_PREFERENCIA, ESPERANDO_CONFIRMACION, VERIFICANDO, LISTO, ERROR }

data class PremiumSerieDia(
    val etiqueta: String,
    val cantidad: Int,
    val destacado: Boolean = false
)

data class PremiumHistorialContacto(
    val idChatCita: Long,
    val nombreContacto: String,
    val tituloServicio: String,
    val fechaTermino: String,
    val resultado: String,
    val estrellas: Int? = null
)

data class PremiumStats(
    val chatsTotales: Int = 0,
    val chatsActivos: Int = 0,
    val mensajesNoLeidos: Int = 0,
    val serviciosActivos: Int = 0,
    val serviciosTotales: Int = 0,
    val citasFinalizadas: Int = 0,
    val citasCanceladas: Int = 0,
    val citasRechazadas: Int = 0,
    val citasTotales: Int = 0,
    val valoracionesTotales: Int = 0,
    val promedioValoracion: Double = 0.0,
    val contactosUltimos7Dias: Int = 0,
    val tasaConversionCita: Int = 0,
    val ingresoTotalCerrado: Int = 0,
    val ticketPromedio: Int = 0,
    val mejorDiaContactos: String = "—",
    val mejorDiaIngresos: String = "—",
    val contactosPorDia: List<PremiumSerieDia> = emptyList(),
    val ingresosPorDia: List<PremiumSerieDia> = emptyList()
)

data class PremiumUiState(
    val esPremium: Boolean = false,
    val estadoPago: EstadoPagoPremium = EstadoPagoPremium.INICIAL,
    val errorPago: String? = null,
    val checkoutUrl: String? = null,
    val cargandoStats: Boolean = true,
    val stats: PremiumStats = PremiumStats(),
    val historialContactos: List<PremiumHistorialContacto> = emptyList()
)

class PremiumViewModel(
    private val repositorioPerfil: RepositorioPerfil,
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioChats: RepositorioChats
) : ViewModel() {
    var uiState by mutableStateOf(PremiumUiState())
        private set

    init { refrescarEstadoPremium() }

    fun refrescarEstadoPremium() {
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) { repositorioPerfil.obtenerPerfilActual() }
            uiState = uiState.copy(esPremium = usuario?.tipoPerfil == TipoPerfil.PREMIUM)
        }
    }

    fun procesarPagoPremium() {
        if (uiState.estadoPago in listOf(EstadoPagoPremium.CREANDO_PREFERENCIA, EstadoPagoPremium.VERIFICANDO)) return
        Log.i("PremiumFlow", "Iniciando creacion de preferencia Premium")
        uiState = uiState.copy(
            estadoPago = EstadoPagoPremium.CREANDO_PREFERENCIA,
            errorPago = null,
            checkoutUrl = null
        )
        viewModelScope.launch {
            delay(350)
            val resultado = withContext(Dispatchers.IO) { repositorioPerfil.crearPreferenciaPremium() }
            uiState = resultado.fold(
                onSuccess = {
                    Log.i("PremiumFlow", "Checkout Premium listo, esperando confirmacion")
                    uiState.copy(
                        estadoPago = EstadoPagoPremium.ESPERANDO_CONFIRMACION,
                        checkoutUrl = it.initPoint
                    )
                },
                onFailure = {
                    Log.w("PremiumFlow", "Fallo al crear preferencia Premium", it)
                    uiState.copy(
                        estadoPago = EstadoPagoPremium.ERROR,
                        errorPago = it.message ?: "No se pudo activar Premium"
                    )
                }
            )
        }
    }

    fun consumirCheckoutUrl() {
        if (uiState.checkoutUrl == null) return
        uiState = uiState.copy(checkoutUrl = null)
    }

    fun verificarEstadoPagoPremium() {
        if (uiState.estadoPago != EstadoPagoPremium.ESPERANDO_CONFIRMACION) return
        Log.i("PremiumFlow", "Verificando estado Premium contra backend")
        uiState = uiState.copy(estadoPago = EstadoPagoPremium.VERIFICANDO, errorPago = null)
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) { repositorioPerfil.verificarEstadoPremium() }
            uiState = resultado.fold(
                onSuccess = { premium ->
                    if (premium) {
                        Log.i("PremiumFlow", "Backend confirma usuario PREMIUM")
                        uiState.copy(estadoPago = EstadoPagoPremium.LISTO, esPremium = true)
                    } else {
                        Log.i("PremiumFlow", "Backend aun responde premium=false")
                        uiState.copy(
                            estadoPago = EstadoPagoPremium.ESPERANDO_CONFIRMACION,
                            errorPago = "Tu pago aun no figura aprobado. Si ya pagaste, vuelve a intentarlo en unos segundos."
                        )
                    }
                },
                onFailure = {
                    Log.w("PremiumFlow", "Fallo verificando estado Premium", it)
                    uiState.copy(
                        estadoPago = EstadoPagoPremium.ERROR,
                        errorPago = it.message ?: "No se pudo verificar el estado del pago"
                    )
                }
            )
        }
    }

    fun reiniciarPago() {
        uiState = uiState.copy(estadoPago = EstadoPagoPremium.INICIAL, errorPago = null, checkoutUrl = null)
    }

    fun cargarEstadisticas() {
        uiState = uiState.copy(cargandoStats = true)
        viewModelScope.launch {
          try {
            val stats = withContext(Dispatchers.IO) {
                val usuario = repositorioPerfil.obtenerPerfilActual()
                val chats = repositorioChats.obtenerChatsActuales()
                val ofertas = repositorioOfertas.obtenerOfertasPropias()
                val citas = repositorioOfertas.obtenerMisCitas()
                val valoraciones = repositorioOfertas.obtenerValoracionesPropiasPorServicio()
                val votos = valoraciones.flatMap { it.valoraciones }.map { it.voto }
                val valoracionesPorChat = valoraciones
                    .flatMap { it.valoraciones }
                    .associateBy { it.idChatCita }
                val citasPorChat = citas.associateBy { it.idChatCita }
                val historial = chats
                    .filter { usuario == null || it.idTrabajador == usuario.idUsuario }
                    .map { chat ->
                        val cita = citasPorChat[chat.idChatCita]
                        val valoracion = valoracionesPorChat[chat.idChatCita]
                        PremiumHistorialContacto(
                            idChatCita = chat.idChatCita,
                            nombreContacto = chat.nombreContacto.ifBlank { chat.usernameContacto.ifBlank { "Contacto" } },
                            tituloServicio = chat.tituloServicio.ifBlank { "Servicio sin título" },
                            fechaTermino = cita?.fechaFinTrabajo
                                ?: cita?.fechaProgramada
                                ?: chat.horaUltimoMensaje.ifBlank { chat.fechaCreacion },
                            resultado = describirResultadoContacto(chat, cita),
                            estrellas = if (chat.chatCerrado) valoracion?.voto else null
                        )
                    }
                    .sortedByDescending { parseFechaPremium(it.fechaTermino) ?: LocalDateTime.MIN }
                val contactosPorDia = construirSerieSemanal(
                    chats.mapNotNull { parseFechaPremium(it.fechaCreacion)?.dayOfWeek }
                )
                val citasFinalizadas = citas.filter { it.estado == EstadoCita.FINALIZADO }
                val ingresosPorDia = construirSerieSemanal(
                    citasFinalizadas.mapNotNull { cita ->
                        parseFechaPremium(cita.fechaFinTrabajo ?: cita.fechaProgramada)?.dayOfWeek
                    }
                )
                val contactosUltimos7Dias = chats.count {
                    val fecha = parseFechaPremium(it.fechaCreacion) ?: return@count false
                    fecha.isAfter(LocalDateTime.now().minusDays(7))
                }
                val ingresoTotal = citasFinalizadas.sumOf { it.precioAcordado.coerceAtLeast(0) }
                val ticketPromedio = if (citasFinalizadas.isEmpty()) 0 else ingresoTotal / citasFinalizadas.size
                Pair(
                    PremiumStats(
                    chatsTotales = chats.size,
                    chatsActivos = chats.count { !it.chatCerrado },
                    mensajesNoLeidos = chats.sumOf { it.mensajesNoLeidos },
                    serviciosActivos = ofertas.count { it.disponible && !it.eliminada },
                    serviciosTotales = ofertas.count { !it.eliminada },
                    citasFinalizadas = citas.count { it.estado == EstadoCita.FINALIZADO },
                    citasCanceladas = citas.count { it.estado == EstadoCita.CANCELADO },
                    citasRechazadas = citas.count { it.estado == EstadoCita.RECHAZADA },
                    citasTotales = citas.size,
                    valoracionesTotales = votos.size,
                    promedioValoracion = if (votos.isEmpty()) 0.0 else votos.average(),
                    contactosUltimos7Dias = contactosUltimos7Dias,
                    tasaConversionCita = if (chats.isEmpty()) 0 else ((citas.size * 100.0) / chats.size).toInt(),
                    ingresoTotalCerrado = ingresoTotal,
                    ticketPromedio = ticketPromedio,
                    mejorDiaContactos = mejorEtiqueta(contactosPorDia),
                    mejorDiaIngresos = mejorEtiqueta(ingresosPorDia),
                    contactosPorDia = contactosPorDia,
                    ingresosPorDia = ingresosPorDia
                    ),
                    historial
                )
            }
            uiState = uiState.copy(
                stats = stats.first,
                historialContactos = stats.second,
                cargandoStats = false
            )
          } catch (e: Exception) {
            // Ante cualquier fallo (red caída, parseo, etc.) no dejamos el menú colgado:
            // se apaga el indicador de carga y se conservan las últimas estadísticas.
            uiState = uiState.copy(cargandoStats = false)
          }
        }
    }

    private fun describirResultadoContacto(chat: ChatCita, cita: CitaServicio?): String {
        return when (cita?.estado ?: chat.estadoCita) {
            EstadoCita.FINALIZADO -> "Finalizado"
            EstadoCita.CANCELADO -> "Cancelado"
            EstadoCita.RECHAZADA -> "Rechazado"
            EstadoCita.CERRADO -> "Cerrado"
            EstadoCita.EN_PROCESO, EstadoCita.COMENZANDO, EstadoCita.FINALIZANDO -> "En proceso"
            EstadoCita.HANDSHAKE -> "Coordinación"
            EstadoCita.PENDIENTE -> "Pendiente"
            else -> if (chat.chatCerrado) "Chat cerrado" else "Activo"
        }
    }

    private fun construirSerieSemanal(dias: List<DayOfWeek>): List<PremiumSerieDia> {
        val orden = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
        val conteos = dias.groupingBy { it }.eachCount()
        val maximo = conteos.values.maxOrNull() ?: 0
        return orden.map { dia ->
            PremiumSerieDia(
                etiqueta = nombreCortoDia(dia),
                cantidad = conteos[dia] ?: 0,
                destacado = maximo > 0 && (conteos[dia] ?: 0) == maximo
            )
        }
    }

    private fun mejorEtiqueta(serie: List<PremiumSerieDia>): String {
        val mejor = serie.maxByOrNull { it.cantidad } ?: return "—"
        return if (mejor.cantidad <= 0) "—" else mejor.etiqueta
    }

    private fun nombreCortoDia(dia: DayOfWeek): String = when (dia) {
        DayOfWeek.MONDAY -> "Lun"
        DayOfWeek.TUESDAY -> "Mar"
        DayOfWeek.WEDNESDAY -> "Mié"
        DayOfWeek.THURSDAY -> "Jue"
        DayOfWeek.FRIDAY -> "Vie"
        DayOfWeek.SATURDAY -> "Sáb"
        DayOfWeek.SUNDAY -> "Dom"
    }

    private fun parseFechaPremium(valor: String?): LocalDateTime? {
        if (valor.isNullOrBlank()) return null
        val patrones = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        for (patron in patrones) {
            try {
                return LocalDateTime.parse(valor.trim(), patron)
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }
}

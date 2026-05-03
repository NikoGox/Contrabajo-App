package com.movil.contrabajo.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.data.repository.RepositorioChats
import com.movil.contrabajo.data.repository.RepositorioOfertas
import com.movil.contrabajo.data.repository.RepositorioPerfil
import com.movil.contrabajo.data.repository.RepositorioReportes
import com.movil.contrabajo.domain.model.AccionModeracion
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
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
    val rangoBusquedaM: Int = 20_000,
    val filtroPorCoordenadasActivo: Boolean = false,
    val filtroCategoriaId: Long? = null,
    val filtroTipoPrecio: Int? = null,
    val soloTrabajadorVerificado: Boolean = false,
    val filtroZonaComunaActivo: Boolean = false,
    val comunaFiltro: String = "",
    val ordenMarketplace: OrdenMarketplace = OrdenMarketplace.FECHA_RECIENTES,
    val mensajePrincipal: String? = null
)

enum class OrdenMarketplace {
    ALFABETICO_A_Z,
    FECHA_RECIENTES,
    FECHA_ANTIGUAS
}

class PrincipalViewModel(
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioPerfil: RepositorioPerfil
) : ViewModel() {
    var uiState by mutableStateOf(PrincipalUiState())
        private set

    init {
        recargar()
    }

    fun recargar() {
        val ubicacionActual = repositorioPerfil.obtenerUbicacionAjustes()
        val ofertas = repositorioOfertas.obtenerOfertasMarketplace(uiState.busqueda)
        val categorias = repositorioOfertas.obtenerCategoriasServicio()
        val filtroActivo = ubicacionActual.latitud != null && ubicacionActual.longitud != null
        val rangoBusqueda = EscalaRango.normalizar(ubicacionActual.rangoBusquedaM)
        val ofertasFiltradas = filtrarPorMatchRangos(
            ofertas = ofertas,
            latitudBase = ubicacionActual.latitud,
            longitudBase = ubicacionActual.longitud,
            rangoBusquedaM = rangoBusqueda
        )
        val ofertasFinales = aplicarFiltrosYOrden(ofertasFiltradas)

        uiState = uiState.copy(
            ofertas = ofertasFinales,
            categoriasDisponibles = categorias,
            rangoBusquedaM = rangoBusqueda,
            filtroPorCoordenadasActivo = filtroActivo
        )
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
        val ubicacionActual = repositorioPerfil.obtenerUbicacionAjustes()
        val rangoNormalizado = EscalaRango.normalizar(valorMetros)
        repositorioPerfil.guardarUbicacionAjustes(
            ubicacionActual.copy(rangoBusquedaM = rangoNormalizado)
        ).onSuccess {
            recargar()
            uiState = uiState.copy(mensajePrincipal = "Rango de busqueda actualizado a ${EscalaRango.formatear(rangoNormalizado)}.")
        }.onFailure {
            uiState = uiState.copy(mensajePrincipal = it.message ?: "No se pudo guardar el rango de busqueda")
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
        rangoBusquedaM: Int
    ): List<OfertaServicio> {
        if (latitudBase == null || longitudBase == null) return emptyList()

        return ofertas.filter { oferta ->
            val latitudOferta = oferta.latitudReferencia
            val longitudOferta = oferta.longitudReferencia
            if (latitudOferta == null || longitudOferta == null) return@filter false

            val distanciaM = calcularDistanciaM(
                lat1 = latitudBase,
                lon1 = longitudBase,
                lat2 = latitudOferta,
                lon2 = longitudOferta
            )
            distanciaM <= rangoBusquedaM
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
    val error: String? = null
) {
    val chatsFiltrados: List<ChatCita> get() {
        if (!filtroChatsContacto && !filtroChatsTrabajador) return chats
        val idActual = idUsuarioActual ?: return chats
        return chats.filter { chat ->
            val comoContacto = chat.idCliente == idActual
            val comoTrabajador = chat.idTrabajador == idActual
            (filtroChatsContacto && comoContacto) || (filtroChatsTrabajador && comoTrabajador)
        }
    }
}

class ChatsViewModel(
    private val repositorioChats: RepositorioChats
) : ViewModel() {
    var uiState by mutableStateOf(ChatsUiState())
        private set

    init {
        recargar()
    }

    fun recargar() {
        val chatsActuales = repositorioChats.obtenerChatsActuales()
        val notificacionesPendientes = repositorioChats.obtenerNotificacionesPendientes()
        uiState = uiState.copy(
            idUsuarioActual = repositorioChats.obtenerIdUsuarioActual(),
            chats = chatsActuales,
            totalMensajesNoLeidos = chatsActuales.sumOf { it.mensajesNoLeidos.coerceAtLeast(0) },
            idPrimerChatPendiente = chatsActuales.firstOrNull { it.mensajesNoLeidos > 0 }?.idChatCita,
            notificacionesPendientes = notificacionesPendientes
        )
    }

    fun actualizarFiltroChatsContacto(activo: Boolean) {
        uiState = uiState.copy(filtroChatsContacto = activo)
    }

    fun actualizarFiltroChatsTrabajador(activo: Boolean) {
        uiState = uiState.copy(filtroChatsTrabajador = activo)
    }

    fun iniciarConversacionDesdeOferta(idOfertaServicio: Long): Result<ChatCita> {
        val resultado = repositorioChats.iniciarConversacionDesdeOferta(idOfertaServicio)
        resultado.onSuccess { chat ->
            uiState = uiState.copy(
                mensajeSistema = "Chat iniciado correctamente.",
                error = null
            )
            recargar()
            abrirChat(chat.idChatCita)
        }.onFailure {
            uiState = uiState.copy(
                error = it.message ?: "No se pudo iniciar la conversacion",
                mensajeSistema = null
            )
        }
        return resultado
    }

    fun abrirChat(idChatCita: Long) {
        val chat = repositorioChats.obtenerChat(idChatCita)
        if (chat == null) {
            uiState = uiState.copy(error = "No se pudo abrir el chat")
            return
        }
        val valoracion = repositorioChats.obtenerValoracionPorChat(idChatCita)
        val esCliente = chat.idCliente == repositorioChats.obtenerIdUsuarioActual()
        val cita = repositorioChats.obtenerCitaPorChat(idChatCita)
        val permiteValorar = cita?.estado in setOf(EstadoCita.FINALIZADO, EstadoCita.CERRADO)
        val mostrarModalValoracion = chat.chatCerrado && esCliente && valoracion == null && permiteValorar
        uiState = uiState.copy(
            chatActivo = chat,
            mensajesActivos = repositorioChats.obtenerMensajes(idChatCita),
            citaActiva = cita,
            valoracionExistente = valoracion,
            mostrarModalValoracion = mostrarModalValoracion,
            votoValoracion = valoracion?.voto ?: 5,
            comentarioValoracion = valoracion?.comentario.orEmpty(),
            borradorMensaje = "",
            idUsuarioActual = repositorioChats.obtenerIdUsuarioActual(),
            error = null
        )
        recargar()
    }

    fun actualizarBorradorMensaje(valor: String) {
        uiState = uiState.copy(borradorMensaje = valor, error = null)
    }

    fun enviarMensaje() {
        val chat = uiState.chatActivo ?: return
        repositorioChats.enviarMensaje(chat.idChatCita, uiState.borradorMensaje)
            .onSuccess {
                uiState = uiState.copy(
                    borradorMensaje = "",
                    mensajesActivos = repositorioChats.obtenerMensajes(chat.idChatCita),
                    error = null
                )
                recargar()
            }
            .onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo enviar el mensaje")
            }
    }

    fun crearCita(fechaProgramada: String, comentario: String) {
        val chat = uiState.chatActivo ?: return
        repositorioChats.crearCitaDesdeChat(
            idChatCita = chat.idChatCita,
            fechaProgramada = fechaProgramada,
            comentario = comentario
        )
            .onSuccess { cita ->
                uiState = uiState.copy(
                    citaActiva = cita,
                    mensajeSistema = "Cita creada correctamente.",
                    error = null
                )
                abrirChat(chat.idChatCita)
            }
            .onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo crear la cita")
            }
    }

    fun aceptarCitaTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.aceptarCitaTrabajador(chat.idChatCita) },
            mensajeExito = "Cita aceptada. Estado: Handshake."
        )
    }

    fun rechazarCitaTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.rechazarCitaTrabajador(chat.idChatCita) },
            mensajeExito = "Propuesta rechazada. Puedes seguir negociando sobre la misma cita."
        )
    }

    fun reenviarPropuestaCitaCliente() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.reenviarPropuestaCitaCliente(chat.idChatCita) },
            mensajeExito = "Propuesta reenviada. Estado actualizado a pendiente."
        )
    }

    fun solicitarInicioTrabajoTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.solicitarInicioTrabajoTrabajador(chat.idChatCita) },
            mensajeExito = "Solicitud de inicio enviada al cliente."
        )
    }

    fun aceptarInicioTrabajoCliente() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.aceptarInicioTrabajoCliente(chat.idChatCita) },
            mensajeExito = "Inicio del trabajo confirmado."
        )
    }

    fun solicitarFinalizarTrabajoTrabajador() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.solicitarFinalizarTrabajoTrabajador(chat.idChatCita) },
            mensajeExito = "Solicitud de finalizacion enviada al cliente."
        )
    }

    fun aceptarFinalizarTrabajoCliente() {
        val chat = uiState.chatActivo ?: return
        procesarTransicionCita(
            accion = { repositorioChats.aceptarFinalizarTrabajoCliente(chat.idChatCita) },
            mensajeExito = "Trabajo finalizado correctamente."
        )
    }

    fun cerrarChatActivo() {
        val chat = uiState.chatActivo ?: return
        repositorioChats.cerrarChat(chat.idChatCita)
            .onSuccess {
                uiState = uiState.copy(
                    mensajeSistema = "Chat finalizado. Queda en solo lectura.",
                    error = null
                )
                abrirChat(chat.idChatCita)
            }
            .onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo finalizar el chat")
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
        repositorioChats.guardarValoracionChat(
            idChatCita = chat.idChatCita,
            voto = uiState.votoValoracion,
            comentario = uiState.comentarioValoracion
        ).onSuccess { valoracion ->
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

    private fun procesarTransicionCita(
        accion: () -> Result<CitaServicio>,
        mensajeExito: String
    ) {
        val chat = uiState.chatActivo ?: return
        accion()
            .onSuccess { citaActualizada ->
                uiState = uiState.copy(
                    citaActiva = citaActualizada,
                    mensajeSistema = mensajeExito,
                    error = null
                )
                abrirChat(chat.idChatCita)
            }
            .onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo actualizar la cita")
            }
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
    val cargandoPantalla: Boolean = false
)

class PerfilViewModel(
    private val repositorioPerfil: RepositorioPerfil,
    private val repositorioOfertas: RepositorioOfertas,
    private val repositorioAutenticacion: RepositorioAutenticacion
) : ViewModel() {
    var uiState by mutableStateOf(PerfilUiState())
        private set

    init {
        recargar()
    }

    fun recargar() {
        val usuario = repositorioPerfil.obtenerPerfilActual()
        val ofertasPropias = repositorioOfertas.obtenerOfertasPropias()
        val categorias = repositorioOfertas.obtenerCategoriasServicio()
        val idsOfertasEnCurso = repositorioOfertas.obtenerIdsOfertasConTrabajoEnCursoPropias()
        val valoracionesPorServicio = repositorioOfertas.obtenerValoracionesPropiasPorServicio()
        val ofertaPrincipal = ofertasPropias.firstOrNull()
        val formularioServicio = if (uiState.mostrandoFormularioServicio) {
            uiState.formularioServicio
        } else {
            ofertaPrincipal.toFormularioServicio()
        }

        uiState = uiState.copy(
            usuario = usuario,
            ofertasPropias = ofertasPropias,
            categorias = categorias,
            idsOfertasEnCurso = idsOfertasEnCurso,
            valoracionesPorServicio = valoracionesPorServicio,
            formularioServicio = formularioServicio,
            runVerificacion = if (uiState.runVerificacion.isBlank()) usuario?.run.orEmpty() else uiState.runVerificacion,
            dvVerificacion = if (uiState.dvVerificacion.isBlank()) usuario?.dv.orEmpty() else uiState.dvVerificacion,
            correoPerfilInput = if (uiState.correoPerfilInput.isBlank()) usuario?.correo.orEmpty() else uiState.correoPerfilInput,
            telefonoPerfilInput = if (uiState.telefonoPerfilInput.isBlank()) {
                usuario?.telefono.orEmpty().normalizarTelefonoMovilSinPrefijo()
            } else {
                uiState.telefonoPerfilInput
            },
            preguntasSeguridad = repositorioPerfil.obtenerPreguntasSeguridad(),
            ubicacionAjustes = repositorioPerfil.obtenerUbicacionAjustes()
        )
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
        val oferta = repositorioOfertas.obtenerOfertaPropiaPorId(idOfertaServicio)
            ?: uiState.ofertasPropias.firstOrNull { it.idOfertaServicio == idOfertaServicio }
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
        val ofertaRecuperada = uiState.idOfertaEditando?.let { repositorioOfertas.obtenerOfertaPropiaPorId(it) }
            ?: uiState.ofertasPropias.firstOrNull()
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
        repositorioOfertas.actualizarDisponibilidadOfertaPropia(idOfertaServicio, valor)
            .onSuccess {
                recargar()
                uiState = uiState.copy(errorServicio = null)
            }
            .onFailure {
                uiState = uiState.copy(errorServicio = it.message ?: "No se pudo actualizar disponibilidad")
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
            repositorioOfertas.guardarOfertaPropia(
                formulario = uiState.formularioServicio,
                idOfertaServicio = uiState.idOfertaEditando
            )
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
            uiState = uiState.copy(cargandoPantalla = true)
            delay(180)
            withContext(Dispatchers.IO) {
                repositorioPerfil.actualizarFotoPerfil(uriLocal)
            }
                .onSuccess { usuarioActualizado ->
                    uiState = uiState.copy(usuario = usuarioActualizado, errorServicio = null)
                }
                .onFailure {
                    uiState = uiState.copy(errorServicio = it.message ?: "No se pudo actualizar la foto de perfil")
                }
            uiState = uiState.copy(cargandoPantalla = false)
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
            repositorioOfertas.eliminarOfertaPropia(idOferta)
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
        repositorioPerfil.guardarPreguntaSeguridad(indice, pregunta, respuesta)
            .onSuccess { preguntas ->
                uiState = uiState.copy(
                    preguntasSeguridad = preguntas,
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
    }

    fun limpiarMensajesPreguntasSeguridad() {
        uiState = uiState.copy(
            errorPreguntasSeguridad = null,
            mensajePreguntasSeguridad = null
        )
    }

    fun validarContrasenaCuenta(contrasena: String): Result<Unit> {
        val usuario = uiState.usuario ?: repositorioPerfil.obtenerPerfilActual()
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
            correoPerfilInput = valor,
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
            }.onFailure {
                uiState = uiState.copy(
                    errorPerfilEdicion = it.message ?: "No se pudo actualizar el perfil",
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
        if (uiState.runVerificacion.length != 8) {
            uiState = uiState.copy(errorVerificacion = "El RUN debe tener 8 digitos")
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
        repositorioPerfil.solicitarVerificacionTrabajador(
            run = uiState.runVerificacion,
            dv = uiState.dvVerificacion,
            numeroDocumento = uiState.numeroDocumentoVerificacion
        ).onSuccess { usuarioActualizado ->
            uiState = uiState.copy(
                usuario = usuarioActualizado,
                errorVerificacion = null,
                mensajeVerificacion = "Cuenta verificada. Tu perfil ahora esta habilitado como trabajador."
            )
        }.onFailure {
            uiState = uiState.copy(
                errorVerificacion = it.message ?: "No se pudo iniciar la verificacion",
                mensajeVerificacion = null
            )
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repositorioAutenticacion.cerrarSesion()
            }
            uiState = uiState.copy(sesionCerrada = true)
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
        uiState = uiState.copy(
            tiposReporte = repositorioReportes.obtenerTiposReporte(),
            reportes = repositorioReportes.obtenerReportesModeracion(
                busqueda = uiState.busqueda,
                idTipoReporte = uiState.filtroTipoReporteId,
                estadoRevision = uiState.filtroEstadoRevision,
                ordenarRecientes = uiState.ordenarRecientes
            )
        )
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
        val detalle = repositorioReportes.obtenerDetalleReporte(idReporte)
        if (detalle == null) {
            uiState = uiState.copy(error = "No se pudo cargar el detalle del reporte")
            return
        }
        uiState = uiState.copy(reporteActivo = detalle, error = null)
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
            repositorioReportes.aplicarMedidaModeracion(idReporte, accion)
                .onSuccess { actualizado ->
                    val etiquetaAccion = when (accion) {
                        AccionModeracion.DESACTIVAR_SERVICIO -> "Servicio desactivado y reporte resuelto."
                        AccionModeracion.ELIMINAR_SERVICIO -> "Servicio eliminado logicamente y reporte resuelto."
                        else -> "Reporte resuelto."
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
    val idUsuarioActual: Long? = null
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
        val idUsuarioActual = repositorioPerfil.obtenerPerfilActual()?.idUsuario
        val ofertasContexto = ofertasContextoMarketplace?.takeIf { it.isNotEmpty() }
        val ofertas = ofertasContexto ?: repositorioOfertas.obtenerOfertasMarketplace()
        if (ofertas.isEmpty()) {
            val oferta = repositorioOfertas.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = true)
            uiState = uiState.copy(ofertas = listOfNotNull(oferta), indiceActual = 0, idUsuarioActual = idUsuarioActual)
            return
        }
        val indice = ofertas.indexOfFirst { it.idOfertaServicio == idOfertaServicio }
        if (indice < 0) {
            val ofertaEliminada = repositorioOfertas.obtenerOfertaPorId(idOfertaServicio, incluirEliminadas = true)
            if (ofertaEliminada != null) {
                uiState = uiState.copy(ofertas = listOf(ofertaEliminada), indiceActual = 0, idUsuarioActual = idUsuarioActual)
                return
            }
            if (ofertasContexto != null) {
                uiState = uiState.copy(ofertas = ofertasContexto, indiceActual = 0, idUsuarioActual = idUsuarioActual)
            } else {
                val fallback = repositorioOfertas.obtenerOfertasMarketplace()
                val indiceFallback = fallback.indexOfFirst { it.idOfertaServicio == idOfertaServicio }.takeIf { it >= 0 } ?: 0
                uiState = uiState.copy(ofertas = fallback, indiceActual = indiceFallback, idUsuarioActual = idUsuarioActual)
            }
        } else {
            uiState = uiState.copy(ofertas = ofertas, indiceActual = indice, idUsuarioActual = idUsuarioActual)
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
}

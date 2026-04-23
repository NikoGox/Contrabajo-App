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
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.FotoServicioLocal
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.EscalaRango
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val chatActivo: ChatCita? = null,
    val mensajesActivos: List<MensajeChat> = emptyList(),
    val citaActiva: CitaServicio? = null,
    val borradorMensaje: String = "",
    val mensajeSistema: String? = null,
    val error: String? = null
)

class ChatsViewModel(
    private val repositorioChats: RepositorioChats
) : ViewModel() {
    var uiState by mutableStateOf(ChatsUiState())
        private set

    init {
        recargar()
    }

    fun recargar() {
        uiState = uiState.copy(
            idUsuarioActual = repositorioChats.obtenerIdUsuarioActual(),
            chats = repositorioChats.obtenerChatsActuales()
        )
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
        uiState = uiState.copy(
            chatActivo = chat,
            mensajesActivos = repositorioChats.obtenerMensajes(idChatCita),
            citaActiva = repositorioChats.obtenerCitaPorChat(idChatCita),
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

    fun crearCita(fechaProgramada: String, detalle: String) {
        val chat = uiState.chatActivo ?: return
        repositorioChats.crearCitaDesdeChat(chat.idChatCita, fechaProgramada, detalle)
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

    fun cambiarEstadoCita(nuevoEstado: Int) {
        val cita = uiState.citaActiva ?: return
        repositorioChats.actualizarEstadoCita(cita.idCita, nuevoEstado)
            .onSuccess { citaActualizada ->
                uiState = uiState.copy(
                    citaActiva = citaActualizada,
                    mensajeSistema = "Estado de cita actualizado.",
                    error = null
                )
                uiState.chatActivo?.let { abrirChat(it.idChatCita) }
            }
            .onFailure {
                uiState = uiState.copy(error = it.message ?: "No se pudo actualizar el estado")
            }
    }

    fun marcarCitaConfirmada() = cambiarEstadoCita(EstadoCita.CONFIRMADA)
    fun marcarCitaEnProceso() = cambiarEstadoCita(EstadoCita.EN_PROCESO)
    fun marcarCitaFinalizada() = cambiarEstadoCita(EstadoCita.FINALIZADA)
    fun marcarCitaPendiente() = cambiarEstadoCita(EstadoCita.PENDIENTE)

    fun consumirMensajes() {
        uiState = uiState.copy(mensajeSistema = null, error = null)
    }
}

data class PerfilUiState(
    val usuario: Usuario? = null,
    val sesionCerrada: Boolean = false,
    val ofertaPropia: OfertaServicio? = null,
    val categorias: List<CategoriaServicio> = emptyList(),
    val formularioServicio: FormularioServicio = FormularioServicio(),
    val mostrandoFormularioServicio: Boolean = false,
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
        val ofertaPropia = repositorioOfertas.obtenerOfertaPropiaActual()
        val categorias = repositorioOfertas.obtenerCategoriasServicio()
        val formularioServicio = if (uiState.mostrandoFormularioServicio) {
            uiState.formularioServicio
        } else {
            ofertaPropia.toFormularioServicio()
        }

        uiState = uiState.copy(
            usuario = usuario,
            ofertaPropia = ofertaPropia,
            categorias = categorias,
            formularioServicio = formularioServicio,
            runVerificacion = if (uiState.runVerificacion.isBlank()) usuario?.run.orEmpty() else uiState.runVerificacion,
            dvVerificacion = if (uiState.dvVerificacion.isBlank()) usuario?.dv.orEmpty() else uiState.dvVerificacion,
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
        uiState = uiState.copy(
            mostrandoFormularioServicio = true,
            formularioServicio = FormularioServicio(),
            errorServicio = null
        )
    }

    fun mostrarFormularioEdicion() {
        val usuario = uiState.usuario
        if (usuario == null || usuario.tipoPerfil !in listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)) {
            uiState = uiState.copy(errorServicio = "Debes verificarte como trabajador para editar servicios")
            return
        }
        uiState = uiState.copy(
            mostrandoFormularioServicio = true,
            formularioServicio = uiState.ofertaPropia.toFormularioServicio(),
            errorServicio = null
        )
    }

    fun cancelarFormularioServicio() {
        uiState = uiState.copy(
            mostrandoFormularioServicio = false,
            formularioServicio = uiState.ofertaPropia.toFormularioServicio(),
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

    fun cambiarDisponibilidadServicioRapido(valor: Boolean) {
        repositorioOfertas.actualizarDisponibilidadOfertaPropia(valor)
            .onSuccess { oferta ->
                uiState = uiState.copy(
                    ofertaPropia = oferta,
                    formularioServicio = oferta.toFormularioServicio(),
                    errorServicio = null
                )
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
            repositorioOfertas.guardarOfertaPropia(uiState.formularioServicio)
                .onSuccess { oferta ->
                    uiState = uiState.copy(
                        ofertaPropia = oferta,
                        formularioServicio = oferta.toFormularioServicio(),
                        mostrandoFormularioServicio = false,
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
            repositorioPerfil.actualizarFotoPerfil(uriLocal)
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
        viewModelScope.launch {
            uiState = uiState.copy(cargandoPantalla = true)
            delay(220)
            repositorioOfertas.eliminarOfertaPropia()
                .onSuccess {
                    uiState = uiState.copy(
                        ofertaPropia = null,
                        formularioServicio = FormularioServicio(),
                        mostrandoFormularioServicio = false,
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
            repositorioPerfil.guardarUbicacionAjustes(normalizada)
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
            repositorioPerfil.guardarUbicacionAjustes(normalizada)
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
                mensajeVerificacion = "Solicitud enviada. En 3 minutos tu perfil se validara automaticamente."
            )
        }.onFailure {
            uiState = uiState.copy(
                errorVerificacion = it.message ?: "No se pudo iniciar la verificacion",
                mensajeVerificacion = null
            )
        }
    }

    fun cerrarSesion() {
        repositorioAutenticacion.cerrarSesion()
        uiState = uiState.copy(sesionCerrada = true)
    }

    fun consumirCierreSesion() {
        uiState = uiState.copy(sesionCerrada = false)
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
            val oferta = repositorioOfertas.obtenerOfertaPorId(idOfertaServicio)
            uiState = uiState.copy(ofertas = listOfNotNull(oferta), indiceActual = 0, idUsuarioActual = idUsuarioActual)
            return
        }
        val indice = ofertas.indexOfFirst { it.idOfertaServicio == idOfertaServicio }
        if (indice < 0) {
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

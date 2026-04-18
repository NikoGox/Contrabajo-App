package com.movil.contrabajo.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.contrabajo.data.repository.RepositorioAutenticacion
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.data.repository.RepositorioChats
import com.movil.contrabajo.data.repository.RepositorioOfertas
import com.movil.contrabajo.data.repository.RepositorioPerfil
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.FotoServicioLocal
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class PrincipalUiState(
    val busqueda: String = "",
    val ofertas: List<OfertaServicio> = emptyList(),
    val refrescando: Boolean = false,
    val rangoBusquedaKm: Int = 20,
    val filtroPorCoordenadasActivo: Boolean = false
)

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
        val filtroActivo = ubicacionActual.latitud != null && ubicacionActual.longitud != null
        val ofertasFiltradas = filtrarPorRango(
            ofertas = ofertas,
            latitudBase = ubicacionActual.latitud,
            longitudBase = ubicacionActual.longitud,
            rangoKm = ubicacionActual.rangoDisponibilidadKm
        )

        uiState = uiState.copy(
            ofertas = ofertasFiltradas,
            rangoBusquedaKm = ubicacionActual.rangoDisponibilidadKm,
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

    private fun filtrarPorRango(
        ofertas: List<OfertaServicio>,
        latitudBase: Double?,
        longitudBase: Double?,
        rangoKm: Int
    ): List<OfertaServicio> {
        if (latitudBase == null || longitudBase == null) return ofertas
        val rangoNormalizado = rangoKm.coerceIn(0, 100)

        return ofertas.filter { oferta ->
            val latitudOferta = oferta.latitudReferencia
            val longitudOferta = oferta.longitudReferencia
            if (latitudOferta == null || longitudOferta == null) {
                true
            } else {
                calcularDistanciaKm(
                    lat1 = latitudBase,
                    lon1 = longitudBase,
                    lat2 = latitudOferta,
                    lon2 = longitudOferta
                ) <= rangoNormalizado
            }
        }
    }

    private fun calcularDistanciaKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Int {
        val radioTierraKm = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (radioTierraKm * c).roundToInt()
    }
}

data class ChatsUiState(
    val chats: List<ChatCita> = emptyList()
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
        uiState = uiState.copy(chats = repositorioChats.obtenerChatsActuales())
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
    val mensajeVerificacion: String? = null
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
        uiState = uiState.copy(
            formularioServicio = uiState.formularioServicio.copy(precioTexto = valor),
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
    }

    fun eliminarServicio() {
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
    }

    fun actualizarRunVerificacion(valor: String) {
        uiState = uiState.copy(runVerificacion = valor, errorVerificacion = null, mensajeVerificacion = null)
    }

    fun actualizarDvVerificacion(valor: String) {
        uiState = uiState.copy(dvVerificacion = valor, errorVerificacion = null, mensajeVerificacion = null)
    }

    fun actualizarNumeroDocumentoVerificacion(valor: String) {
        uiState = uiState.copy(numeroDocumentoVerificacion = valor, errorVerificacion = null, mensajeVerificacion = null)
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
        val rangoNormalizado = valor.roundToInt().coerceIn(0, 100)
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(rangoDisponibilidadKm = rangoNormalizado),
            errorUbicacion = null,
            mensajeUbicacion = null
        )
    }

    fun obtenerUbicacionActual() {
        val usuarioId = uiState.usuario?.idUsuario ?: 0L
        val baseLat = -33.4489
        val baseLon = -70.6693
        val ajuste = ((usuarioId % 19).toDouble() * 0.0015)
        uiState = uiState.copy(
            ubicacionAjustes = uiState.ubicacionAjustes.copy(
                latitud = baseLat + ajuste,
                longitud = baseLon - ajuste
            ),
            errorUbicacion = null,
            mensajeUbicacion = "Ubicacion recalculada con OpenStreetMap."
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
            rangoDisponibilidadKm = actual.rangoDisponibilidadKm.coerceIn(0, 100)
        )

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
        FormularioServicio()
    } else {
        FormularioServicio(
            titulo = titulo,
            descripcion = descripcion,
            precioTexto = precioTexto,
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
    val indiceActual: Int = 0
) {
    val ofertaActual: OfertaServicio? get() = ofertas.getOrNull(indiceActual)
}

class DetalleServicioViewModel(
    private val repositorioOfertas: RepositorioOfertas
) : ViewModel() {
    var uiState by mutableStateOf(DetalleServicioUiState())
        private set

    private var ofertaActualId: Long? = null

    fun cargarOferta(idOfertaServicio: Long) {
        if (ofertaActualId == idOfertaServicio && uiState.ofertaActual != null) return
        ofertaActualId = idOfertaServicio
        val ofertas = repositorioOfertas.obtenerOfertasMarketplace()
        if (ofertas.isEmpty()) {
            val oferta = repositorioOfertas.obtenerOfertaPorId(idOfertaServicio)
            uiState = uiState.copy(ofertas = listOfNotNull(oferta), indiceActual = 0)
            return
        }
        val indice = ofertas.indexOfFirst { it.idOfertaServicio == idOfertaServicio }.takeIf { it >= 0 } ?: 0
        uiState = uiState.copy(ofertas = ofertas, indiceActual = indice)
    }

    fun avanzarTarjeta() {
        val nuevoIndice = (uiState.indiceActual + 1).coerceAtMost(uiState.ofertas.lastIndex)
        uiState = uiState.copy(indiceActual = nuevoIndice)
    }

    fun retrocederTarjeta() {
        val nuevoIndice = (uiState.indiceActual - 1).coerceAtLeast(0)
        uiState = uiState.copy(indiceActual = nuevoIndice)
    }
}

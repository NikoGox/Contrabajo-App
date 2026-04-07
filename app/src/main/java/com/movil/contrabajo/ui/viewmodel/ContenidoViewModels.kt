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
import com.movil.contrabajo.domain.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PrincipalUiState(
    val busqueda: String = "",
    val ofertas: List<OfertaServicio> = emptyList(),
    val refrescando: Boolean = false
)

class PrincipalViewModel(
    private val repositorioOfertas: RepositorioOfertas
) : ViewModel() {
    var uiState by mutableStateOf(PrincipalUiState())
        private set

    init {
        recargar()
    }

    fun recargar() {
        uiState = uiState.copy(ofertas = repositorioOfertas.obtenerOfertasMarketplace(uiState.busqueda))
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
    val errorServicio: String? = null
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
            formularioServicio = formularioServicio
        )
    }

    fun mostrarFormularioCreacion() {
        uiState = uiState.copy(
            mostrandoFormularioServicio = true,
            formularioServicio = FormularioServicio(),
            errorServicio = null
        )
    }

    fun mostrarFormularioEdicion() {
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
}

data class DetalleServicioUiState(
    val oferta: OfertaServicio? = null
)

class DetalleServicioViewModel(
    private val repositorioOfertas: RepositorioOfertas
) : ViewModel() {
    var uiState by mutableStateOf(DetalleServicioUiState())
        private set

    private var ofertaActualId: Long? = null

    fun cargarOferta(idOfertaServicio: Long) {
        if (ofertaActualId == idOfertaServicio && uiState.oferta != null) return
        ofertaActualId = idOfertaServicio
        uiState = uiState.copy(oferta = repositorioOfertas.obtenerOfertaPorId(idOfertaServicio))
    }
}

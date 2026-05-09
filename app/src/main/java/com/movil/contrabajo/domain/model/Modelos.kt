package com.movil.contrabajo.domain.model

data class Usuario(
    val idUsuario: Long = 0,
    val run: String,
    val dv: String,
    val username: String,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val telefono: String,
    val correo: String,
    val contrasenaHash: String,
    val fechaRegistro: String,
    val fechaNacimiento: String,
    val verificado: Boolean,
    val tipoPerfil: Int = TipoPerfil.USUARIO_BASE,
    val rangoDisponibilidadM: Int = 20_000,
    val rangoBusquedaM: Int = 20_000,
    val numeroDocumentoIdentidad: String? = null,
    val preguntaRecuperacion: String = "",
    val respuestaRecuperacion: String = "",
    val verificacionTrabajadorPendiente: Boolean = false,
    val fechaSolicitudVerificacionMs: Long? = null,
    val fotoPerfilUrl: String? = null,
    val direccionCalle: String = "",
    val direccionNumero: String = "",
    val direccionComuna: String = "",
    val direccionRegion: String = "Region Metropolitana",
    val direccionLatitud: Double? = null,
    val direccionLongitud: Double? = null
)

data class OfertaServicio(
    val idOfertaServicio: Long = 0,
    val titulo: String,
    val descripcion: String,
    val precioTexto: String,
    val tipoPrecio: Int = TipoPrecio.FIJO,
    val montoBase: Int = 0,
    val disponible: Boolean,
    val fechaPublicacion: String,
    val idCategoriaServicio: Long,
    val idTrabajador: Long,
    val idCliente: Long? = null,
    val idFotoPortada: Long? = null,
    val nombreTrabajador: String = "",
    val usernameTrabajador: String = "",
    val nombreCategoria: String = "",
    val puntuacionPromedio: Double = 0.0,
    val trabajadorVerificado: Boolean = false,
    val ubicacionReferencia: String = "",
    val rangoDisponibilidadM: Int = 20_000,
    val latitudReferencia: Double? = null,
    val longitudReferencia: Double? = null,
    val fotoUrlReferencia: String = "",
    val fotoNombreArchivo: String = "",
    val fotoMimeType: String = "",
    val fotoPendienteSincronizacion: Boolean = false,
    val fotoPerfilTrabajador: String = "",
    val eliminada: Boolean = false,
    val fechaEliminacion: String? = null
)

data class ChatCita(
    val idChatCita: Long = 0,
    val fechaCreacion: String,
    val idTrabajador: Long,
    val idCliente: Long,
    val idOfertaServicio: Long? = null,
    val idCita: Long? = null,
    val nombreContacto: String = "",
    val usernameContacto: String = "",
    val tituloServicio: String = "",
    val categoriaServicio: String = "",
    val ultimoMensaje: String = "",
    val horaUltimoMensaje: String = "",
    val mensajesNoLeidos: Int = 0,
    val estadoCita: Int? = null,
    val chatCerrado: Boolean = false,
    val bloqueadoHastaMs: Long? = null
)

data class MensajeChat(
    val idMensajeChat: Long = 0,
    val fechaEnvio: String,
    val fechaRecibido: String? = null,
    val fechaLeido: String? = null,
    val idEmisor: Long,
    val idReceptor: Long,
    val idChatCita: Long,
    val idEstado: Long,
    val contenido: String,
    /** 0 = normal, 1 = sistema (se renderiza como pastilla gris centrada) */
    val tipo: Int = 0
)

data class NotificacionMensajePendiente(
    val idMensajeChat: Long,
    val idChatCita: Long,
    val titulo: String,
    val contenido: String
)

data class CitaServicio(
    val idCita: Long = 0,
    val idChatCita: Long,
    val fechaCreacion: String,
    val fechaProgramada: String,
    val comentario: String,
    val precioAcordado: Int,
    val fechaInicioTrabajo: String? = null,
    val fechaFinTrabajo: String? = null,
    val estado: Int
)

object EstadoCita {
    const val PENDIENTE = 401
    const val HANDSHAKE = 402
    const val COMENZANDO = 403
    const val EN_PROCESO = 404
    const val FINALIZANDO = 405
    const val FINALIZADO = 406
    const val CANCELADO = 407
    const val CERRADO = 408
    const val RECHAZADA = 409
}

object EstadoCodigo {
    const val MSG_ENVIADO = 301L
    const val MSG_ENTREGADO = 302L
    const val MSG_LEIDO = 303L
}

data class CategoriaServicio(
    val idCategoriaServicio: Long = 0,
    val nombre: String
)

data class Estado(
    val idEstado: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String
)

data class Foto(
    val idFoto: Long = 0,
    val fechaSubida: String,
    val enlace: String,
    val detalle: String,
    val nombreArchivo: String = "",
    val mimeType: String = "",
    val estadoSincronizacion: String = "pendiente",
    val urlRemota: String? = null
)

data class FotoOferta(
    val idFoto: Long = 0,
    val enlace: String,
    val nombreOriginal: String = "",
    val tipoMime: String = "",
    val tamanoBytes: Long = 0,
    val anchoPx: Int? = null,
    val altoPx: Int? = null,
    val fechaSubida: String = "",
    val idOfertaServicio: Long = 0,
    val idUsuario: Long = 0
)

data class FotoServicioLocal(
    val idFoto: Long? = null,
    val uriLocal: String = "",
    val nombreArchivo: String = "",
    val mimeType: String = "",
    val pendienteSincronizacion: Boolean = true,
    val urlRemota: String? = null
)

data class FormularioServicio(
    val titulo: String = "",
    val descripcion: String = "",
    val precioTexto: String = "",
    val tipoPrecio: Int = TipoPrecio.FIJO,
    val montoBase: Int = 0,
    val idCategoriaServicio: Long? = null,
    val disponible: Boolean = true,
    val foto: FotoServicioLocal? = null
)

data class Direccion(
    val idDireccion: Long = 0,
    val calle: String,
    val numero: String,
    val villa: String,
    val idCoordenadas: Long
)

data class Coordenadas(
    val idCoordenadas: Long = 0,
    val latitud: Double,
    val longitud: Double,
    val detalle: String
)

data class Valoracion(
    val idValoracion: Long = 0,
    val voto: Int,
    val fechaVoto: String,
    val comentario: String,
    val idTrabajador: Long,
    val idCliente: Long,
    val idChatCita: Long,
    val idOfertaServicio: Long,
    val usernameCliente: String = "",
    val fechaFinalizacionCita: String? = null
)

data class TipoReporte(
    val idTipoReporte: Long = 0,
    val nombre: String,
    val descripcion: String
)

data class Reporte(
    val idReporte: Long = 0,
    val idEmisor: Long,
    val idUsuarioReportado: Long? = null,
    val idOfertaServicio: Long? = null,
    val idChatCita: Long? = null,
    val idTipoReporte: Long,
    val comentario: String,
    val fechaCreacion: String,
    val estadoRevision: String = EstadoReporte.PENDIENTE,
    val idModeradorRevisor: Long? = null,
    val fechaRevision: String? = null,
    val medidaAplicada: String? = null,
    val tipoReporteNombre: String = "",
    val emisorUsername: String = "",
    val usuarioReportadoUsername: String = "",
    val usuarioReportadoNombre: String = "",
    val servicioTitulo: String = "",
    val servicioFotoUrl: String = ""
)

object EstadoReporte {
    const val PENDIENTE = "PENDIENTE"
    const val EN_REVISION = "EN_REVISION"
    const val RESUELTO = "RESUELTO"
}

object AccionModeracion {
    const val DESACTIVAR_SERVICIO = "DESACTIVAR_SERVICIO"
    const val ELIMINAR_SERVICIO = "ELIMINAR_SERVICIO"
}

data class SesionLocal(
    val idSesionLocal: Long = 0,
    val idUsuario: Long,
    val tokenLocal: String,
    val fechaInicio: String,
    val fechaUltimoAcceso: String,
    val recordarme: Boolean,
    val activa: Boolean
)

data class ConfiguracionApp(
    val idConfiguracionApp: Long = 0,
    val idUsuario: Long? = null,
    val tema: String,
    val notificacionesActivas: Boolean,
    val primeraEjecucion: Boolean,
    val ultimaPantalla: String,
    val fechaActualizacion: String
)

data class RegistroPendiente(
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val run: String = "",
    val dv: String = "",
    val telefono: String = "",
    val region: String = "Region Metropolitana",
    val idComuna: Int? = null,
    val comuna: String = "",
    val calle: String = "",
    val numeroDireccion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val username: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val tipoPerfil: Int = TipoPerfil.USUARIO_BASE,
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val preguntaSeguridad1: String = "",
    val respuestaSeguridad1: String = "",
    val preguntaSeguridad2: String = "",
    val respuestaSeguridad2: String = ""
)

data class ComunaCatalogo(
    val id: Int,
    val nombre: String,
    val idRegion: Int? = null,
    val region: String = ""
)

object TipoPerfil {
    const val MODERADOR = 0
    const val USUARIO_BASE = 1
    const val TRABAJADOR = 2
    const val PREMIUM = 3
}

object TipoPrecio {
    const val FIJO = 0
    const val POR_HORA = 1
    const val DESDE = 2
    const val CONTACTAR = 3
}

data class PreguntaSeguridadConfig(
    val indice: Int,
    val pregunta: String = "",
    val respuesta: String = ""
) {
    val configurada: Boolean get() = pregunta.isNotBlank()
}

object PreguntasSeguridadCatalogo {
    val opciones: List<String> = listOf(
        "Nombre de mascota",
        "Ciudad de nacimiento",
        "Pelicula favorita",
        "Comida favorita",
        "Primer vehiculo"
    )

    fun esValida(pregunta: String): Boolean = opciones.contains(pregunta.trim())
}

data class ValoracionesServicio(
    val oferta: OfertaServicio,
    val valoraciones: List<Valoracion> = emptyList()
)

data class UbicacionAjustesConfig(
    val region: String = "Region Metropolitana",
    val comuna: String = "Sin comuna",
    val calle: String = "Sin calle",
    val numero: String = "Sin numero",
    val detalle: String = "Sin detalle",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val rangoDisponibilidadM: Int = 20_000,
    val rangoBusquedaM: Int = 20_000
)

data class FiltroMarketplaceConfig(
    val categoriaId: Long? = null,
    val tipoPrecio: Int? = null,
    val soloTrabajadorVerificado: Boolean = false,
    val ordenMarketplace: String = "FECHA_RECIENTES",
    val filtroZonaComunaActivo: Boolean = false,
    val comunaFiltro: String = ""
)

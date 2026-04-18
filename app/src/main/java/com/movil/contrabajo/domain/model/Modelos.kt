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
    val numeroDocumentoIdentidad: String? = null,
    val preguntaRecuperacion: String = "",
    val respuestaRecuperacion: String = "",
    val verificacionTrabajadorPendiente: Boolean = false,
    val fechaSolicitudVerificacionMs: Long? = null
)

data class OfertaServicio(
    val idOfertaServicio: Long = 0,
    val titulo: String,
    val descripcion: String,
    val precioTexto: String,
    val disponible: Boolean,
    val fechaPublicacion: String,
    val idCategoriaServicio: Long,
    val idTrabajador: Long,
    val idCliente: Long? = null,
    val idFotoPortada: Long? = null,
    val nombreTrabajador: String = "",
    val nombreCategoria: String = "",
    val puntuacionPromedio: Double = 0.0,
    val trabajadorVerificado: Boolean = false,
    val ubicacionReferencia: String = "",
    val rangoDisponibilidadKm: Int = 20,
    val latitudReferencia: Double? = null,
    val longitudReferencia: Double? = null,
    val fotoUrlReferencia: String = "",
    val fotoNombreArchivo: String = "",
    val fotoMimeType: String = "",
    val fotoPendienteSincronizacion: Boolean = false
)

data class ChatCita(
    val idChatCita: Long = 0,
    val fechaCreacion: String,
    val idTrabajador: Long,
    val idCliente: Long,
    val idCita: Long? = null,
    val nombreContacto: String = "",
    val ultimoMensaje: String = "",
    val horaUltimoMensaje: String = ""
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
    val contenido: String
)

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
    val idCliente: Long
)

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
    val username: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val tipoPerfil: Int = TipoPerfil.USUARIO_BASE,
    val contrasena: String = "",
    val confirmarContrasena: String = ""
)

object TipoPerfil {
    const val MODERADOR = 0
    const val USUARIO_BASE = 1
    const val TRABAJADOR = 2
    const val PREMIUM = 3
}

data class PreguntaSeguridadConfig(
    val indice: Int,
    val pregunta: String = "",
    val respuesta: String = ""
) {
    val configurada: Boolean get() = pregunta.isNotBlank() && respuesta.isNotBlank()
}

data class UbicacionAjustesConfig(
    val region: String = "Region Metropolitana",
    val comuna: String = "Santiago",
    val calle: String = "Sin calle",
    val numero: String = "Sin numero",
    val detalle: String = "Sin detalle",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val rangoDisponibilidadKm: Int = 20
)

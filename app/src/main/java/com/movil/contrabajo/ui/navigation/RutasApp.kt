package com.movil.contrabajo.ui.navigation

sealed class RutasApp(val ruta: String) {
    data object Inicio : RutasApp("inicio")
    data object Login : RutasApp("login")
    data object RegistroPasoUno : RutasApp("registro_paso_uno")
    data object RegistroPasoDireccion : RutasApp("registro_paso_direccion")
    data object RegistroPasoDos : RutasApp("registro_paso_dos")
    data object PrincipalShell : RutasApp("principal_shell")
    data object Principal : RutasApp("principal")
    data object Chats : RutasApp("chats")
    data object ChatDetalle : RutasApp("chat_detalle/{idChatCita}") {
        fun crearRuta(idChatCita: Long): String = "chat_detalle/$idChatCita"
    }
    data object Perfil : RutasApp("perfil")
    data object Ajustes : RutasApp("ajustes")
    data object AjustesSeguridad : RutasApp("ajustes/seguridad")
    data object AjustesVerificacion : RutasApp("ajustes/seguridad/verificacion")
    data object AjustesPreguntas : RutasApp("ajustes/seguridad/preguntas")
    data object AjustesCuenta : RutasApp("ajustes/cuenta")
    data object AjustesUbicacion : RutasApp("ajustes/ubicacion")
    data object ServicioEditor : RutasApp("servicio_editor/{modo}") {
        fun crearRuta(modo: String): String = "servicio_editor/$modo"
    }
    data object Servicio : RutasApp("servicio/{idOfertaServicio}") {
        fun crearRuta(idOfertaServicio: Long): String = "servicio/$idOfertaServicio"
    }

    companion object {
        private val rutasPrincipales = listOf(
            Perfil.ruta,
            Principal.ruta,
            Chats.ruta
        )

        fun esRutaPrincipal(ruta: String?): Boolean = indiceRutaPrincipal(ruta) >= 0

        fun indiceRutaPrincipal(ruta: String?): Int {
            val rutaBase = ruta?.substringBefore("/")
            return rutasPrincipales.indexOf(rutaBase)
        }
    }
}

package com.movil.contrabajo.ui.navigation

sealed class RutasApp(val ruta: String) {
    data object Inicio : RutasApp("inicio")
    data object Login : RutasApp("login")
    data object RegistroPasoUno : RutasApp("registro_paso_uno")
    data object RegistroPasoDos : RutasApp("registro_paso_dos")
    data object Principal : RutasApp("principal")
    data object Chats : RutasApp("chats")
    data object Perfil : RutasApp("perfil")
    data object Servicio : RutasApp("servicio/{idOfertaServicio}") {
        fun crearRuta(idOfertaServicio: Long): String = "servicio/$idOfertaServicio"
    }
}

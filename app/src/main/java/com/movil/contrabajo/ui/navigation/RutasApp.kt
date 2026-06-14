package com.movil.contrabajo.ui.navigation

sealed class RutasApp(val ruta: String) {
    data object Inicio : RutasApp("inicio")
    data object Login : RutasApp("login")
    data object RegistroPasoUno : RutasApp("registro_paso_uno")
    data object RegistroPasoDireccion : RutasApp("registro_paso_direccion")
    data object RegistroPasoDos : RutasApp("registro_paso_dos")
    data object RegistroPasoSeguridad : RutasApp("registro_paso_seguridad")
    data object RecuperarCuenta : RutasApp("recuperar_cuenta")
    data object PrincipalShell : RutasApp("principal_shell")
    data object Principal : RutasApp("principal")
    data object ReportesModerador : RutasApp("reportes_moderador")
    data object ReporteDetalle : RutasApp("reporte_detalle/{idReporte}") {
        fun crearRuta(idReporte: Long): String = "reporte_detalle/$idReporte"
    }
    data object Chats : RutasApp("chats")
    data object ChatDetalle : RutasApp("chat_detalle/{idChatCita}") {
        fun crearRuta(idChatCita: Long): String = "chat_detalle/$idChatCita"
    }
    data object CitaDetalle : RutasApp("cita_detalle/{idChatCita}") {
        fun crearRuta(idChatCita: Long): String = "cita_detalle/$idChatCita"
    }
    data object Perfil : RutasApp("perfil")
    data object PerfilEditar : RutasApp("perfil/editar")
    data object Ajustes : RutasApp("ajustes")
    data object AjustesSeguridad : RutasApp("ajustes/seguridad")
    data object AjustesVerificacion : RutasApp("ajustes/seguridad/verificacion")
    data object AjustesPreguntas : RutasApp("ajustes/seguridad/preguntas")
    data object AjustesCuenta : RutasApp("ajustes/cuenta")
    data object AjustesUbicacion : RutasApp("ajustes/ubicacion")
    data object AjustesPreferencias : RutasApp("ajustes/preferencias")
    data object AjustesModerarBaneos : RutasApp("ajustes/moderacion/baneos")
    data object ValoracionesServicios : RutasApp("perfil/valoraciones")
    data object ServicioEditor : RutasApp("servicio_editor/{modo}/{idOfertaServicio}") {
        fun crearRuta(modo: String, idOfertaServicio: Long): String = "servicio_editor/$modo/$idOfertaServicio"
    }
    data object Servicio : RutasApp("servicio/{idOfertaServicio}") {
        fun crearRuta(idOfertaServicio: Long): String = "servicio/$idOfertaServicio"
    }
    data object PremiumBienvenida : RutasApp("premium_bienvenida")
    data object PremiumActivado : RutasApp("premium_activado")
    data object MenuPremium : RutasApp("menu_premium")
    data object PremiumHistorialContactos : RutasApp("premium_historial_contactos")
    data object PremiumEstadisticas : RutasApp("premium_estadisticas")

    companion object {
        private val rutasPrincipales = listOf(
            Perfil.ruta,
            Principal.ruta,
            ReportesModerador.ruta,
            Chats.ruta
        )

        fun esRutaPrincipal(ruta: String?): Boolean = indiceRutaPrincipal(ruta) >= 0

        fun indiceRutaPrincipal(ruta: String?): Int {
            val rutaBase = ruta?.substringBefore("/")
            return rutasPrincipales.indexOf(rutaBase)
        }
    }
}

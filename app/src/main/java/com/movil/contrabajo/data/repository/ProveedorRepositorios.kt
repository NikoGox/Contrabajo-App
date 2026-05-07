package com.movil.contrabajo.data.repository

import android.content.Context
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.ServiciosApiClient
import com.movil.contrabajo.data.remote.UsuariosApiClient

class ProveedorRepositorios(context: Context) {
    private val sessionStore = RemoteSessionStore(context.applicationContext)

    val autenticacion: RepositorioAutenticacion = RepositorioAutenticacionRemoto(
        api = UsuariosApiClient.api,
        sessionStore = sessionStore
    )
    val perfil: RepositorioPerfil = RepositorioPerfilRemoto(
        api = UsuariosApiClient.api,
        sessionStore = sessionStore
    )
    val ofertas: RepositorioOfertas = RepositorioOfertasRemoto(
        api = ServiciosApiClient.api,
        sessionStore = sessionStore
    )
    val chats: RepositorioChats = RepositorioChatsRecortado(sessionStore)
    val reportes: RepositorioReportes = RepositorioReportesRecortado()
}

package com.movil.contrabajo.data.repository

import android.content.Context
import com.movil.contrabajo.data.remote.ComunicacionesApiClient
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
        sessionStore = sessionStore,
        context = context.applicationContext
    )
    val ofertas: RepositorioOfertas = RepositorioOfertasRemoto(
        api = ServiciosApiClient.api,
        sessionStore = sessionStore,
        context = context.applicationContext
    )
    val chats: RepositorioChats = RepositorioChatRemoto(
        comunicacionesApi = ComunicacionesApiClient.api,
        serviciosApi = ServiciosApiClient.api,
        sessionStore = sessionStore
    )
    val reportes: RepositorioReportes = RepositorioReportesRemoto(
        comunicacionesApi = ComunicacionesApiClient.api,
        sessionStore = sessionStore
    )
}

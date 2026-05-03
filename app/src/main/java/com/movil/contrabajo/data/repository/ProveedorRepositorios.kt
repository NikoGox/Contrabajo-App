package com.movil.contrabajo.data.repository

import android.content.Context
import com.movil.contrabajo.data.local.ContrabajoSQLiteHelper
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.UsuariosApiClient

class ProveedorRepositorios(context: Context) {
    private val db = ContrabajoSQLiteHelper(context.applicationContext)
    private val sessionStore = RemoteSessionStore(context.applicationContext)
    private val perfilLocal = RepositorioPerfilLocal(db)

    val autenticacion: RepositorioAutenticacion = RepositorioAutenticacionRemoto(
        api = UsuariosApiClient.api,
        sessionStore = sessionStore
    )
    val perfil: RepositorioPerfil = RepositorioPerfilRemoto(
        api = UsuariosApiClient.api,
        sessionStore = sessionStore,
        localFallback = perfilLocal
    )
    val ofertas: RepositorioOfertas = RepositorioOfertasLocal(db)
    val chats: RepositorioChats = RepositorioChatsLocal(db)
    val reportes: RepositorioReportes = RepositorioReportesLocal(db)
}

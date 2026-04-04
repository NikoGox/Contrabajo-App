package com.movil.contrabajo.data.repository

import android.content.Context
import com.movil.contrabajo.data.local.ContrabajoSQLiteHelper

class ProveedorRepositorios(context: Context) {
    private val db = ContrabajoSQLiteHelper(context.applicationContext)

    val autenticacion: RepositorioAutenticacion = RepositorioAutenticacionLocal(db)
    val perfil: RepositorioPerfil = RepositorioPerfilLocal(db)
    val ofertas: RepositorioOfertas = RepositorioOfertasLocal(db)
    val chats: RepositorioChats = RepositorioChatsLocal(db)
}

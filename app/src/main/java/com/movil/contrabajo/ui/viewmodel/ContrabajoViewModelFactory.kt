package com.movil.contrabajo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.contrabajo.data.repository.ProveedorRepositorios

class ContrabajoViewModelFactory(
    private val repositorios: ProveedorRepositorios
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(InicioViewModel::class.java) ->
            InicioViewModel(repositorios.autenticacion) as T

        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(repositorios.autenticacion) as T

        modelClass.isAssignableFrom(RegistroViewModel::class.java) ->
            RegistroViewModel(repositorios.autenticacion) as T

        modelClass.isAssignableFrom(PrincipalViewModel::class.java) ->
            PrincipalViewModel(
                repositorioOfertas = repositorios.ofertas,
                repositorioPerfil = repositorios.perfil
            ) as T

        modelClass.isAssignableFrom(ChatsViewModel::class.java) ->
            ChatsViewModel(repositorios.chats) as T

        modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
            PerfilViewModel(
                repositorioPerfil = repositorios.perfil,
                repositorioOfertas = repositorios.ofertas,
                repositorioAutenticacion = repositorios.autenticacion
            ) as T

        modelClass.isAssignableFrom(DetalleServicioViewModel::class.java) ->
            DetalleServicioViewModel(
                repositorioOfertas = repositorios.ofertas,
                repositorioPerfil = repositorios.perfil
            ) as T

        modelClass.isAssignableFrom(ReportesViewModel::class.java) ->
            ReportesViewModel(repositorioReportes = repositorios.reportes) as T

        else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    }
}

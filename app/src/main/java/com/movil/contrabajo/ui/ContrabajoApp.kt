package com.movil.contrabajo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movil.contrabajo.data.repository.ProveedorRepositorios
import com.movil.contrabajo.domain.model.RegistroPendiente
import com.movil.contrabajo.ui.navigation.RutasApp
import com.movil.contrabajo.ui.screens.autenticacion.PantallaLogin
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoDos
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoUno
import com.movil.contrabajo.ui.screens.chats.PantallaChats
import com.movil.contrabajo.ui.screens.inicio.PantallaInicial
import com.movil.contrabajo.ui.screens.perfil.PantallaPerfil
import com.movil.contrabajo.ui.screens.principal.PantallaPrincipal
import com.movil.contrabajo.ui.screens.servicio.PantallaDetalleServicio

@Composable
fun ContrabajoApp() {
    val context = LocalContext.current
    val repositorios = remember { ProveedorRepositorios(context) }
    val navController = rememberNavController()
    val registroPendiente = remember { mutableStateOf(RegistroPendiente()) }

    NavHost(
        navController = navController,
        startDestination = RutasApp.Inicio.ruta
    ) {
        composable(RutasApp.Inicio.ruta) {
            PantallaInicial(
                obtenerSesionActiva = { repositorios.autenticacion.obtenerSesionActiva() },
                irALogin = { navController.navigate(RutasApp.Login.ruta) },
                irARegistro = { navController.navigate(RutasApp.RegistroPasoUno.ruta) },
                irAPrincipal = {
                    navController.navigate(RutasApp.Principal.ruta) {
                        popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                    }
                }
            )
        }
        composable(RutasApp.Login.ruta) {
            PantallaLogin(
                onVolver = { navController.popBackStack() },
                onLoginExitoso = {
                    navController.navigate(RutasApp.Principal.ruta) {
                        popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                    }
                },
                onRegistrarse = { navController.navigate(RutasApp.RegistroPasoUno.ruta) },
                repositorioAutenticacion = repositorios.autenticacion
            )
        }
        composable(RutasApp.RegistroPasoUno.ruta) {
            PantallaRegistroPasoUno(
                estadoInicial = registroPendiente.value,
                onVolver = { navController.popBackStack() },
                onContinuar = {
                    registroPendiente.value = it
                    navController.navigate(RutasApp.RegistroPasoDos.ruta)
                }
            )
        }
        composable(RutasApp.RegistroPasoDos.ruta) {
            PantallaRegistroPasoDos(
                estadoInicial = registroPendiente.value,
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = {
                    registroPendiente.value = RegistroPendiente()
                    navController.navigate(RutasApp.Principal.ruta) {
                        popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                    }
                },
                repositorioAutenticacion = repositorios.autenticacion
            )
        }
        composable(RutasApp.Principal.ruta) {
            PantallaPrincipal(
                oferta = repositorios.ofertas.obtenerOfertaPrincipal(),
                onAbrirServicio = { idOferta ->
                    navController.navigate(RutasApp.Servicio.crearRuta(idOferta))
                },
                onNavegar = { ruta ->
                    navController.navigate(ruta) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(RutasApp.Chats.ruta) {
            PantallaChats(
                chats = repositorios.chats.obtenerChatsActuales(),
                onNavegar = { ruta ->
                    navController.navigate(ruta) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(RutasApp.Perfil.ruta) {
            PantallaPerfil(
                usuario = repositorios.perfil.obtenerPerfilActual(),
                onCerrarSesion = {
                    repositorios.autenticacion.cerrarSesion()
                    navController.navigate(RutasApp.Inicio.ruta) {
                        popUpTo(0)
                    }
                },
                onNavegar = { ruta ->
                    navController.navigate(ruta) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = RutasApp.Servicio.ruta,
            arguments = listOf(navArgument("idOfertaServicio") { type = NavType.LongType })
        ) { backStackEntry ->
            val idOferta = backStackEntry.arguments?.getLong("idOfertaServicio") ?: 1L
            PantallaDetalleServicio(
                oferta = repositorios.ofertas.obtenerOfertaPorId(idOferta),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

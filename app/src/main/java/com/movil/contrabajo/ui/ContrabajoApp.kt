package com.movil.contrabajo.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movil.contrabajo.data.repository.ProveedorRepositorios
import com.movil.contrabajo.ui.components.ContenedorConNavbarFlotante
import com.movil.contrabajo.ui.components.FondoContrabajo
import com.movil.contrabajo.ui.navigation.RutasApp
import com.movil.contrabajo.ui.screens.autenticacion.PantallaLogin
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoDos
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoUno
import com.movil.contrabajo.ui.screens.ajustes.PantallaAjustes
import com.movil.contrabajo.ui.screens.ajustes.PantallaAjustesSeguridad
import com.movil.contrabajo.ui.screens.ajustes.PantallaCuenta
import com.movil.contrabajo.ui.screens.ajustes.PantallaPreguntasSeguridad
import com.movil.contrabajo.ui.screens.ajustes.PantallaUbicacion
import com.movil.contrabajo.ui.screens.ajustes.PantallaVerificarCuentaTrabajador
import com.movil.contrabajo.ui.screens.chats.PantallaChats
import com.movil.contrabajo.ui.screens.inicio.PantallaInicial
import com.movil.contrabajo.ui.screens.perfil.PantallaPerfil
import com.movil.contrabajo.ui.screens.principal.PantallaPrincipal
import com.movil.contrabajo.ui.screens.servicio.PantallaDetalleServicio
import com.movil.contrabajo.ui.screens.servicio.PantallaEditorServicio
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel
import com.movil.contrabajo.ui.viewmodel.ContrabajoViewModelFactory
import com.movil.contrabajo.ui.viewmodel.DetalleServicioViewModel
import com.movil.contrabajo.ui.viewmodel.InicioViewModel
import com.movil.contrabajo.ui.viewmodel.LoginViewModel
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel
import com.movil.contrabajo.ui.viewmodel.PrincipalViewModel
import com.movil.contrabajo.ui.viewmodel.RegistroViewModel
import kotlin.math.abs

@Composable
fun ContrabajoApp() {
    val context = LocalContext.current
    val repositorios = remember(context) { ProveedorRepositorios(context) }
    val factory = remember(repositorios) { ContrabajoViewModelFactory(repositorios) }
    val navController = rememberNavController()
    val registroViewModel: RegistroViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = RutasApp.Inicio.ruta,
        enterTransition = { crearTransicionEntrada() },
        exitTransition = { crearTransicionSalida() },
        popEnterTransition = { crearTransicionPopEntrada() },
        popExitTransition = { crearTransicionPopSalida() }
    ) {
        composable(RutasApp.Inicio.ruta) {
            val inicioViewModel: InicioViewModel = viewModel(factory = factory)
            PantallaInicial(
                viewModel = inicioViewModel,
                irALogin = { navController.navigate(RutasApp.Login.ruta) },
                irARegistro = { navController.navigate(RutasApp.RegistroPasoUno.ruta) },
                irAPrincipal = {
                    navController.navigate(RutasApp.PrincipalShell.ruta) {
                        popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                    }
                }
            )
        }
        composable(RutasApp.Login.ruta) {
            val loginViewModel: LoginViewModel = viewModel(factory = factory)
            PantallaLogin(
                onVolver = { navController.popBackStack() },
                onLoginExitoso = {
                    navController.navigate(RutasApp.PrincipalShell.ruta) {
                        popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                    }
                },
                onRegistrarse = { navController.navigate(RutasApp.RegistroPasoUno.ruta) },
                viewModel = loginViewModel
            )
        }
        composable(RutasApp.RegistroPasoUno.ruta) {
            PantallaRegistroPasoUno(
                viewModel = registroViewModel,
                onVolver = { navController.popBackStack() },
                onContinuar = {
                    navController.navigate(RutasApp.RegistroPasoDos.ruta)
                }
            )
        }
        composable(RutasApp.RegistroPasoDos.ruta) {
            PantallaRegistroPasoDos(
                viewModel = registroViewModel,
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = {
                    navController.navigate(RutasApp.PrincipalShell.ruta) {
                        popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                    }
                }
            )
        }
        composable(RutasApp.PrincipalShell.ruta) {
            val principalViewModel: PrincipalViewModel = viewModel(factory = factory)
            val chatsViewModel: ChatsViewModel = viewModel(factory = factory)
            val perfilViewModel: PerfilViewModel = viewModel(factory = factory)
            ShellPrincipal(
                principalViewModel = principalViewModel,
                chatsViewModel = chatsViewModel,
                perfilViewModel = perfilViewModel,
                onAbrirCrearServicio = {
                    navController.navigate(RutasApp.ServicioEditor.crearRuta("crear"))
                },
                onAbrirEditarServicio = {
                    navController.navigate(RutasApp.ServicioEditor.crearRuta("editar"))
                },
                onAbrirServicio = { idOferta ->
                    navController.navigate(RutasApp.Servicio.crearRuta(idOferta))
                },
                onAbrirAjustes = {
                    navController.navigate(RutasApp.Ajustes.ruta)
                },
                onCerrarSesion = {
                    navController.navigate(RutasApp.Inicio.ruta) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(RutasApp.Ajustes.ruta) { backStackEntry ->
            val principalShellEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RutasApp.PrincipalShell.ruta)
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = principalShellEntry,
                factory = factory
            )
            PantallaAjustes(
                onVolver = { navController.popBackStack() },
                onAbrirSeguridad = { navController.navigate(RutasApp.AjustesSeguridad.ruta) },
                onAbrirCuenta = { navController.navigate(RutasApp.AjustesCuenta.ruta) },
                onAbrirUbicacion = { navController.navigate(RutasApp.AjustesUbicacion.ruta) },
                onCerrarSesion = {
                    perfilViewModel.cerrarSesion()
                    navController.navigate(RutasApp.Inicio.ruta) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(RutasApp.AjustesSeguridad.ruta) {
            PantallaAjustesSeguridad(
                onVolver = { navController.popBackStack() },
                onAbrirVerificacion = { navController.navigate(RutasApp.AjustesVerificacion.ruta) },
                onAbrirPreguntas = { navController.navigate(RutasApp.AjustesPreguntas.ruta) }
            )
        }
        composable(RutasApp.AjustesVerificacion.ruta) { backStackEntry ->
            val principalShellEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RutasApp.PrincipalShell.ruta)
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = principalShellEntry,
                factory = factory
            )
            PantallaVerificarCuentaTrabajador(
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(RutasApp.AjustesPreguntas.ruta) { backStackEntry ->
            val principalShellEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RutasApp.PrincipalShell.ruta)
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = principalShellEntry,
                factory = factory
            )
            PantallaPreguntasSeguridad(
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(RutasApp.AjustesCuenta.ruta) { backStackEntry ->
            val principalShellEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RutasApp.PrincipalShell.ruta)
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = principalShellEntry,
                factory = factory
            )
            PantallaCuenta(
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(RutasApp.AjustesUbicacion.ruta) { backStackEntry ->
            val principalShellEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RutasApp.PrincipalShell.ruta)
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = principalShellEntry,
                factory = factory
            )
            PantallaUbicacion(
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = RutasApp.ServicioEditor.ruta,
            arguments = listOf(navArgument("modo") { type = NavType.StringType })
        ) { backStackEntry ->
            val principalShellEntry = remember(backStackEntry) {
                navController.getBackStackEntry(RutasApp.PrincipalShell.ruta)
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = principalShellEntry,
                factory = factory
            )
            val modo = backStackEntry.arguments?.getString("modo").orEmpty()
            PantallaEditorServicio(
                modo = modo,
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = RutasApp.Servicio.ruta,
            arguments = listOf(navArgument("idOfertaServicio") { type = NavType.LongType })
        ) { backStackEntry ->
            val detalleServicioViewModel: DetalleServicioViewModel = viewModel(factory = factory)
            val idOferta = backStackEntry.arguments?.getLong("idOfertaServicio") ?: 1L
            PantallaDetalleServicio(
                idOfertaServicio = idOferta,
                viewModel = detalleServicioViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun ShellPrincipal(
    principalViewModel: PrincipalViewModel,
    chatsViewModel: ChatsViewModel,
    perfilViewModel: PerfilViewModel,
    onAbrirCrearServicio: () -> Unit,
    onAbrirEditarServicio: () -> Unit,
    onAbrirServicio: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rutaActual by rememberSaveable { mutableStateOf(RutasApp.Principal.ruta) }
    var desplazamientoHorizontal by remember { mutableStateOf(0f) }
    val rutasSwipe = remember { listOf(RutasApp.Principal.ruta, RutasApp.Chats.ruta, RutasApp.Perfil.ruta) }

    LaunchedEffect(rutaActual) {
        when (rutaActual) {
            RutasApp.Chats.ruta -> chatsViewModel.recargar()
            RutasApp.Perfil.ruta -> perfilViewModel.recargar()
        }
    }

    ContenedorConNavbarFlotante(
        actual = rutaActual,
        alNavegar = { nuevaRuta ->
            if (rutaActual != nuevaRuta) {
                rutaActual = nuevaRuta
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rutaActual) {
                    detectHorizontalDragGestures(
                        onDragStart = { desplazamientoHorizontal = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            desplazamientoHorizontal += dragAmount
                        },
                        onDragEnd = {
                            val umbral = 85f
                            if (abs(desplazamientoHorizontal) >= umbral) {
                                val indiceActual = rutasSwipe.indexOf(rutaActual).let { if (it < 0) 0 else it }
                                val indiceDestino = if (desplazamientoHorizontal < 0f) {
                                    (indiceActual + 1).coerceAtMost(rutasSwipe.lastIndex)
                                } else {
                                    (indiceActual - 1).coerceAtLeast(0)
                                }
                                rutaActual = rutasSwipe[indiceDestino]
                            }
                            desplazamientoHorizontal = 0f
                        },
                        onDragCancel = { desplazamientoHorizontal = 0f }
                    )
                }
        ) {
            FondoContrabajo(modifier = Modifier.fillMaxSize())
            AnimatedContent(
                targetState = rutaActual,
                transitionSpec = {
                    val indiceInicial = RutasApp.indiceRutaPrincipal(initialState)
                    val indiceDestino = RutasApp.indiceRutaPrincipal(targetState)
                    val haciaIzquierda = indiceDestino > indiceInicial

                    (
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 280),
                            initialOffsetX = { ancho -> if (haciaIzquierda) ancho else -ancho }
                        ) + fadeIn(animationSpec = tween(durationMillis = 180))
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 260),
                            targetOffsetX = { ancho -> if (haciaIzquierda) -ancho else ancho }
                        ) + fadeOut(animationSpec = tween(durationMillis = 150))
                    )
                },
                label = "contenidoPrincipalAnimado"
            ) { rutaSeleccionada ->
                when (rutaSeleccionada) {
                    RutasApp.Principal.ruta -> PantallaPrincipal(
                        viewModel = principalViewModel,
                        onAbrirServicio = onAbrirServicio,
                        onAbrirAjustes = onAbrirAjustes,
                        modifier = Modifier.padding(innerPadding)
                    )

                    RutasApp.Chats.ruta -> PantallaChats(
                        viewModel = chatsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )

                    RutasApp.Perfil.ruta -> PantallaPerfil(
                        viewModel = perfilViewModel,
                        onAbrirCrearServicio = onAbrirCrearServicio,
                        onAbrirEditarServicio = onAbrirEditarServicio,
                        onCerrarSesion = onCerrarSesion,
                        modifier = Modifier.padding(innerPadding)
                    )

                    else -> PantallaPrincipal(
                        viewModel = principalViewModel,
                        onAbrirServicio = onAbrirServicio,
                        onAbrirAjustes = onAbrirAjustes,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionEntrada(): EnterTransition {
    return when {
        targetState.destination.route == RutasApp.Servicio.ruta ->
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = 260)
            ) + fadeIn(animationSpec = tween(durationMillis = 180))
        else -> fadeIn(animationSpec = tween(durationMillis = 180))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionSalida(): ExitTransition {
    return when {
        targetState.destination.route == RutasApp.Servicio.ruta ->
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = 220)
            ) + fadeOut(animationSpec = tween(durationMillis = 140))
        else -> fadeOut(animationSpec = tween(durationMillis = 120))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionPopEntrada(): EnterTransition {
    return when {
        initialState.destination.route == RutasApp.Servicio.ruta ->
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = 260)
            ) + fadeIn(animationSpec = tween(durationMillis = 180))
        else -> fadeIn(animationSpec = tween(durationMillis = 160))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionPopSalida(): ExitTransition {
    return when {
        initialState.destination.route == RutasApp.Servicio.ruta ->
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = 220)
            ) + fadeOut(animationSpec = tween(durationMillis = 140))
        else -> fadeOut(animationSpec = tween(durationMillis = 120))
    }
}

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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.navigation.RutasApp
import com.movil.contrabajo.ui.notificaciones.NotificacionesMensajes
import com.movil.contrabajo.ui.screens.autenticacion.PantallaLogin
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoDireccion
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoDos
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoUno
import com.movil.contrabajo.ui.screens.ajustes.PantallaAjustes
import com.movil.contrabajo.ui.screens.ajustes.PantallaAjustesSeguridad
import com.movil.contrabajo.ui.screens.ajustes.PantallaCuenta
import com.movil.contrabajo.ui.screens.ajustes.PantallaPreguntasSeguridad
import com.movil.contrabajo.ui.screens.ajustes.PantallaUbicacion
import com.movil.contrabajo.ui.screens.ajustes.PantallaVerificarCuentaTrabajador
import com.movil.contrabajo.ui.screens.chats.PantallaDetalleChat
import com.movil.contrabajo.ui.screens.chats.PantallaCitaServicio
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ContrabajoApp(
    chatNotificacionPendienteId: Long? = null,
    onConsumirChatNotificacionPendiente: () -> Unit = {}
) {
    val context = LocalContext.current
    val repositorios = remember(context) { ProveedorRepositorios(context) }
    val factory = remember(repositorios) { ContrabajoViewModelFactory(repositorios) }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val registroViewModel: RegistroViewModel = viewModel(factory = factory)
    val principalViewModel: PrincipalViewModel = viewModel(factory = factory)
    val chatsViewModel: ChatsViewModel = viewModel(factory = factory)
    val perfilViewModel: PerfilViewModel = viewModel(factory = factory)
    val detalleServicioViewModel: DetalleServicioViewModel = viewModel(factory = factory)
    var mostrarCargaGlobal by rememberSaveable { mutableStateOf(false) }
    var mensajeCargaGlobal by rememberSaveable { mutableStateOf("Cargando...") }
    var progresoCargaGlobal by rememberSaveable { mutableStateOf(0f) }
    var mostrarIndicadorCargaGlobal by rememberSaveable { mutableStateOf(true) }
    var modoSuaveCargaGlobal by rememberSaveable { mutableStateOf(false) }
    var navegacionEnCarga by rememberSaveable { mutableStateOf(false) }

    val abrirPrincipalConCarga: () -> Unit = {
        if (!navegacionEnCarga) {
            scope.launch {
                val inicioCargaMs = System.currentTimeMillis()
                navegacionEnCarga = true
                mensajeCargaGlobal = "Iniciando sesion..."
                progresoCargaGlobal = 0.08f
                mostrarIndicadorCargaGlobal = true
                modoSuaveCargaGlobal = false
                mostrarCargaGlobal = true

                delay(120)
                mensajeCargaGlobal = "Validando acceso..."
                progresoCargaGlobal = 0.18f

                delay(120)
                mensajeCargaGlobal = "Cargando servicios..."
                progresoCargaGlobal = 0.34f
                principalViewModel.recargar()

                delay(90)
                mensajeCargaGlobal = "Cargando chats..."
                progresoCargaGlobal = 0.52f
                chatsViewModel.recargar()

                delay(90)
                mensajeCargaGlobal = "Cargando perfil..."
                progresoCargaGlobal = 0.70f
                perfilViewModel.recargar()

                delay(80)
                mensajeCargaGlobal = "Preparando inicio..."
                progresoCargaGlobal = 0.84f
                navController.navigate(RutasApp.PrincipalShell.ruta) {
                    popUpTo(RutasApp.Inicio.ruta) { inclusive = true }
                }

                progresoCargaGlobal = 0.94f
                val faltante = (3_000L - (System.currentTimeMillis() - inicioCargaMs)).coerceAtLeast(0L)
                if (faltante > 0) delay(faltante)
                mensajeCargaGlobal = "Listo"
                progresoCargaGlobal = 1f
                delay(90)
                mostrarCargaGlobal = false
                modoSuaveCargaGlobal = false
                navegacionEnCarga = false
            }
        }
    }

    val abrirDetalleConCarga: (Long) -> Unit = { idOferta ->
        if (!navegacionEnCarga) {
            scope.launch {
                navegacionEnCarga = true
                try {
                    detalleServicioViewModel.prepararContextoMarketplace(principalViewModel.uiState.ofertas)
                    detalleServicioViewModel.cargarOferta(idOfertaServicio = idOferta, forzarRecarga = true)
                    navController.navigate(RutasApp.Servicio.crearRuta(idOferta))
                } finally {
                    navegacionEnCarga = false
                }
            }
        }
    }

    val abrirChatDesdeOferta: (Long) -> Unit = { idOferta ->
        chatsViewModel.iniciarConversacionDesdeOferta(idOferta)
            .onSuccess { chat ->
                navController.navigate(RutasApp.ChatDetalle.crearRuta(chat.idChatCita))
            }
    }

    LaunchedEffect(chatNotificacionPendienteId) {
        val idChat = chatNotificacionPendienteId ?: return@LaunchedEffect
        navController.navigate(RutasApp.PrincipalShell.ruta) {
            launchSingleTop = true
        }
        navController.navigate(RutasApp.ChatDetalle.crearRuta(idChat)) {
            launchSingleTop = true
        }
        onConsumirChatNotificacionPendiente()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    irAPrincipal = abrirPrincipalConCarga
                )
            }
            composable(RutasApp.Login.ruta) {
                val loginViewModel: LoginViewModel = viewModel(factory = factory)
                PantallaLogin(
                    onVolver = { navController.popBackStack() },
                    onLoginExitoso = abrirPrincipalConCarga,
                    onRegistrarse = { navController.navigate(RutasApp.RegistroPasoUno.ruta) },
                    viewModel = loginViewModel
                )
            }
            composable(RutasApp.RegistroPasoUno.ruta) {
                PantallaRegistroPasoUno(
                    viewModel = registroViewModel,
                    onVolver = { navController.popBackStack() },
                    onContinuar = {
                        navController.navigate(RutasApp.RegistroPasoDireccion.ruta)
                    }
                )
            }
            composable(RutasApp.RegistroPasoDireccion.ruta) {
                PantallaRegistroPasoDireccion(
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
                    onRegistroExitoso = abrirPrincipalConCarga
                )
            }
            composable(RutasApp.PrincipalShell.ruta) {
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
                    onAbrirServicio = abrirDetalleConCarga,
                    onAbrirChat = { idChat ->
                        navController.navigate(RutasApp.ChatDetalle.crearRuta(idChat))
                    },
                    onAbrirAjustes = {
                        navController.navigate(RutasApp.Ajustes.ruta)
                    },
                    onAbrirUbicacionRapida = {
                        navController.navigate(RutasApp.AjustesUbicacion.ruta)
                    }
                )
            }
            composable(RutasApp.Ajustes.ruta) {
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
            composable(RutasApp.AjustesVerificacion.ruta) {
                PantallaVerificarCuentaTrabajador(
                    viewModel = perfilViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.AjustesPreguntas.ruta) {
                PantallaPreguntasSeguridad(
                    viewModel = perfilViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.AjustesCuenta.ruta) {
                PantallaCuenta(
                    viewModel = perfilViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.AjustesUbicacion.ruta) {
                PantallaUbicacion(
                    viewModel = perfilViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = RutasApp.ChatDetalle.ruta,
                arguments = listOf(navArgument("idChatCita") { type = NavType.LongType })
            ) { backStackEntry ->
                val idChat = backStackEntry.arguments?.getLong("idChatCita") ?: 0L
                PantallaDetalleChat(
                    idChatCita = idChat,
                    viewModel = chatsViewModel,
                    onVolver = { navController.popBackStack() },
                    onAbrirCita = { idChatCita ->
                        navController.navigate(RutasApp.CitaDetalle.crearRuta(idChatCita))
                    },
                    onAbrirServicioAsociado = { idOfertaServicio ->
                        navController.navigate(RutasApp.Servicio.crearRuta(idOfertaServicio))
                    }
                )
            }
            composable(
                route = RutasApp.CitaDetalle.ruta,
                arguments = listOf(navArgument("idChatCita") { type = NavType.LongType })
            ) { backStackEntry ->
                val idChat = backStackEntry.arguments?.getLong("idChatCita") ?: 0L
                PantallaCitaServicio(
                    idChatCita = idChat,
                    viewModel = chatsViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = RutasApp.ServicioEditor.ruta,
                arguments = listOf(navArgument("modo") { type = NavType.StringType })
            ) { backStackEntry ->
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
                val idOferta = backStackEntry.arguments?.getLong("idOfertaServicio") ?: 1L
                PantallaDetalleServicio(
                    idOfertaServicio = idOferta,
                    viewModel = detalleServicioViewModel,
                    onEditarServicio = {
                        navController.navigate(RutasApp.ServicioEditor.crearRuta("editar"))
                    },
                    onContactarServicio = abrirChatDesdeOferta,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        OverlayPantallaCarga(
            visible = mostrarCargaGlobal,
            mensaje = mensajeCargaGlobal,
            progreso = progresoCargaGlobal,
            mostrarIndicador = mostrarIndicadorCargaGlobal,
            modoSuave = modoSuaveCargaGlobal
        )
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
    onAbrirChat: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    onAbrirUbicacionRapida: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rutaContenido by rememberSaveable { mutableStateOf(RutasApp.Principal.ruta) }
    var desplazamientoHorizontal by remember { mutableStateOf(0f) }
    val rutasSwipe = remember { listOf(RutasApp.Perfil.ruta, RutasApp.Principal.ruta, RutasApp.Chats.ruta) }

    LaunchedEffect(rutaContenido) {
        when (rutaContenido) {
            RutasApp.Principal.ruta -> chatsViewModel.recargar()
            RutasApp.Chats.ruta -> chatsViewModel.recargar()
            RutasApp.Perfil.ruta -> perfilViewModel.recargar()
        }
    }

    LaunchedEffect(chatsViewModel.uiState.notificacionesPendientes) {
        val pendientes = chatsViewModel.uiState.notificacionesPendientes
        if (pendientes.isEmpty()) return@LaunchedEffect
        NotificacionesMensajes.mostrarNotificacionesMensajes(context, pendientes)
        chatsViewModel.marcarNotificacionesMostradas(pendientes.map { it.idMensajeChat })
    }

    ContenedorConNavbarFlotante(
        actual = rutaContenido,
        alNavegar = { nuevaRuta ->
            if (rutaContenido != nuevaRuta) {
                rutaContenido = nuevaRuta
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rutaContenido) {
                    detectHorizontalDragGestures(
                        onDragStart = { desplazamientoHorizontal = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            desplazamientoHorizontal += dragAmount
                        },
                        onDragEnd = {
                            val umbral = 85f
                            if (abs(desplazamientoHorizontal) >= umbral) {
                                val indiceActual = rutasSwipe.indexOf(rutaContenido).let { if (it < 0) 0 else it }
                                val indiceDestino = if (desplazamientoHorizontal < 0f) {
                                    (indiceActual + 1).coerceAtMost(rutasSwipe.lastIndex)
                                } else {
                                    (indiceActual - 1).coerceAtLeast(0)
                                }
                                val destino = rutasSwipe[indiceDestino]
                                rutaContenido = destino
                            }
                            desplazamientoHorizontal = 0f
                        },
                        onDragCancel = { desplazamientoHorizontal = 0f }
                    )
                }
        ) {
            FondoContrabajo(modifier = Modifier.fillMaxSize())
            AnimatedContent(
                targetState = rutaContenido,
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
                        onAbrirUbicacionRapida = onAbrirUbicacionRapida,
                        modifier = Modifier.padding(innerPadding)
                    )

                    RutasApp.Chats.ruta -> PantallaChats(
                        viewModel = chatsViewModel,
                        onAbrirChat = onAbrirChat,
                        modifier = Modifier.padding(innerPadding)
                    )

                    RutasApp.Perfil.ruta -> PantallaPerfil(
                        viewModel = perfilViewModel,
                        onAbrirCrearServicio = onAbrirCrearServicio,
                        onAbrirEditarServicio = onAbrirEditarServicio,
                        modifier = Modifier.padding(innerPadding)
                    )

                    else -> PantallaPrincipal(
                        viewModel = principalViewModel,
                        onAbrirServicio = onAbrirServicio,
                        onAbrirAjustes = onAbrirAjustes,
                        onAbrirUbicacionRapida = onAbrirUbicacionRapida,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionEntrada(): EnterTransition {
    return when {
        routeEsRegistro(targetState.destination.route) ->
            slideInVertically(
                animationSpec = tween(durationMillis = 250),
                initialOffsetY = { it / 4 }
            ) + fadeIn(animationSpec = tween(durationMillis = 220))
        routeEsServicio(targetState.destination.route) ->
            slideInVertically(
                animationSpec = tween(durationMillis = 240),
                initialOffsetY = { (it * 0.45f).toInt() }
            ) + fadeIn(animationSpec = tween(durationMillis = 220))
        routeEsAjustes(targetState.destination.route) ->
            fadeIn(animationSpec = tween(durationMillis = 120))
        else -> fadeIn(animationSpec = tween(durationMillis = 140))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionSalida(): ExitTransition {
    return when {
        routeEsRegistro(targetState.destination.route) && routeEsInicio(initialState.destination.route) ->
            slideOutVertically(
                animationSpec = tween(durationMillis = 200),
                targetOffsetY = { -it / 6 }
            ) + fadeOut(animationSpec = tween(durationMillis = 160))
        routeEsServicio(targetState.destination.route) ->
            fadeOut(animationSpec = tween(durationMillis = 100))
        routeEsAjustes(targetState.destination.route) ->
            fadeOut(animationSpec = tween(durationMillis = 90))
        else -> fadeOut(animationSpec = tween(durationMillis = 110))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionPopEntrada(): EnterTransition {
    return when {
        routeEsRegistro(initialState.destination.route) ->
            fadeIn(animationSpec = tween(durationMillis = 150))
        routeEsServicio(initialState.destination.route) ->
            fadeIn(animationSpec = tween(durationMillis = 120))
        routeEsAjustes(initialState.destination.route) ->
            fadeIn(animationSpec = tween(durationMillis = 90))
        else -> fadeIn(animationSpec = tween(durationMillis = 120))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionPopSalida(): ExitTransition {
    return when {
        routeEsRegistro(initialState.destination.route) ->
            slideOutVertically(
                animationSpec = tween(durationMillis = 200),
                targetOffsetY = { it / 5 }
            ) + fadeOut(animationSpec = tween(durationMillis = 160))
        routeEsServicio(initialState.destination.route) ->
            slideOutVertically(
                animationSpec = tween(durationMillis = 200),
                targetOffsetY = { (it * 0.45f).toInt() }
            ) + fadeOut(animationSpec = tween(durationMillis = 180))
        routeEsAjustes(initialState.destination.route) ->
            fadeOut(animationSpec = tween(durationMillis = 90))
        else -> fadeOut(animationSpec = tween(durationMillis = 110))
    }
}

private fun routeEsAjustes(route: String?): Boolean {
    val base = route?.substringBefore("/")
    return base == RutasApp.Ajustes.ruta
}

private fun routeEsInicio(route: String?): Boolean {
    val base = route?.substringBefore("/")
    return base == RutasApp.Inicio.ruta
}

private fun routeEsRegistro(route: String?): Boolean {
    val base = route?.substringBefore("/")
    return base == RutasApp.RegistroPasoUno.ruta ||
        base == RutasApp.RegistroPasoDireccion.ruta ||
        base == RutasApp.RegistroPasoDos.ruta
}

private fun routeEsServicio(route: String?): Boolean {
    val base = route?.substringBefore("/")
    return base == RutasApp.Servicio.ruta.substringBefore("/")
}

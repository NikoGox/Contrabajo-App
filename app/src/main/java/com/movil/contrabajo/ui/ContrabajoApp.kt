package com.movil.contrabajo.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movil.contrabajo.data.repository.ProveedorRepositorios
import com.movil.contrabajo.ui.components.BotonPrimario
import com.movil.contrabajo.ui.components.ContenedorConNavbarFlotante
import com.movil.contrabajo.ui.components.FondoContrabajo
import com.movil.contrabajo.ui.components.OverlayPantallaCarga
import com.movil.contrabajo.ui.navigation.RutasApp
import com.movil.contrabajo.ui.notificaciones.NotificacionesMensajes
import com.movil.contrabajo.ui.screens.autenticacion.PantallaLogin
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRecuperarCuenta
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoDireccion
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoDos
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoSeguridad
import com.movil.contrabajo.ui.screens.autenticacion.PantallaRegistroPasoUno
import com.movil.contrabajo.ui.screens.ajustes.PantallaAjustes
import com.movil.contrabajo.ui.screens.ajustes.PantallaAjustesSeguridad
import com.movil.contrabajo.ui.screens.ajustes.PantallaPreferencias
import com.movil.contrabajo.ui.screens.ajustes.PantallaBaneos
import com.movil.contrabajo.ui.screens.ajustes.PantallaCuenta
import com.movil.contrabajo.ui.screens.ajustes.PantallaPreguntasSeguridad
import com.movil.contrabajo.ui.screens.ajustes.PantallaUbicacion
import com.movil.contrabajo.ui.screens.ajustes.PantallaVerificarCuentaTrabajador
import com.movil.contrabajo.ui.screens.chats.PantallaDetalleChat
import com.movil.contrabajo.ui.screens.chats.PantallaCitaServicio
import com.movil.contrabajo.ui.screens.chats.PantallaChats
import com.movil.contrabajo.ui.screens.inicio.PantallaInicial
import com.movil.contrabajo.ui.screens.perfil.PantallaPerfil
import com.movil.contrabajo.ui.screens.premium.PantallaBienvenidaPremium
import com.movil.contrabajo.ui.screens.premium.PantallaHistorialContactosPremium
import com.movil.contrabajo.ui.screens.premium.PantallaMenuPremium
import com.movil.contrabajo.ui.screens.premium.PantallaPremiumActivado
import com.movil.contrabajo.ui.screens.premium.PantallaEstadisticasPremium
import com.movil.contrabajo.ui.screens.perfil.PantallaEditarPerfil
import com.movil.contrabajo.ui.screens.perfil.PantallaValoracionesServicios
import com.movil.contrabajo.ui.screens.principal.PantallaPrincipal
import com.movil.contrabajo.ui.screens.reportes.PantallaDetalleReporteModerador
import com.movil.contrabajo.ui.screens.reportes.PantallaReportesModerador
import com.movil.contrabajo.ui.screens.servicio.PantallaDetalleServicio
import com.movil.contrabajo.ui.screens.servicio.PantallaEditorServicio
import com.movil.contrabajo.ui.viewmodel.BaneosViewModel
import com.movil.contrabajo.ui.viewmodel.ChatsViewModel
import com.movil.contrabajo.ui.viewmodel.ContrabajoViewModelFactory
import com.movil.contrabajo.ui.viewmodel.DetalleServicioViewModel
import com.movil.contrabajo.ui.viewmodel.InicioViewModel
import com.movil.contrabajo.ui.viewmodel.LoginViewModel
import com.movil.contrabajo.ui.viewmodel.PerfilViewModel
import com.movil.contrabajo.ui.viewmodel.PremiumViewModel
import com.movil.contrabajo.ui.viewmodel.PrincipalViewModel
import com.movil.contrabajo.ui.viewmodel.RegistroViewModel
import com.movil.contrabajo.ui.viewmodel.ReportesViewModel
import com.movil.contrabajo.data.remote.RemoteSessionStore
import com.movil.contrabajo.data.remote.SesionEventos
import com.movil.contrabajo.data.remote.WsManager
import com.movil.contrabajo.data.workers.MensajesPollWorker
import com.movil.contrabajo.domain.model.TipoPerfil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun ContrabajoApp(
    chatNotificacionPendienteId: Long? = null,
    onConsumirChatNotificacionPendiente: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repositorios = remember(context) { ProveedorRepositorios(context) }
    val factory = remember(repositorios) { ContrabajoViewModelFactory(repositorios) }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val registroViewModel: RegistroViewModel = viewModel(factory = factory)
    val principalViewModel: PrincipalViewModel = viewModel(factory = factory)
    val chatsViewModel: ChatsViewModel = viewModel(factory = factory)
    val perfilViewModel: PerfilViewModel = viewModel(factory = factory)
    val reportesViewModel: ReportesViewModel = viewModel(factory = factory)
    val detalleServicioViewModel: DetalleServicioViewModel = viewModel(factory = factory)
    val baneosViewModel: BaneosViewModel = viewModel(factory = factory)
    var mostrarCargaGlobal by rememberSaveable { mutableStateOf(false) }
    var mensajeCargaGlobal by rememberSaveable { mutableStateOf("Cargando...") }
    var progresoCargaGlobal by rememberSaveable { mutableStateOf(0f) }
    var mostrarIndicadorCargaGlobal by rememberSaveable { mutableStateOf(true) }
    var modoSuaveCargaGlobal by rememberSaveable { mutableStateOf(false) }
    var navegacionEnCarga by rememberSaveable { mutableStateOf(false) }
    val sesionCerrada = perfilViewModel.uiState.sesionCerrada

    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) {
            // Desconectar WebSocket y cancelar polling al cerrar sesion
            WsManager.desconectar()
            MensajesPollWorker.cancelar(context)

            mensajeCargaGlobal = "Cerrando sesion..."
            progresoCargaGlobal = 1f
            mostrarIndicadorCargaGlobal = false
            modoSuaveCargaGlobal = true
            mostrarCargaGlobal = true
            delay(900)
            perfilViewModel.consumirCierreSesion()
            navController.navigate(RutasApp.Inicio.ruta) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
            mostrarCargaGlobal = false
        }
    }

    // Interceptor 401 → sesion invalidada desde otro dispositivo o por el backend
    LaunchedEffect(Unit) {
        SesionEventos.flujoSesionInvalida.collect {
            WsManager.desconectar()
            MensajesPollWorker.cancelar(context)
            RemoteSessionStore.limpiarSesionEstatica(context)

            mensajeCargaGlobal = "Sesion cerrada"
            progresoCargaGlobal = 1f
            mostrarIndicadorCargaGlobal = false
            modoSuaveCargaGlobal = true
            mostrarCargaGlobal = true
            delay(900)
            navController.navigate(RutasApp.Inicio.ruta) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
            mostrarCargaGlobal = false
            SesionEventos.resetear()
        }
    }

    val abrirPrincipalConCarga: () -> Unit = {
        if (!navegacionEnCarga) {
            scope.launch {
                val inicioCargaMs = System.currentTimeMillis()
                navegacionEnCarga = true
                SesionEventos.resetear() // Limpiar flag por si hubo 401 en segundo plano sin subscriber
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
                chatsViewModel.recargar(notificarNoLeidos = true)

                delay(90)
                mensajeCargaGlobal = "Cargando perfil..."
                progresoCargaGlobal = 0.70f
                perfilViewModel.recargar()

                delay(80)
                mensajeCargaGlobal = "Cargando reportes..."
                progresoCargaGlobal = 0.78f
                reportesViewModel.recargar()

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

                // Conectar WebSocket y programar polling de fondo tras login exitoso
                val token = RemoteSessionStore.obtenerTokenEstatico(context)
                val idUsuario = RemoteSessionStore.obtenerIdUsuarioEstatico(context)
                if (token != null && idUsuario != null) {
                    WsManager.conectar(token, idUsuario)
                    MensajesPollWorker.programar(context)
                }
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

    // Recibe (idOferta, tituloServicio, usernameTrabajador) desde PantallaDetalleServicio
    val abrirChatDesdeOferta: (Long, String, String) -> Unit = { idOferta, titulo, usernameTrab ->
        chatsViewModel.iniciarConversacionDesdeOferta(
            idOfertaServicio   = idOferta,
            tituloServicio     = titulo,
            usernameTrabajador = usernameTrab
            // usernameCliente lo obtiene el repositorio del store de sesion
        )
    }

    // Navega al chat recien creado cuando el ViewModel señala el ID
    LaunchedEffect(chatsViewModel.uiState.pendingNavChatId) {
        val idChat = chatsViewModel.uiState.pendingNavChatId ?: return@LaunchedEffect
        chatsViewModel.consumirNavChatId()
        navController.navigate(RutasApp.ChatDetalle.crearRuta(idChat))
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

    // Al volver del segundo plano, forzar reconexion/resync de mensajeria.
    // Evita sockets "zombie" que quedan sin recibir eventos al reanudar la app.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val token = RemoteSessionStore.obtenerTokenEstatico(context)
                val idUsuario = RemoteSessionStore.obtenerIdUsuarioEstatico(context)
                if (token != null && idUsuario != null) {
                    if (!WsManager.estaConectado()) {
                        WsManager.desconectar()
                        WsManager.conectar(token, idUsuario)
                    }
                    chatsViewModel.recargar(notificarNoLeidos = true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Monitor de conexión: detecta caídas del backend y muestra modal de intermitencias
    var mostrarModalIntermitencias by rememberSaveable { mutableStateOf(false) }
    var segundosSinConexion by rememberSaveable { mutableStateOf(0) }
    var monitoreoActivo by rememberSaveable { mutableStateOf(false) }

    val rutaActual = navController.currentBackStackEntry?.destination?.route
    val enPantallaPrincipal = rutaActual in listOf(
        "principal",
        "perfil",
        "chats"
    )

    LaunchedEffect(enPantallaPrincipal) {
        if (enPantallaPrincipal) {
            monitoreoActivo = true
            segundosSinConexion = 0
            mostrarModalIntermitencias = false
        } else {
            monitoreoActivo = false
            segundosSinConexion = 0
            mostrarModalIntermitencias = false
        }
    }

    LaunchedEffect(monitoreoActivo) {
        if (!monitoreoActivo) return@LaunchedEffect

        while (isActive) {
            delay(5000) // Verificar cada 5 segundos

            val token = RemoteSessionStore.obtenerTokenEstatico(context)
            if (token == null) {
                segundosSinConexion = 0
                continue
            }

            val conexionOk = try {
                withContext(Dispatchers.IO) {
                    val call = com.movil.contrabajo.data.remote.UsuariosApiClient.api.validarSesion(token)
                    val response = call.execute()
                    response.isSuccessful
                }
            } catch (_: Exception) {
                false
            }

            if (conexionOk) {
                segundosSinConexion = 0
                mostrarModalIntermitencias = false
            } else {
                segundosSinConexion += 5

                // A los 20 segundos, mostrar modal de advertencia
                if (segundosSinConexion >= 20 && !mostrarModalIntermitencias) {
                    mostrarModalIntermitencias = true
                }

                // A los 40 segundos, cerrar la app
                if (segundosSinConexion >= 40) {
                    val activity = context.findActivity()
                    activity?.finishAffinity()
                    break
                }
            }
        }
    }

    // Resetear contador al volver a tener conexión
    LaunchedEffect(mostrarModalIntermitencias) {
        if (!mostrarModalIntermitencias && segundosSinConexion == 0) {
            // La conexión se recuperó, todo bien
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    onRecuperarCuenta = { navController.navigate(RutasApp.RecuperarCuenta.ruta) },
                    viewModel = loginViewModel
                )
            }
            composable(RutasApp.RecuperarCuenta.ruta) {
                val loginViewModel: LoginViewModel = viewModel(factory = factory)
                PantallaRecuperarCuenta(
                    viewModel = loginViewModel,
                    onVolver = { navController.popBackStack() }
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
                    onContinuar = { navController.navigate(RutasApp.RegistroPasoSeguridad.ruta) }
                )
            }
            composable(RutasApp.RegistroPasoSeguridad.ruta) {
                PantallaRegistroPasoSeguridad(
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
                    reportesViewModel = reportesViewModel,
                    onAbrirCrearServicio = {
                        navController.navigate(RutasApp.ServicioEditor.crearRuta("crear", 0L))
                    },
                    onAbrirEditarServicio = { idOfertaServicio ->
                        navController.navigate(RutasApp.ServicioEditor.crearRuta("editar", idOfertaServicio))
                    },
                    onAbrirServicio = abrirDetalleConCarga,
                    onAbrirChat = { idChat ->
                        navController.navigate(RutasApp.ChatDetalle.crearRuta(idChat))
                    },
                    onAbrirAjustes = {
                        navController.navigate(RutasApp.Ajustes.ruta)
                    },
                    onAbrirValoraciones = {
                        navController.navigate(RutasApp.ValoracionesServicios.ruta)
                    },
                    onAbrirEditarPerfil = {
                        navController.navigate(RutasApp.PerfilEditar.ruta)
                    },
                    onAbrirDetalleReporte = { idReporte ->
                        navController.navigate(RutasApp.ReporteDetalle.crearRuta(idReporte))
                    },
                    onAbrirUbicacionRapida = {
                        navController.navigate(RutasApp.AjustesUbicacion.ruta)
                    },
                    onAbrirPremium = {
                        val destinoPremium = if (perfilViewModel.uiState.usuario?.tipoPerfil == TipoPerfil.PREMIUM) {
                            RutasApp.MenuPremium.ruta
                        } else {
                            RutasApp.PremiumBienvenida.ruta
                        }
                        navController.navigate(destinoPremium)
                    }
                )
            }
            composable(RutasApp.PremiumBienvenida.ruta) {
                val premiumViewModel: PremiumViewModel = viewModel(factory = factory)
                PantallaBienvenidaPremium(
                    viewModel = premiumViewModel,
                    onPremiumActivado = {
                        perfilViewModel.recargar()
                        navController.navigate(RutasApp.PremiumActivado.ruta) {
                            launchSingleTop = true
                        }
                    },
                    onIrAMenu = {
                        perfilViewModel.recargar()
                        navController.navigate(RutasApp.MenuPremium.ruta) {
                            popUpTo(RutasApp.PremiumBienvenida.ruta) { inclusive = true }
                        }
                    },
                    onVolver = {
                        perfilViewModel.recargar()
                        navController.popBackStack()
                    }
                )
            }
            composable(RutasApp.PremiumActivado.ruta) {
                val premiumViewModel: PremiumViewModel = viewModel(factory = factory)
                PantallaPremiumActivado(
                    viewModel = premiumViewModel,
                    onIniciarSesion = {
                        // El rol Premium viaja en el JWT; para que el backend lo reconozca
                        // (p. ej. activar 3 servicios) se fuerza un re-login que emite un token nuevo.
                        perfilViewModel.cerrarSesion()
                    },
                    onVolver = {
                        perfilViewModel.recargar()
                        navController.popBackStack()
                    }
                )
            }
            composable(RutasApp.MenuPremium.ruta) {
                val premiumViewModel: PremiumViewModel = viewModel(factory = factory)
                PantallaMenuPremium(
                    viewModel = premiumViewModel,
                    onAbrirHistorial = { navController.navigate(RutasApp.PremiumHistorialContactos.ruta) },
                    onAbrirEstadisticas = { navController.navigate(RutasApp.PremiumEstadisticas.ruta) },
                    onVolver = {
                        perfilViewModel.recargar()
                        navController.popBackStack()
                    }
                )
            }
            composable(RutasApp.PremiumHistorialContactos.ruta) {
                val premiumViewModel: PremiumViewModel = viewModel(factory = factory)
                PantallaHistorialContactosPremium(
                    viewModel = premiumViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.PremiumEstadisticas.ruta) {
                val premiumViewModel: PremiumViewModel = viewModel(factory = factory)
                PantallaEstadisticasPremium(
                    viewModel = premiumViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.PerfilEditar.ruta) {
                PantallaEditarPerfil(
                    viewModel = perfilViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.ValoracionesServicios.ruta) {
                PantallaValoracionesServicios(
                    viewModel = perfilViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(
                route = RutasApp.ReporteDetalle.ruta,
                arguments = listOf(navArgument("idReporte") { type = NavType.LongType })
            ) { backStackEntry ->
                val idReporte = backStackEntry.arguments?.getLong("idReporte") ?: 0L
                PantallaDetalleReporteModerador(
                    idReporte = idReporte,
                    viewModel = reportesViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.Ajustes.ruta) {
                PantallaAjustes(
                    onVolver = { navController.popBackStack() },
                    onAbrirSeguridad = { navController.navigate(RutasApp.AjustesSeguridad.ruta) },
                    onAbrirCuenta = { navController.navigate(RutasApp.AjustesCuenta.ruta) },
                    onAbrirUbicacion = { navController.navigate(RutasApp.AjustesUbicacion.ruta) },
                    onAbrirPreferencias = { navController.navigate(RutasApp.AjustesPreferencias.ruta) },
                    onCerrarSesion = {
                        perfilViewModel.cerrarSesion()
                        navController.navigate(RutasApp.Inicio.ruta) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    esModerador = perfilViewModel.uiState.usuario?.tipoPerfil == TipoPerfil.MODERADOR,
                    onAbrirBaneos = {
                        baneosViewModel.cargar()
                        navController.navigate(RutasApp.AjustesModerarBaneos.ruta)
                    }
                )
            }
            composable(RutasApp.AjustesPreferencias.ruta) {
                PantallaPreferencias(
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.AjustesModerarBaneos.ruta) {
                PantallaBaneos(
                    viewModel = baneosViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(RutasApp.AjustesSeguridad.ruta) {
                PantallaAjustesSeguridad(
                    viewModel = perfilViewModel,
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
                    onVolver = { navController.popBackStack() },
                    onCerrarSesion = { perfilViewModel.cerrarSesion() }
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
                    reportesViewModel = reportesViewModel,
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
                arguments = listOf(
                    navArgument("modo") { type = NavType.StringType },
                    navArgument("idOfertaServicio") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val modo = backStackEntry.arguments?.getString("modo").orEmpty()
                val idOfertaServicio = backStackEntry.arguments?.getLong("idOfertaServicio") ?: 0L
                PantallaEditorServicio(
                    modo = modo,
                    idOfertaServicio = idOfertaServicio,
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
                    reportesViewModel = reportesViewModel,
                    onEditarServicio = { idOfertaServicio ->
                        navController.navigate(RutasApp.ServicioEditor.crearRuta("editar", idOfertaServicio))
                    },
                    onContactarServicio = { idOferta, titulo, usernameTrab ->
                        abrirChatDesdeOferta(idOferta, titulo, usernameTrab)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // Modal de intermitencias de conexión
        if (mostrarModalIntermitencias && enPantallaPrincipal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Conexión intermitente",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Se están presentando intermitencias con los servicios de Contrabajo.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        BotonPrimario(
                            texto = "Aceptar",
                            onClick = {
                                mostrarModalIntermitencias = false
                                segundosSinConexion = 0
                            }
                        )
                    }
                }
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

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
private fun ShellPrincipal(
    principalViewModel: PrincipalViewModel,
    chatsViewModel: ChatsViewModel,
    perfilViewModel: PerfilViewModel,
    reportesViewModel: ReportesViewModel,
    onAbrirCrearServicio: () -> Unit,
    onAbrirEditarServicio: (Long) -> Unit,
    onAbrirServicio: (Long) -> Unit,
    onAbrirChat: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    onAbrirValoraciones: () -> Unit,
    onAbrirEditarPerfil: () -> Unit,
    onAbrirDetalleReporte: (Long) -> Unit,
    onAbrirUbicacionRapida: () -> Unit,
    onAbrirPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val esModerador = perfilViewModel.uiState.usuario?.tipoPerfil == TipoPerfil.MODERADOR
    val mostrarBotonPremium = perfilViewModel.uiState.usuario?.tipoPerfil in
        listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM)
    var rutaContenido by rememberSaveable(esModerador) {
        mutableStateOf(if (esModerador) RutasApp.ReportesModerador.ruta else RutasApp.Principal.ruta)
    }
    var desplazamientoHorizontal by remember { mutableStateOf(0f) }
    val rutaCentral = if (esModerador) RutasApp.ReportesModerador.ruta else RutasApp.Principal.ruta
    val rutasSwipe = remember(esModerador) {
        if (esModerador) {
            listOf(RutasApp.Perfil.ruta, RutasApp.ReportesModerador.ruta)
        } else {
            listOf(RutasApp.Perfil.ruta, rutaCentral, RutasApp.Chats.ruta)
        }
    }

    LaunchedEffect(esModerador) {
        if (rutaContenido == RutasApp.Principal.ruta && esModerador) {
            rutaContenido = RutasApp.ReportesModerador.ruta
        } else if (rutaContenido == RutasApp.Chats.ruta && esModerador) {
            rutaContenido = RutasApp.ReportesModerador.ruta
        } else if (rutaContenido == RutasApp.ReportesModerador.ruta && !esModerador) {
            rutaContenido = RutasApp.Principal.ruta
        }
    }

    LaunchedEffect(rutaContenido) {
        when (rutaContenido) {
            RutasApp.Principal.ruta -> chatsViewModel.recargar()
            RutasApp.Chats.ruta -> chatsViewModel.recargar()
            RutasApp.Perfil.ruta -> perfilViewModel.recargar()
            RutasApp.ReportesModerador.ruta -> reportesViewModel.recargar()
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
        modoModerador = esModerador,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        onAbrirPremium = onAbrirPremium,
                        mostrarBotonPremium = mostrarBotonPremium,
                        modifier = Modifier.padding(innerPadding)
                    )

                    RutasApp.Chats.ruta -> {
                        PantallaChats(
                            viewModel = chatsViewModel,
                            onAbrirChat = onAbrirChat,
                            esTrabajador = perfilViewModel.uiState.usuario?.tipoPerfil in
                                listOf(TipoPerfil.TRABAJADOR, TipoPerfil.PREMIUM),
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    RutasApp.Perfil.ruta -> PantallaPerfil(
                        viewModel = perfilViewModel,
                        onAbrirCrearServicio = onAbrirCrearServicio,
                        onAbrirEditarServicio = onAbrirEditarServicio,
                        onAbrirValoraciones = onAbrirValoraciones,
                        onEditarPerfil = onAbrirEditarPerfil,
                        onCerrarSesion = {
                            perfilViewModel.cerrarSesion()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    RutasApp.ReportesModerador.ruta -> PantallaReportesModerador(
                        viewModel = reportesViewModel,
                        onAbrirDetalleReporte = onAbrirDetalleReporte,
                        onAbrirAjustes = onAbrirAjustes,
                        modifier = Modifier.padding(innerPadding)
                    )

                    else -> PantallaPrincipal(
                        viewModel = principalViewModel,
                        onAbrirServicio = onAbrirServicio,
                        onAbrirAjustes = onAbrirAjustes,
                        onAbrirUbicacionRapida = onAbrirUbicacionRapida,
                        onAbrirPremium = onAbrirPremium,
                        mostrarBotonPremium = mostrarBotonPremium,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.crearTransicionEntrada(): EnterTransition {
    return when {
        routeEsLogin(targetState.destination.route) ->
            slideInVertically(
                animationSpec = tween(durationMillis = 300),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = tween(durationMillis = 250))
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
        routeEsLogin(initialState.destination.route) ->
            slideOutVertically(
                animationSpec = tween(durationMillis = 280),
                targetOffsetY = { it }
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
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
        routeEsLogin(targetState.destination.route) ->
            slideOutVertically(
                animationSpec = tween(durationMillis = 280),
                targetOffsetY = { it }
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
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
        base == RutasApp.RegistroPasoDos.ruta ||
        base == RutasApp.RegistroPasoSeguridad.ruta
}

private fun routeEsServicio(route: String?): Boolean {
    val base = route?.substringBefore("/")
    return base == RutasApp.Servicio.ruta.substringBefore("/")
}

private fun routeEsLogin(route: String?): Boolean {
    return route == RutasApp.Login.ruta || route == RutasApp.RecuperarCuenta.ruta
}

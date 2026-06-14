package com.movil.contrabajo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Theme raíz de Contrabajo.
 *
 * Lee las preferencias de [ControladorPreferenciasUi] (modo de tema, paleta de
 * color para daltonismo y escala de texto) y reconstruye el [MaterialTheme] de
 * forma reactiva. Además provee:
 *  - [LocalColoresContrabajo]: colores semánticos de marca (éxito, info, premium…).
 *  - [LocalControladorPreferenciasUi]: para que la pantalla de Accesibilidad
 *    pueda leer y modificar las preferencias.
 */
@Composable
fun ContrabajoTheme(
    controlador: ControladorPreferenciasUi,
    content: @Composable () -> Unit
) {
    val preferencias = controlador.estado

    val oscuro = when (preferencias.modoTema) {
        ModoTema.CLARO -> false
        ModoTema.OSCURO -> true
        ModoTema.SISTEMA -> isSystemInDarkTheme()
    }

    val colorScheme = esquemaColor(preferencias.paleta, oscuro)
    val coloresExtra = coloresContrabajo(preferencias.paleta, oscuro)
    val tipografia = escalarTipografia(preferencias.escalaTexto.factor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = !oscuro
            insetsController.isAppearanceLightNavigationBars = !oscuro
        }
    }

    CompositionLocalProvider(
        LocalControladorPreferenciasUi provides controlador,
        LocalColoresContrabajo provides coloresExtra
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = tipografia,
            content = content
        )
    }
}

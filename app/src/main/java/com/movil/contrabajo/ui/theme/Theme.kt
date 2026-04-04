package com.movil.contrabajo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Blanco,
    secondary = Turquesa,
    onSecondary = Blanco,
    tertiary = Coral,
    background = Arena,
    onBackground = Grafito,
    surface = Blanco,
    onSurface = Grafito,
    surfaceVariant = CianSuave,
    onSurfaceVariant = GrisTexto,
    secondaryContainer = CianSuave,
    onSecondaryContainer = AzulProfundo
)

private val DarkColorScheme = darkColorScheme(
    primary = Turquesa,
    onPrimary = Grafito,
    secondary = Coral,
    tertiary = CianSuave,
    background = Grafito,
    onBackground = Blanco,
    surface = ColorTokens.superficieOscura,
    onSurface = Blanco,
    surfaceVariant = ColorTokens.superficieOscuraSecundaria,
    onSurfaceVariant = CianSuave
)

private object ColorTokens {
    val superficieOscura = androidx.compose.ui.graphics.Color(0xFF163540)
    val superficieOscuraSecundaria = androidx.compose.ui.graphics.Color(0xFF214854)
}

@Composable
fun ContrabajoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

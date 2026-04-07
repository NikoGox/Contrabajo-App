package com.movil.contrabajo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AzulPetroleo,
    onPrimary = Blanco,
    secondary = TurquesaBrillante,
    onSecondary = Blanco,
    tertiary = CoralSuave,
    background = MentaNiebla,
    onBackground = Grafito,
    surface = Blanco,
    onSurface = Grafito,
    surfaceVariant = TurquesaPastel,
    onSurfaceVariant = GrisAcero,
    secondaryContainer = TurquesaSuave,
    onSecondaryContainer = AzulPetroleo,
    tertiaryContainer = AmarilloSuave,
    outline = GrisBorde
)

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

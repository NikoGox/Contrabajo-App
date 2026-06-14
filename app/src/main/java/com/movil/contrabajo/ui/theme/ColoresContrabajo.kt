package com.movil.contrabajo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores semánticos de Contrabajo que NO existen en el [ColorScheme] de Material
 * (estados de éxito/info/advertencia, marca Premium, navbar, burbujas de chat,
 * overlays de mapa, etc.).
 *
 * Antes estaban hardcodeados a lo largo de la app con literales `Color(0xFF…)`,
 * lo que impedía el modo oscuro y las paletas para daltonismo. Ahora se resuelven
 * por (paleta + claro/oscuro) y se exponen vía [LocalColoresContrabajo].
 */
data class ColoresContrabajo(
    val exito: Color,
    val onExito: Color,
    val exitoContenedor: Color,
    val onExitoContenedor: Color,
    val info: Color,
    val onInfo: Color,
    val infoContenedor: Color,
    val onInfoContenedor: Color,
    val advertencia: Color,
    val onAdvertencia: Color,
    val advertenciaContenedor: Color,
    val onAdvertenciaContenedor: Color,
    val premiumInicio: Color,
    val premiumMedio: Color,
    val premiumFin: Color,
    val premiumBrillo: Color,
    val premiumEstrella: Color,
    val onPremium: Color,
    val navbarFondo: Color,
    val navbarIconoInactivo: Color,
    val navbarSeleccion: Color,
    val navbarBrillo: Color,
    val mapaRelleno: Color,
    val mapaBorde: Color,
    val coral: Color,
    val onCoral: Color,
    val burbujaPropia: Color,
    val onBurbujaPropia: Color,
    val burbujaAjena: Color,
    val onBurbujaAjena: Color,
    val superficieAlterna: Color,
    val borde: Color,
    val sombra: Color
)

val LocalColoresContrabajo = staticCompositionLocalOf<ColoresContrabajo> {
    error("ColoresContrabajo no provisto. Envuelve la UI en ContrabajoTheme(controlador).")
}

// ─────────────────────────────────────────────────────────────────────────────
// Paleta Okabe-Ito (segura para daltonismo) — usada por las paletas accesibles.
// ─────────────────────────────────────────────────────────────────────────────
private val OI_Naranja = Color(0xFFE69F00)
private val OI_CelesteCielo = Color(0xFF56B4E9)
private val OI_VerdeAzulado = Color(0xFF009E73)
private val OI_Azul = Color(0xFF0072B2)
private val OI_Bermellon = Color(0xFFD55E00)
private val OI_PurpuraRojizo = Color(0xFFCC79A7)

// ─────────────────────────────────────────────────────────────────────────────
// ESQUEMAS MATERIAL (primary/secondary/tertiary/surfaces/error) por modo.
// ─────────────────────────────────────────────────────────────────────────────

private val EsquemaClaroBase = lightColorScheme(
    primary = AzulPetroleo,
    onPrimary = Blanco,
    secondary = TurquesaBrillante,
    onSecondary = Blanco,
    tertiary = CoralSuave,
    onTertiary = Blanco,
    background = MentaNiebla,
    onBackground = Grafito,
    surface = Blanco,
    onSurface = Grafito,
    surfaceVariant = TurquesaPastel,
    onSurfaceVariant = GrisAcero,
    secondaryContainer = TurquesaSuave,
    onSecondaryContainer = AzulPetroleo,
    tertiaryContainer = AmarilloSuave,
    onTertiaryContainer = Color(0xFF4A3A00),
    outline = GrisBorde,
    error = Color(0xFFB00020),
    onError = Blanco,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C)
)

private val EsquemaOscuroBase = darkColorScheme(
    primary = TurquesaBrillante,
    onPrimary = Color(0xFF00282E),
    secondary = Color(0xFF7FD7DE),
    onSecondary = Color(0xFF00282E),
    tertiary = CoralSuave,
    onTertiary = Color(0xFF3A1A0C),
    background = Color(0xFF0E1A1C),
    onBackground = Color(0xFFE6F2F2),
    surface = Color(0xFF0F2124),
    onSurface = Color(0xFFE6F2F2),
    surfaceVariant = Color(0xFF17343A),
    onSurfaceVariant = Color(0xFFB0C7CB),
    secondaryContainer = Color(0xFF11424D),
    onSecondaryContainer = Color(0xFFCFEFF1),
    tertiaryContainer = Color(0xFF5A3A2E),
    onTertiaryContainer = Color(0xFFFFE2D6),
    outline = Color(0xFF3A565C),
    error = Color(0xFFCF6679),
    onError = Color(0xFF1A0004),
    errorContainer = Color(0xFF5A1E22),
    onErrorContainer = Color(0xFFFFD9DC)
)

private val EsquemaAltoContrasteClaro = lightColorScheme(
    primary = Color(0xFF00343B),
    onPrimary = Blanco,
    secondary = Color(0xFF00525C),
    onSecondary = Blanco,
    tertiary = Color(0xFF7A3B00),
    onTertiary = Blanco,
    background = Blanco,
    onBackground = Color(0xFF000000),
    surface = Blanco,
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE6ECEC),
    onSurfaceVariant = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFFC9E6EA),
    onSecondaryContainer = Color(0xFF00242A),
    tertiaryContainer = Color(0xFFFFE0C2),
    onTertiaryContainer = Color(0xFF2E1500),
    outline = Color(0xFF000000),
    error = Color(0xFFC20012),
    onError = Blanco,
    errorContainer = Color(0xFFFFD6D6),
    onErrorContainer = Color(0xFF5A0008)
)

private val EsquemaAltoContrasteOscuro = darkColorScheme(
    primary = Color(0xFF00E5F0),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFFFD23C),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFFFFB060),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Blanco,
    surface = Color(0xFF0A0A0A),
    onSurface = Blanco,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFE6E6E6),
    secondaryContainer = Color(0xFF1F1F1F),
    onSecondaryContainer = Blanco,
    tertiaryContainer = Color(0xFF2A1A00),
    onTertiaryContainer = Color(0xFFFFE0C2),
    outline = Blanco,
    error = Color(0xFFFF6E6E),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF3A0000),
    onErrorContainer = Color(0xFFFFD6D6)
)

/** Aplica acentos seguros para daltonismo sobre un esquema base claro/oscuro. */
private fun ColorScheme.conAcentos(
    primary: Color,
    secondary: Color,
    tertiary: Color,
    error: Color,
    onAcento: Color
): ColorScheme = copy(
    primary = primary,
    onPrimary = onAcento,
    secondary = secondary,
    onSecondary = onAcento,
    tertiary = tertiary,
    onTertiary = onAcento,
    error = error,
    onError = onAcento,
    secondaryContainer = secondary.copy(alpha = 0.18f).compositar(this.surface),
    onSecondaryContainer = if (isLuminoso(this.surface)) primary else Blanco
)

fun esquemaColor(paleta: PaletaColor, oscuro: Boolean): ColorScheme = when (paleta) {
    PaletaColor.ESTANDAR ->
        if (oscuro) EsquemaOscuroBase else EsquemaClaroBase

    PaletaColor.ROJO_VERDE ->
        (if (oscuro) EsquemaOscuroBase else EsquemaClaroBase).conAcentos(
            primary = OI_Azul,
            secondary = OI_CelesteCielo,
            tertiary = OI_Naranja,
            error = OI_Bermellon,
            onAcento = Blanco
        )

    PaletaColor.AZUL_AMARILLO ->
        (if (oscuro) EsquemaOscuroBase else EsquemaClaroBase).conAcentos(
            primary = OI_VerdeAzulado,
            secondary = OI_PurpuraRojizo,
            tertiary = OI_Bermellon,
            error = Color(0xFFC20012),
            onAcento = Blanco
        )

    PaletaColor.ALTO_CONTRASTE ->
        if (oscuro) EsquemaAltoContrasteOscuro else EsquemaAltoContrasteClaro
}

// ─────────────────────────────────────────────────────────────────────────────
// COLORES EXTENDIDOS por modo.
// ─────────────────────────────────────────────────────────────────────────────

private val ExtendidoClaro = ColoresContrabajo(
    exito = Color(0xFF17A673),
    onExito = Blanco,
    exitoContenedor = Color(0xFFE8F5E9),
    onExitoContenedor = Color(0xFF0B5138),
    info = Color(0xFF1E88E5),
    onInfo = Blanco,
    infoContenedor = Color(0xFFE7F0FF),
    onInfoContenedor = Color(0xFF0B3A66),
    advertencia = Color(0xFFF5A623),
    onAdvertencia = Color(0xFF3A2A00),
    advertenciaContenedor = Color(0xFFFFF3D6),
    onAdvertenciaContenedor = Color(0xFF5A4200),
    premiumInicio = Color(0xFF0D5665),
    premiumMedio = Color(0xFF1A9BA8),
    premiumFin = Color(0xFF44CCD8),
    premiumBrillo = Color(0xFFD4F5F8),
    premiumEstrella = Color(0xFFFFC93C),
    onPremium = Blanco,
    navbarFondo = Color(0xFF29939E),
    navbarIconoInactivo = Color(0xFF324E50),
    navbarSeleccion = Blanco,
    navbarBrillo = Color(0xFF9AD9DD),
    mapaRelleno = Color(0x3319A1A8),
    mapaBorde = Color(0xFF0E8C94),
    coral = CoralSuave,
    onCoral = Blanco,
    burbujaPropia = Color(0xFF0D5B66),
    onBurbujaPropia = Blanco,
    burbujaAjena = Color(0xFFE8ECF0),
    onBurbujaAjena = Grafito,
    superficieAlterna = Color(0xFFF5F7FA),
    borde = GrisBorde,
    sombra = SombraPetroleo
)

private val ExtendidoOscuro = ColoresContrabajo(
    exito = Color(0xFF3DDC97),
    onExito = Color(0xFF00150C),
    exitoContenedor = Color(0xFF12362A),
    onExitoContenedor = Color(0xFFB6F2D6),
    info = Color(0xFF5AA9F0),
    onInfo = Color(0xFF001426),
    infoContenedor = Color(0xFF102A40),
    onInfoContenedor = Color(0xFFCFE5FF),
    advertencia = Color(0xFFFFC93C),
    onAdvertencia = Color(0xFF1A1400),
    advertenciaContenedor = Color(0xFF3A2E00),
    onAdvertenciaContenedor = Color(0xFFFFE8A3),
    premiumInicio = Color(0xFF1A9BA8),
    premiumMedio = Color(0xFF44CCD8),
    premiumFin = Color(0xFF7EE0EB),
    premiumBrillo = Color(0xFFC8EEF2),
    premiumEstrella = Color(0xFFFFC93C),
    onPremium = Color(0xFFE0F5F7),
    navbarFondo = Color(0xFF11424D),
    navbarIconoInactivo = Color(0xFF6BA3A8),
    navbarSeleccion = Blanco,
    navbarBrillo = Color(0xFF35E0F0),
    mapaRelleno = Color(0x3319A1A8),
    mapaBorde = Color(0xFF35E0F0),
    coral = CoralSuave,
    onCoral = Color(0xFF3A1A0C),
    burbujaPropia = Color(0xFF0E8894),
    onBurbujaPropia = Blanco,
    burbujaAjena = Color(0xFF1E2E31),
    onBurbujaAjena = Color(0xFFE6F2F2),
    superficieAlterna = Color(0xFF152528),
    borde = Color(0xFF2E474C),
    sombra = Color(0x66000000)
)

/** Sustituye los acentos semánticos por equivalentes seguros para daltonismo. */
private fun ColoresContrabajo.conAcentosAccesibles(
    exito: Color,
    info: Color,
    advertencia: Color,
    premiumInicio: Color,
    premiumFin: Color,
    estrella: Color,
    navbar: Color
): ColoresContrabajo = copy(
    exito = exito,
    info = info,
    advertencia = advertencia,
    premiumInicio = premiumInicio,
    premiumMedio = premiumFin,
    premiumFin = premiumFin,
    premiumBrillo = premiumFin,
    premiumEstrella = estrella,
    navbarFondo = navbar,
    navbarBrillo = premiumFin,
    mapaBorde = info
)

fun coloresContrabajo(paleta: PaletaColor, oscuro: Boolean): ColoresContrabajo {
    val base = if (oscuro) ExtendidoOscuro else ExtendidoClaro
    return when (paleta) {
        PaletaColor.ESTANDAR -> base
        PaletaColor.ROJO_VERDE -> base.conAcentosAccesibles(
            exito = OI_Azul,
            info = OI_CelesteCielo,
            advertencia = OI_Naranja,
            premiumInicio = OI_Azul,
            premiumFin = OI_CelesteCielo,
            estrella = OI_Naranja,
            navbar = OI_Azul
        )
        PaletaColor.AZUL_AMARILLO -> base.conAcentosAccesibles(
            exito = OI_VerdeAzulado,
            info = OI_PurpuraRojizo,
            advertencia = OI_Bermellon,
            premiumInicio = OI_VerdeAzulado,
            premiumFin = Color(0xFF5DE0B0),
            estrella = OI_Bermellon,
            navbar = OI_VerdeAzulado
        )
        PaletaColor.ALTO_CONTRASTE -> base.copy(
            exito = if (oscuro) Color(0xFF4DE9A0) else Color(0xFF0A6B3D),
            info = if (oscuro) Color(0xFF6CB6FF) else Color(0xFF0046A8),
            advertencia = if (oscuro) Color(0xFFFFC93C) else Color(0xFF8A5A00),
            borde = if (oscuro) Blanco else Color(0xFF000000)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilidades
// ─────────────────────────────────────────────────────────────────────────────

private fun isLuminoso(color: Color): Boolean =
    (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue) > 0.5

/** Composita un color con alpha sobre un fondo opaco (para contenedores tintados). */
private fun Color.compositar(fondo: Color): Color {
    val a = alpha
    return Color(
        red = red * a + fondo.red * (1 - a),
        green = green * a + fondo.green * (1 - a),
        blue = blue * a + fondo.blue * (1 - a),
        alpha = 1f
    )
}

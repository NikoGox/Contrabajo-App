package com.movil.contrabajo.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Preferencias de interfaz y accesibilidad del usuario.
 *
 * Se persisten en SharedPreferences (mismo patrón que `RemoteSessionStore`) y se
 * exponen de forma reactiva mediante [ControladorPreferenciasUi] para que el
 * theme se reconstruya al instante cuando el usuario cambia una opción.
 */

/** Modo de color de la app. */
enum class ModoTema { CLARO, OSCURO, SISTEMA }

/**
 * Paleta de color. Además de la estándar, ofrece paletas seguras para los
 * principales tipos de daltonismo, basadas en la paleta Okabe-Ito (estándar
 * científico de colores distinguibles para visión defectiva del color).
 */
enum class PaletaColor(val etiqueta: String, val descripcion: String) {
    ESTANDAR("Estándar", "Paleta original de Contrabajo."),
    ROJO_VERDE("Rojo-Verde", "Para protanopia/deuteranopia (dificultad rojo-verde)."),
    AZUL_AMARILLO("Azul-Amarillo", "Para tritanopia (dificultad azul-amarillo)."),
    ALTO_CONTRASTE("Alto contraste", "Máximo contraste para baja visión.")
}

/** Escala del tamaño de letra de toda la app. */
enum class EscalaTexto(val factor: Float, val etiqueta: String) {
    PEQUENO(0.85f, "Pequeño"),
    NORMAL(1.0f, "Normal"),
    GRANDE(1.15f, "Grande"),
    MUY_GRANDE(1.30f, "Muy grande")
}

data class PreferenciasUi(
    val modoTema: ModoTema = ModoTema.SISTEMA,
    val paleta: PaletaColor = PaletaColor.ESTANDAR,
    val escalaTexto: EscalaTexto = EscalaTexto.NORMAL
)

/**
 * Mantiene el estado reactivo de [PreferenciasUi] y lo persiste.
 * Se crea una instancia por proceso (en las Activities) leyendo el mismo archivo
 * de preferencias, por lo que cualquier cambio queda disponible en el próximo
 * arranque y en ambas Activities.
 */
class ControladorPreferenciasUi(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    var estado by mutableStateOf(cargar())
        private set

    private fun cargar(): PreferenciasUi = PreferenciasUi(
        modoTema = leerEnum(CLAVE_MODO, ModoTema.entries, ModoTema.SISTEMA),
        paleta = leerEnum(CLAVE_PALETA, PaletaColor.entries, PaletaColor.ESTANDAR),
        escalaTexto = leerEnum(CLAVE_ESCALA, EscalaTexto.entries, EscalaTexto.NORMAL)
    )

    fun cambiarModoTema(modo: ModoTema) {
        estado = estado.copy(modoTema = modo)
        prefs.edit().putString(CLAVE_MODO, modo.name).apply()
    }

    fun cambiarPaleta(paleta: PaletaColor) {
        estado = estado.copy(paleta = paleta)
        prefs.edit().putString(CLAVE_PALETA, paleta.name).apply()
    }

    fun cambiarEscalaTexto(escala: EscalaTexto) {
        estado = estado.copy(escalaTexto = escala)
        prefs.edit().putString(CLAVE_ESCALA, escala.name).apply()
    }

    private fun <T : Enum<T>> leerEnum(clave: String, valores: List<T>, porDefecto: T): T {
        val guardado = prefs.getString(clave, null) ?: return porDefecto
        return valores.firstOrNull { it.name == guardado } ?: porDefecto
    }

    companion object {
        private const val ARCHIVO = "contrabajo_ui_prefs"
        private const val CLAVE_MODO = "modo_tema"
        private const val CLAVE_PALETA = "paleta_color"
        private const val CLAVE_ESCALA = "escala_texto"
    }
}

/** Acceso al controlador de preferencias desde cualquier composable. */
val LocalControladorPreferenciasUi = staticCompositionLocalOf<ControladorPreferenciasUi> {
    error("ControladorPreferenciasUi no provisto. Envuelve la UI en ContrabajoTheme(controlador).")
}

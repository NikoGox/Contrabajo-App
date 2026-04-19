package com.movil.contrabajo.domain.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PrecioUtils {
    const val MIN_MONTO = 1
    const val MAX_MONTO = 10_000_000

    private val symbols = DecimalFormatSymbols(Locale("es", "CL")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val milesFormatter = DecimalFormat("#,###", symbols)

    fun requiereMonto(tipoPrecio: Int): Boolean = tipoPrecio != TipoPrecio.CONTACTAR

    fun esMontoValido(tipoPrecio: Int, montoBase: Int): Boolean {
        if (!requiereMonto(tipoPrecio)) return true
        return montoBase in MIN_MONTO..MAX_MONTO
    }

    fun normalizarMonto(tipoPrecio: Int, montoBase: Int): Int {
        return if (!requiereMonto(tipoPrecio)) 0 else montoBase.coerceIn(MIN_MONTO, MAX_MONTO)
    }

    fun formatearMonto(montoBase: Int): String = milesFormatter.format(montoBase.coerceAtLeast(0))

    fun construirPrecioTexto(tipoPrecio: Int, montoBase: Int): String {
        if (tipoPrecio == TipoPrecio.CONTACTAR) return "Contactar para saber precio"
        val montoTexto = "$${formatearMonto(normalizarMonto(tipoPrecio, montoBase))}"
        return when (tipoPrecio) {
            TipoPrecio.POR_HORA -> "$montoTexto/hora"
            TipoPrecio.DESDE -> "Desde $montoTexto"
            else -> montoTexto
        }
    }
}

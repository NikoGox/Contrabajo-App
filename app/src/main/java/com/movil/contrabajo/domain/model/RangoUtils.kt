package com.movil.contrabajo.domain.model

import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object EscalaRango {
    const val MIN_METROS = 20
    const val MAX_METROS = 50_000

    val valoresMetros: List<Int> = buildList {
        addAll(20..100 step 10)
        addAll(150..950 step 50)
        var metros = 1_000
        while (metros <= MAX_METROS) {
            add(metros)
            metros += 500
        }
    }

    fun normalizar(valorMetros: Int): Int = valorPorIndice(indiceMasCercano(valorMetros))

    fun indiceMasCercano(valorMetros: Int): Int {
        val objetivo = valorMetros.coerceAtLeast(MIN_METROS)
        return valoresMetros.indices.minByOrNull { indice ->
            (valoresMetros[indice] - objetivo).absoluteValue
        } ?: 0
    }

    fun valorPorIndice(indice: Int): Int {
        val seguro = indice.coerceIn(0, valoresMetros.lastIndex)
        return valoresMetros[seguro]
    }

    fun valorPorPosicionSlider(posicion: Float): Int {
        val indice = posicion.roundToInt().coerceIn(0, valoresMetros.lastIndex)
        return valorPorIndice(indice)
    }

    fun posicionSliderPorValor(valorMetros: Int): Float = indiceMasCercano(valorMetros).toFloat()

    fun formatear(valorMetros: Int): String {
        val valor = normalizar(valorMetros)
        return if (valor < 1_000) {
            "$valor m"
        } else {
            val km = valor / 1_000.0
            if (valor % 1_000 == 0) {
                "${km.toInt()} km"
            } else {
                String.format("%.1f km", km)
            }
        }
    }
}

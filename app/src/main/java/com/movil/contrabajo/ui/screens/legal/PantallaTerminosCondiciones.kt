package com.movil.contrabajo.ui.screens.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Pantalla que muestra los Términos y Condiciones de Uso de Contrabajo.
 *
 * El texto se lee desde el archivo empaquetado en la app
 * (`assets/terminos_y_condiciones.md`), por lo que NO requiere conexión a
 * internet ni abrir enlaces externos. Se renderiza con un parser ligero de
 * Markdown (títulos, negritas, listas y tablas simples).
 */
private const val ARCHIVO_TERMINOS = "terminos_y_condiciones.md"

@Composable
fun PantallaTerminosCondiciones(onCerrar: () -> Unit) {
    val context = LocalContext.current
    val bloques = remember {
        val texto = runCatching {
            context.assets.open(ARCHIVO_TERMINOS).bufferedReader().use { it.readText() }
        }.getOrElse { "No se pudo cargar el documento de Términos y Condiciones." }
        parsearMarkdown(texto)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior con botón de volver
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCerrar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "Términos y Condiciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(bloques) { bloque -> BloqueMarkdown(bloque) }
            }
        }
    }
}

@Composable
private fun BloqueMarkdown(bloque: BloqueTC) {
    when (bloque) {
        is BloqueTC.Titulo -> {
            val estilo = when (bloque.nivel) {
                1 -> MaterialTheme.typography.titleLarge
                2 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            }
            Spacer(Modifier.height(if (bloque.nivel == 1) 10.dp else 4.dp))
            Text(
                text = bloque.texto,
                style = estilo,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        is BloqueTC.Parrafo -> {
            Text(
                text = parseInlineBold(bloque.texto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        is BloqueTC.Item -> {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bloque.vinneta + "  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = parseInlineBold(bloque.texto),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        is BloqueTC.FilaTabla -> {
            Surface(
                color = if (bloque.esCabecera) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    bloque.celdas.forEachIndexed { i, celda ->
                        Text(
                            text = parseInlineBold(celda),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (bloque.esCabecera) FontWeight.Bold else FontWeight.Normal,
                            color = if (bloque.esCabecera) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = if (i < bloque.celdas.size - 1) 8.dp else 0.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Modelo de bloques ──────────────────────────────────────────────────────
private sealed interface BloqueTC {
    data class Titulo(val texto: String, val nivel: Int) : BloqueTC
    data class Parrafo(val texto: String) : BloqueTC
    data class Item(val vinneta: String, val texto: String) : BloqueTC
    data class FilaTabla(val celdas: List<String>, val esCabecera: Boolean) : BloqueTC
}

// ── Parser de Markdown (ligero) ─────────────────────────────────────────────
private fun parsearMarkdown(md: String): List<BloqueTC> {
    val bloques = mutableListOf<BloqueTC>()
    val lineas = md.replace("\r\n", "\n").split("\n")
    var i = 0
    var cabeceraTablaPendiente = false

    fun desescapar(s: String): String = s.replace(Regex("""\\([\\`*_{}\[\]()#+\-.!|])"""), "$1")

    while (i < lineas.size) {
        val cruda = lineas[i]
        val l = cruda.trim()

        if (l.isEmpty()) { i++; cabeceraTablaPendiente = false; continue }

        // Títulos
        if (l.startsWith("## ")) {
            bloques += BloqueTC.Titulo(desescapar(l.removePrefix("## ").replace("**", "").trim()), 2)
            i++; continue
        }
        if (l.startsWith("# ")) {
            bloques += BloqueTC.Titulo(desescapar(l.removePrefix("# ").replace("**", "").trim()), 1)
            i++; continue
        }

        // Tablas (líneas que empiezan con |)
        if (l.startsWith("|")) {
            // separador |---|---| -> marca que la fila previa era cabecera
            if (Regex("""^\|?[\s:|-]+\|?$""").matches(l) && l.contains("-")) {
                if (bloques.isNotEmpty() && bloques.last() is BloqueTC.FilaTabla) {
                    val ult = bloques.removeAt(bloques.size - 1) as BloqueTC.FilaTabla
                    bloques += ult.copy(esCabecera = true)
                }
                i++; continue
            }
            var s = l
            if (s.startsWith("|")) s = s.substring(1)
            if (s.endsWith("|")) s = s.substring(0, s.length - 1)
            val celdas = s.split("|").map { desescapar(it.trim()) }
            bloques += BloqueTC.FilaTabla(celdas, esCabecera = false)
            i++; continue
        }

        // Listas: "a)", "1.", "1)", "-", "*"
        val mItem = Regex("""^([a-zA-Z]\)|\d+[.\)]|[-*])\s+(.*)$""").find(l)
        if (mItem != null && !l.startsWith("**")) {
            val marca = mItem.groupValues[1]
            val vinneta = if (marca == "-" || marca == "*") "•" else marca
            bloques += BloqueTC.Item(vinneta, desescapar(mItem.groupValues[2]))
            i++; continue 
        }

        // Párrafo normal (puede llevar **negrita**, la resolvemos al render)
        bloques += BloqueTC.Parrafo(desescapar(l))
        i++
    }
    return bloques
}

/** Convierte `**texto**` en negrita dentro de una AnnotatedString. */
private fun parseInlineBold(texto: String): AnnotatedString = buildAnnotatedString {
    val re = Regex("""\*\*(.+?)\*\*""")
    var last = 0
    for (m in re.findAll(texto)) {
        if (m.range.first > last) append(texto.substring(last, m.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[1]) }
        last = m.range.last + 1
    }
    if (last < texto.length) append(texto.substring(last))
}

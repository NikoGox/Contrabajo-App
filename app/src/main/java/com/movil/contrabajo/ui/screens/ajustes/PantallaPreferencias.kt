package com.movil.contrabajo.ui.screens.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.contrabajo.ui.components.EncabezadoPantalla
import com.movil.contrabajo.ui.components.PantallaBase
import com.movil.contrabajo.ui.components.TarjetaBase
import com.movil.contrabajo.ui.theme.EscalaTexto
import com.movil.contrabajo.ui.theme.LocalControladorPreferenciasUi
import com.movil.contrabajo.ui.theme.ModoTema
import com.movil.contrabajo.ui.theme.PaletaColor
import com.movil.contrabajo.ui.theme.esquemaColor

/**
 * Pantalla de Preferencias / Accesibilidad.
 *
 * Permite:
 *  - Activar/desactivar el modo oscuro (o seguir el sistema).
 *  - Elegir una paleta de color segura para distintos tipos de daltonismo.
 *  - Ajustar el tamaño de la letra de toda la app.
 *
 * Lee y modifica el estado vía [LocalControladorPreferenciasUi]; el theme se
 * reconstruye al instante con cada cambio.
 */
@Composable
fun PantallaPreferencias(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controlador = LocalControladorPreferenciasUi.current
    val preferencias = controlador.estado
    val sistemaOscuro = isSystemInDarkTheme()
    val seguirSistema = preferencias.modoTema == ModoTema.SISTEMA
    val oscuroEfectivo = when (preferencias.modoTema) {
        ModoTema.CLARO -> false
        ModoTema.OSCURO -> true
        ModoTema.SISTEMA -> sistemaOscuro
    }

    PantallaBase(modifier = modifier, mostrarFondo = false) {
        BarraSuperiorAjustes(
            titulo = "Accesibilidad",
            onVolver = onVolver,
            iconoDerecha = Icons.Filled.Accessibility
        )

        // ── Apariencia / Modo oscuro ──
        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Apariencia",
                subtitulo = "Ajusta el tema de la aplicación."
            )

            FilaSwitch(
                titulo = "Modo oscuro",
                subtitulo = if (seguirSistema) {
                    "Controlado por el sistema"
                } else {
                    "Usa una paleta oscura para reducir el brillo"
                },
                checked = oscuroEfectivo,
                enabled = !seguirSistema,
                onCheckedChange = { activado ->
                    controlador.cambiarModoTema(if (activado) ModoTema.OSCURO else ModoTema.CLARO)
                }
            )

            FilaSwitch(
                titulo = "Seguir el sistema",
                subtitulo = "Activa el modo oscuro según la configuración del dispositivo",
                checked = seguirSistema,
                onCheckedChange = { activado ->
                    controlador.cambiarModoTema(
                        if (activado) ModoTema.SISTEMA
                        else if (sistemaOscuro) ModoTema.OSCURO else ModoTema.CLARO
                    )
                }
            )
        }

        // ── Paleta de color (daltonismo) ──
        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Paleta de color",
                subtitulo = "Elige una paleta pensada para distintos tipos de daltonismo."
            )
            PaletaColor.entries.forEach { paleta ->
                OpcionPaleta(
                    paleta = paleta,
                    oscuro = oscuroEfectivo,
                    seleccionada = preferencias.paleta == paleta,
                    onSeleccionar = { controlador.cambiarPaleta(paleta) }
                )
            }
        }

        // ── Tamaño de letra ──
        TarjetaBase {
            EncabezadoPantalla(
                titulo = "Tamaño de letra",
                subtitulo = "Aumenta el tamaño del texto para mejorar la lectura."
            )
            EscalaTexto.entries.forEach { escala ->
                OpcionEscala(
                    escala = escala,
                    seleccionada = preferencias.escalaTexto == escala,
                    onSeleccionar = { controlador.cambiarEscalaTexto(escala) }
                )
            }
            // Vista previa que refleja la escala activa (la tipografía ya está escalada).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Vista previa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Así se verá el texto dentro de la app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaSwitch(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.78f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun OpcionPaleta(
    paleta: PaletaColor,
    oscuro: Boolean,
    seleccionada: Boolean,
    onSeleccionar: () -> Unit
) {
    val esquema = esquemaColor(paleta, oscuro)
    OpcionContenedor(seleccionada = seleccionada, onSeleccionar = onSeleccionar) {
        RadioButton(selected = seleccionada, onClick = onSeleccionar)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = paleta.etiqueta,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = paleta.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Muestra(esquema.primary)
            Muestra(esquema.secondary)
            Muestra(esquema.tertiary)
            Muestra(esquema.error)
        }
    }
}

@Composable
private fun OpcionEscala(
    escala: EscalaTexto,
    seleccionada: Boolean,
    onSeleccionar: () -> Unit
) {
    OpcionContenedor(seleccionada = seleccionada, onSeleccionar = onSeleccionar) {
        RadioButton(selected = seleccionada, onClick = onSeleccionar)
        Text(
            text = escala.etiqueta,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun OpcionContenedor(
    seleccionada: Boolean,
    onSeleccionar: () -> Unit,
    contenido: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (seleccionada) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .clickable(onClick = onSeleccionar)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = contenido
    )
}

@Composable
private fun Muestra(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                CircleShape
            )
    )
}

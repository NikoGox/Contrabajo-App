package com.movil.contrabajo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 44.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
)

/**
 * Devuelve la tipografía base escalada por [factor] (accesibilidad: tamaño de letra).
 * Escala fontSize y lineHeight de los 15 estilos estándar de Material 3.
 */
fun escalarTipografia(factor: Float): Typography {
    if (factor == 1f) return Typography

    fun TextStyle.escalar(): TextStyle = copy(
        fontSize = if (fontSize.isSpecified) fontSize * factor else fontSize,
        lineHeight = if (lineHeight.isSpecified) lineHeight * factor else lineHeight
    )

    return Typography(
        displayLarge = Typography.displayLarge.escalar(),
        displayMedium = Typography.displayMedium.escalar(),
        displaySmall = Typography.displaySmall.escalar(),
        headlineLarge = Typography.headlineLarge.escalar(),
        headlineMedium = Typography.headlineMedium.escalar(),
        headlineSmall = Typography.headlineSmall.escalar(),
        titleLarge = Typography.titleLarge.escalar(),
        titleMedium = Typography.titleMedium.escalar(),
        titleSmall = Typography.titleSmall.escalar(),
        bodyLarge = Typography.bodyLarge.escalar(),
        bodyMedium = Typography.bodyMedium.escalar(),
        bodySmall = Typography.bodySmall.escalar(),
        labelLarge = Typography.labelLarge.escalar(),
        labelMedium = Typography.labelMedium.escalar(),
        labelSmall = Typography.labelSmall.escalar()
    )
}

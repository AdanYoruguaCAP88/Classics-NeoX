package com.classicsneox.mahjong.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Dos familias con roles distintos, no una sola por defecto:
 * - Serif para todo lo que "es" una ficha (el glifo tallado) y para
 *   números protagonistas (puntaje grande). Evoca algo grabado/tallado.
 * - Sans para el HUD y cualquier texto de interfaz (botones, contadores
 *   chicos): tiene que leerse rápido, no ser parte de la escenografía.
 *
 * Se usan las familias del sistema (serif/sans-serif) en vez de bajar
 * tipografías propias, para no depender de assets adicionales; si más
 * adelante CLASSICS NEOX define una tipografía de marca, sólo hay que
 * reemplazar [MahjongType.tileGlyph] y compañía acá.
 */
object MahjongType {
    private val serif = FontFamily.Serif
    private val sans = FontFamily.SansSerif

    val tileGlyph = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
    val tileGlyphSmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

    val scoreDisplay = TextStyle(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = 0.sp)
    val screenTitle = TextStyle(fontFamily = serif, fontWeight = FontWeight.Bold, fontSize = 26.sp)

    val hudLabel = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.2.sp)
    val hudValue = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    val body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp)
    val buttonLabel = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
}

/** Typography de Material3 derivada de [MahjongType], para los pocos componentes M3 que se usan tal cual. */
val MahjongTypography = Typography(
    headlineMedium = MahjongType.screenTitle,
    bodyLarge = MahjongType.body,
    labelLarge = MahjongType.buttonLabel,
)

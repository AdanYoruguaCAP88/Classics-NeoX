package com.classicsneox.mahjong.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta pensada desde el objeto real, no desde la paleta por defecto de
 * Material: un tablero de mahjong es fieltro oscuro, fichas de hueso/marfil
 * y detalles lacados en rojo y dorado. Nada de azul/púrpura genérico.
 */
object MahjongPalette {
    // Fondo: "fieltro" verde-tinta oscuro de una mesa de juego.
    val TableFeltDark = Color(0xFF122620)
    val TableFeltDeeper = Color(0xFF0B1A16)

    // Fichas: hueso/marfil cálido, con una sombra de borde para dar volumen.
    val TileIvory = Color(0xFFF3ECDD)
    val TileIvoryShadow = Color(0xFFD8CBA9)
    val TileEdge = Color(0xFFBFAF87)

    // Tinta para los glifos grabados en la ficha.
    val InkBlack = Color(0xFF211D16)
    val InkRed = Color(0xFFA23B2E)   // círculos y dragón rojo
    val InkGreen = Color(0xFF3E6B52) // bambú y dragón verde

    // Detalles lacados: acento principal (selección, botones primarios).
    val LacquerRed = Color(0xFFB1432F)
    val LacquerRedDark = Color(0xFF7C2C1F)

    // Detalle dorado: racha, puntaje, victoria.
    val GoldLeaf = Color(0xFFC9A227)
    val GoldLeafBright = Color(0xFFE6C34A)

    // Estados.
    val FreeTileGlow = Color(0xFFE6C34A)
    val SelectedGlow = Color(0xFFEFA23A)
    val LockedOverlay = Color(0xCC0B1A16)
    val TextOnFelt = Color(0xFFEFE7D6)
    val TextOnFeltMuted = Color(0xFFB9AF9A)
}

package com.classicsneox.mahjong.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * El juego siempre se ve como una mesa de fieltro oscuro con fichas de
 * marfil, de día o de noche: no tiene una variante "clara" tradicional
 * (igual que un tablero físico no cambia de color con el modo del
 * teléfono). [isSystemInDarkTheme] sólo se usa para decidir el matiz del
 * fieltro, no para saltar a una paleta clara genérica.
 */
private val NeoXMahjongColorScheme = darkColorScheme(
    primary = MahjongPalette.LacquerRed,
    onPrimary = MahjongPalette.TileIvory,
    secondary = MahjongPalette.GoldLeaf,
    onSecondary = MahjongPalette.InkBlack,
    background = MahjongPalette.TableFeltDark,
    onBackground = MahjongPalette.TextOnFelt,
    surface = MahjongPalette.TileIvory,
    onSurface = MahjongPalette.InkBlack,
    error = MahjongPalette.LacquerRedDark,
)

@Composable
fun NeoXMahjongTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeoXMahjongColorScheme,
        typography = MahjongTypography,
        content = content,
    )
}

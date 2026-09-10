package com.classicsneox.mahjong.ui

import androidx.compose.ui.graphics.Color
import com.classicsneox.mahjong.core.model.Dragon
import com.classicsneox.mahjong.core.model.Flower
import com.classicsneox.mahjong.core.model.Season
import com.classicsneox.mahjong.core.model.Suit
import com.classicsneox.mahjong.core.model.TileType
import com.classicsneox.mahjong.core.model.Wind
import com.classicsneox.mahjong.ui.theme.MahjongPalette

/**
 * En vez de necesitar un set de imágenes (asset pipeline, distintas
 * densidades, etc.), las caras de las fichas se dibujan con los glifos
 * reales del bloque Unicode "Mahjong Tiles" (U+1F000-U+1F02B): existe un
 * carácter para cada ficha del set estándar. Esto da una cara auténtica,
 * vectorial (escala perfecto a cualquier tamaño de ficha) y sin un solo
 * asset binario que mantener. El costo es depender de que la fuente del
 * sistema los sepa dibujar -algunos Android más viejos podrían mostrar un
 * cuadrado "tofu"-, por eso cada ficha también lleva [shortLabel] como
 * respaldo textual (ver [TileType.shortLabel]) que la UI puede mostrar
 * debajo del glifo en tamaños de fuente chicos si hace falta.
 *
 * Códigos verificados contra la tabla oficial del bloque (Unicode 5.1+):
 * vientos 1F000-1F003, dragones 1F004-1F006, caracteres 1F007-1F00F,
 * bambúes 1F010-1F018, círculos 1F019-1F021, flores 1F022-1F025,
 * estaciones 1F026-1F029.
 */
object TileGlyphs {

    private fun glyph(codePoint: Int): String = String(Character.toChars(codePoint))

    fun glyphFor(type: TileType): String = when (type) {
        is TileType.Suited -> glyph(
            when (type.suit) {
                Suit.CHARACTERS -> 0x1F007 + (type.rank - 1)
                Suit.BAMBOO -> 0x1F010 + (type.rank - 1)
                Suit.CIRCLES -> 0x1F019 + (type.rank - 1)
            },
        )
        is TileType.Honor -> glyph(
            when (type.wind) {
                Wind.EAST -> 0x1F000
                Wind.SOUTH -> 0x1F001
                Wind.WEST -> 0x1F002
                Wind.NORTH -> 0x1F003
            },
        )
        is TileType.DragonTile -> glyph(
            when (type.dragon) {
                Dragon.RED -> 0x1F004
                Dragon.GREEN -> 0x1F005
                Dragon.WHITE -> 0x1F006
            },
        )
        is TileType.FlowerTile -> glyph(
            when (type.flower) {
                Flower.PLUM -> 0x1F022
                Flower.ORCHID -> 0x1F023
                Flower.BAMBOO_FLOWER -> 0x1F024
                Flower.CHRYSANTHEMUM -> 0x1F025
            },
        )
        is TileType.SeasonTile -> glyph(
            when (type.season) {
                Season.SPRING -> 0x1F026
                Season.SUMMER -> 0x1F027
                Season.AUTUMN -> 0x1F028
                Season.WINTER -> 0x1F029
            },
        )
    }

    /** Color de tinta del glifo, agrupado por familia (igual que en una ficha física). */
    fun inkColorFor(type: TileType): Color = when (type) {
        is TileType.Suited -> when (type.suit) {
            Suit.CIRCLES -> MahjongPalette.InkRed
            Suit.BAMBOO -> MahjongPalette.InkGreen
            Suit.CHARACTERS -> MahjongPalette.InkBlack
        }
        is TileType.DragonTile -> when (type.dragon) {
            Dragon.RED -> MahjongPalette.InkRed
            Dragon.GREEN -> MahjongPalette.InkGreen
            Dragon.WHITE -> MahjongPalette.InkBlack
        }
        is TileType.Honor -> MahjongPalette.InkBlack
        is TileType.FlowerTile -> MahjongPalette.InkGreen
        is TileType.SeasonTile -> MahjongPalette.InkRed
    }
}

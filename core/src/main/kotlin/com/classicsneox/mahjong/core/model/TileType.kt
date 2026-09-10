package com.classicsneox.mahjong.core.model

enum class Suit { BAMBOO, CIRCLES, CHARACTERS }

enum class Wind { EAST, SOUTH, WEST, NORTH }

enum class Dragon { RED, GREEN, WHITE }

enum class Flower { PLUM, ORCHID, CHRYSANTHEMUM, BAMBOO_FLOWER }

enum class Season { SPRING, SUMMER, AUTUMN, WINTER }

/**
 * Identidad "de cara" de una ficha. No confundir con [com.classicsneox.mahjong.core.model.Tile],
 * que es la ficha física puesta en una posición concreta del tablero.
 *
 * Regla de emparejamiento (ver [matches]): todo es igualdad estricta,
 * EXCEPTO Flor-con-Flor y Estación-con-Estación, que emparejan entre sí
 * aunque sean variantes distintas. Esa es la regla especial clásica del
 * Mahjong Solitario (a diferencia del Mahjong de mesa/competitivo).
 */
sealed class TileType {
    data class Suited(val suit: Suit, val rank: Int) : TileType() {
        init { require(rank in 1..9) { "El rango de una ficha de palo debe estar entre 1 y 9" } }
    }
    data class Honor(val wind: Wind) : TileType()
    data class DragonTile(val dragon: Dragon) : TileType()
    data class FlowerTile(val flower: Flower) : TileType()
    data class SeasonTile(val season: Season) : TileType()

    /** Etiqueta corta usada para depuración, tests y como respaldo textual en la UI. */
    fun shortLabel(): String = when (this) {
        is Suited -> "$rank${suit.glyphSuffix()}"
        is Honor -> wind.name.first().toString()
        is DragonTile -> dragon.name.first().toString()
        is FlowerTile -> "F${flower.ordinal + 1}"
        is SeasonTile -> "E${season.ordinal + 1}"
    }

    companion object {
        private fun Suit.glyphSuffix(): String = when (this) {
            Suit.BAMBOO -> "B"
            Suit.CIRCLES -> "C"
            Suit.CHARACTERS -> "M"
        }
    }
}

/**
 * Determina si dos fichas se consideran "la misma" a los efectos de
 * poder retirarlas juntas del tablero.
 */
fun TileType.matches(other: TileType): Boolean = when {
    this is TileType.FlowerTile && other is TileType.FlowerTile -> true
    this is TileType.SeasonTile && other is TileType.SeasonTile -> true
    else -> this == other
}

/**
 * Construye el mazo estándar de 144 fichas de Mahjong:
 * 3 palos x 9 rangos x 4 copias (108) + 4 vientos x 4 copias (16) +
 * 3 dragones x 4 copias (12) + 4 flores únicas (4) + 4 estaciones únicas (4) = 144.
 */
fun standardTileSet(): List<TileType> = buildList {
    for (suit in Suit.entries) {
        for (rank in 1..9) {
            repeat(4) { add(TileType.Suited(suit, rank)) }
        }
    }
    for (wind in Wind.entries) repeat(4) { add(TileType.Honor(wind)) }
    for (dragon in Dragon.entries) repeat(4) { add(TileType.DragonTile(dragon)) }
    for (flower in Flower.entries) add(TileType.FlowerTile(flower))
    for (season in Season.entries) add(TileType.SeasonTile(season))
}

/** Cantidad total de fichas de un set estándar. Usado para validar layouts. */
const val STANDARD_TILE_COUNT = 144

/**
 * Codifica un [TileType] como texto plano compacto, pensado para
 * persistencia. Se eligió un formato de texto propio -en vez de delegar
 * en kotlinx.serialization directamente sobre esta jerarquía sellada- para
 * que `:core` no dependa de ninguna librería externa (ver README, sección
 * de decisiones de arquitectura). Cualquier motor de serialización de más
 * arriba (JSON, DataStore, lo que sea) sólo necesita saber guardar un
 * String.
 *
 * Formato: "S:<PALO>:<rango>" | "H:<VIENTO>" | "D:<DRAGON>" | "F:<FLOR>" | "T:<ESTACION>"
 */
fun TileType.encode(): String = when (this) {
    is TileType.Suited -> "S:${suit.name}:$rank"
    is TileType.Honor -> "H:${wind.name}"
    is TileType.DragonTile -> "D:${dragon.name}"
    is TileType.FlowerTile -> "F:${flower.name}"
    is TileType.SeasonTile -> "T:${season.name}"
}

/** Contraparte de [encode]. Lanza [IllegalArgumentException] ante texto que no reconoce. */
fun decodeTileType(encoded: String): TileType {
    val parts = encoded.split(":")
    return when (parts.getOrNull(0)) {
        "S" -> TileType.Suited(Suit.valueOf(parts[1]), parts[2].toInt())
        "H" -> TileType.Honor(Wind.valueOf(parts[1]))
        "D" -> TileType.DragonTile(Dragon.valueOf(parts[1]))
        "F" -> TileType.FlowerTile(Flower.valueOf(parts[1]))
        "T" -> TileType.SeasonTile(Season.valueOf(parts[1]))
        else -> throw IllegalArgumentException("Texto de ficha irreconocible: '$encoded'")
    }
}

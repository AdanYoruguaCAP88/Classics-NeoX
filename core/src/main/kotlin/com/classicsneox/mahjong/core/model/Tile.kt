package com.classicsneox.mahjong.core.model

/** Identificador estable de una ficha física a lo largo de toda la partida. */
@JvmInline
value class TileId(val value: Int)

/**
 * Una ficha física del tablero: combina una identidad de juego ([type])
 * con el lugar donde está apoyada ([position]). El [id] no cambia nunca
 * durante la partida (ni siquiera al reordenar por un reshuffle), lo cual
 * simplifica animaciones y el manejo de selección en la capa de UI.
 */
data class Tile(
    val id: TileId,
    val type: TileType,
    val position: Position,
)

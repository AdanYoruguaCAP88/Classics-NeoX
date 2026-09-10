package com.classicsneox.mahjong.core.engine

import com.classicsneox.mahjong.core.model.Tile
import com.classicsneox.mahjong.core.model.TileId

enum class GameStatus { PLAYING, PAUSED, WON, LOST }

/**
 * Foto inmutable y completa del estado de la partida en un instante dado.
 * Es lo único que la capa de UI necesita leer: no conoce el motor, sólo
 * este snapshot. Se recalcula bajo demanda (el tablero es chico: 144
 * fichas como máximo), así que no hay riesgo de quedar desactualizado.
 */
data class GameState(
    val layoutId: String,
    val layoutDisplayName: String,
    /** Fichas que siguen en el tablero (las retiradas no aparecen más). */
    val tiles: List<Tile>,
    val freeTileIds: Set<TileId>,
    val selectedTileId: TileId?,
    val status: GameStatus,
    val score: Int,
    val movesCount: Int,
    val matchesCount: Int,
    val hintsUsed: Int,
    val shufflesUsed: Int,
    val currentStreak: Int,
    val elapsedSeconds: Long,
    val canUndo: Boolean,
    val canShuffle: Boolean,
    val hasAvailableMove: Boolean,
    val totalTileCount: Int,
) {
    val remainingTileCount: Int get() = tiles.size
    val isFinished: Boolean get() = status == GameStatus.WON || status == GameStatus.LOST
}

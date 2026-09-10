package com.classicsneox.mahjong.ui

import androidx.compose.ui.graphics.Color
import com.classicsneox.mahjong.core.engine.GameState
import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.model.TileId

/** Una ficha, ya lista para dibujar: nada de lógica de juego de este lado. */
data class TileUiModel(
    val id: TileId,
    val glyph: String,
    val fallbackLabel: String,
    val inkColor: Color,
    val gridX: Int,
    val gridY: Int,
    val gridZ: Int,
    val isFree: Boolean,
    val isSelected: Boolean,
    val isHinted: Boolean,
)

/**
 * Proyección completa del estado del motor a algo consumible por
 * Compose. `MahjongViewModel` es la única que construye instancias de
 * esto (vía [from]); las pantallas sólo la leen.
 */
data class MahjongUiState(
    val tiles: List<TileUiModel>,
    val totalTileCount: Int,
    val remainingTileCount: Int,
    val score: Int,
    val streak: Int,
    val elapsedSeconds: Long,
    val hintsUsed: Int,
    val shufflesUsed: Int,
    val canUndo: Boolean,
    val canShuffle: Boolean,
    val hasAvailableMove: Boolean,
    val status: GameStatus,
) {
    val isWon: Boolean get() = status == GameStatus.WON
    val isLost: Boolean get() = status == GameStatus.LOST
    val isPaused: Boolean get() = status == GameStatus.PAUSED

    companion object {
        fun from(state: GameState, hintedPair: Pair<TileId, TileId>?): MahjongUiState = MahjongUiState(
            tiles = state.tiles.map { tile ->
                TileUiModel(
                    id = tile.id,
                    glyph = TileGlyphs.glyphFor(tile.type),
                    fallbackLabel = tile.type.shortLabel(),
                    inkColor = TileGlyphs.inkColorFor(tile.type),
                    gridX = tile.position.x,
                    gridY = tile.position.y,
                    gridZ = tile.position.z,
                    isFree = tile.id in state.freeTileIds,
                    isSelected = tile.id == state.selectedTileId,
                    isHinted = tile.id == hintedPair?.first || tile.id == hintedPair?.second,
                )
                // capas bajas primero: así una ficha de una capa superior se dibuja
                // encima de las que tiene debajo, sin depender del z-index de Compose.
            }.sortedWith(compareBy({ it.gridZ }, { it.gridY }, { it.gridX })),
            totalTileCount = state.totalTileCount,
            remainingTileCount = state.remainingTileCount,
            score = state.score,
            streak = state.currentStreak,
            elapsedSeconds = state.elapsedSeconds,
            hintsUsed = state.hintsUsed,
            shufflesUsed = state.shufflesUsed,
            canUndo = state.canUndo,
            canShuffle = state.canShuffle,
            hasAvailableMove = state.hasAvailableMove,
            status = state.status,
        )
    }
}

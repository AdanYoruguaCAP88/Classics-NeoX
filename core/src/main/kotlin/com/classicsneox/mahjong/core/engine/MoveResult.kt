package com.classicsneox.mahjong.core.engine

import com.classicsneox.mahjong.core.model.TileId

/**
 * Resultado de llamar a [MahjongEngine.selectTile]. Modelado como sealed
 * class (en vez de, por ejemplo, un enum + campos nulleables) para que el
 * `when` del lado de la UI sea exhaustivo y el compilador avise si se
 * agrega un caso nuevo y la UI no lo contempla.
 */
sealed class MoveResult {
    /** Primera ficha de una posible pareja: queda seleccionada, esperando la segunda. */
    data object FirstTileSelected : MoveResult()

    /** Se tocó la ficha ya seleccionada de nuevo: se deselecciona, sin efecto en el tablero. */
    data object Deselected : MoveResult()

    /** Dos fichas libres que no matchean: la selección pasa a ser la nueva ficha tocada. */
    data class SelectionReplaced(val previousTileId: TileId, val newTileId: TileId) : MoveResult()

    /** Match válido: ambas fichas se retiraron del tablero. */
    data class Matched(
        val firstTileId: TileId,
        val secondTileId: TileId,
        val pointsAwarded: Int,
        val newStreak: Int,
        val boardCleared: Boolean,
    ) : MoveResult()

    /** La ficha tocada no está libre (tapada o bloqueada por los costados). */
    data object TileNotFree : MoveResult()

    /** El id de ficha no corresponde a ninguna ficha viva en el tablero. */
    data object TileNotFound : MoveResult()

    /** No se puede jugar: la partida está pausada, ganada o perdida. */
    data class GameNotActive(val status: GameStatus) : MoveResult()
}

package com.classicsneox.mahjong.core.engine

import com.classicsneox.mahjong.core.model.Position

/**
 * Reglas puras de geometría del tablero, independientes de qué ficha hay
 * en cada lugar. Se usan tanto en vivo (para saber qué puede tocar el
 * jugador) como en la generación del tablero (para saber qué posiciones
 * "quedarían libres" si tal o cual ficha ya no estuviera).
 *
 * Nota de diseño: [isCovered] mira TODAS las capas por encima, no sólo la
 * inmediata siguiente. Para los layouts embebidos (que siempre exigen
 * apoyo completo, ver [com.classicsneox.mahjong.core.model.BoardLayout.validate])
 * alcanzaría con mirar una capa arriba, pero mirar todas las capas hace
 * que la función sea correcta también para layouts mal formados o
 * cargados desde afuera, sin suponer nada que no esté garantizado por tipo.
 */
object BoardTopology {

    /** True si existe alguna posición en [occupied] en una capa superior que tape a [position]. */
    fun isCovered(position: Position, occupied: Set<Position>): Boolean =
        occupied.any { it.z > position.z && it.overlapsFootprint(position) }

    /** True si el lado izquierdo de [position] (misma capa) está bloqueado por otra ficha ocupada. */
    fun isLeftBlocked(position: Position, occupied: Set<Position>): Boolean =
        occupied.any { it.z == position.z && it.overlapsFootprint(position.leftNeighbor()) }

    /** True si el lado derecho de [position] (misma capa) está bloqueado por otra ficha ocupada. */
    fun isRightBlocked(position: Position, occupied: Set<Position>): Boolean =
        occupied.any { it.z == position.z && it.overlapsFootprint(position.rightNeighbor()) }

    /**
     * Una posición está "libre" -se puede tocar/retirar- si no está tapada
     * desde arriba y al menos uno de sus dos lados (izquierda o derecha,
     * misma capa) está abierto. Esta es la regla estándar del Mahjong
     * Solitario.
     */
    fun isFree(position: Position, occupied: Set<Position>): Boolean =
        !isCovered(position, occupied) &&
            (!isLeftBlocked(position, occupied) || !isRightBlocked(position, occupied))

    /** Subconjunto de [occupied] que está libre en este momento. */
    fun freePositions(occupied: Set<Position>): Set<Position> =
        occupied.filterTo(mutableSetOf()) { isFree(it, occupied) }
}

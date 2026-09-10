package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.engine.BoardTopology
import com.classicsneox.mahjong.core.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopologyTest {

    @Test
    fun `una ficha sola esta libre`() {
        val p = Position(0, 0, 0)
        assertTrue(BoardTopology.isFree(p, setOf(p)))
    }

    @Test
    fun `una ficha tapada por otra encima no esta libre`() {
        val base = Position(0, 0, 0)
        val above = Position(0, 0, 1) // mismo footprint (x,y), capa superior
        assertTrue(BoardTopology.isCovered(base, setOf(base, above)))
        assertFalse(BoardTopology.isFree(base, setOf(base, above)))
        // la de arriba, en cambio, no está tapada por nada
        assertTrue(BoardTopology.isFree(above, setOf(base, above)))
    }

    @Test
    fun `ficha con ambos lados ocupados no esta libre`() {
        val center = Position(2, 0, 0)
        val left = Position(0, 0, 0)
        val right = Position(4, 0, 0)
        val occupied = setOf(left, center, right)
        assertTrue(BoardTopology.isLeftBlocked(center, occupied))
        assertTrue(BoardTopology.isRightBlocked(center, occupied))
        assertFalse(BoardTopology.isFree(center, occupied))
    }

    @Test
    fun `ficha con un solo lado abierto esta libre`() {
        val center = Position(2, 0, 0)
        val left = Position(0, 0, 0)
        val occupied = setOf(left, center) // sin vecino a la derecha
        assertTrue(BoardTopology.isFree(center, occupied))
    }

    @Test
    fun `overlapsFootprint solo es cierto para footprints que realmente se cruzan`() {
        val a = Position(0, 0, 0)
        assertTrue(a.overlapsFootprint(Position(0, 0, 5))) // misma xy, distinta capa: igual "se cruza" en el plano
        assertTrue(a.overlapsFootprint(Position(1, 1, 0)))
        assertFalse(a.overlapsFootprint(Position(2, 0, 0))) // vecino inmediato, no se solapa
        assertFalse(a.overlapsFootprint(Position(0, 2, 0)))
    }

    @Test
    fun `freePositions devuelve exactamente el subconjunto libre`() {
        val a = Position(0, 0, 0)
        val b = Position(2, 0, 0)
        val coveringA = Position(0, 0, 1)
        val occupied = setOf(a, b, coveringA)
        val free = BoardTopology.freePositions(occupied)
        assertEquals(setOf(b, coveringA), free)
    }
}

package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.engine.BoardGenerator
import com.classicsneox.mahjong.core.engine.BoardTopology
import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.engine.MahjongEngine
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.Position
import com.classicsneox.mahjong.core.model.TileType
import com.classicsneox.mahjong.core.model.matches
import com.classicsneox.mahjong.core.model.standardTileSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

class BoardGeneratorTest {

    // ---- tablero de producción completo (144 fichas) --------------------

    @Test
    fun `el tablero generado usa exactamente el mismo multiset de fichas recibido`() {
        repeat(10) { seed ->
            val generated = BoardGenerator.generate(
                LayoutCatalog.PIRAMIDE_NEOX.positions,
                standardTileSet(),
                Random(seed.toLong()),
            )
            assertEquals(144, generated.tiles.size)
            val expectedCounts = standardTileSet().groupingBy { it }.eachCount()
            val actualCounts = generated.tiles.map { it.type }.groupingBy { it }.eachCount()
            assertEquals(expectedCounts, actualCounts)
        }
    }

    @Test
    fun `el tablero generado ocupa exactamente las posiciones del layout, sin repetir ninguna`() {
        val generated = BoardGenerator.generate(LayoutCatalog.PIRAMIDE_NEOX.positions, standardTileSet(), Random(7))
        val usedPositions = generated.tiles.map { it.position }.toSet()
        assertEquals(LayoutCatalog.PIRAMIDE_NEOX.positions.toSet(), usedPositions)
        assertEquals(144, generated.tiles.map { it.position }.toSet().size)
    }

    @Test
    fun `el orden constructivo, jugado contra el motor real, siempre limpia el tablero`() {
        repeat(15) { seed ->
            val generated = BoardGenerator.generate(
                LayoutCatalog.PIRAMIDE_NEOX.positions,
                standardTileSet(),
                Random(seed.toLong() * 31 + 1),
            )
            val engine = MahjongEngine.restore(
                layout = LayoutCatalog.PIRAMIDE_NEOX,
                tiles = generated.tiles,
                status = GameStatus.PLAYING,
                score = 0, movesCount = 0, matchesCount = 0,
                hintsUsed = 0, shufflesUsed = 0, streak = 0, elapsedSeconds = 0L,
            )
            val order = generated.constructiveClearOrder
            assertEquals(144, order.size)
            for (i in order.indices step 2) {
                val firstResult = engine.selectTile(order[i])
                assertTrue(
                    "Paso $i: se esperaba FirstTileSelected, fue $firstResult (seed=$seed)",
                    firstResult is com.classicsneox.mahjong.core.engine.MoveResult.FirstTileSelected,
                )
                val secondResult = engine.selectTile(order[i + 1])
                assertTrue(
                    "Paso ${i + 1}: se esperaba Matched, fue $secondResult (seed=$seed)",
                    secondResult is com.classicsneox.mahjong.core.engine.MoveResult.Matched,
                )
            }
            assertEquals("seed=$seed", GameStatus.WON, engine.state.status)
            assertTrue("seed=$seed", engine.state.tiles.isEmpty())
        }
    }

    @Test
    fun `generar es determinístico dado el mismo seed`() {
        val a = BoardGenerator.generate(LayoutCatalog.PIRAMIDE_NEOX.positions, standardTileSet(), Random(42))
        val b = BoardGenerator.generate(LayoutCatalog.PIRAMIDE_NEOX.positions, standardTileSet(), Random(42))
        assertEquals(a.tiles.map { it.position to it.type }, b.tiles.map { it.position to it.type })
    }

    // ---- detección de layouts irresolubles -------------------------------

    @Test
    fun `un layout degenerado sin forma de liberar dos fichas a la vez lanza UnsolvableLayoutException`() {
        // Dos posiciones: una tapa exactamente a la otra, y ninguna tiene
        // vecinos laterales. Sólo la de arriba puede estar libre a la vez:
        // nunca hay 2 libres simultáneamente, así que no hay forma de
        // construir un orden de retiro de a pares.
        val positions = listOf(Position(0, 0, 0), Position(0, 0, 1))
        val types = listOf(TileType.Honor(com.classicsneox.mahjong.core.model.Wind.EAST), TileType.Honor(com.classicsneox.mahjong.core.model.Wind.EAST))
        try {
            BoardGenerator.generate(positions, types, Random(1))
            fail("Se esperaba UnsolvableLayoutException")
        } catch (e: BoardGenerator.UnsolvableLayoutException) {
            // esperado
        }
    }

    // ---- layout chico, verificado con un solver exhaustivo independiente ---

    /** Layout de 8 fichas en 2 capas, con bloqueo real, usado sólo en tests. */
    private fun tinyLayoutPositions(): List<Position> = listOf(
        Position(0, 0, 0), Position(2, 0, 0), Position(4, 0, 0),
        Position(0, 2, 0), Position(2, 2, 0), Position(4, 2, 0),
        Position(2, 0, 1), Position(2, 2, 1),
    )

    private fun tinyLayoutTypes(): List<TileType> {
        val w = com.classicsneox.mahjong.core.model.Wind.entries
        // 4 parejas exactas, usando 4 vientos distintos para que no haya ambigüedad de matching cruzado.
        return listOf(
            TileType.Honor(w[0]), TileType.Honor(w[0]),
            TileType.Honor(w[1]), TileType.Honor(w[1]),
            TileType.Honor(w[2]), TileType.Honor(w[2]),
            TileType.Honor(w[3]), TileType.Honor(w[3]),
        )
    }

    /** Solver exhaustivo por backtracking. Sólo para tests, sobre tableros chicos. */
    private fun isFullySolvable(positions: List<Position>, typeOf: Map<Position, TileType>): Boolean {
        fun solve(remaining: Set<Position>): Boolean {
            if (remaining.isEmpty()) return true
            val free = BoardTopology.freePositions(remaining).toList()
            for (i in free.indices) {
                for (j in i + 1 until free.size) {
                    if (typeOf.getValue(free[i]).matches(typeOf.getValue(free[j]))) {
                        if (solve(remaining - free[i] - free[j])) return true
                    }
                }
            }
            return false
        }
        return solve(positions.toSet())
    }

    @Test
    fun `layout chico con bloqueo real - el generador siempre produce algo resoluble segun un solver independiente`() {
        repeat(25) { seed ->
            val generated = BoardGenerator.generate(tinyLayoutPositions(), tinyLayoutTypes(), Random(seed.toLong()))
            val typeOf = generated.tiles.associate { it.position to it.type }
            assertTrue("seed=$seed", isFullySolvable(generated.tiles.map { it.position }, typeOf))
        }
    }
}

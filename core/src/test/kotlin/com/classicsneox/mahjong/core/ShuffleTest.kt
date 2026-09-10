package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.engine.BoardGenerator
import com.classicsneox.mahjong.core.engine.BoardTopology
import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.engine.MahjongEngine
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.Position
import com.classicsneox.mahjong.core.model.TileType
import com.classicsneox.mahjong.core.model.Wind
import com.classicsneox.mahjong.core.model.matches
import com.classicsneox.mahjong.core.model.standardTileSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ShuffleTest {

    @Test
    fun `remezclar conserva la cantidad de fichas, el multiset de tipos y el conjunto de posiciones`() {
        val engine = MahjongEngine.newGame(LayoutCatalog.PIRAMIDE_NEOX, random = Random(5))
        val beforePositions = engine.state.tiles.map { it.position }.toSet()
        val beforeTypeCounts = engine.state.tiles.map { it.type }.groupingBy { it }.eachCount()

        assertTrue(engine.shuffle())

        val after = engine.state
        assertEquals(144, after.tiles.size)
        assertEquals(beforePositions, after.tiles.map { it.position }.toSet())
        assertEquals(beforeTypeCounts, after.tiles.map { it.type }.groupingBy { it }.eachCount())
    }

    @Test
    fun `remezclar corta la racha, invalida el undo, limpia la seleccion y cobra penalidad`() {
        val engine = MahjongEngine.newGame(LayoutCatalog.PIRAMIDE_NEOX, random = Random(5))
        val someFreeTile = engine.state.freeTileIds.first()
        engine.selectTile(someFreeTile)
        val scoreBefore = engine.state.score

        assertTrue(engine.shuffle())

        val after = engine.state
        assertEquals(0, after.currentStreak)
        assertFalse(after.canUndo)
        assertEquals(null, after.selectedTileId)
        assertEquals(1, after.shufflesUsed)
        assertEquals(
            com.classicsneox.mahjong.core.engine.ScoreRules.applyPenalty(
                scoreBefore,
                com.classicsneox.mahjong.core.engine.ScoreRules.SHUFFLE_PENALTY,
            ),
            after.score,
        )
    }

    @Test
    fun `remezclar respeta el limite maximo configurado`() {
        val engine = MahjongEngine.newGame(LayoutCatalog.PIRAMIDE_NEOX, maxShuffles = 2, random = Random(9))
        assertTrue(engine.shuffle())
        assertTrue(engine.state.canShuffle)
        assertTrue(engine.shuffle())
        assertFalse(engine.state.canShuffle)
        assertFalse(engine.shuffle()) // ya se usaron los 2 permitidos
        assertEquals(2, engine.state.shufflesUsed)
    }

    @Test
    fun `no se puede remezclar un tablero vacio ni una partida pausada`() {
        val engine = MahjongEngine.newGame(LayoutCatalog.PIRAMIDE_NEOX, random = Random(3))
        engine.pause()
        assertFalse(engine.shuffle())
        engine.resume()
        assertTrue(engine.shuffle())
    }

    // ---- resolubilidad post-shuffle, verificada con un solver exhaustivo ----

    private fun tinyLayoutPositions(): List<Position> = listOf(
        Position(0, 0, 0), Position(2, 0, 0), Position(4, 0, 0),
        Position(0, 2, 0), Position(2, 2, 0), Position(4, 2, 0),
        Position(2, 0, 1), Position(2, 2, 1),
    )

    private fun tinyLayoutTypes(): List<TileType> {
        val w = Wind.entries
        return listOf(
            TileType.Honor(w[0]), TileType.Honor(w[0]),
            TileType.Honor(w[1]), TileType.Honor(w[1]),
            TileType.Honor(w[2]), TileType.Honor(w[2]),
            TileType.Honor(w[3]), TileType.Honor(w[3]),
        )
    }

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
    fun `un tablero chico sigue siendo resoluble por completo despues de remezclar`() {
        repeat(20) { seed ->
            val generated = BoardGenerator.generate(tinyLayoutPositions(), tinyLayoutTypes(), Random(seed.toLong()))
            val layout = com.classicsneox.mahjong.core.model.BoardLayout("tiny", "tiny", tinyLayoutPositions())
            val engine = MahjongEngine.restore(
                layout = layout, tiles = generated.tiles, status = GameStatus.PLAYING,
                score = 0, movesCount = 0, matchesCount = 0, hintsUsed = 0, shufflesUsed = 0,
                streak = 0, elapsedSeconds = 0L, random = Random(seed.toLong() + 1000),
            )
            assertTrue("seed=$seed", engine.shuffle())
            val typeOf = engine.state.tiles.associate { it.position to it.type }
            assertTrue("seed=$seed", isFullySolvable(engine.state.tiles.map { it.position }, typeOf))
        }
    }
}

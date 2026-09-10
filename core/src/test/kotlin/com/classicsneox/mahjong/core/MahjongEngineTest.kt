package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.engine.MahjongEngine
import com.classicsneox.mahjong.core.engine.MoveResult
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.model.Position
import com.classicsneox.mahjong.core.model.Suit
import com.classicsneox.mahjong.core.model.Tile
import com.classicsneox.mahjong.core.model.TileId
import com.classicsneox.mahjong.core.model.TileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MahjongEngineTest {

    /**
     * Cuatro fichas bien separadas entre sí (nunca se tapan ni se
     * bloquean lateralmente): pos0/pos1 matchean entre sí, pos2/pos3
     * matchean entre sí, y ningún par cruzado matchea. Pensado para
     * aislar la máquina de estados de selección de la geometría del
     * tablero, que ya se prueba aparte en TopologyTest/BoardGeneratorTest.
     */
    private fun simpleFourTileLayoutAndTiles(): Pair<BoardLayout, List<Tile>> {
        val positions = listOf(Position(0, 0, 0), Position(10, 0, 0), Position(0, 10, 0), Position(10, 10, 0))
        val layout = BoardLayout("test_simple4", "Test simple", positions)
        val tiles = listOf(
            Tile(TileId(0), TileType.Suited(Suit.BAMBOO, 5), positions[0]),
            Tile(TileId(1), TileType.Suited(Suit.BAMBOO, 5), positions[1]),
            Tile(TileId(2), TileType.Suited(Suit.CIRCLES, 3), positions[2]),
            Tile(TileId(3), TileType.Suited(Suit.CIRCLES, 3), positions[3]),
        )
        return layout to tiles
    }

    private fun freshEngine(): MahjongEngine {
        val (layout, tiles) = simpleFourTileLayoutAndTiles()
        return MahjongEngine.restore(
            layout = layout, tiles = tiles, status = GameStatus.PLAYING,
            score = 0, movesCount = 0, matchesCount = 0, hintsUsed = 0, shufflesUsed = 0,
            streak = 0, elapsedSeconds = 0L,
        )
    }

    @Test
    fun `tocar una ficha libre por primera vez la selecciona`() {
        val engine = freshEngine()
        assertEquals(MoveResult.FirstTileSelected, engine.selectTile(TileId(0)))
        assertEquals(TileId(0), engine.state.selectedTileId)
    }

    @Test
    fun `tocar la misma ficha seleccionada la deselecciona`() {
        val engine = freshEngine()
        engine.selectTile(TileId(0))
        assertEquals(MoveResult.Deselected, engine.selectTile(TileId(0)))
        assertNull(engine.state.selectedTileId)
    }

    @Test
    fun `tocar una segunda ficha que no matchea reemplaza la seleccion, sin retirar nada`() {
        val engine = freshEngine()
        engine.selectTile(TileId(0))
        val result = engine.selectTile(TileId(2))
        assertEquals(MoveResult.SelectionReplaced(TileId(0), TileId(2)), result)
        assertEquals(TileId(2), engine.state.selectedTileId)
        assertEquals(4, engine.state.tiles.size)
    }

    @Test
    fun `tocar una segunda ficha que matchea retira ambas y suma puntaje`() {
        val engine = freshEngine()
        engine.selectTile(TileId(0))
        val result = engine.selectTile(TileId(1))
        assertTrue(result is MoveResult.Matched)
        result as MoveResult.Matched
        assertEquals(TileId(0), result.firstTileId)
        assertEquals(TileId(1), result.secondTileId)
        assertEquals(10, result.pointsAwarded) // ScoreRules.BASE_MATCH_POINTS, streak=0 antes del match
        assertFalse(result.boardCleared)
        assertEquals(2, engine.state.tiles.size)
        assertEquals(10, engine.state.score)
        assertEquals(1, engine.state.matchesCount)
        assertNull(engine.state.selectedTileId)
    }

    @Test
    fun `retirar todas las fichas gana la partida`() {
        val engine = freshEngine()
        engine.selectTile(TileId(0)); engine.selectTile(TileId(1)) // match 1
        engine.selectTile(TileId(2))
        val result = engine.selectTile(TileId(3)) // match 2, tablero queda vacío
        assertTrue(result is MoveResult.Matched)
        assertTrue((result as MoveResult.Matched).boardCleared)
        assertEquals(GameStatus.WON, engine.state.status)
        assertTrue(engine.state.tiles.isEmpty())
        // el segundo match tuvo streak=1 antes de sumarse -> 10 + 1*2 = 12 puntos
        assertEquals(10 + 12, engine.state.score)
    }

    @Test
    fun `una ficha que no existe devuelve TileNotFound`() {
        val engine = freshEngine()
        assertEquals(MoveResult.TileNotFound, engine.selectTile(TileId(999)))
    }

    @Test
    fun `una ficha tapada no se puede seleccionar`() {
        // Dos fichas, una exactamente encima de la otra: la de abajo está
        // tapada y debe rechazarse aunque su tipo matchee con cualquier cosa.
        val bottom = Tile(TileId(0), TileType.Suited(Suit.BAMBOO, 1), Position(0, 0, 0))
        val top = Tile(TileId(1), TileType.Suited(Suit.BAMBOO, 1), Position(0, 0, 1))
        val layout = BoardLayout("test_cover", "Test cover", listOf(bottom.position, top.position))
        val engine = MahjongEngine.restore(
            layout = layout, tiles = listOf(bottom, top), status = GameStatus.PLAYING,
            score = 0, movesCount = 0, matchesCount = 0, hintsUsed = 0, shufflesUsed = 0, streak = 0, elapsedSeconds = 0L,
        )
        assertEquals(MoveResult.TileNotFree, engine.selectTile(bottom.id))
        assertEquals(MoveResult.FirstTileSelected, engine.selectTile(top.id))
    }

    @Test
    fun `no se puede jugar mientras esta pausada, y se puede reanudar`() {
        val engine = freshEngine()
        assertTrue(engine.pause())
        assertEquals(GameStatus.PAUSED, engine.state.status)
        assertEquals(MoveResult.GameNotActive(GameStatus.PAUSED), engine.selectTile(TileId(0)))
        assertTrue(engine.resume())
        assertEquals(GameStatus.PLAYING, engine.state.status)
        assertEquals(MoveResult.FirstTileSelected, engine.selectTile(TileId(0)))
    }

    @Test
    fun `undo revierte el ultimo match por completo`() {
        val engine = freshEngine()
        engine.selectTile(TileId(0)); engine.selectTile(TileId(1))
        assertEquals(2, engine.state.tiles.size)
        assertEquals(10, engine.state.score)
        assertTrue(engine.state.canUndo)

        assertTrue(engine.undo())

        assertEquals(4, engine.state.tiles.size)
        assertEquals(0, engine.state.score)
        assertEquals(0, engine.state.matchesCount)
        assertEquals(0, engine.state.movesCount)
        assertFalse(engine.state.canUndo)
        assertNull(engine.state.selectedTileId)
    }

    @Test
    fun `undo sin jugadas previas no hace nada y devuelve false`() {
        val engine = freshEngine()
        assertFalse(engine.undo())
    }

    @Test
    fun `pedir pista devuelve una pareja que matchea, cobra penalidad y corta la racha`() {
        val engine = freshEngine()
        val hint = engine.requestHint()
        assertNotNull(hint)
        hint!!
        val tilesById = engine.state.tiles.associateBy { it.id }
        assertTrue(tilesById.getValue(hint.first).type.let { a -> a == tilesById.getValue(hint.second).type })
        assertEquals(1, engine.state.hintsUsed)
        assertEquals(0, engine.state.score) // 0 - 15, con piso en 0
    }

    @Test
    fun `pedir pista sin movimientos disponibles devuelve null`() {
        val positions = listOf(Position(0, 0, 0), Position(10, 0, 0))
        val layout = BoardLayout("test_nomatch", "Test sin match", positions)
        val tiles = listOf(
            Tile(TileId(0), TileType.Suited(Suit.BAMBOO, 1), positions[0]),
            Tile(TileId(1), TileType.Suited(Suit.CIRCLES, 9), positions[1]),
        )
        val engine = MahjongEngine.restore(
            layout = layout, tiles = tiles, status = GameStatus.PLAYING,
            score = 0, movesCount = 0, matchesCount = 0, hintsUsed = 0, shufflesUsed = 0, streak = 0, elapsedSeconds = 0L,
        )
        assertFalse(engine.hasAvailableMove())
        assertNull(engine.requestHint())
        assertEquals(0, engine.state.hintsUsed) // no se cobra penalidad si no había nada para señalar
    }

    @Test
    fun `una partida nueva sobre el layout de produccion arranca jugable y con las 144 fichas`() {
        val engine = MahjongEngine.newGame(LayoutCatalog.PIRAMIDE_NEOX, random = Random(123))
        val state = engine.state
        assertEquals(GameStatus.PLAYING, state.status)
        assertEquals(144, state.tiles.size)
        assertEquals(144, state.totalTileCount)
        assertTrue(state.freeTileIds.isNotEmpty())
        assertTrue(state.hasAvailableMove)
    }
}

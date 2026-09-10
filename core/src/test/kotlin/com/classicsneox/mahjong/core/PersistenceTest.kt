package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.engine.MahjongEngine
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.decodeTileType
import com.classicsneox.mahjong.core.model.encode
import com.classicsneox.mahjong.core.model.standardTileSet
import com.classicsneox.mahjong.core.persistence.SnapshotMapper
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class PersistenceTest {

    @Test
    fun `todo tipo de ficha del set estandar sobrevive un encode-decode`() {
        for (type in standardTileSet().distinct()) {
            assertEquals(type, decodeTileType(type.encode()))
        }
    }

    @Test
    fun `guardar y restaurar una partida en curso reproduce el mismo estado jugable`() {
        val engine = MahjongEngine.newGame(LayoutCatalog.PIRAMIDE_NEOX, random = Random(11))
        // jugamos un par de matches reales para que el snapshot no sea el estado inicial "de fábrica"
        val free = engine.state.freeTileIds.toList()
        engine.selectTile(free[0])
        engine.requestHint() // ejercita también que hints/score entren en el snapshot

        val snapshot = SnapshotMapper.toSnapshot(engine.state, maxShuffles = 3, nowEpochMillis = 1_700_000_000_000)
        val restored = SnapshotMapper.fromSnapshot(snapshot)

        val a = engine.state
        val b = restored.state
        assertEquals(a.layoutId, b.layoutId)
        assertEquals(a.status, b.status)
        assertEquals(a.score, b.score)
        assertEquals(a.movesCount, b.movesCount)
        assertEquals(a.matchesCount, b.matchesCount)
        assertEquals(a.hintsUsed, b.hintsUsed)
        assertEquals(a.shufflesUsed, b.shufflesUsed)
        assertEquals(a.currentStreak, b.currentStreak)
        assertEquals(a.elapsedSeconds, b.elapsedSeconds)
        assertEquals(a.tiles.map { it.id to it.type to it.position }.toSet(), b.tiles.map { it.id to it.type to it.position }.toSet())
    }

    @Test
    fun `restaurar una partida ganada preserva el estado WON`() {
        // SnapshotMapper.fromSnapshot sólo necesita poder encontrar el layout
        // por id en el catálogo; con un snapshot "de fábrica" en estado WON y
        // sin fichas alcanza para probar el mapeo de los campos de estado.
        val snapshot = com.classicsneox.mahjong.core.persistence.GameSnapshot(
            schemaVersion = com.classicsneox.mahjong.core.persistence.GameSnapshot.CURRENT_SCHEMA_VERSION,
            layoutId = LayoutCatalog.PIRAMIDE_NEOX.id,
            tiles = emptyList(),
            statusName = GameStatus.WON.name,
            score = 500, movesCount = 72, matchesCount = 72, hintsUsed = 1, shufflesUsed = 0,
            streak = 10, elapsedSeconds = 300, maxShuffles = null, savedAtEpochMillis = 0,
        )
        val restored = SnapshotMapper.fromSnapshot(snapshot)
        assertEquals(GameStatus.WON, restored.state.status)
        assertEquals(500, restored.state.score)
        assertEquals(true, restored.state.tiles.isEmpty())
    }
}

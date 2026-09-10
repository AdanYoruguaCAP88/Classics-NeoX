package com.classicsneox.mahjong.core.persistence

import com.classicsneox.mahjong.core.engine.GameState
import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.engine.MahjongEngine
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.Position
import com.classicsneox.mahjong.core.model.Tile
import com.classicsneox.mahjong.core.model.TileId
import com.classicsneox.mahjong.core.model.decodeTileType
import com.classicsneox.mahjong.core.model.encode
import kotlin.random.Random

/**
 * Convierte entre el mundo en memoria (motor/[GameState]) y el mundo para
 * guardar en disco ([GameSnapshot]). Vive en `:core` para que tanto una
 * implementación Android (DataStore) como, en el futuro, una de escritorio
 * o de tests puedan reusar exactamente la misma lógica de mapeo.
 */
object SnapshotMapper {

    fun toSnapshot(state: GameState, maxShuffles: Int?, nowEpochMillis: Long): GameSnapshot = GameSnapshot(
        schemaVersion = GameSnapshot.CURRENT_SCHEMA_VERSION,
        layoutId = state.layoutId,
        tiles = state.tiles.map { tile ->
            TileSnapshot(
                id = tile.id.value,
                encodedType = tile.type.encode(),
                x = tile.position.x,
                y = tile.position.y,
                z = tile.position.z,
            )
        },
        statusName = state.status.name,
        score = state.score,
        movesCount = state.movesCount,
        matchesCount = state.matchesCount,
        hintsUsed = state.hintsUsed,
        shufflesUsed = state.shufflesUsed,
        streak = state.currentStreak,
        elapsedSeconds = state.elapsedSeconds,
        maxShuffles = maxShuffles,
        savedAtEpochMillis = nowEpochMillis,
    )

    /**
     * Reconstruye un [MahjongEngine] listo para seguir jugando.
     * @throws IllegalArgumentException si el snapshot referencia un layout
     *   que no existe en [LayoutCatalog] (por ejemplo, tras desinstalar una
     *   versión que traía un layout que ya no está).
     */
    fun fromSnapshot(snapshot: GameSnapshot, random: Random = Random.Default): MahjongEngine {
        val layout = LayoutCatalog.byId(snapshot.layoutId)
            ?: throw IllegalArgumentException("Layout desconocido en el snapshot: '${snapshot.layoutId}'")
        val tiles = snapshot.tiles.map { ts ->
            Tile(
                id = TileId(ts.id),
                type = decodeTileType(ts.encodedType),
                position = Position(ts.x, ts.y, ts.z),
            )
        }
        return MahjongEngine.restore(
            layout = layout,
            tiles = tiles,
            status = GameStatus.valueOf(snapshot.statusName),
            score = snapshot.score,
            movesCount = snapshot.movesCount,
            matchesCount = snapshot.matchesCount,
            hintsUsed = snapshot.hintsUsed,
            shufflesUsed = snapshot.shufflesUsed,
            streak = snapshot.streak,
            elapsedSeconds = snapshot.elapsedSeconds,
            maxShuffles = snapshot.maxShuffles,
            random = random,
        )
    }
}

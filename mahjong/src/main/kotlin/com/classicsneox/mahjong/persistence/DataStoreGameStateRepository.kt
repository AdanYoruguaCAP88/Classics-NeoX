package com.classicsneox.mahjong.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.classicsneox.mahjong.core.persistence.GameSnapshot
import com.classicsneox.mahjong.core.persistence.GameStateRepository
import com.classicsneox.mahjong.core.persistence.TileSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Única implementación concreta de [GameStateRepository] del proyecto:
 * un solo registro (la partida en curso) en Preferences DataStore, como
 * JSON. Es deliberadamente la única partida guardada -este juego no
 * necesita un historial de partidas, sólo "seguir donde quedaste"-.
 *
 * Se define acá (no en `:core`) precisamente para que `:core` no tenga
 * que depender de DataStore ni de kotlinx.serialization: ver el contrato
 * en [GameStateRepository] y el DTO plano en [GameSnapshot].
 */
class DataStoreGameStateRepository(private val dataStore: DataStore<Preferences>) : GameStateRepository {

    private object Keys {
        val SNAPSHOT_JSON = stringPreferencesKey("neox_mahjong_snapshot_json")
    }

    override suspend fun save(snapshot: GameSnapshot) {
        val dto = snapshot.toDto()
        val json = Json.encodeToString(dto)
        dataStore.edit { prefs -> prefs[Keys.SNAPSHOT_JSON] = json }
    }

    override suspend fun load(): GameSnapshot? {
        val json = dataStore.data.first()[Keys.SNAPSHOT_JSON] ?: return null
        return runCatching { Json.decodeFromString<GameSnapshotDto>(json).toDomain() }.getOrNull()
    }

    override suspend fun clear() {
        dataStore.edit { prefs -> prefs.remove(Keys.SNAPSHOT_JSON) }
    }
}

// ---- DTOs serializables. `:core.GameSnapshot` se mantiene libre de anotaciones
// de kotlinx.serialization a propósito (ver KDoc de la clase); acá se mapea 1 a 1. ----

@Serializable
private data class GameSnapshotDto(
    val schemaVersion: Int,
    val layoutId: String,
    val tiles: List<TileSnapshotDto>,
    val statusName: String,
    val score: Int,
    val movesCount: Int,
    val matchesCount: Int,
    val hintsUsed: Int,
    val shufflesUsed: Int,
    val streak: Int,
    val elapsedSeconds: Long,
    val maxShuffles: Int?,
    val savedAtEpochMillis: Long,
)

@Serializable
private data class TileSnapshotDto(val id: Int, val encodedType: String, val x: Int, val y: Int, val z: Int)

private fun GameSnapshot.toDto() = GameSnapshotDto(
    schemaVersion, layoutId, tiles.map { TileSnapshotDto(it.id, it.encodedType, it.x, it.y, it.z) },
    statusName, score, movesCount, matchesCount, hintsUsed, shufflesUsed, streak,
    elapsedSeconds, maxShuffles, savedAtEpochMillis,
)

private fun GameSnapshotDto.toDomain() = GameSnapshot(
    schemaVersion, layoutId, tiles.map { TileSnapshot(it.id, it.encodedType, it.x, it.y, it.z) },
    statusName, score, movesCount, matchesCount, hintsUsed, shufflesUsed, streak,
    elapsedSeconds, maxShuffles, savedAtEpochMillis,
)

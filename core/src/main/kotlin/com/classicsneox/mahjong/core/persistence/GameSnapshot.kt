package com.classicsneox.mahjong.core.persistence

/**
 * Foto de una partida guardada. Sólo tipos primitivos: a propósito, para
 * que cualquier mecanismo de serialización (kotlinx.serialization,
 * DataStore Preferences, un archivo de texto propio) lo pueda usar sin
 * que `:core` tenga que elegir esa dependencia por el resto del proyecto.
 */
data class GameSnapshot(
    val schemaVersion: Int,
    val layoutId: String,
    val tiles: List<TileSnapshot>,
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
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

/** Una ficha, tal como se guarda: id + tipo codificado como texto (ver [com.classicsneox.mahjong.core.model.encode]) + posición. */
data class TileSnapshot(
    val id: Int,
    val encodedType: String,
    val x: Int,
    val y: Int,
    val z: Int,
)

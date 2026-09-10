package com.classicsneox.mahjong.core.persistence

/**
 * Contrato de guardado/carga. `:core` define QUÉ se puede hacer;
 * `:mahjong` (Android) decide CÓMO -DataStore, hoy- sin que el motor ni
 * los tests de lógica necesiten saber que Android existe.
 */
interface GameStateRepository {
    suspend fun save(snapshot: GameSnapshot)
    suspend fun load(): GameSnapshot?
    suspend fun clear()
}

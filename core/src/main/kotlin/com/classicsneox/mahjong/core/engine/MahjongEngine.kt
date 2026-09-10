package com.classicsneox.mahjong.core.engine

import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.model.Tile
import com.classicsneox.mahjong.core.model.TileId
import com.classicsneox.mahjong.core.model.TileType
import com.classicsneox.mahjong.core.model.matches
import com.classicsneox.mahjong.core.model.standardTileSet
import kotlin.random.Random

/**
 * Motor de NeoX Mahjong. Kotlin puro, sin una sola referencia a Android:
 * se puede instanciar y testear en una JVM lisa, sin emulador ni Robolectric.
 * La capa `:mahjong` (Compose) es una envoltura fina sobre esto: traduce
 * eventos de UI en llamadas acá, y refleja [state] en pantalla.
 *
 * El motor es intencionalmente síncrono y sin locks: se espera que quien
 * lo use (normalmente un ViewModel) serialice el acceso desde un único
 * hilo/scope, como es lo normal en Android con `StateFlow` + un scope de
 * corrutinas.
 *
 * Nota sobre ids tras un [shuffle]: las fichas que siguen en el tablero
 * después de remezclar reciben ids NUEVOS (los ids sólo se garantizan
 * estables entre jugadas normales y un [undo], no a través de un shuffle).
 * Esto es correcto porque el remezclado ya invalida la pila de undo y
 * cualquier selección en curso.
 */
class MahjongEngine private constructor(
    private val layout: BoardLayout,
    initialTiles: List<Tile>,
    initialStatus: GameStatus,
    initialScore: Int,
    initialMovesCount: Int,
    initialMatchesCount: Int,
    initialHintsUsed: Int,
    initialShufflesUsed: Int,
    initialStreak: Int,
    initialElapsedSeconds: Long,
    private val maxShuffles: Int?,
    private val random: Random,
) {
    private val tilesById = LinkedHashMap<TileId, Tile>().apply {
        initialTiles.forEach { put(it.id, it) }
    }
    private var status = initialStatus
    private var score = initialScore
    private var movesCount = initialMovesCount
    private var matchesCount = initialMatchesCount
    private var hintsUsed = initialHintsUsed
    private var shufflesUsed = initialShufflesUsed
    private var streak = initialStreak
    private var elapsedSeconds = initialElapsedSeconds
    private var selectedTileId: TileId? = null
    private val undoStack = ArrayDeque<UndoEntry>()

    private data class UndoEntry(val a: Tile, val b: Tile, val pointsAwarded: Int, val streakBefore: Int)

    /** Snapshot inmutable y completo del estado actual. Barato de calcular: el tablero es chico. */
    val state: GameState get() = buildState()

    // ---- lectura -----------------------------------------------------

    private fun occupiedPositions() = tilesById.values.mapTo(mutableSetOf()) { it.position }

    private fun freeTiles(): List<Tile> {
        val occupied = occupiedPositions()
        return tilesById.values.filter { BoardTopology.isFree(it.position, occupied) }
    }

    /** Busca cualquier pareja de fichas libres que matcheen entre sí, si existe. */
    private fun findAvailablePair(): Pair<Tile, Tile>? {
        val free = freeTiles()
        for (i in free.indices) {
            for (j in i + 1 until free.size) {
                if (free[i].type.matches(free[j].type)) return free[i] to free[j]
            }
        }
        return null
    }

    fun hasAvailableMove(): Boolean = findAvailablePair() != null

    private fun canShuffleNow(): Boolean = maxShuffles == null || shufflesUsed < maxShuffles

    private fun buildState(): GameState = GameState(
        layoutId = layout.id,
        layoutDisplayName = layout.displayName,
        tiles = tilesById.values.toList(),
        freeTileIds = freeTiles().mapTo(mutableSetOf()) { it.id },
        selectedTileId = selectedTileId,
        status = status,
        score = score,
        movesCount = movesCount,
        matchesCount = matchesCount,
        hintsUsed = hintsUsed,
        shufflesUsed = shufflesUsed,
        currentStreak = streak,
        elapsedSeconds = elapsedSeconds,
        canUndo = undoStack.isNotEmpty(),
        canShuffle = status == GameStatus.PLAYING && tilesById.isNotEmpty() && canShuffleNow(),
        hasAvailableMove = hasAvailableMove(),
        totalTileCount = layout.positions.size,
    )

    // ---- acciones ------------------------------------------------------

    /**
     * Toca una ficha. Ver [MoveResult] para todos los desenlaces posibles.
     * Si ya había una ficha seleccionada y la nueva no matchea, la
     * selección simplemente pasa a ser la ficha nueva (no se penaliza:
     * es una decisión de UX para que el juego se sienta fluido).
     */
    fun selectTile(id: TileId): MoveResult {
        if (status != GameStatus.PLAYING) return MoveResult.GameNotActive(status)
        val tile = tilesById[id] ?: return MoveResult.TileNotFound
        if (!BoardTopology.isFree(tile.position, occupiedPositions())) return MoveResult.TileNotFree

        val selectedId = selectedTileId
        if (selectedId == null) {
            selectedTileId = id
            return MoveResult.FirstTileSelected
        }
        if (selectedId == id) {
            selectedTileId = null
            return MoveResult.Deselected
        }
        val firstTile = tilesById[selectedId]
        if (firstTile == null) {
            // Estado defensivo: la ficha seleccionada ya no existe (no debería pasar). Reintenta limpio.
            selectedTileId = id
            return MoveResult.FirstTileSelected
        }

        return if (firstTile.type.matches(tile.type)) {
            resolveMatch(firstTile, tile)
        } else {
            selectedTileId = id
            MoveResult.SelectionReplaced(previousTileId = firstTile.id, newTileId = id)
        }
    }

    private fun resolveMatch(a: Tile, b: Tile): MoveResult.Matched {
        val streakBefore = streak
        val points = ScoreRules.pointsForMatch(streakBefore)

        tilesById.remove(a.id)
        tilesById.remove(b.id)
        score += points
        movesCount += 1
        matchesCount += 1
        streak += 1
        selectedTileId = null
        undoStack.addLast(UndoEntry(a, b, points, streakBefore))

        val cleared = tilesById.isEmpty()
        status = when {
            cleared -> GameStatus.WON
            !hasAvailableMove() && !canShuffleNow() -> GameStatus.LOST
            else -> status
        }
        return MoveResult.Matched(a.id, b.id, points, streak, cleared)
    }

    /** Deshace el último match. No se puede deshacer un shuffle ni una pista. */
    fun undo(): Boolean {
        if (status != GameStatus.PLAYING) return false
        val entry = undoStack.removeLastOrNull() ?: return false
        tilesById[entry.a.id] = entry.a
        tilesById[entry.b.id] = entry.b
        score = (score - entry.pointsAwarded).coerceAtLeast(0)
        movesCount = (movesCount - 1).coerceAtLeast(0)
        matchesCount = (matchesCount - 1).coerceAtLeast(0)
        streak = entry.streakBefore
        selectedTileId = null
        return true
    }

    /**
     * Devuelve una pareja de fichas libres que matchean, si existe, y
     * cobra la penalidad de puntos correspondiente. No las retira del
     * tablero: sólo las señala para que la UI las resalte.
     */
    fun requestHint(): Pair<TileId, TileId>? {
        if (status != GameStatus.PLAYING) return null
        val pair = findAvailablePair() ?: return null
        hintsUsed += 1
        streak = 0
        score = ScoreRules.applyPenalty(score, ScoreRules.HINT_PENALTY)
        return pair.first.id to pair.second.id
    }

    /**
     * Reparte de nuevo, al azar, los VALORES de las fichas que quedan
     * sobre las MISMAS posiciones físicas (el "esqueleto" del tablero no
     * cambia), garantizando de nuevo solvabilidad completa desde ese punto.
     * Devuelve false si la partida no está en curso, si ya no quedan
     * fichas, o si se alcanzó el límite de remezclados configurado.
     */
    fun shuffle(): Boolean {
        if (status != GameStatus.PLAYING) return false
        if (tilesById.isEmpty()) return false
        if (!canShuffleNow()) return false

        val positions = tilesById.values.map { it.position }
        val types = tilesById.values.map { it.type }
        val generated = try {
            BoardGenerator.generate(positions, types, random)
        } catch (e: BoardGenerator.UnsolvableLayoutException) {
            return false
        }

        tilesById.clear()
        generated.tiles.forEach { tilesById[it.id] = it }
        shufflesUsed += 1
        streak = 0
        score = ScoreRules.applyPenalty(score, ScoreRules.SHUFFLE_PENALTY)
        selectedTileId = null
        undoStack.clear()
        if (!hasAvailableMove() && !canShuffleNow()) status = GameStatus.LOST
        return true
    }

    fun pause(): Boolean {
        if (status != GameStatus.PLAYING) return false
        status = GameStatus.PAUSED
        return true
    }

    fun resume(): Boolean {
        if (status != GameStatus.PAUSED) return false
        status = GameStatus.PLAYING
        return true
    }

    /** Avanza el reloj de la partida. No hace nada si no está en curso (pausa detiene el tiempo). */
    fun tick(deltaSeconds: Long) {
        if (status == GameStatus.PLAYING) elapsedSeconds += deltaSeconds
    }

    companion object {
        /** Arranca una partida nueva generando un tablero garantizado resoluble. */
        fun newGame(
            layout: BoardLayout,
            tileSet: List<TileType> = standardTileSet(),
            maxShuffles: Int? = null,
            random: Random = Random.Default,
        ): MahjongEngine {
            require(layout.validate() is BoardLayout.ValidationResult.Valid) {
                "Layout inválido: ${(layout.validate() as? BoardLayout.ValidationResult.Invalid)?.reasons}"
            }
            val generated = BoardGenerator.generate(layout.positions, tileSet, random)
            return MahjongEngine(
                layout = layout,
                initialTiles = generated.tiles,
                initialStatus = GameStatus.PLAYING,
                initialScore = 0,
                initialMovesCount = 0,
                initialMatchesCount = 0,
                initialHintsUsed = 0,
                initialShufflesUsed = 0,
                initialStreak = 0,
                initialElapsedSeconds = 0L,
                maxShuffles = maxShuffles,
                random = random,
            )
        }

        /** Reconstruye un motor a partir de un estado guardado (ver capa de persistencia). */
        fun restore(
            layout: BoardLayout,
            tiles: List<Tile>,
            status: GameStatus,
            score: Int,
            movesCount: Int,
            matchesCount: Int,
            hintsUsed: Int,
            shufflesUsed: Int,
            streak: Int,
            elapsedSeconds: Long,
            maxShuffles: Int? = null,
            random: Random = Random.Default,
        ): MahjongEngine = MahjongEngine(
            layout, tiles, status, score, movesCount, matchesCount,
            hintsUsed, shufflesUsed, streak, elapsedSeconds, maxShuffles, random,
        )
    }
}

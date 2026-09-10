package com.classicsneox.mahjong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.engine.MahjongEngine
import com.classicsneox.mahjong.core.engine.MoveResult
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.model.TileId
import com.classicsneox.mahjong.core.persistence.GameStateRepository
import com.classicsneox.mahjong.core.persistence.SnapshotMapper
import com.classicsneox.mahjong.ui.MahjongUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Único punto de contacto entre Compose y [MahjongEngine]. No tiene
 * lógica de juego propia -eso ya está resuelto y testeado en `:core`-,
 * sólo: (a) mantiene vivo el motor durante la vida del ViewModel, (b)
 * traduce cada evento de UI en una llamada al motor, (c) reconstruye
 * [MahjongUiState] después de cada cambio, y (d) orquesta el reloj y el
 * guardado automático.
 */
class MahjongViewModel(
    private val repository: GameStateRepository,
    private val layout: BoardLayout = LayoutCatalog.PIRAMIDE_NEOX,
    private val random: Random = Random.Default,
) : ViewModel() {

    private var engine: MahjongEngine = MahjongEngine.newGame(layout, random = random)
    private var hintedPair: Pair<TileId, TileId>? = null
    private var hintClearJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(MahjongUiState.from(engine.state, hintedPair))
    val uiState: StateFlow<MahjongUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.load()?.let { snapshot ->
                runCatching { SnapshotMapper.fromSnapshot(snapshot, random) }
                    .onSuccess { restored ->
                        engine = restored
                        publish()
                    }
                // si falla la restauración (snapshot corrupto, layout desconocido, etc.)
                // seguimos con la partida nueva que ya se creó al declarar `engine` arriba.
            }
        }
        runClock()
    }

    private fun publish() {
        _uiState.value = MahjongUiState.from(engine.state, hintedPair)
    }

    private fun runClock() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (engine.state.status == GameStatus.PLAYING) {
                    engine.tick(1)
                    publish()
                }
            }
        }
    }

    private fun clearHintAfterDelay() {
        hintClearJob?.cancel()
        hintClearJob = viewModelScope.launch {
            delay(4000)
            hintedPair = null
            publish()
        }
    }

    // ---- eventos desde la UI --------------------------------------------

    fun onTileClick(id: TileId) {
        if (hintedPair != null) {
            hintClearJob?.cancel()
            hintedPair = null
        }
        val result = engine.selectTile(id)
        publish()
        if (result is MoveResult.Matched) persistAsync()
    }

    fun onHintClick() {
        hintedPair = engine.requestHint()
        publish()
        if (hintedPair != null) clearHintAfterDelay()
        persistAsync()
    }

    fun onShuffleClick() {
        engine.shuffle()
        publish()
        persistAsync()
    }

    fun onUndoClick() {
        engine.undo()
        publish()
        persistAsync()
    }

    fun onRestartClick() {
        engine = MahjongEngine.newGame(layout, random = random)
        hintedPair = null
        publish()
        persistAsync()
    }

    fun onPauseClick() {
        engine.pause()
        publish()
        persistAsync()
    }

    fun onResumeClick() {
        engine.resume()
        publish()
    }

    private fun persistAsync() {
        viewModelScope.launch { saveNow() }
    }

    /**
     * Guarda el estado actual y espera a que termine. [persistAsync] (usada
     * tras cada jugada) es la vía normal y no bloquea la UI; esta versión
     * pública existe para que un host imperativo (ver [com.classicsneox.mahjong.NeoXMahjongModule])
     * pueda forzar un guardado y saber que ya se completó -por ejemplo, en
     * `onStop()` de una Activity que se está por ir a segundo plano-.
     */
    suspend fun saveNow() {
        val snapshot = SnapshotMapper.toSnapshot(
            state = engine.state,
            maxShuffles = null,
            nowEpochMillis = System.currentTimeMillis(),
        )
        repository.save(snapshot)
    }

    /** Fábrica simple: evita traer un framework de inyección de dependencias para un solo ViewModel. */
    class Factory(
        private val repository: GameStateRepository,
        private val layout: BoardLayout = LayoutCatalog.PIRAMIDE_NEOX,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MahjongViewModel::class.java))
            return MahjongViewModel(repository, layout) as T
        }
    }
}

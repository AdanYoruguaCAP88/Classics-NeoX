package com.classicsneox.mahjong

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.classicsneox.mahjong.core.engine.GameStatus
import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.persistence.GameStateRepository
import com.classicsneox.mahjong.ui.MahjongScreen
import com.classicsneox.mahjong.ui.MahjongUiState
import com.classicsneox.mahjong.viewmodel.MahjongViewModel

/**
 * ## Adaptador de compatibilidad, NO la superficie principal
 *
 * El brief de CLASSICS NEOX sugiere, como posible contrato de
 * integración, algo con forma de ciclo de vida imperativo:
 * `initialize / startGame / pauseGame / resumeGame / restartGame /
 * saveState / loadState / getScore / getGameState / destroy`.
 *
 * Se decidió NO usar esa forma como API principal porque no encaja bien
 * con cómo funciona Compose + ViewModel de verdad: acá no hace falta
 * "initialize" (el ViewModel se crea solo, atado al ciclo de vida real de
 * Android, y sobrevive rotaciones sin que nadie tenga que orquestarlo) ni
 * "loadState" (la restauración pasa sola, la primera vez que se compone
 * el juego). Forzar esa forma hubiera significado o bien mentir en la
 * implementación (métodos que no hacen lo que el nombre sugiere) o
 * pelear contra el framework.
 *
 * En cambio, la superficie real es [NeoXMahjongGame]: un composable +
 * `StateFlow`, que es como se integra cualquier feature module de
 * Compose. Esta clase es un envoltorio fino sobre esa misma pieza, para
 * el equipo de integración que prefiera (o ya tenga pensado) ese estilo
 * de API imperativa -por ejemplo, si el resto de CLASSICS NEOX está
 * armado alrededor de un ciclo de vida de juego común para los 5 módulos-.
 * Nada de lo que hace este adaptador es lógica nueva: todo delega en
 * [MahjongViewModel].
 */
class NeoXMahjongModule(
    private val repository: GameStateRepository,
    private val layout: BoardLayout = LayoutCatalog.PIRAMIDE_NEOX,
) {
    private var viewModel: MahjongViewModel? = null

    /** No-op documentado: no hay nada que preparar antes de [Content]. Existe para calzar con el contrato sugerido. */
    fun initialize() = Unit

    /**
     * El punto de entrada real. Debe llamarse desde dentro de composición
     * (reemplaza a `startGame()`+`loadState()` del contrato sugerido: al
     * componerse por primera vez, ya arranca o restaura la partida sola).
     */
    @Composable
    fun Content(modifier: Modifier = Modifier, onExit: (() -> Unit)? = null) {
        val vm: MahjongViewModel = viewModel(factory = MahjongViewModel.Factory(repository, layout))
        viewModel = vm
        val state by vm.uiState.collectAsStateWithLifecycle()
        MahjongScreen(
            state = state,
            onTileClick = vm::onTileClick,
            onHint = vm::onHintClick,
            onShuffle = vm::onShuffleClick,
            onUndo = vm::onUndoClick,
            onRestart = vm::onRestartClick,
            onPause = vm::onPauseClick,
            onResume = vm::onResumeClick,
            onExit = onExit,
            modifier = modifier,
        )
    }

    fun pauseGame() {
        viewModel?.onPauseClick()
    }

    fun resumeGame() {
        viewModel?.onResumeClick()
    }

    fun restartGame() {
        viewModel?.onRestartClick()
    }

    /** Fuerza un guardado inmediato (el autoguardado tras cada jugada ya cubre el caso normal). */
    suspend fun saveState() {
        viewModel?.saveNow()
    }

    /** No-op documentado: la restauración ya pasó sola al componer [Content] por primera vez. */
    fun loadState() = Unit

    fun getScore(): Int = viewModel?.uiState?.value?.score ?: 0

    fun getGameState(): MahjongUiState? = viewModel?.uiState?.value

    fun getGameStatus(): GameStatus = viewModel?.uiState?.value?.status ?: GameStatus.PLAYING

    /** Suelta la referencia al ViewModel. El ViewModel en sí lo destruye Android cuando corresponda. */
    fun destroy() {
        viewModel = null
    }
}
